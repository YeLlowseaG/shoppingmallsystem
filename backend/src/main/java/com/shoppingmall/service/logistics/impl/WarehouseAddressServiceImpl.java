package com.shoppingmall.service.logistics.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.WarehouseAddressDTO;
import com.shoppingmall.entity.WarehouseAddress;
import com.shoppingmall.repository.logistics.WarehouseAddressRepository;
import com.shoppingmall.service.logistics.WarehouseAddressService;
import com.shoppingmall.vo.WarehouseAddressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发货地址库服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseAddressServiceImpl implements WarehouseAddressService {

    private final WarehouseAddressRepository warehouseAddressRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<WarehouseAddressVO> getWarehouseAddressList(Integer page, Integer pageSize, String keyword, Integer status) {
        Page<WarehouseAddress> addressPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<WarehouseAddress> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(WarehouseAddress::getWarehouseName, keyword)
                    .or()
                    .like(WarehouseAddress::getContactName, keyword)
                    .or()
                    .like(WarehouseAddress::getContactPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(WarehouseAddress::getStatus, status);
        }

        wrapper.orderByDesc(WarehouseAddress::getIsDefault);
        wrapper.orderByDesc(WarehouseAddress::getCreateTime);

        Page<WarehouseAddress> result = warehouseAddressRepository.selectPage(addressPage, wrapper);

        Page<WarehouseAddressVO> voPage = new Page<>(page, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<WarehouseAddressVO> getAllEnabledWarehouseAddresses() {
        LambdaQueryWrapper<WarehouseAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarehouseAddress::getStatus, 1);
        wrapper.orderByDesc(WarehouseAddress::getIsDefault);
        wrapper.orderByDesc(WarehouseAddress::getCreateTime);

        List<WarehouseAddress> addresses = warehouseAddressRepository.selectList(wrapper);
        return addresses.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public WarehouseAddressVO getDefaultWarehouseAddress() {
        LambdaQueryWrapper<WarehouseAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarehouseAddress::getStatus, 1);
        wrapper.eq(WarehouseAddress::getIsDefault, 1);
        wrapper.orderByDesc(WarehouseAddress::getCreateTime);
        wrapper.last("LIMIT 1");

        WarehouseAddress address = warehouseAddressRepository.selectOne(wrapper);
        if (address == null) {
            // 如果没有默认地址，返回第一个启用的地址
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WarehouseAddress::getStatus, 1);
            wrapper.orderByDesc(WarehouseAddress::getCreateTime);
            wrapper.last("LIMIT 1");
            address = warehouseAddressRepository.selectOne(wrapper);
        }

        return address != null ? convertToVO(address) : null;
    }

    @Override
    public WarehouseAddressVO getWarehouseAddressById(Long id) {
        WarehouseAddress address = warehouseAddressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "发货地址不存在");
        }
        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWarehouseAddress(WarehouseAddressDTO dto) {
        // 如果设置为默认地址，需要取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            cancelOtherDefaultAddresses();
        }

        WarehouseAddress address = new WarehouseAddress();
        BeanUtils.copyProperties(dto, address);
        if (address.getStatus() == null) {
            address.setStatus(1);
        }
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }

        warehouseAddressRepository.insert(address);
        log.info("新增发货地址成功: id={}, warehouseName={}", address.getId(), address.getWarehouseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouseAddress(Long id, WarehouseAddressDTO dto) {
        WarehouseAddress address = warehouseAddressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "发货地址不存在");
        }

        // 如果设置为默认地址，需要取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1 && address.getIsDefault() != 1) {
            cancelOtherDefaultAddresses();
        }

        BeanUtils.copyProperties(dto, address, "id", "createTime");
        warehouseAddressRepository.updateById(address);
        log.info("更新发货地址成功: id={}, warehouseName={}", id, address.getWarehouseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouseAddress(Long id) {
        WarehouseAddress address = warehouseAddressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "发货地址不存在");
        }

        warehouseAddressRepository.deleteById(id);
        log.info("删除发货地址成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouseAddressStatus(Long id, Integer status) {
        WarehouseAddress address = warehouseAddressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "发货地址不存在");
        }

        address.setStatus(status);
        warehouseAddressRepository.updateById(address);
        log.info("更新发货地址状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultWarehouseAddress(Long id) {
        WarehouseAddress address = warehouseAddressRepository.selectById(id);
        if (address == null) {
            throw new BusinessException(404, "发货地址不存在");
        }

        if (address.getStatus() != 1) {
            throw new BusinessException(400, "只有启用的地址才能设置为默认地址");
        }

        // 取消其他默认地址
        cancelOtherDefaultAddresses();

        // 设置当前地址为默认
        address.setIsDefault(1);
        warehouseAddressRepository.updateById(address);
        log.info("设置默认发货地址成功: id={}", id);
    }

    /**
     * 取消其他默认地址
     */
    private void cancelOtherDefaultAddresses() {
        LambdaQueryWrapper<WarehouseAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarehouseAddress::getIsDefault, 1);
        List<WarehouseAddress> defaultAddresses = warehouseAddressRepository.selectList(wrapper);
        for (WarehouseAddress addr : defaultAddresses) {
            addr.setIsDefault(0);
            warehouseAddressRepository.updateById(addr);
        }
    }

    /**
     * 实体转VO
     */
    private WarehouseAddressVO convertToVO(WarehouseAddress address) {
        WarehouseAddressVO vo = new WarehouseAddressVO();
        BeanUtils.copyProperties(address, vo);
        if (address.getCreateTime() != null) {
            vo.setCreateTime(address.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        if (address.getUpdateTime() != null) {
            vo.setUpdateTime(address.getUpdateTime().format(DATE_TIME_FORMATTER));
        }
        return vo;
    }
}

