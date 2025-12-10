package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.AnnouncementDTO;
import com.shoppingmall.service.admin.AnnouncementService;
import com.shoppingmall.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理控制器（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("adminAnnouncementController")
@RequestMapping("/api/admin/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取公告列表（分页）
     */
    @GetMapping("/list")
    public Result<IPage<AnnouncementVO>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status
    ) {
        IPage<AnnouncementVO> page = announcementService.getAnnouncementList(pageNum, pageSize, title, status);
        return Result.success(page);
    }

    /**
     * 根据ID获取公告信息
     */
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncementById(@PathVariable Long id) {
        AnnouncementVO announcement = announcementService.getAnnouncementById(id);
        return Result.success(announcement);
    }

    /**
     * 新增公告
     */
    @PostMapping
    public Result<Void> addAnnouncement(@RequestBody @Validated AnnouncementDTO announcementDTO) {
        announcementService.addAnnouncement(announcementDTO);
        return Result.success();
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody @Validated AnnouncementDTO announcementDTO) {
        announcementService.updateAnnouncement(id, announcementDTO);
        return Result.success();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    /**
     * 启用/禁用公告
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateAnnouncementStatus(@PathVariable Long id, @RequestParam Integer status) {
        announcementService.updateAnnouncementStatus(id, status);
        return Result.success();
    }
}

