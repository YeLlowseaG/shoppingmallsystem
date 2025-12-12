package com.shoppingmall.repository.consultation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 咨询Repository
 */
@Mapper
public interface ConsultationRepository extends BaseMapper<Consultation> {
    
    /**
     * 分页查询咨询记录（管理端）
     */
    Page<Consultation> selectPageWithProduct(Page<Consultation> page, 
                                           @Param("productName") String productName,
                                           @Param("contactName") String contactName,
                                           @Param("status") Integer status);
    
    /**
     * 分页查询用户的咨询记录
     */
    Page<Consultation> selectUserConsultations(Page<Consultation> page, 
                                             @Param("userId") Long userId);
}