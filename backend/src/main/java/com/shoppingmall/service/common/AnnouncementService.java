package com.shoppingmall.service.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.vo.AnnouncementVO;

/**
 * 公告服务接口（前端使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface AnnouncementService {

    /**
     * 分页查询公告列表（只返回启用的公告）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<AnnouncementVO> getAnnouncementList(Integer pageNum, Integer pageSize);

    /**
     * 根据ID获取公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    AnnouncementVO getAnnouncementById(Long id);

    /**
     * 获取上一篇公告
     *
     * @param id 当前公告ID
     * @return 上一篇公告，如果没有则返回null
     */
    AnnouncementVO getPrevAnnouncement(Long id);

    /**
     * 获取下一篇公告
     *
     * @param id 当前公告ID
     * @return 下一篇公告，如果没有则返回null
     */
    AnnouncementVO getNextAnnouncement(Long id);
}












































