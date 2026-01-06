package com.shoppingmall.service.buyer;

import com.shoppingmall.dto.AddressDTO;
import com.shoppingmall.vo.AddressVO;

import java.util.List;

/**
 * 收货地址服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-08
 */
public interface AddressService {

    /**
     * 获取用户的收货地址列表
     *
     * @param userId 用户ID
     * @return 收货地址列表
     */
    List<AddressVO> getAddressList(Long userId);

    /**
     * 根据ID获取收货地址详情
     *
     * @param id     地址ID
     * @param userId  用户ID
     * @return 收货地址详情
     */
    AddressVO getAddressById(Long id, Long userId);

    /**
     * 新增收货地址
     *
     * @param userId     用户ID
     * @param addressDTO 地址信息
     * @return 新增的地址ID
     */
    Long addAddress(Long userId, AddressDTO addressDTO);

    /**
     * 更新收货地址
     *
     * @param id         地址ID
     * @param userId     用户ID
     * @param addressDTO 地址信息
     */
    void updateAddress(Long id, Long userId, AddressDTO addressDTO);

    /**
     * 删除收货地址
     *
     * @param id     地址ID
     * @param userId 用户ID
     */
    void deleteAddress(Long id, Long userId);

    /**
     * 设置默认收货地址
     *
     * @param id     地址ID
     * @param userId 用户ID
     */
    void setDefaultAddress(Long id, Long userId);
}




































































