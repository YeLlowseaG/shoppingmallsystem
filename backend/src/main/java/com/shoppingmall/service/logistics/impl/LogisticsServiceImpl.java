package com.shoppingmall.service.logistics.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.LogisticsCompanyDTO;
import com.shoppingmall.entity.LogisticsCompany;
import com.shoppingmall.repository.logistics.LogisticsCompanyRepository;
import com.shoppingmall.service.logistics.LogisticsService;
import com.shoppingmall.vo.LogisticsCompanyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流管理服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final LogisticsCompanyRepository logisticsCompanyRepository;

    @Override
    public Page<LogisticsCompanyVO> getLogisticsCompanyList(Integer page, Integer pageSize, String keyword, Integer status) {
        Page<LogisticsCompany> companyPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<LogisticsCompany> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(LogisticsCompany::getCompanyName, keyword)
                    .or()
                    .like(LogisticsCompany::getCompanyCode, keyword));
        }

        if (status != null) {
            wrapper.eq(LogisticsCompany::getStatus, status);
        }

        wrapper.orderByAsc(LogisticsCompany::getSortOrder);
        wrapper.orderByDesc(LogisticsCompany::getCreateTime);

        Page<LogisticsCompany> result = logisticsCompanyRepository.selectPage(companyPage, wrapper);

        Page<LogisticsCompanyVO> voPage = new Page<>(page, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<LogisticsCompanyVO> getAllEnabledLogisticsCompanies() {
        LambdaQueryWrapper<LogisticsCompany> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsCompany::getStatus, 1);
        wrapper.orderByAsc(LogisticsCompany::getSortOrder);

        List<LogisticsCompany> companies = logisticsCompanyRepository.selectList(wrapper);
        return companies.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public LogisticsCompanyVO getLogisticsCompanyById(Long id) {
        LogisticsCompany company = logisticsCompanyRepository.selectById(id);
        if (company == null) {
            throw new BusinessException(404, "物流公司不存在");
        }
        return convertToVO(company);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addLogisticsCompany(LogisticsCompanyDTO dto) {
        // 检查编码是否已存在
        LambdaQueryWrapper<LogisticsCompany> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsCompany::getCompanyCode, dto.getCompanyCode());
        if (logisticsCompanyRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "物流公司编码已存在");
        }

        LogisticsCompany company = new LogisticsCompany();
        BeanUtils.copyProperties(dto, company);
        if (company.getStatus() == null) {
            company.setStatus(1);
        }
        if (company.getSortOrder() == null) {
            company.setSortOrder(0);
        }

        logisticsCompanyRepository.insert(company);
        log.info("新增物流公司成功: id={}, code={}", company.getId(), company.getCompanyCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLogisticsCompany(Long id, LogisticsCompanyDTO dto) {
        LogisticsCompany company = logisticsCompanyRepository.selectById(id);
        if (company == null) {
            throw new BusinessException(404, "物流公司不存在");
        }

        // 检查编码是否已被其他公司使用
        if (!company.getCompanyCode().equals(dto.getCompanyCode())) {
            LambdaQueryWrapper<LogisticsCompany> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LogisticsCompany::getCompanyCode, dto.getCompanyCode());
            wrapper.ne(LogisticsCompany::getId, id);
            if (logisticsCompanyRepository.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "物流公司编码已被使用");
            }
        }

        company.setCompanyCode(dto.getCompanyCode());
        company.setCompanyName(dto.getCompanyName());
        company.setCompanyShortName(dto.getCompanyShortName());
        company.setContactPhone(dto.getContactPhone());
        company.setWebsite(dto.getWebsite());
        company.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) {
            company.setStatus(dto.getStatus());
        }

        logisticsCompanyRepository.updateById(company);
        log.info("更新物流公司成功: id={}, code={}", id, dto.getCompanyCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLogisticsCompany(Long id) {
        LogisticsCompany company = logisticsCompanyRepository.selectById(id);
        if (company == null) {
            throw new BusinessException(404, "物流公司不存在");
        }

        logisticsCompanyRepository.deleteById(id);
        log.info("删除物流公司成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLogisticsCompanyStatus(Long id, Integer status) {
        LogisticsCompany company = logisticsCompanyRepository.selectById(id);
        if (company == null) {
            throw new BusinessException(404, "物流公司不存在");
        }

        company.setStatus(status);
        logisticsCompanyRepository.updateById(company);
        log.info("更新物流公司状态成功: id={}, status={}", id, status);
    }

    /**
     * 实体转VO
     */
    private LogisticsCompanyVO convertToVO(LogisticsCompany company) {
        LogisticsCompanyVO vo = new LogisticsCompanyVO();
        BeanUtils.copyProperties(company, vo);
        return vo;
    }
}










