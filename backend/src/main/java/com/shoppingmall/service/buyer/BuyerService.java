package com.shoppingmall.service.buyer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.BuyerDTO;
import com.shoppingmall.vo.BuyerVO;

/**
 * 采购者管理服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
public interface BuyerService {

    /**
     * 获取采购者列表（分页）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param username 用户名（可选）
     * @param phone    手机号（可选）
     * @param status   状态（可选）
     * @param isMember 是否会员（可选，0-普通用户，1-会员）
     * @param memberLevelId 会员等级ID（可选）
     * @return 采购者列表
     */
    Page<BuyerVO> getBuyerList(Integer page, Integer pageSize, String username, String phone, Integer status, Integer isMember, Long memberLevelId);

    /**
     * 根据ID获取采购者信息
     *
     * @param id 采购者ID
     * @return 采购者信息
     */
    BuyerVO getBuyerById(Long id);

    /**
     * 更新采购者信息
     *
     * @param id        采购者ID
     * @param buyerDTO  采购者信息
     * @param auditorId 审核人ID
     */
    void updateBuyer(Long id, BuyerDTO buyerDTO, Long auditorId);

    /**
     * 更新采购者状态
     *
     * @param id     采购者ID
     * @param status 状态（0-待审核，1-已激活，2-已禁用）
     */
    void updateBuyerStatus(Long id, Integer status);

    /**
     * 更新采购者会员信息
     *
     * @param id            采购者ID
     * @param isMember      是否会员（0-普通用户，1-会员）
     * @param memberLevelId 会员等级ID（关联 member_level 表，普通用户为 NULL）
     */
    void updateBuyerMemberInfo(Long id, Integer isMember, Long memberLevelId);

    /**
     * 审核采购者
     *
     * @param id        采购者ID
     * @param buyerDTO  审核信息
     * @param auditorId 审核人ID
     */
    void auditBuyer(Long id, BuyerDTO buyerDTO, Long auditorId);

    /**
     * 获取待审核采购者列表（分页）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param username 用户名（可选）
     * @param realName 姓名（可选）
     * @param phone    手机号（可选）
     * @return 待审核采购者列表
     */
    Page<BuyerVO> getPendingAuditList(Integer page, Integer pageSize, String username, String realName, String phone);

    /**
     * 重置采购者密码
     *
     * @param id       采购者ID
     * @param password 新密码
     */
    void resetPassword(Long id, String password);
}
