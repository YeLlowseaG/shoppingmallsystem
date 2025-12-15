package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.MemberLevelDTO;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.vo.MemberLevelVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员等级管理控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@RestController("adminMemberLevelController")
@RequestMapping("/api/admin/member/level")
@RequiredArgsConstructor
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    /**
     * 分页查询会员等级列表
     */
    @GetMapping("/page")
    public Result<Page<MemberLevelVO>> getMemberLevelPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String levelName,
            @RequestParam(required = false) Integer status
    ) {
        Page<MemberLevelVO> result = memberLevelService.getMemberLevelPage(page, pageSize, levelName, status);
        return Result.success("获取成功", result);
    }

    /**
     * 获取所有启用的会员等级列表
     */
    @GetMapping("/all")
    public Result<List<MemberLevelVO>> getAllEnabledMemberLevels() {
        List<MemberLevelVO> result = memberLevelService.getAllEnabledMemberLevels();
        return Result.success("获取成功", result);
    }

    /**
     * 根据ID获取会员等级详情
     */
    @GetMapping("/{id}")
    public Result<MemberLevelVO> getMemberLevelById(@PathVariable Long id) {
        MemberLevelVO result = memberLevelService.getMemberLevelById(id);
        return Result.success("获取成功", result);
    }

    /**
     * 根据积分获取对应的会员等级
     * 已屏蔽：业务上不需要积分功能
     */
    // @GetMapping("/by-points")
    // public Result<MemberLevelVO> getMemberLevelByPoints(@RequestParam Integer points) {
    //     MemberLevelVO result = memberLevelService.getMemberLevelByPoints(points);
    //     return Result.success("获取成功", result);
    // }

    /**
     * 创建会员等级
     */
    @PostMapping
    public Result<Long> createMemberLevel(@Valid @RequestBody MemberLevelDTO memberLevelDTO) {
        Long id = memberLevelService.createMemberLevel(memberLevelDTO);
        return Result.success("创建成功", id);
    }

    /**
     * 更新会员等级
     */
    @PutMapping
    public Result<Void> updateMemberLevel(@Valid @RequestBody MemberLevelDTO memberLevelDTO) {
        memberLevelService.updateMemberLevel(memberLevelDTO);
        return Result.success("更新成功", null);
    }

    /**
     * 删除会员等级
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMemberLevel(@PathVariable Long id) {
        memberLevelService.deleteMemberLevel(id);
        return Result.success("删除成功", null);
    }

    /**
     * 更新会员等级状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateMemberLevelStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        memberLevelService.updateMemberLevelStatus(id, status);
        return Result.success("更新状态成功", null);
    }
}



