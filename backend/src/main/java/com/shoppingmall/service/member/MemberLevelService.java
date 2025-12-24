package com.shoppingmall.service.member;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.dto.MemberLevelDTO;
import com.shoppingmall.vo.MemberLevelVO;

import java.util.List;

/**
 * 会员等级服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
public interface MemberLevelService {

    /**
     * 分页查询会员等级列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param levelName 等级名称（可选）
     * @param status   状态（可选）
     * @return 会员等级分页列表
     */
    Page<MemberLevelVO> getMemberLevelPage(Integer page, Integer pageSize, String levelName, Integer status);

    /**
     * 获取所有启用的会员等级列表（按排序号排序）
     *
     * @return 会员等级列表
     */
    List<MemberLevelVO> getAllEnabledMemberLevels();

    /**
     * 根据ID获取会员等级详情
     *
     * @param id 等级ID
     * @return 会员等级详情
     */
    MemberLevelVO getMemberLevelById(Long id);

    /**
     * 根据积分获取对应的会员等级
     *
     * @param points 积分
     * @return 会员等级，如果不存在则返回null
     */
    MemberLevelVO getMemberLevelByPoints(Integer points);

    /**
     * 创建会员等级
     *
     * @param memberLevelDTO 会员等级信息
     * @return 等级ID
     */
    Long createMemberLevel(MemberLevelDTO memberLevelDTO);

    /**
     * 更新会员等级
     *
     * @param memberLevelDTO 会员等级信息
     */
    void updateMemberLevel(MemberLevelDTO memberLevelDTO);

    /**
     * 删除会员等级
     *
     * @param id 等级ID
     */
    void deleteMemberLevel(Long id);

    /**
     * 更新会员等级状态
     *
     * @param id     等级ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateMemberLevelStatus(Long id, Integer status);
}















