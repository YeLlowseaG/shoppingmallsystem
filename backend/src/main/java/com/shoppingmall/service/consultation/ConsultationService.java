package com.shoppingmall.service.consultation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.ConsultationDTO;
import com.shoppingmall.dto.ConsultationReplyDTO;
import com.shoppingmall.entity.Consultation;
import com.shoppingmall.vo.ConsultationVO;

/**
 * 咨询服务接口
 */
public interface ConsultationService {
    
    /**
     * 提交咨询
     */
    void submitConsultation(ConsultationDTO consultationDTO, Long userId);
    
    /**
     * 管理端分页查询咨询
     */
    Page<ConsultationVO> getConsultationPage(int current, int size, 
                                           String productName, String contactName, Integer status);
    
    /**
     * 用户分页查询自己的咨询
     */
    Page<ConsultationVO> getUserConsultations(int current, int size, Long userId);
    
    /**
     * 回复咨询
     */
    void replyConsultation(Long consultationId, ConsultationReplyDTO replyDTO, Long adminId);
    
    /**
     * 更新咨询状态
     */
    void updateConsultationStatus(Long consultationId, Integer status);
    
    /**
     * 根据ID获取咨询详情
     */
    ConsultationVO getConsultationById(Long consultationId);
    
    /**
     * 删除咨询
     */
    void deleteConsultation(Long consultationId);
}