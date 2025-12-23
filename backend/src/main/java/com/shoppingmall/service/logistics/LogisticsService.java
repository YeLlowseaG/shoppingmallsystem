package com.shoppingmall.service.logistics;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.LogisticsCompanyDTO;
import com.shoppingmall.vo.LogisticsCompanyVO;

import java.util.List;

/**
 * 物流管理服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
public interface LogisticsService {

    /**
     * 获取物流公司列表（分页）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  关键词（公司名称/编码）
     * @param status   状态
     * @return 物流公司列表
     */
    Page<LogisticsCompanyVO> getLogisticsCompanyList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 获取所有启用的物流公司列表
     *
     * @return 物流公司列表
     */
    List<LogisticsCompanyVO> getAllEnabledLogisticsCompanies();

    /**
     * 根据ID获取物流公司信息
     *
     * @param id 物流公司ID
     * @return 物流公司信息
     */
    LogisticsCompanyVO getLogisticsCompanyById(Long id);

    /**
     * 新增物流公司
     *
     * @param dto 物流公司信息
     */
    void addLogisticsCompany(LogisticsCompanyDTO dto);

    /**
     * 更新物流公司信息
     *
     * @param id  物流公司ID
     * @param dto 物流公司信息
     */
    void updateLogisticsCompany(Long id, LogisticsCompanyDTO dto);

    /**
     * 删除物流公司
     *
     * @param id 物流公司ID
     */
    void deleteLogisticsCompany(Long id);

    /**
     * 启用/禁用物流公司
     *
     * @param id     物流公司ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateLogisticsCompanyStatus(Long id, Integer status);
}




































