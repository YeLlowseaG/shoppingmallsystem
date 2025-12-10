package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.AnnouncementDTO;
import com.shoppingmall.entity.Announcement;
import com.shoppingmall.repository.announcement.AnnouncementRepository;
import com.shoppingmall.service.admin.AnnouncementService;
import com.shoppingmall.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 公告管理服务实现类（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service("adminAnnouncementService")
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<AnnouncementVO> getAnnouncementList(Integer pageNum, Integer pageSize, String title, Integer status) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getDeleted, 0)
                .like(StringUtils.hasText(title), Announcement::getTitle, title)
                .eq(status != null, Announcement::getStatus, status)
                .orderByDesc(Announcement::getPublishDate)
                .orderByAsc(Announcement::getSort);

        Page<Announcement> announcementPage = announcementRepository.selectPage(page, wrapper);

        // 转换为VO
        Page<AnnouncementVO> voPage = new Page<>(announcementPage.getCurrent(), announcementPage.getSize(), announcementPage.getTotal());
        List<AnnouncementVO> voList = announcementPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AnnouncementVO getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }
        return convertToVO(announcement);
    }

    @Override
    @Transactional
    public void addAnnouncement(AnnouncementDTO announcementDTO) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(announcementDTO, announcement);

        // 转换图片数组为JSON字符串
        if (announcementDTO.getImages() != null && !announcementDTO.getImages().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(announcementDTO.getImages());
                announcement.setImages(imagesJson);
            } catch (Exception e) {
                log.error("转换图片数组为JSON失败", e);
                throw new BusinessException("图片数据格式错误");
            }
        } else {
            announcement.setImages("[]");
        }

        announcementRepository.insert(announcement);
    }

    @Override
    @Transactional
    public void updateAnnouncement(Long id, AnnouncementDTO announcementDTO) {
        Announcement announcement = announcementRepository.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }

        BeanUtils.copyProperties(announcementDTO, announcement);
        announcement.setId(id);

        // 转换图片数组为JSON字符串
        if (announcementDTO.getImages() != null && !announcementDTO.getImages().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(announcementDTO.getImages());
                announcement.setImages(imagesJson);
            } catch (Exception e) {
                log.error("转换图片数组为JSON失败", e);
                throw new BusinessException("图片数据格式错误");
            }
        } else {
            announcement.setImages("[]");
        }

        announcementRepository.updateById(announcement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }

        // 逻辑删除
        announcementRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateAnnouncementStatus(Long id, Integer status) {
        Announcement announcement = announcementRepository.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BusinessException("公告不存在");
        }

        announcement.setStatus(status);
        announcementRepository.updateById(announcement);
    }

    /**
     * 转换为VO
     */
    private AnnouncementVO convertToVO(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);

        // 解析图片JSON
        if (announcement.getImages() != null && !announcement.getImages().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(announcement.getImages(), new TypeReference<List<String>>() {});
                vo.setImages(images);
            } catch (Exception e) {
                log.error("解析公告图片JSON失败: {}", announcement.getImages(), e);
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        return vo;
    }
}

