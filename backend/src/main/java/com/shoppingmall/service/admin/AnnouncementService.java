package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.AnnouncementDTO;
import com.shoppingmall.vo.AnnouncementVO;

/**
 * 公告管理服务接口（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface AnnouncementService {

    /**
     * 获取公告列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    标题（可选，模糊查询）
     * @param status   状态（可选，0-禁用，1-启用）
     * @return 分页结果
     */
    IPage<AnnouncementVO> getAnnouncementList(Integer pageNum, Integer pageSize, String title, Integer status);

    /**
     * 根据ID获取公告信息
     *
     * @param id 公告ID
     * @return 公告信息
     */
    AnnouncementVO getAnnouncementById(Long id);

    /**
     * 新增公告
     *
     * @param announcementDTO 公告DTO
     */
    void addAnnouncement(AnnouncementDTO announcementDTO);

    /**
     * 更新公告
     *
     * @param id               公告ID
     * @param announcementDTO 公告DTO
     */
    void updateAnnouncement(Long id, AnnouncementDTO announcementDTO);

    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    void deleteAnnouncement(Long id);

    /**
     * 启用/禁用公告
     *
     * @param id     公告ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateAnnouncementStatus(Long id, Integer status);
}

