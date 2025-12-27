package com.shoppingmall.service.logistics;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.WarehouseAddressDTO;
import com.shoppingmall.vo.WarehouseAddressVO;

import java.util.List;

/**
 * 发货地址库服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
public interface WarehouseAddressService {

    /**
     * 获取发货地址列表（分页）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  关键词（仓库名称/联系人）
     * @param status   状态
     * @return 发货地址列表
     */
    Page<WarehouseAddressVO> getWarehouseAddressList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 获取所有启用的发货地址列表
     *
     * @return 发货地址列表
     */
    List<WarehouseAddressVO> getAllEnabledWarehouseAddresses();

    /**
     * 获取默认发货地址
     *
     * @return 默认发货地址
     */
    WarehouseAddressVO getDefaultWarehouseAddress();

    /**
     * 根据ID获取发货地址信息
     *
     * @param id 发货地址ID
     * @return 发货地址信息
     */
    WarehouseAddressVO getWarehouseAddressById(Long id);

    /**
     * 新增发货地址
     *
     * @param dto 发货地址信息
     */
    void addWarehouseAddress(WarehouseAddressDTO dto);

    /**
     * 更新发货地址信息
     *
     * @param id  发货地址ID
     * @param dto 发货地址信息
     */
    void updateWarehouseAddress(Long id, WarehouseAddressDTO dto);

    /**
     * 删除发货地址
     *
     * @param id 发货地址ID
     */
    void deleteWarehouseAddress(Long id);

    /**
     * 启用/禁用发货地址
     *
     * @param id     发货地址ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateWarehouseAddressStatus(Long id, Integer status);

    /**
     * 设置默认发货地址
     *
     * @param id 发货地址ID
     */
    void setDefaultWarehouseAddress(Long id);
}

