package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.AddressDTO;
import com.shoppingmall.entity.UserAddress;
import com.shoppingmall.repository.user.UserAddressRepository;
import com.shoppingmall.service.buyer.AddressService;
import com.shoppingmall.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收货地址服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final UserAddressRepository addressRepository;

    @Override
    public List<AddressVO> getAddressList(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        wrapper.orderByDesc(UserAddress::getIsDefault);
        wrapper.orderByDesc(UserAddress::getCreateTime);

        List<UserAddress> addresses = addressRepository.selectList(wrapper);
        return addresses.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public AddressVO getAddressById(Long id, Long userId) {
        UserAddress address = addressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "收货地址不存在");
        }

        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问该收货地址");
        }

        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(Long userId, AddressDTO addressDTO) {
        // 验证电话或手机至少填写一项
        if ((addressDTO.getPhone() == null || addressDTO.getPhone().trim().isEmpty()) &&
            (addressDTO.getMobile() == null || addressDTO.getMobile().trim().isEmpty())) {
            throw new BusinessException(400, "联系电话和手机号码必须填写一项");
        }

        // 如果设置为默认地址，需要取消其他默认地址
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            setOtherAddressesNotDefault(userId);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(addressDTO.getRecipient());
        address.setReceiverPhone(addressDTO.getPhone());
        address.setReceiverMobile(addressDTO.getMobile());
        address.setProvince(addressDTO.getProvince());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setDetailAddress(addressDTO.getAddress());
        address.setZipCode(addressDTO.getZipCode());
        address.setIsDefault(Boolean.TRUE.equals(addressDTO.getIsDefault()) ? 1 : 0);

        addressRepository.insert(address);
        log.info("新增收货地址成功: userId={}, addressId={}", userId, address.getId());
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long id, Long userId, AddressDTO addressDTO) {
        UserAddress address = addressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "收货地址不存在");
        }

        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改该收货地址");
        }

        // 验证电话或手机至少填写一项
        if ((addressDTO.getPhone() == null || addressDTO.getPhone().trim().isEmpty()) &&
            (addressDTO.getMobile() == null || addressDTO.getMobile().trim().isEmpty())) {
            throw new BusinessException(400, "联系电话和手机号码必须填写一项");
        }

        // 如果设置为默认地址，需要取消其他默认地址
        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && address.getIsDefault() == 0) {
            setOtherAddressesNotDefault(userId, id);
        }

        address.setReceiverName(addressDTO.getRecipient());
        address.setReceiverPhone(addressDTO.getPhone());
        address.setReceiverMobile(addressDTO.getMobile());
        address.setProvince(addressDTO.getProvince());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setDetailAddress(addressDTO.getAddress());
        address.setZipCode(addressDTO.getZipCode());
        address.setIsDefault(Boolean.TRUE.equals(addressDTO.getIsDefault()) ? 1 : 0);

        addressRepository.updateById(address);
        log.info("更新收货地址成功: userId={}, addressId={}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long id, Long userId) {
        UserAddress address = addressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "收货地址不存在");
        }

        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除该收货地址");
        }

        addressRepository.deleteById(id);
        log.info("删除收货地址成功: userId={}, addressId={}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long id, Long userId) {
        UserAddress address = addressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "收货地址不存在");
        }

        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权设置该收货地址为默认");
        }

        // 取消其他默认地址
        setOtherAddressesNotDefault(userId, id);

        // 设置当前地址为默认
        address.setIsDefault(1);
        addressRepository.updateById(address);
        log.info("设置默认收货地址成功: userId={}, addressId={}", userId, id);
    }

    /**
     * 取消用户的其他默认地址（新增时使用）
     */
    private void setOtherAddressesNotDefault(Long userId) {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        wrapper.eq(UserAddress::getIsDefault, 1);
        wrapper.set(UserAddress::getIsDefault, 0);
        addressRepository.update(null, wrapper);
    }

    /**
     * 取消用户的其他默认地址（更新时使用，排除当前地址）
     */
    private void setOtherAddressesNotDefault(Long userId, Long excludeId) {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        wrapper.eq(UserAddress::getIsDefault, 1);
        wrapper.ne(UserAddress::getId, excludeId);
        wrapper.set(UserAddress::getIsDefault, 0);
        addressRepository.update(null, wrapper);
    }

    /**
     * 实体转VO
     */
    private AddressVO convertToVO(UserAddress address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        vo.setRecipient(address.getReceiverName());
        vo.setPhone(address.getReceiverPhone());
        vo.setMobile(address.getReceiverMobile());
        vo.setAddress(address.getDetailAddress());
        vo.setIsDefault(address.getIsDefault() == 1);

        // 构建完整地址
        StringBuilder fullAddress = new StringBuilder();
        if (address.getProvince() != null) {
            fullAddress.append(address.getProvince());
        }
        if (address.getCity() != null) {
            fullAddress.append(address.getCity());
        }
        if (address.getDistrict() != null) {
            fullAddress.append(address.getDistrict());
        }
        if (address.getDetailAddress() != null) {
            fullAddress.append(address.getDetailAddress());
        }
        vo.setFullAddress(fullAddress.toString());

        return vo;
    }
}






































































