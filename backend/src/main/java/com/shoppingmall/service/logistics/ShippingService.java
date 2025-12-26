package com.shoppingmall.service.logistics;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.ShippingFeeCalculateDTO;
import com.shoppingmall.dto.ShippingMethodDTO;
import com.shoppingmall.dto.ShippingTemplateDTO;
import com.shoppingmall.vo.ShippingMethodVO;
import com.shoppingmall.vo.ShippingTemplateVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运费管理服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
public interface ShippingService {

    /**
     * 获取配送方式列表（买家端，只返回启用的）
     *
     * @return 配送方式列表
     */
    List<ShippingMethodVO> getEnabledShippingMethods();

    /**
     * 获取配送方式列表（管理端，分页）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  关键词（配送方式名称/编码）
     * @param status   状态
     * @return 配送方式列表
     */
    Page<ShippingMethodVO> getShippingMethodList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 根据ID获取配送方式信息
     *
     * @param id 配送方式ID
     * @return 配送方式信息
     */
    ShippingMethodVO getShippingMethodById(Long id);

    /**
     * 新增配送方式
     *
     * @param dto 配送方式信息
     */
    void addShippingMethod(ShippingMethodDTO dto);

    /**
     * 更新配送方式信息
     *
     * @param id  配送方式ID
     * @param dto 配送方式信息
     */
    void updateShippingMethod(Long id, ShippingMethodDTO dto);

    /**
     * 删除配送方式
     *
     * @param id 配送方式ID
     */
    void deleteShippingMethod(Long id);

    /**
     * 启用/禁用配送方式
     *
     * @param id     配送方式ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateShippingMethodStatus(Long id, Integer status);

    /**
     * 计算运费
     *
     * @param calculateDTO 运费计算参数
     * @return 运费金额
     */
    BigDecimal calculateShippingFee(ShippingFeeCalculateDTO calculateDTO);

    /**
     * 获取运费模板列表（分页）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  关键词（模板名称）
     * @param status   状态
     * @return 运费模板列表
     */
    Page<ShippingTemplateVO> getShippingTemplateList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 获取所有启用的运费模板列表
     *
     * @return 运费模板列表
     */
    List<ShippingTemplateVO> getAllEnabledShippingTemplates();

    /**
     * 根据ID获取运费模板信息
     *
     * @param id 运费模板ID
     * @return 运费模板信息（包含规则）
     */
    ShippingTemplateVO getShippingTemplateById(Long id);

    /**
     * 新增运费模板
     *
     * @param dto 运费模板信息（包含规则）
     */
    void addShippingTemplate(ShippingTemplateDTO dto);

    /**
     * 更新运费模板信息
     *
     * @param id  运费模板ID
     * @param dto 运费模板信息（包含规则）
     */
    void updateShippingTemplate(Long id, ShippingTemplateDTO dto);

    /**
     * 删除运费模板
     *
     * @param id 运费模板ID
     */
    void deleteShippingTemplate(Long id);

    /**
     * 启用/禁用运费模板
     *
     * @param id     运费模板ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateShippingTemplateStatus(Long id, Integer status);
}













































