package com.shoppingmall.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.entity.Announcement;
import com.shoppingmall.repository.announcement.AnnouncementRepository;
import com.shoppingmall.service.common.AnnouncementService;
import com.shoppingmall.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 公告服务实现类（前端使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service("commonAnnouncementService")
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<AnnouncementVO> getAnnouncementList(Integer pageNum, Integer pageSize) {
        // 只查询启用的公告，按发布日期倒序，排序字段升序
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getDeleted, 0)
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
        if (announcement == null || announcement.getDeleted() == 1 || announcement.getStatus() == 0) {
            return null;
        }
        return convertToVO(announcement);
    }

    @Override
    public AnnouncementVO getPrevAnnouncement(Long id) {
        // 先获取当前公告
        Announcement current = announcementRepository.selectById(id);
        if (current == null || current.getDeleted() == 1 || current.getStatus() == 0) {
            return null;
        }

        // 查询发布日期小于当前公告，或者发布日期相同但排序更大的公告
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getDeleted, 0)
                .and(w -> w.lt(Announcement::getPublishDate, current.getPublishDate())
                        .or()
                        .and(sub -> sub.eq(Announcement::getPublishDate, current.getPublishDate())
                                .gt(Announcement::getSort, current.getSort())))
                .orderByDesc(Announcement::getPublishDate)
                .orderByAsc(Announcement::getSort)
                .last("LIMIT 1");

        Announcement prev = announcementRepository.selectOne(wrapper);
        return prev != null ? convertToVO(prev) : null;
    }

    @Override
    public AnnouncementVO getNextAnnouncement(Long id) {
        // 先获取当前公告
        Announcement current = announcementRepository.selectById(id);
        if (current == null || current.getDeleted() == 1 || current.getStatus() == 0) {
            return null;
        }

        // 查询发布日期大于当前公告，或者发布日期相同但排序更小的公告
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getDeleted, 0)
                .and(w -> w.gt(Announcement::getPublishDate, current.getPublishDate())
                        .or()
                        .and(sub -> sub.eq(Announcement::getPublishDate, current.getPublishDate())
                                .lt(Announcement::getSort, current.getSort())))
                .orderByAsc(Announcement::getPublishDate)
                .orderByAsc(Announcement::getSort)
                .last("LIMIT 1");

        Announcement next = announcementRepository.selectOne(wrapper);
        return next != null ? convertToVO(next) : null;
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

