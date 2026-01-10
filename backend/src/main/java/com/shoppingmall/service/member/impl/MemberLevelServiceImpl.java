package com.shoppingmall.service.member.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.MemberLevelDTO;
import com.shoppingmall.entity.MemberLevel;
import com.shoppingmall.entity.ProductMemberPrice;
import com.shoppingmall.entity.ProductSkuMemberPrice;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.member.MemberLevelRepository;
import com.shoppingmall.repository.product.ProductMemberPriceRepository;
import com.shoppingmall.repository.sku.ProductSkuMemberPriceRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.vo.MemberLevelVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员等级服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberLevelServiceImpl implements MemberLevelService {

    private final MemberLevelRepository memberLevelRepository;
    private final UserRepository userRepository;
    private final ProductMemberPriceRepository productMemberPriceRepository;
    private final ProductSkuMemberPriceRepository productSkuMemberPriceRepository;

    @Override
    public Page<MemberLevelVO> getMemberLevelPage(Integer page, Integer pageSize, String levelName, Integer status) {
        Page<MemberLevel> memberLevelPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();

        // 等级名称筛选
        if (StringUtil.isNotBlank(levelName)) {
            wrapper.like(MemberLevel::getLevelName, levelName);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(MemberLevel::getStatus, status);
        }

        // 按排序号升序，然后按ID升序
        wrapper.orderByAsc(MemberLevel::getSortOrder);
        wrapper.orderByAsc(MemberLevel::getId);

        Page<MemberLevel> result = memberLevelRepository.selectPage(memberLevelPage, wrapper);

        Page<MemberLevelVO> voPage = new Page<>(page, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<MemberLevelVO> getAllEnabledMemberLevels() {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getStatus, 1)
                .orderByAsc(MemberLevel::getSortOrder)
                .orderByAsc(MemberLevel::getId);

        List<MemberLevel> levels = memberLevelRepository.selectList(wrapper);
        return levels.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public MemberLevelVO getMemberLevelById(Long id) {
        MemberLevel memberLevel = memberLevelRepository.selectById(id);
        if (memberLevel == null) {
            throw new BusinessException(404, "会员等级不存在");
        }
        return convertToVO(memberLevel);
    }

    @Override
    public MemberLevelVO getMemberLevelByPoints(Integer points) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getStatus, 1)
                .le(MemberLevel::getMinPoints, points)
                .and(w -> w.isNull(MemberLevel::getMaxPoints)
                        .or()
                        .gt(MemberLevel::getMaxPoints, points))
                .orderByDesc(MemberLevel::getSortOrder)
                .orderByDesc(MemberLevel::getMinPoints)
                .last("LIMIT 1");

        MemberLevel memberLevel = memberLevelRepository.selectOne(wrapper);
        return memberLevel != null ? convertToVO(memberLevel) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMemberLevel(MemberLevelDTO memberLevelDTO) {
        // 积分区间验证已屏蔽：业务上不需要积分功能
        // validatePointsRange(memberLevelDTO.getMinPoints(), memberLevelDTO.getMaxPoints(), null);

        MemberLevel memberLevel = new MemberLevel();
        BeanUtils.copyProperties(memberLevelDTO, memberLevel);
        // 设置默认积分值（已屏蔽但保留字段）
        memberLevel.setMinPoints(0);
        memberLevel.setMaxPoints(null);
        memberLevelRepository.insert(memberLevel);
        log.info("创建会员等级成功: {}", memberLevel.getLevelName());
        return memberLevel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberLevel(MemberLevelDTO memberLevelDTO) {
        if (memberLevelDTO.getId() == null) {
            throw new BusinessException(400, "等级ID不能为空");
        }

        MemberLevel existing = memberLevelRepository.selectById(memberLevelDTO.getId());
        if (existing == null) {
            throw new BusinessException(404, "会员等级不存在");
        }

        // 积分区间验证已屏蔽：业务上不需要积分功能
        // validatePointsRange(memberLevelDTO.getMinPoints(), memberLevelDTO.getMaxPoints(), memberLevelDTO.getId());

        BeanUtils.copyProperties(memberLevelDTO, existing);
        // 设置默认积分值（已屏蔽但保留字段）
        existing.setMinPoints(0);
        existing.setMaxPoints(null);
        memberLevelRepository.updateById(existing);
        log.info("更新会员等级成功: id={}, levelName={}", existing.getId(), existing.getLevelName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMemberLevel(Long id) {
        MemberLevel existing = memberLevelRepository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "会员等级不存在");
        }

        // 收集所有关联信息
        List<String> reasons = new ArrayList<>();

        // 1. 检查是否有用户使用了该会员等级
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getIsMember, 1)  // 是会员
                   .eq(User::getMemberLevelId, id)  // 使用了该等级
                   .eq(User::getDeleted, 0);  // 未删除
        Long userCount = userRepository.selectCount(userWrapper);
        if (userCount != null && userCount > 0) {
            reasons.add(String.format("%d 位会员正在使用", userCount));
        }

        // 2. 检查是否有商品配置了该会员等级的会员价
        LambdaQueryWrapper<ProductMemberPrice> productPriceWrapper = new LambdaQueryWrapper<>();
        productPriceWrapper.eq(ProductMemberPrice::getMemberLevelId, id);
        Long productPriceCount = productMemberPriceRepository.selectCount(productPriceWrapper);
        if (productPriceCount != null && productPriceCount > 0) {
            reasons.add(String.format("%d 个商品配置了会员价", productPriceCount));
        }

        // 3. 检查是否有SKU配置了该会员等级的会员价
        LambdaQueryWrapper<ProductSkuMemberPrice> skuPriceWrapper = new LambdaQueryWrapper<>();
        skuPriceWrapper.eq(ProductSkuMemberPrice::getMemberLevelId, id);
        Long skuPriceCount = productSkuMemberPriceRepository.selectCount(skuPriceWrapper);
        if (skuPriceCount != null && skuPriceCount > 0) {
            reasons.add(String.format("%d 个SKU配置了会员价", skuPriceCount));
        }

        // 如果有任何关联，抛出异常
        if (!reasons.isEmpty()) {
            String reasonText = String.join("、", reasons);
            throw new BusinessException(400, 
                String.format("该会员等级无法删除，原因：%s。请先处理相关数据后再删除。", reasonText));
        }

        // 所有检查通过，执行删除
        memberLevelRepository.deleteById(id);
        log.info("删除会员等级成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberLevelStatus(Long id, Integer status) {
        MemberLevel memberLevel = memberLevelRepository.selectById(id);
        if (memberLevel == null) {
            throw new BusinessException(404, "会员等级不存在");
        }

        memberLevel.setStatus(status);
        memberLevelRepository.updateById(memberLevel);
        log.info("更新会员等级状态成功: id={}, status={}", id, status);
    }

    /**
     * 验证积分区间是否重叠
     * 两个区间 [a1, a2) 和 [b1, b2) 重叠的条件是：a1 < b2 && b1 < a2
     */
    private void validatePointsRange(Integer minPoints, Integer maxPoints, Long excludeId) {
        if (maxPoints != null && maxPoints <= minPoints) {
            throw new BusinessException(400, "最高积分必须大于最低积分");
        }

        // 获取所有已存在的等级（排除当前编辑的等级）
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (excludeId != null) {
            wrapper.ne(MemberLevel::getId, excludeId);
        }
        List<MemberLevel> existingLevels = memberLevelRepository.selectList(wrapper);

        // 检查新区间是否与已存在区间重叠
        for (MemberLevel existing : existingLevels) {
            Integer existingMin = existing.getMinPoints();
            Integer existingMax = existing.getMaxPoints();
            
            // 新区间：[minPoints, maxPoints) 或 [minPoints, +∞)
            // 已存在区间：[existingMin, existingMax) 或 [existingMin, +∞)
            // 重叠条件：minPoints < existingMax && existingMin < (maxPoints == null ? +∞ : maxPoints)
            
            boolean overlaps = false;
            
            if (maxPoints == null && existingMax == null) {
                // 两个区间都是无上限，只要起点不同就会重叠（实际上不应该有两个无上限的区间）
                overlaps = true;
            } else if (maxPoints == null) {
                // 新区间无上限，已存在区间有上限
                overlaps = minPoints < existingMax && existingMin < Integer.MAX_VALUE;
            } else if (existingMax == null) {
                // 新区间有上限，已存在区间无上限
                overlaps = minPoints < Integer.MAX_VALUE && existingMin < maxPoints;
            } else {
                // 两个区间都有上限
                overlaps = minPoints < existingMax && existingMin < maxPoints;
            }
            
            if (overlaps) {
                throw new BusinessException(400, String.format("积分区间与已存在的等级\"%s\"重叠", existing.getLevelName()));
            }
        }
    }

    /**
     * 转换为VO对象
     */
    private MemberLevelVO convertToVO(MemberLevel memberLevel) {
        MemberLevelVO vo = new MemberLevelVO();
        BeanUtils.copyProperties(memberLevel, vo);

        // 积分区间显示文本已屏蔽：业务上不需要积分功能
        // if (memberLevel.getMaxPoints() != null) {
        //     vo.setPointsRangeText(memberLevel.getMinPoints() + " - " + memberLevel.getMaxPoints());
        // } else {
        //     vo.setPointsRangeText(memberLevel.getMinPoints() + " 及以上");
        // }
        vo.setPointsRangeText(""); // 设置为空字符串

        // 设置折扣率显示文本
        BigDecimal discountRate = memberLevel.getDiscountRate();
        if (discountRate.compareTo(new BigDecimal("100.00")) == 0) {
            vo.setDiscountRateText("无折扣");
        } else {
            vo.setDiscountRateText(discountRate.intValue() + "折");
        }

        // 设置状态名称
        vo.setStatusName(memberLevel.getStatus() == 1 ? "启用" : "禁用");

        return vo;
    }
}



