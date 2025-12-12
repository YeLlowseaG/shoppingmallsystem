package com.shoppingmall.service.sku.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.dto.ProductSpecKeyDTO;
import com.shoppingmall.entity.ProductSpecKey;
import com.shoppingmall.entity.ProductSpecValue;
import com.shoppingmall.repository.sku.ProductSpecKeyRepository;
import com.shoppingmall.repository.sku.ProductSpecValueRepository;
import com.shoppingmall.service.sku.ProductSpecKeyService;
import com.shoppingmall.vo.ProductSpecKeyVO;
import com.shoppingmall.vo.ProductSpecValueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品规格属性Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSpecKeyServiceImpl implements ProductSpecKeyService {
    
    private final ProductSpecKeyRepository specKeyRepository;
    private final ProductSpecValueRepository specValueRepository;
    
    @Override
    @Transactional
    public Long createSpecKey(ProductSpecKeyDTO dto) {
        ProductSpecKey entity = new ProductSpecKey();
        BeanUtils.copyProperties(dto, entity);
        
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        
        specKeyRepository.insert(entity);
        log.info("创建规格属性成功，ID: {}, 商品ID: {}, 规格名称: {}", 
                entity.getId(), dto.getProductId(), dto.getSpecName());
        return entity.getId();
    }
    
    @Override
    @Transactional
    public boolean updateSpecKey(Long id, ProductSpecKeyDTO dto) {
        ProductSpecKey entity = specKeyRepository.selectById(id);
        if (entity == null) {
            log.warn("规格属性不存在，ID: {}", id);
            return false;
        }
        
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        
        int result = specKeyRepository.updateById(entity);
        log.info("更新规格属性成功，ID: {}, 规格名称: {}", id, dto.getSpecName());
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteSpecKey(Long id) {
        ProductSpecKey entity = specKeyRepository.selectById(id);
        if (entity == null) {
            log.warn("规格属性不存在，ID: {}", id);
            return false;
        }
        
        // 删除关联的规格值
        specValueRepository.deleteBySpecKeyId(id);
        
        // 删除规格属性
        int result = specKeyRepository.deleteById(id);
        log.info("删除规格属性成功，ID: {}", id);
        return result > 0;
    }
    
    @Override
    public List<ProductSpecKeyVO> getSpecKeysByProductId(Long productId) {
        List<ProductSpecKey> specKeys = specKeyRepository.findByProductId(productId);
        
        return specKeys.stream().map(specKey -> {
            ProductSpecKeyVO vo = new ProductSpecKeyVO();
            BeanUtils.copyProperties(specKey, vo);
            
            // 查询关联的规格值
            List<ProductSpecValue> specValues = specValueRepository.findBySpecKeyId(specKey.getId());
            List<ProductSpecValueVO> specValueVOs = specValues.stream().map(specValue -> {
                ProductSpecValueVO valueVO = new ProductSpecValueVO();
                BeanUtils.copyProperties(specValue, valueVO);
                return valueVO;
            }).collect(Collectors.toList());
            
            vo.setSpecValues(specValueVOs);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public ProductSpecKeyVO getSpecKeyById(Long id) {
        ProductSpecKey entity = specKeyRepository.selectById(id);
        if (entity == null) {
            return null;
        }
        
        ProductSpecKeyVO vo = new ProductSpecKeyVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 查询关联的规格值
        List<ProductSpecValue> specValues = specValueRepository.findBySpecKeyId(id);
        List<ProductSpecValueVO> specValueVOs = specValues.stream().map(specValue -> {
            ProductSpecValueVO valueVO = new ProductSpecValueVO();
            BeanUtils.copyProperties(specValue, valueVO);
            return valueVO;
        }).collect(Collectors.toList());
        
        vo.setSpecValues(specValueVOs);
        return vo;
    }
}