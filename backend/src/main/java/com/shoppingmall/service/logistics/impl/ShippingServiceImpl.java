package com.shoppingmall.service.logistics.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.ShippingFeeCalculateDTO;
import com.shoppingmall.dto.ShippingMethodDTO;
import com.shoppingmall.dto.ShippingRuleDTO;
import com.shoppingmall.dto.ShippingTemplateDTO;
import com.shoppingmall.entity.LogisticsCompany;
import com.shoppingmall.entity.ShippingMethod;
import com.shoppingmall.entity.ShippingRule;
import com.shoppingmall.entity.ShippingTemplate;
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

    /**
     * 根据运费模板计算运费
     */
    private BigDecimal calculateByTemplate(Long templateId, ShippingFeeCalculateDTO calculateDTO) {
        ShippingTemplate template = shippingTemplateRepository.selectById(templateId);
        if (template == null || template.getStatus() == 0) {
            throw new BusinessException(404, "运费模板不存在或已禁用");
        }

        // 检查包邮条件
        if (template.getFreeShippingAmount() != null && calculateDTO.getTotalAmount() != null
                && calculateDTO.getTotalAmount().compareTo(template.getFreeShippingAmount()) >= 0) {
            return BigDecimal.ZERO;
        }

        if (template.getFreeShippingWeight() != null && calculateDTO.getTotalWeight() != null
                && calculateDTO.getTotalWeight().compareTo(template.getFreeShippingWeight()) >= 0) {
            return BigDecimal.ZERO;
        }

        if (template.getFreeShippingQuantity() != null && calculateDTO.getTotalQuantity() != null
                && calculateDTO.getTotalQuantity() >= template.getFreeShippingQuantity()) {
            return BigDecimal.ZERO;
        }

        // 查找匹配的地区规则
        ShippingRule matchedRule = findMatchedRule(templateId, calculateDTO);

        BigDecimal firstWeight = matchedRule != null && matchedRule.getFirstWeight() != null
                ? matchedRule.getFirstWeight() : template.getDefaultFirstWeight();
        BigDecimal firstPrice = matchedRule != null && matchedRule.getFirstPrice() != null
                ? matchedRule.getFirstPrice() : template.getDefaultFirstPrice();
        BigDecimal continueWeight = matchedRule != null && matchedRule.getContinueWeight() != null
                ? matchedRule.getContinueWeight() : template.getDefaultContinueWeight();
        BigDecimal continuePrice = matchedRule != null && matchedRule.getContinuePrice() != null
                ? matchedRule.getContinuePrice() : template.getDefaultContinuePrice();

        if (firstPrice == null || firstPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 根据计算方式计算运费
        if (template.getCalculationType() == 1) { // 按重量
            if (calculateDTO.getTotalWeight() == null || calculateDTO.getTotalWeight().compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }

            if (calculateDTO.getTotalWeight().compareTo(firstWeight) <= 0) {
                return firstPrice;
            }

            BigDecimal exceedWeight = calculateDTO.getTotalWeight().subtract(firstWeight);
            BigDecimal exceedPrice = exceedWeight.divide(continueWeight != null && continueWeight.compareTo(BigDecimal.ZERO) > 0
                    ? continueWeight : BigDecimal.ONE, 0, RoundingMode.CEILING).multiply(continuePrice != null ? continuePrice : BigDecimal.ZERO);
            return firstPrice.add(exceedPrice);
        } else if (template.getCalculationType() == 2) { // 按件数
            if (calculateDTO.getTotalQuantity() == null || calculateDTO.getTotalQuantity() <= 0) {
                return BigDecimal.ZERO;
            }

            if (calculateDTO.getTotalQuantity() == 1) {
                return firstPrice;
            }

            int exceedQuantity = calculateDTO.getTotalQuantity() - 1;
            BigDecimal exceedPrice = BigDecimal.valueOf(exceedQuantity).multiply(continuePrice != null ? continuePrice : BigDecimal.ZERO);
            return firstPrice.add(exceedPrice);
        } else if (template.getCalculationType() == 3) { // 按金额
            // 简化实现，可以根据订单金额设置不同的运费
            return firstPrice;
        }

        return BigDecimal.ZERO;
    }

    /**
     * 查找匹配的运费规则
     */
    private ShippingRule findMatchedRule(Long templateId, ShippingFeeCalculateDTO calculateDTO) {
        LambdaQueryWrapper<ShippingRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingRule::getTemplateId, templateId);
        wrapper.orderByAsc(ShippingRule::getSortOrder);

        List<ShippingRule> rules = shippingRuleRepository.selectList(wrapper);

        // 优先匹配最具体的地区规则（省市区）
        if (StringUtils.hasText(calculateDTO.getDistrict())) {
            for (ShippingRule rule : rules) {
                if (StringUtils.hasText(rule.getRegionCode()) && rule.getRegionCode().contains(calculateDTO.getDistrict())) {
                    return rule;
                }
            }
        }

        // 匹配城市规则
        if (StringUtils.hasText(calculateDTO.getCity())) {
            for (ShippingRule rule : rules) {
                if (StringUtils.hasText(rule.getRegionCode()) && rule.getRegionCode().contains(calculateDTO.getCity())) {
                    return rule;
                }
            }
        }

        // 匹配省份规则
        if (StringUtils.hasText(calculateDTO.getProvince())) {
            for (ShippingRule rule : rules) {
                if (StringUtils.hasText(rule.getRegionCode()) && rule.getRegionCode().contains(calculateDTO.getProvince())) {
                    return rule;
                }
            }
        }

        // 返回默认规则（regionCode为空）
        for (ShippingRule rule : rules) {
            if (!StringUtils.hasText(rule.getRegionCode())) {
                return rule;
            }
        }

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
































