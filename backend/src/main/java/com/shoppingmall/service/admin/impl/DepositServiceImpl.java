package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.DepositType;
import com.shoppingmall.dto.AdminDepositQueryDTO;
import com.shoppingmall.entity.PreDepositDetail;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.deposit.PreDepositDetailRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.admin.DepositService;
import com.shoppingmall.vo.AdminDepositRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台预存款服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service("adminDepositServiceImpl")
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final PreDepositDetailRepository preDepositDetailRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public IPage<AdminDepositRecordVO> getDepositRecordList(AdminDepositQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<PreDepositDetail> wrapper = new LambdaQueryWrapper<>();

        // 如果指定了用户名搜索，先查询用户ID列表
        if (StringUtils.hasText(queryDTO.getUsername())) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getUsername, queryDTO.getUsername());
            List<User> users = userRepository.selectList(userWrapper);
            if (users.isEmpty()) {
                // 没有匹配的用户，返回空结果
                Page<AdminDepositRecordVO> emptyPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
                emptyPage.setRecords(new ArrayList<>());
                emptyPage.setTotal(0);
                emptyPage.setPages(0);
                return emptyPage;
            }
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(PreDepositDetail::getUserId, userIds);
        } else if (queryDTO.getUserId() != null) {
            // 用户ID
            wrapper.eq(PreDepositDetail::getUserId, queryDTO.getUserId());
        }

        // 事件类型
        if (StringUtils.hasText(queryDTO.getEvent())) {
            wrapper.eq(PreDepositDetail::getEvent, queryDTO.getEvent());
        }

        // 类型
        if (queryDTO.getType() != null) {
            wrapper.eq(PreDepositDetail::getType, queryDTO.getType());
        }

        // 状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(PreDepositDetail::getStatus, queryDTO.getStatus());
        }

        // 订单号
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.eq(PreDepositDetail::getOrderNo, queryDTO.getOrderNo());
        }

        // 外部交易号
        if (StringUtils.hasText(queryDTO.getExternalTradeNo())) {
            wrapper.eq(PreDepositDetail::getExternalTradeNo, queryDTO.getExternalTradeNo());
        }

        // 内部订单号
        if (StringUtils.hasText(queryDTO.getInternalOrderNo())) {
            wrapper.eq(PreDepositDetail::getInternalOrderNo, queryDTO.getInternalOrderNo());
        }

        // 日期范围
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            LocalDate startDate = LocalDate.parse(queryDTO.getStartDate(), DATE_FORMATTER);
            wrapper.ge(PreDepositDetail::getCreateTime, startDate.atStartOfDay());
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            LocalDate endDate = LocalDate.parse(queryDTO.getEndDate(), DATE_FORMATTER);
            wrapper.le(PreDepositDetail::getCreateTime, endDate.atTime(23, 59, 59));
        }

        // 排序
        wrapper.orderByDesc(PreDepositDetail::getCreateTime);

        // 分页查询
        Page<PreDepositDetail> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PreDepositDetail> detailPage = preDepositDetailRepository.selectPage(page, wrapper);

        // 获取所有用户ID
        List<Long> userIds = detailPage.getRecords().stream()
                .map(PreDepositDetail::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 查询用户信息
        Map<Long, User> userMap = userRepository.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        List<AdminDepositRecordVO> recordVOList = new ArrayList<>();
        for (PreDepositDetail detail : detailPage.getRecords()) {
            AdminDepositRecordVO vo = new AdminDepositRecordVO();
            BeanUtils.copyProperties(detail, vo);

            // 设置用户名
            User user = userMap.get(detail.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }

            // 设置类型名称
            if (detail.getType() != null) {
                if (DepositType.RECHARGE.equals(detail.getType())) {
                    vo.setTypeName("充值");
                } else if (DepositType.CONSUME.equals(detail.getType())) {
                    vo.setTypeName("消费");
                } else if (DepositType.REFUND.equals(detail.getType())) {
                    vo.setTypeName("退款");
                }
            }

            // 设置状态名称
            if (detail.getStatus() != null) {
                switch (detail.getStatus()) {
                    case 0:
                        vo.setStatusName("待审核");
                        break;
                    case 1:
                        vo.setStatusName("已通过");
                        break;
                    case 2:
                        vo.setStatusName("已拒绝");
                        break;
                    case 3:
                        vo.setStatusName("支付中");
                        break;
                    case 4:
                        vo.setStatusName("已超时");
                        break;
                    default:
                        vo.setStatusName("未知");
                }
            }

            recordVOList.add(vo);
        }

        // 构建分页结果
        Page<AdminDepositRecordVO> resultPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        resultPage.setRecords(recordVOList);
        resultPage.setTotal(detailPage.getTotal());
        resultPage.setPages(detailPage.getPages());

        return resultPage;
    }

    @Override
    public AdminDepositRecordVO getDepositRecordById(Long id) {
        PreDepositDetail detail = preDepositDetailRepository.selectById(id);
        if (detail == null) {
            return null;
        }

        AdminDepositRecordVO vo = new AdminDepositRecordVO();
        BeanUtils.copyProperties(detail, vo);

        // 查询用户信息
        User user = userRepository.selectById(detail.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }

        // 设置类型名称
        if (detail.getType() != null) {
            if (DepositType.RECHARGE.equals(detail.getType())) {
                vo.setTypeName("充值");
            } else if (DepositType.CONSUME.equals(detail.getType())) {
                vo.setTypeName("消费");
            } else if (DepositType.REFUND.equals(detail.getType())) {
                vo.setTypeName("退款");
            }
        }

        // 设置状态名称
        if (detail.getStatus() != null) {
            switch (detail.getStatus()) {
                case 0:
                    vo.setStatusName("待审核");
                    break;
                case 1:
                    vo.setStatusName("已通过");
                    break;
                case 2:
                    vo.setStatusName("已拒绝");
                    break;
                case 3:
                    vo.setStatusName("支付中");
                    break;
                case 4:
                    vo.setStatusName("已超时");
                    break;
                default:
                    vo.setStatusName("未知");
            }
        }

        return vo;
    }
}

