package com.shoppingmall.controller.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.common.AnnouncementService;
import com.shoppingmall.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器（前端使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("commonAnnouncementController")
@RequestMapping("/api/common/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 分页查询公告列表
     */
    @GetMapping("/list")
    public Result<IPage<AnnouncementVO>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        IPage<AnnouncementVO> page = announcementService.getAnnouncementList(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 根据ID获取公告详情
     */
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncementById(@PathVariable Long id) {
        AnnouncementVO announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return Result.error("公告不存在或已下架");
        }
        return Result.success(announcement);
    }

    /**
     * 获取上一篇公告
     */
    @GetMapping("/{id}/prev")
    public Result<AnnouncementVO> getPrevAnnouncement(@PathVariable Long id) {
        AnnouncementVO announcement = announcementService.getPrevAnnouncement(id);
        return Result.success(announcement);
    }

    /**
     * 获取下一篇公告
     */
    @GetMapping("/{id}/next")
    public Result<AnnouncementVO> getNextAnnouncement(@PathVariable Long id) {
        AnnouncementVO announcement = announcementService.getNextAnnouncement(id);
        return Result.success(announcement);
    }
}
































