package com.shoppingmall.service.consultation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.ConsultationDTO;
import com.shoppingmall.dto.ConsultationReplyDTO;
import com.shoppingmall.entity.AdminUser;
import com.shoppingmall.entity.Consultation;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.consultation.ConsultationRepository;
import com.shoppingmall.repository.permission.AdminUserRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.consultation.ConsultationService;
import com.shoppingmall.vo.ConsultationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;

/**
 * 咨询服务实现
 */
@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {
    
    private final ConsultationRepository consultationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    
    @Override
    public void submitConsultation(ConsultationDTO consultationDTO, Long userId) {
        // 验证商品是否存在
        Product product = productRepository.selectById(consultationDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        Consultation consultation = new Consultation();
        BeanUtils.copyProperties(consultationDTO, consultation);
        consultation.setUserId(userId);
        consultation.setStatus(0); // 待回复
        
        consultationRepository.insert(consultation);
    }
    
    @Override
    public Page<ConsultationVO> getConsultationPage(int current, int size, 
                                                  String productName, String contactName, Integer status) {
        Page<Consultation> page = new Page<>(current, size);
        Page<Consultation> consultationPage = consultationRepository.selectPageWithProduct(
                page, productName, contactName, status);
        
        return convertToConsultationVOPage(consultationPage);
    }
    
    @Override
    public Page<ConsultationVO> getUserConsultations(int current, int size, Long userId) {
        Page<Consultation> page = new Page<>(current, size);
        Page<Consultation> consultationPage = consultationRepository.selectUserConsultations(page, userId);
        
        return convertToConsultationVOPage(consultationPage);
    }
    
    @Override
    public void replyConsultation(Long consultationId, ConsultationReplyDTO replyDTO, Long adminId) {
        Consultation consultation = consultationRepository.selectById(consultationId);
        if (consultation == null) {
            throw new BusinessException("咨询不存在");
        }
        
        consultation.setReplyContent(replyDTO.getReplyContent());
        consultation.setReplyTime(LocalDateTime.now());
        consultation.setReplyAdminId(adminId);
        consultation.setStatus(1); // 已回复
        
        consultationRepository.updateById(consultation);
    }
    
    @Override
    public void updateConsultationStatus(Long consultationId, Integer status) {
        Consultation consultation = consultationRepository.selectById(consultationId);
        if (consultation == null) {
            throw new BusinessException("咨询不存在");
        }
        
        consultation.setStatus(status);
        consultationRepository.updateById(consultation);
    }
    
    @Override
    public ConsultationVO getConsultationById(Long consultationId) {
        Consultation consultation = consultationRepository.selectById(consultationId);
        if (consultation == null) {
            throw new BusinessException("咨询不存在");
        }
        
        return convertToConsultationVO(consultation);
    }
    
    @Override
    public void deleteConsultation(Long consultationId) {
        consultationRepository.deleteById(consultationId);
    }
    
    private Page<ConsultationVO> convertToConsultationVOPage(Page<Consultation> consultationPage) {
        Page<ConsultationVO> voPage = new Page<>(consultationPage.getCurrent(),
                                               consultationPage.getSize(),
                                               consultationPage.getTotal());

        // 创建新的ArrayList来存储转换后的VO对象
        List<ConsultationVO> voList = new ArrayList<>();
        for (Consultation consultation : consultationPage.getRecords()) {
            voList.add(convertToConsultationVO(consultation));
        }
        voPage.setRecords(voList);

        return voPage;
    }
    
    private ConsultationVO convertToConsultationVO(Consultation consultation) {
        ConsultationVO vo = new ConsultationVO();
        BeanUtils.copyProperties(consultation, vo);
        
        // 获取商品信息
        if (consultation.getProductId() != null) {
            Product product = productRepository.selectById(consultation.getProductId());
            if (product != null) {
                vo.setProductName(product.getProductName());
                vo.setProductImage(product.getMainImage());
            }
        }
        
        // 获取用户信息
        if (consultation.getUserId() != null) {
            User user = userRepository.selectById(consultation.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
            }
        }
        
        // 获取回复管理员信息
        if (consultation.getReplyAdminId() != null) {
            AdminUser adminUser = adminUserRepository.selectById(consultation.getReplyAdminId());
            if (adminUser != null) {
                vo.setReplyAdminName(adminUser.getUsername());
            }
        }
        
        // 设置状态文本
        vo.setStatusText(getStatusText(consultation.getStatus()));
        
        return vo;
    }
    
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待回复";
            case 1: return "已回复";
            case 2: return "已关闭";
            default: return "未知";
        }
    }
}