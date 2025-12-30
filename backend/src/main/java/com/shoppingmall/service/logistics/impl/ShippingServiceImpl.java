package com.shoppingmall.service.logistics.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.RegionInfoDTO;
import com.shoppingmall.dto.ShippingFeeCalculateDTO;
import com.shoppingmall.dto.ShippingMethodDTO;
import com.shoppingmall.dto.ShippingRuleDTO;
import com.shoppingmall.dto.ShippingTemplateDTO;
import com.shoppingmall.entity.LogisticsCompany;
import com.shoppingmall.entity.Region;
import com.shoppingmall.entity.ShippingMethod;
import com.shoppingmall.entity.ShippingRule;
import com.shoppingmall.entity.ShippingTemplate;
import com.shoppingmall.repository.common.RegionRepository;
import com.shoppingmall.repository.logistics.LogisticsCompanyRepository;
import com.shoppingmall.repository.logistics.ShippingMethodRepository;
import com.shoppingmall.repository.logistics.ShippingRuleRepository;
import com.shoppingmall.repository.logistics.ShippingTemplateRepository;
import com.shoppingmall.service.logistics.ShippingService;
import com.shoppingmall.vo.ShippingMethodVO;
import com.shoppingmall.vo.ShippingRuleVO;
import com.shoppingmall.vo.ShippingTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运费管理服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingMethodRepository shippingMethodRepository;
    private final ShippingTemplateRepository shippingTemplateRepository;
    private final ShippingRuleRepository shippingRuleRepository;
    private final LogisticsCompanyRepository logisticsCompanyRepository;
    private final RegionRepository regionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<ShippingMethodVO> getEnabledShippingMethods() {
        LambdaQueryWrapper<ShippingMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingMethod::getStatus, 1);
        wrapper.orderByAsc(ShippingMethod::getSortOrder);

        List<ShippingMethod> methods = shippingMethodRepository.selectList(wrapper);
        return methods.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public Page<ShippingMethodVO> getShippingMethodList(Integer page, Integer pageSize, String keyword, Integer status) {
        Page<ShippingMethod> methodPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ShippingMethod> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ShippingMethod::getMethodName, keyword)
                    .or()
                    .like(ShippingMethod::getMethodCode, keyword));
        }

        if (status != null) {
            wrapper.eq(ShippingMethod::getStatus, status);
        }

        wrapper.orderByAsc(ShippingMethod::getSortOrder);
        wrapper.orderByDesc(ShippingMethod::getCreateTime);

        Page<ShippingMethod> result = shippingMethodRepository.selectPage(methodPage, wrapper);

        Page<ShippingMethodVO> voPage = new Page<>(page, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public ShippingMethodVO getShippingMethodById(Long id) {
        ShippingMethod method = shippingMethodRepository.selectById(id);
        if (method == null) {
            throw new BusinessException(404, "配送方式不存在");
        }
        return convertToVO(method);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addShippingMethod(ShippingMethodDTO dto) {
        // 检查编码是否已存在
        LambdaQueryWrapper<ShippingMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingMethod::getMethodCode, dto.getMethodCode());
        if (shippingMethodRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "配送方式编码已存在");
        }

        ShippingMethod method = new ShippingMethod();
        BeanUtils.copyProperties(dto, method);
        if (method.getStatus() == null) {
            method.setStatus(1);
        }
        if (method.getSortOrder() == null) {
            method.setSortOrder(0);
        }
        if (method.getCalculationType() == null) {
            method.setCalculationType(1); // 默认固定运费
        }
        if (method.getBasePrice() == null) {
            method.setBasePrice(BigDecimal.ZERO);
        }

        shippingMethodRepository.insert(method);
        log.info("新增配送方式成功: id={}, code={}", method.getId(), method.getMethodCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShippingMethod(Long id, ShippingMethodDTO dto) {
        ShippingMethod method = shippingMethodRepository.selectById(id);
        if (method == null) {
            throw new BusinessException(404, "配送方式不存在");
        }

        // 检查编码是否已被其他方式使用
        if (!method.getMethodCode().equals(dto.getMethodCode())) {
            LambdaQueryWrapper<ShippingMethod> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShippingMethod::getMethodCode, dto.getMethodCode());
            wrapper.ne(ShippingMethod::getId, id);
            if (shippingMethodRepository.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "配送方式编码已被使用");
            }
        }

        method.setMethodCode(dto.getMethodCode());
        method.setMethodName(dto.getMethodName());
        method.setLogisticsCompanyId(dto.getLogisticsCompanyId());
        method.setDescription(dto.getDescription());
        method.setShippingTemplateId(dto.getShippingTemplateId());
        method.setBasePrice(dto.getBasePrice());
        method.setCalculationType(dto.getCalculationType());
        method.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) {
            method.setStatus(dto.getStatus());
        }

        shippingMethodRepository.updateById(method);
        log.info("更新配送方式成功: id={}, code={}", id, dto.getMethodCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShippingMethod(Long id) {
        ShippingMethod method = shippingMethodRepository.selectById(id);
        if (method == null) {
            throw new BusinessException(404, "配送方式不存在");
        }

        shippingMethodRepository.deleteById(id);
        log.info("删除配送方式成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShippingMethodStatus(Long id, Integer status) {
        ShippingMethod method = shippingMethodRepository.selectById(id);
        if (method == null) {
            throw new BusinessException(404, "配送方式不存在");
        }

        method.setStatus(status);
        shippingMethodRepository.updateById(method);
        log.info("更新配送方式状态成功: id={}, status={}", id, status);
    }

    @Override
    public BigDecimal calculateShippingFee(ShippingFeeCalculateDTO calculateDTO) {
        ShippingMethod method = shippingMethodRepository.selectById(calculateDTO.getShippingMethodId());
        if (method == null || method.getStatus() == 0) {
            throw new BusinessException(404, "配送方式不存在或已禁用");
        }

        // 1. 固定运费
        if (method.getCalculationType() == 1) {
            return method.getBasePrice() != null ? method.getBasePrice() : BigDecimal.ZERO;
        }

        // 2. 运费模板计算
        if (method.getCalculationType() == 5 && method.getShippingTemplateId() != null) {
            return calculateByTemplate(method.getShippingTemplateId(), calculateDTO);
        }

        // 3. 按重量计算
        if (method.getCalculationType() == 2) {
            // 简化实现：首重+续重
            BigDecimal firstWeight = method.getBasePrice() != null ? method.getBasePrice() : BigDecimal.ZERO;
            BigDecimal continueWeight = BigDecimal.ONE; // 默认1kg续重
            BigDecimal continuePrice = BigDecimal.valueOf(5); // 默认续重价格5元

            if (calculateDTO.getTotalWeight().compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }

            if (calculateDTO.getTotalWeight().compareTo(continueWeight) <= 0) {
                return firstWeight;
            }

            BigDecimal exceedWeight = calculateDTO.getTotalWeight().subtract(continueWeight);
            BigDecimal exceedPrice = exceedWeight.divide(continueWeight, 0, RoundingMode.CEILING)
                    .multiply(continuePrice);
            return firstWeight.add(exceedPrice);
        }

        // 4. 按件数计算
        if (method.getCalculationType() == 3) {
            BigDecimal firstPrice = method.getBasePrice() != null ? method.getBasePrice() : BigDecimal.ZERO;
            BigDecimal continuePrice = BigDecimal.valueOf(2); // 默认续件价格2元

            if (calculateDTO.getTotalQuantity() <= 0) {
                return BigDecimal.ZERO;
            }

            if (calculateDTO.getTotalQuantity() == 1) {
                return firstPrice;
            }

            int exceedQuantity = calculateDTO.getTotalQuantity() - 1;
            BigDecimal exceedPrice = BigDecimal.valueOf(exceedQuantity).multiply(continuePrice);
            return firstPrice.add(exceedPrice);
        }

        // 5. 按金额计算（简化实现）
        if (method.getCalculationType() == 4) {
            // 可以根据订单金额设置不同的运费
            return method.getBasePrice() != null ? method.getBasePrice() : BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateShippingFeeByTemplate(Long templateId, ShippingFeeCalculateDTO calculateDTO) {
        if (templateId == null) {
            log.info("运费模板ID为null，返回包邮（运费0）");
            return BigDecimal.ZERO;
        }
        return calculateByTemplate(templateId, calculateDTO);
    }

    /**
     * 根据运费模板计算运费
     */
    private BigDecimal calculateByTemplate(Long templateId, ShippingFeeCalculateDTO calculateDTO) {
        log.info("========== 开始计算运费 ==========");
        log.info("运费模板ID: {}", templateId);
        log.info("计算参数: province={}, city={}, district={}, totalWeight={}kg, totalAmount={}, totalQuantity={}", 
            calculateDTO.getProvince(), calculateDTO.getCity(), calculateDTO.getDistrict(),
            calculateDTO.getTotalWeight(), calculateDTO.getTotalAmount(), calculateDTO.getTotalQuantity());
        
        ShippingTemplate template = shippingTemplateRepository.selectById(templateId);
        if (template == null || template.getStatus() == 0) {
            log.error("运费模板不存在或已禁用: templateId={}, template={}", templateId, template);
            throw new BusinessException(404, "运费模板不存在或已禁用");
        }
        
        log.info("运费模板信息: name={}, calculationType={}, freeShippingAmount={}, freeShippingWeight={}, freeShippingQuantity={}",
            template.getTemplateName(), template.getCalculationType(), 
            template.getFreeShippingAmount(), template.getFreeShippingWeight(), template.getFreeShippingQuantity());
        log.info("默认运费规则: firstWeight={}kg, firstPrice={}, continueWeight={}kg, continuePrice={}",
            template.getDefaultFirstWeight(), template.getDefaultFirstPrice(),
            template.getDefaultContinueWeight(), template.getDefaultContinuePrice());

        // 检查包邮条件（只检查包邮重量，因为目前只有按重量计算）
        // 只有当包邮重量 > 0 时才检查（0表示不按重量包邮）
        if (template.getFreeShippingWeight() != null 
                && template.getFreeShippingWeight().compareTo(BigDecimal.ZERO) > 0
                && calculateDTO.getTotalWeight() != null
                && calculateDTO.getTotalWeight().compareTo(template.getFreeShippingWeight()) >= 0) {
            log.info("满足包邮条件（重量）: 订单重量{}kg >= 包邮重量{}kg", 
                calculateDTO.getTotalWeight(), template.getFreeShippingWeight());
            return BigDecimal.ZERO;
        }
        
        log.info("不满足包邮条件，继续计算运费");

        // 查找匹配的地区规则
        ShippingRule matchedRule = findMatchedRule(templateId, calculateDTO);
        if (matchedRule != null) {
            log.info("找到匹配的地区规则: ruleId={}, regionName={}, firstWeight={}kg, firstPrice={}, continueWeight={}kg, continuePrice={}",
                matchedRule.getId(), matchedRule.getRegionName(),
                matchedRule.getFirstWeight(), matchedRule.getFirstPrice(),
                matchedRule.getContinueWeight(), matchedRule.getContinuePrice());
        } else {
            log.info("未找到匹配的地区规则，使用默认规则");
        }

        BigDecimal firstWeight = matchedRule != null && matchedRule.getFirstWeight() != null
                ? matchedRule.getFirstWeight() : template.getDefaultFirstWeight();
        BigDecimal firstPrice = matchedRule != null && matchedRule.getFirstPrice() != null
                ? matchedRule.getFirstPrice() : template.getDefaultFirstPrice();
        BigDecimal continueWeight = matchedRule != null && matchedRule.getContinueWeight() != null
                ? matchedRule.getContinueWeight() : template.getDefaultContinueWeight();
        BigDecimal continuePrice = matchedRule != null && matchedRule.getContinuePrice() != null
                ? matchedRule.getContinuePrice() : template.getDefaultContinuePrice();
        
        log.info("使用的运费规则: firstWeight={}kg, firstPrice={}, continueWeight={}kg, continuePrice={}",
            firstWeight, firstPrice, continueWeight, continuePrice);

        if (firstPrice == null || firstPrice.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("首重价格为0或null，返回运费0: firstPrice={}", firstPrice);
            return BigDecimal.ZERO;
        }

        // 根据计算方式计算运费
        if (template.getCalculationType() == 1) { // 按重量
            log.info("按重量计算运费");
            if (calculateDTO.getTotalWeight() == null || calculateDTO.getTotalWeight().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("订单重量为0或null，返回运费0: totalWeight={}", calculateDTO.getTotalWeight());
                return BigDecimal.ZERO;
            }

            if (calculateDTO.getTotalWeight().compareTo(firstWeight) <= 0) {
                log.info("订单重量{}kg <= 首重{}kg，运费为首重价格: {}", 
                    calculateDTO.getTotalWeight(), firstWeight, firstPrice);
                log.info("========== 运费计算完成: {} ==========", firstPrice);
                return firstPrice;
            }

            BigDecimal exceedWeight = calculateDTO.getTotalWeight().subtract(firstWeight);
            BigDecimal exceedPrice = exceedWeight.divide(continueWeight != null && continueWeight.compareTo(BigDecimal.ZERO) > 0
                    ? continueWeight : BigDecimal.ONE, 0, RoundingMode.CEILING).multiply(continuePrice != null ? continuePrice : BigDecimal.ZERO);
            BigDecimal totalFee = firstPrice.add(exceedPrice);
            
            log.info("订单重量{}kg > 首重{}kg，超出重量{}kg，续重价格={}，总运费={}",
                calculateDTO.getTotalWeight(), firstWeight, exceedWeight, exceedPrice, totalFee);
            log.info("========== 运费计算完成: {} ==========", totalFee);
            return totalFee;
        } else if (template.getCalculationType() == 2) { // 按件数
            log.info("按件数计算运费");
            if (calculateDTO.getTotalQuantity() == null || calculateDTO.getTotalQuantity() <= 0) {
                log.warn("订单件数为0或null，返回运费0: totalQuantity={}", calculateDTO.getTotalQuantity());
                return BigDecimal.ZERO;
            }

            if (calculateDTO.getTotalQuantity() == 1) {
                log.info("订单件数为1，运费为首件价格: {}", firstPrice);
                return firstPrice;
            }

            int exceedQuantity = calculateDTO.getTotalQuantity() - 1;
            BigDecimal exceedPrice = BigDecimal.valueOf(exceedQuantity).multiply(continuePrice != null ? continuePrice : BigDecimal.ZERO);
            BigDecimal totalFee = firstPrice.add(exceedPrice);
            log.info("订单件数{}，超出件数{}，续件价格={}，总运费={}",
                calculateDTO.getTotalQuantity(), exceedQuantity, exceedPrice, totalFee);
            return totalFee;
        } else if (template.getCalculationType() == 3) { // 按金额
            log.info("按金额计算运费，返回固定运费: {}", firstPrice);
            return firstPrice;
        }

        log.warn("未知的计算方式: calculationType={}，返回运费0", template.getCalculationType());
        return BigDecimal.ZERO;
    }

    /**
     * 查找匹配的运费规则
     * 优先级：区县 > 城市 > 省份 > 默认规则
     */
    private ShippingRule findMatchedRule(Long templateId, ShippingFeeCalculateDTO calculateDTO) {
        log.info("查找匹配的运费规则: templateId={}, province={}, city={}, district={}",
            templateId, calculateDTO.getProvince(), calculateDTO.getCity(), calculateDTO.getDistrict());
        
        LambdaQueryWrapper<ShippingRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingRule::getTemplateId, templateId);
        wrapper.orderByAsc(ShippingRule::getSortOrder);

        List<ShippingRule> rules = shippingRuleRepository.selectList(wrapper);
        log.info("找到{}条运费规则", rules.size());
        for (ShippingRule rule : rules) {
            log.info("规则ID={}, regionCode={}, regionName={}, firstPrice={}",
                rule.getId(), rule.getRegionCode(), rule.getRegionName(), rule.getFirstPrice());
        }

        String province = calculateDTO.getProvince();
        String city = calculateDTO.getCity();
        String district = calculateDTO.getDistrict();

        // 1. 优先匹配最具体的地区规则（区县）
        if (StringUtils.hasText(district)) {
            log.info("尝试匹配区县规则: district={}", district);
            ShippingRule matchedRule = findRuleByRegion(rules, province, city, district);
            if (matchedRule != null) {
                log.info("匹配到区县规则: ruleId={}", matchedRule.getId());
                return matchedRule;
            }
        }

        // 2. 匹配城市规则
        if (StringUtils.hasText(city)) {
            log.info("尝试匹配城市规则: city={}", city);
            ShippingRule matchedRule = findRuleByRegion(rules, province, city, null);
            if (matchedRule != null) {
                log.info("匹配到城市规则: ruleId={}", matchedRule.getId());
                return matchedRule;
            }
        }

        // 3. 匹配省份规则
        if (StringUtils.hasText(province)) {
            log.info("尝试匹配省份规则: province={}", province);
            ShippingRule matchedRule = findRuleByRegion(rules, province, null, null);
            if (matchedRule != null) {
                log.info("匹配到省份规则: ruleId={}", matchedRule.getId());
                return matchedRule;
            }
        }

        // 4. 返回默认规则（regionCode为空）
        for (ShippingRule rule : rules) {
            if (!StringUtils.hasText(rule.getRegionCode())) {
                log.info("使用默认规则: ruleId={}", rule.getId());
                return rule;
            }
        }

        log.warn("未找到任何匹配的规则，返回null");
        return null;
    }

    /**
     * 根据地区信息查找匹配的规则
     * 
     * @param rules 规则列表
     * @param province 省份名称
     * @param city 城市名称（可选）
     * @param district 区县名称（可选）
     * @return 匹配的规则，如果没有匹配则返回null
     */
    private ShippingRule findRuleByRegion(List<ShippingRule> rules, String province, String city, String district) {
        log.info("在{}条规则中查找匹配: province={}, city={}, district={}", 
            rules.size(), province, city, district);
        
        // 如果提供了省份名称，先查找对应的省份编码
        String provinceCode = null;
        if (StringUtils.hasText(province)) {
            LambdaQueryWrapper<Region> regionWrapper = new LambdaQueryWrapper<>();
            regionWrapper.eq(Region::getName, province);
            regionWrapper.eq(Region::getLevel, 1);
            regionWrapper.eq(Region::getStatus, 1);
            regionWrapper.last("LIMIT 1");
            Region region = regionRepository.selectOne(regionWrapper);
            if (region != null) {
                provinceCode = region.getCode();
                log.info("省份名称'{}'对应的编码: {}", province, provinceCode);
            } else {
                log.warn("未找到省份名称'{}'对应的编码", province);
            }
        }
        
        for (ShippingRule rule : rules) {
            if (!StringUtils.hasText(rule.getRegionCode())) {
                continue; // 跳过默认规则
            }

            try {
                // 解析JSON格式的地区编码
                List<RegionInfoDTO> regions = objectMapper.readValue(
                    rule.getRegionCode(),
                    new TypeReference<List<RegionInfoDTO>>() {}
                );
                
                log.info("规则ID={}，包含{}个地区", rule.getId(), regions.size());

                // 检查是否匹配
                for (RegionInfoDTO region : regions) {
                    log.info("检查地区: provinceCode={}, provinceName={}, cityCode={}, cityName={}, districtCode={}, districtName={}",
                        region.getProvinceCode(), region.getProvinceName(),
                        region.getCityCode(), region.getCityName(),
                        region.getDistrictCode(), region.getDistrictName());
                    
                    // 修复省份匹配逻辑
                    boolean provinceMatch = false;
                    if (StringUtils.hasText(region.getProvinceCode())) {
                        // 规则中有省份编码，需要匹配编码
                        if (provinceCode != null) {
                            provinceMatch = region.getProvinceCode().equals(provinceCode);
                        }
                    } else if (StringUtils.hasText(region.getProvinceName())) {
                        // 规则中没有省份编码，使用省份名称匹配
                        provinceMatch = StringUtils.hasText(province) && province.equals(region.getProvinceName());
                    }
                    
                    log.info("省份匹配结果: provinceMatch={} (province={}, provinceCode={}, rule.provinceCode={}, rule.provinceName={})",
                        provinceMatch, province, provinceCode, region.getProvinceCode(), region.getProvinceName());
                    
                    if (!provinceMatch) {
                        log.info("省份不匹配，跳过此规则");
                        continue;
                    }

                    // 如果指定了区县，必须完全匹配（省+市+区）
                    if (StringUtils.hasText(district)) {
                        boolean cityMatch = !StringUtils.hasText(region.getCityCode()) || 
                                          (StringUtils.hasText(city) && city.equals(region.getCityName()));
                        boolean districtMatch = !StringUtils.hasText(region.getDistrictCode()) || 
                                              district.equals(region.getDistrictName());
                        
                        log.info("区县匹配结果: cityMatch={}, districtMatch={} (city={}, district={})",
                            cityMatch, districtMatch, city, district);
                        
                        if (cityMatch && districtMatch) {
                            log.info("匹配成功！返回规则ID={}", rule.getId());
                            return rule;
                        }
                    }
                    // 如果指定了城市，必须匹配（省+市）
                    else if (StringUtils.hasText(city)) {
                        boolean cityMatch = !StringUtils.hasText(region.getCityCode()) || 
                                          city.equals(region.getCityName());
                        // 如果规则中没有指定区县，则匹配
                        boolean noDistrict = !StringUtils.hasText(region.getDistrictCode());
                        
                        log.info("城市匹配结果: cityMatch={}, noDistrict={} (city={})",
                            cityMatch, noDistrict, city);
                        
                        if (cityMatch && noDistrict) {
                            log.info("匹配成功！返回规则ID={}", rule.getId());
                            return rule;
                        }
                    }
                    // 只指定了省份，匹配省份规则（规则中不能有城市和区县）
                    else {
                        boolean noCity = !StringUtils.hasText(region.getCityCode());
                        boolean noDistrict = !StringUtils.hasText(region.getDistrictCode());
                        
                        log.info("省份规则匹配: noCity={}, noDistrict={}", noCity, noDistrict);
                        
                        if (noCity && noDistrict) {
                            log.info("匹配成功！返回规则ID={}", rule.getId());
                            return rule;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析运费规则地区信息失败: ruleId={}, regionCode={}", rule.getId(), rule.getRegionCode(), e);
                // 如果JSON解析失败，尝试使用旧的字符串匹配方式（向后兼容）
                String regionCode = rule.getRegionCode();
                log.info("尝试使用字符串匹配方式: regionCode={}", regionCode);
                if (StringUtils.hasText(district) && regionCode.contains(district)) {
                    log.info("字符串匹配成功（区县）: district={}", district);
                    return rule;
                } else if (StringUtils.hasText(city) && regionCode.contains(city)) {
                    log.info("字符串匹配成功（城市）: city={}", city);
                    return rule;
                } else if (StringUtils.hasText(province) && regionCode.contains(province)) {
                    log.info("字符串匹配成功（省份）: province={}", province);
                    return rule;
                }
            }
        }

        log.info("未找到匹配的规则");
        return null;
    }

    @Override
    public Page<ShippingTemplateVO> getShippingTemplateList(Integer page, Integer pageSize, String keyword, Integer status) {
        Page<ShippingTemplate> templatePage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ShippingTemplate> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ShippingTemplate::getTemplateName, keyword);
        }

        if (status != null) {
            wrapper.eq(ShippingTemplate::getStatus, status);
        }

        wrapper.orderByDesc(ShippingTemplate::getCreateTime);

        Page<ShippingTemplate> result = shippingTemplateRepository.selectPage(templatePage, wrapper);

        Page<ShippingTemplateVO> voPage = new Page<>(page, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(template -> {
            ShippingTemplateVO vo = convertToVO(template);
            // 加载规则
            loadTemplateRules(vo);
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<ShippingTemplateVO> getAllEnabledShippingTemplates() {
        LambdaQueryWrapper<ShippingTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingTemplate::getStatus, 1);
        wrapper.orderByDesc(ShippingTemplate::getCreateTime);

        List<ShippingTemplate> templates = shippingTemplateRepository.selectList(wrapper);
        return templates.stream().map(template -> {
            ShippingTemplateVO vo = convertToVO(template);
            loadTemplateRules(vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ShippingTemplateVO getShippingTemplateById(Long id) {
        ShippingTemplate template = shippingTemplateRepository.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "运费模板不存在");
        }

        ShippingTemplateVO vo = convertToVO(template);
        loadTemplateRules(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addShippingTemplate(ShippingTemplateDTO dto) {
        ShippingTemplate template = new ShippingTemplate();
        BeanUtils.copyProperties(dto, template);
        if (template.getStatus() == null) {
            template.setStatus(1);
        }

        shippingTemplateRepository.insert(template);

        // 保存运费规则
        if (dto.getRules() != null && !dto.getRules().isEmpty()) {
            for (ShippingRuleDTO ruleDTO : dto.getRules()) {
                ShippingRule rule = new ShippingRule();
                BeanUtils.copyProperties(ruleDTO, rule);
                rule.setTemplateId(template.getId());
                if (rule.getSortOrder() == null) {
                    rule.setSortOrder(0);
                }
                shippingRuleRepository.insert(rule);
            }
        }

        log.info("新增运费模板成功: id={}, name={}", template.getId(), template.getTemplateName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShippingTemplate(Long id, ShippingTemplateDTO dto) {
        ShippingTemplate template = shippingTemplateRepository.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "运费模板不存在");
        }

        BeanUtils.copyProperties(dto, template, "id");
        if (dto.getStatus() != null) {
            template.setStatus(dto.getStatus());
        }

        shippingTemplateRepository.updateById(template);

        // 删除旧规则
        LambdaQueryWrapper<ShippingRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingRule::getTemplateId, id);
        shippingRuleRepository.delete(wrapper);

        // 保存新规则
        if (dto.getRules() != null && !dto.getRules().isEmpty()) {
            for (ShippingRuleDTO ruleDTO : dto.getRules()) {
                ShippingRule rule = new ShippingRule();
                BeanUtils.copyProperties(ruleDTO, rule, "id");
                rule.setTemplateId(id);
                if (rule.getSortOrder() == null) {
                    rule.setSortOrder(0);
                }
                shippingRuleRepository.insert(rule);
            }
        }

        log.info("更新运费模板成功: id={}, name={}", id, dto.getTemplateName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShippingTemplate(Long id) {
        ShippingTemplate template = shippingTemplateRepository.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "运费模板不存在");
        }

        // 删除规则
        LambdaQueryWrapper<ShippingRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingRule::getTemplateId, id);
        shippingRuleRepository.delete(wrapper);

        shippingTemplateRepository.deleteById(id);
        log.info("删除运费模板成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShippingTemplateStatus(Long id, Integer status) {
        ShippingTemplate template = shippingTemplateRepository.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "运费模板不存在");
        }

        template.setStatus(status);
        shippingTemplateRepository.updateById(template);
        log.info("更新运费模板状态成功: id={}, status={}", id, status);
    }

    /**
     * 加载运费模板规则
     */
    private void loadTemplateRules(ShippingTemplateVO vo) {
        LambdaQueryWrapper<ShippingRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingRule::getTemplateId, vo.getId());
        wrapper.orderByAsc(ShippingRule::getSortOrder);

        List<ShippingRule> rules = shippingRuleRepository.selectList(wrapper);
        vo.setRules(rules.stream().map(this::convertRuleToVO).collect(Collectors.toList()));
    }

    /**
     * 实体转VO
     */
    private ShippingMethodVO convertToVO(ShippingMethod method) {
        ShippingMethodVO vo = new ShippingMethodVO();
        BeanUtils.copyProperties(method, vo);

        // 加载物流公司名称
        if (method.getLogisticsCompanyId() != null) {
            LogisticsCompany company = logisticsCompanyRepository.selectById(method.getLogisticsCompanyId());
            if (company != null) {
                vo.setLogisticsCompanyName(company.getCompanyName());
            }
        }

        return vo;
    }

    /**
     * 运费模板实体转VO
     */
    private ShippingTemplateVO convertToVO(ShippingTemplate template) {
        ShippingTemplateVO vo = new ShippingTemplateVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }

    /**
     * 运费规则实体转VO
     */
    private ShippingRuleVO convertRuleToVO(ShippingRule rule) {
        ShippingRuleVO vo = new ShippingRuleVO();
        BeanUtils.copyProperties(rule, vo);
        return vo;
    }
}













































