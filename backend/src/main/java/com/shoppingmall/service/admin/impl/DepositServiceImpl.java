package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.DepositType;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.AdminDepositQueryDTO;
import com.shoppingmall.dto.RefundRequestDTO;
import com.shoppingmall.entity.PreDeposit;
import com.shoppingmall.entity.PreDepositDetail;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.deposit.PreDepositDetailRepository;
import com.shoppingmall.repository.deposit.PreDepositRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.payment.exception.PaymentException;
import com.shoppingmall.payment.service.PaymentGatewayService;
import com.shoppingmall.service.admin.DepositService;
import com.shoppingmall.vo.AdminDepositRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
    private final PreDepositRepository preDepositRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;

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
                        // 根据支付方式判断：线上支付显示"支付失败"，线下充值显示"已拒绝"
                        String paymentMethod = detail.getPaymentMethod();
                        if ("alipay".equalsIgnoreCase(paymentMethod) || "wechat".equalsIgnoreCase(paymentMethod)) {
                            vo.setStatusName("支付失败");
                        } else {
                            vo.setStatusName("已拒绝");
                        }
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
                    // 根据支付方式判断：线上支付显示"支付失败"，线下充值显示"已拒绝"
                    String paymentMethod = detail.getPaymentMethod();
                    if ("alipay".equalsIgnoreCase(paymentMethod) || "wechat".equalsIgnoreCase(paymentMethod)) {
                        vo.setStatusName("支付失败");
                    } else {
                        vo.setStatusName("已拒绝");
                    }
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundDepositRecharge(RefundRequestDTO refundDTO) {
        // 1. 验证充值记录
        PreDepositDetail detail = preDepositDetailRepository.selectById(refundDTO.getDepositDetailId());
        if (detail == null) {
            throw new BusinessException(404, "充值记录不存在");
        }

        // 2. 验证是否为充值记录
        if (!DepositType.RECHARGE.equals(detail.getType())) {
            throw new BusinessException(400, "该记录不是充值记录");
        }

        // 3. 验证状态
        if (detail.getStatus() != 1) {
            throw new BusinessException(400, "只有已通过的充值记录才能退款");
        }

        // 4. 验证退款金额
        BigDecimal depositAmount = detail.getDepositAmount() != null 
                ? detail.getDepositAmount() 
                : detail.getAmount();
        
        if (refundDTO.getRefundAmount().compareTo(depositAmount) > 0) {
            throw new BusinessException(400, "退款金额不能超过充值金额：" + depositAmount);
        }

        if (refundDTO.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "退款金额必须大于0");
        }

        // 5. 检查预存款余额（如果已消费部分，需要确保余额足够）
        PreDeposit preDeposit = preDepositRepository.selectOne(
                new LambdaQueryWrapper<PreDeposit>()
                        .eq(PreDeposit::getUserId, detail.getUserId())
        );

        if (preDeposit == null) {
            throw new BusinessException(404, "预存款账户不存在");
        }

        // 6. 从预存款账户扣除退款金额（因为已经充到账户了，需要先扣除）
        BigDecimal newBalance = preDeposit.getBalance().subtract(refundDTO.getRefundAmount());
        BigDecimal newAvailableBalance = preDeposit.getAvailableBalance().subtract(refundDTO.getRefundAmount());

        if (newAvailableBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "预存款余额不足，无法退款");
        }

        preDeposit.setBalance(newBalance);
        preDeposit.setAvailableBalance(newAvailableBalance);
        preDepositRepository.updateById(preDeposit);

        // 7. 根据充值时的支付方式，原路退回
        String paymentMethod = detail.getPaymentMethod();
        String internalOrderNo = detail.getInternalOrderNo(); // 内部订单号（创建支付订单时使用的订单号）
        String externalTradeNo = detail.getExternalTradeNo(); // 外部交易号（支付宝返回的交易号）
        String refundExternalTradeNo = null;

        if (paymentMethod != null) {
            String paymentMethodUpper = paymentMethod.toUpperCase();
            
            if (PaymentMethod.WECHAT.equals(paymentMethodUpper) || "WECHAT".equals(paymentMethodUpper)) {
                // 微信退款（调用真实退款接口）
                // 重要：微信退款API需要的是商户订单号（out_trade_no），即创建支付订单时的订单号
                if (internalOrderNo == null || internalOrderNo.isEmpty()) {
                    throw new BusinessException(400, "内部订单号不存在，无法退款");
                }
                
                try {
                    log.info("开始调用微信退款接口，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，退款原因：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), refundDTO.getRefundReason());
                    
                    refundExternalTradeNo = paymentGatewayService.refund(
                            PaymentMethod.WECHAT,
                            internalOrderNo, // 使用内部订单号（创建支付订单时的订单号）
                            refundDTO.getRefundAmount(),
                            refundDTO.getRefundReason() != null ? refundDTO.getRefundReason() : "预存款充值退款"
                    );
                    
                    log.info("微信充值退款成功：充值记录ID={}, internalOrderNo={}, externalTradeNo={}, refundAmount={}, refundExternalTradeNo={}", 
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), refundExternalTradeNo);
                } catch (PaymentException e) {
                    log.error("微信充值退款失败，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，错误信息：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), e.getMessage(), e);
                    // 如果退款失败，需要回滚预存款余额
                    BigDecimal rollbackBalance = preDeposit.getBalance().add(refundDTO.getRefundAmount());
                    BigDecimal rollbackAvailableBalance = preDeposit.getAvailableBalance().add(refundDTO.getRefundAmount());
                    preDeposit.setBalance(rollbackBalance);
                    preDeposit.setAvailableBalance(rollbackAvailableBalance);
                    preDepositRepository.updateById(preDeposit);
                    throw new BusinessException(500, "微信退款失败：" + e.getMessage() + "，已回滚预存款余额");
                } catch (Exception e) {
                    log.error("微信充值退款异常，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), e);
                    // 如果退款异常，需要回滚预存款余额
                    BigDecimal rollbackBalance = preDeposit.getBalance().add(refundDTO.getRefundAmount());
                    BigDecimal rollbackAvailableBalance = preDeposit.getAvailableBalance().add(refundDTO.getRefundAmount());
                    preDeposit.setBalance(rollbackBalance);
                    preDeposit.setAvailableBalance(rollbackAvailableBalance);
                    preDepositRepository.updateById(preDeposit);
                    throw new BusinessException(500, "微信退款异常：" + e.getMessage() + "，已回滚预存款余额");
                }
            } else if (PaymentMethod.ALIPAY.equals(paymentMethodUpper) || "ALIPAY".equals(paymentMethodUpper)) {
                // 支付宝退款（调用真实退款接口）
                // 重要：支付宝退款API需要的是商户订单号（out_trade_no），即创建支付订单时的订单号
                if (internalOrderNo == null || internalOrderNo.isEmpty()) {
                    throw new BusinessException(400, "内部订单号不存在，无法退款");
                }
                
                try {
                    log.info("开始调用支付宝退款接口，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，退款原因：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), refundDTO.getRefundReason());
                    
                    refundExternalTradeNo = paymentGatewayService.refund(
                            PaymentMethod.ALIPAY,
                            internalOrderNo, // 使用内部订单号（创建支付订单时的订单号），而不是外部交易号
                            refundDTO.getRefundAmount(),
                            refundDTO.getRefundReason() != null ? refundDTO.getRefundReason() : "预存款充值退款"
                    );
                    
                    log.info("支付宝充值退款成功：充值记录ID={}, internalOrderNo={}, externalTradeNo={}, refundAmount={}, refundExternalTradeNo={}", 
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), refundExternalTradeNo);
                } catch (PaymentException e) {
                    log.error("支付宝充值退款失败，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，错误信息：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), e.getMessage(), e);
                    // 如果退款失败，需要回滚预存款余额
                    BigDecimal rollbackBalance = preDeposit.getBalance().add(refundDTO.getRefundAmount());
                    BigDecimal rollbackAvailableBalance = preDeposit.getAvailableBalance().add(refundDTO.getRefundAmount());
                    preDeposit.setBalance(rollbackBalance);
                    preDeposit.setAvailableBalance(rollbackAvailableBalance);
                    preDepositRepository.updateById(preDeposit);
                    throw new BusinessException(500, "支付宝退款失败：" + e.getMessage() + "，已回滚预存款余额");
                } catch (Exception e) {
                    log.error("支付宝充值退款异常，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), e);
                    // 如果退款异常，需要回滚预存款余额
                    BigDecimal rollbackBalance = preDeposit.getBalance().add(refundDTO.getRefundAmount());
                    BigDecimal rollbackAvailableBalance = preDeposit.getAvailableBalance().add(refundDTO.getRefundAmount());
                    preDeposit.setBalance(rollbackBalance);
                    preDeposit.setAvailableBalance(rollbackAvailableBalance);
                    preDepositRepository.updateById(preDeposit);
                    throw new BusinessException(500, "支付宝退款异常：" + e.getMessage() + "，已回滚预存款余额");
                }
            } else if (PaymentMethod.PRE_DEPOSIT.equals(paymentMethodUpper) || "PRE_DEPOSIT".equals(paymentMethodUpper)) {
                // 预存款充值退款：直接退回预存款账户（但这里已经扣除了，所以不需要额外操作）
                // 预存款充值退款实际上就是扣除预存款余额，不需要调用第三方接口
                refundExternalTradeNo = "DEPOSIT_REFUND_" + System.currentTimeMillis();
                log.info("预存款充值退款：直接从预存款账户扣除，充值记录ID={}, refundAmount={}, refundExternalTradeNo={}", 
                        detail.getId(), refundDTO.getRefundAmount(), refundExternalTradeNo);
            } else {
                // 其他支付方式，暂时不支持退款
                // 如果退款失败，需要回滚预存款余额
                BigDecimal rollbackBalance = preDeposit.getBalance().add(refundDTO.getRefundAmount());
                BigDecimal rollbackAvailableBalance = preDeposit.getAvailableBalance().add(refundDTO.getRefundAmount());
                preDeposit.setBalance(rollbackBalance);
                preDeposit.setAvailableBalance(rollbackAvailableBalance);
                preDepositRepository.updateById(preDeposit);
                throw new BusinessException(400, "不支持的支付方式退款：" + paymentMethod + "，已回滚预存款余额");
            }
        } else {
            // 如果没有支付方式信息，默认按预存款处理
            refundExternalTradeNo = "DEPOSIT_REFUND_" + System.currentTimeMillis();
            log.warn("充值记录没有支付方式信息，按预存款处理退款：depositDetailId={}, refundExternalTradeNo={}", 
                    detail.getId(), refundExternalTradeNo);
        }

        // 8. 创建退款记录
        PreDepositDetail refundDetail = new PreDepositDetail();
        refundDetail.setUserId(detail.getUserId());
        refundDetail.setAmount(refundDTO.getRefundAmount());
        refundDetail.setDepositAmount(BigDecimal.ZERO);
        refundDetail.setExpenseAmount(refundDTO.getRefundAmount());
        refundDetail.setFrozenAmount(BigDecimal.ZERO);
        refundDetail.setUnfrozenAmount(BigDecimal.ZERO);
        refundDetail.setCurrentBalance(newBalance);
        refundDetail.setAvailableBalance(newAvailableBalance);
        refundDetail.setType(DepositType.REFUND);
        refundDetail.setStatus(1); // 已通过
        refundDetail.setEvent("充值退款");
        refundDetail.setPaymentMethod(paymentMethod); // 记录原支付方式
        refundDetail.setExternalTradeNo(refundExternalTradeNo); // 记录退款外部交易号
        refundDetail.setRemark("充值退款，原充值记录ID：" + detail.getId() + 
                "，原支付方式：" + (paymentMethod != null ? paymentMethod : "未知") + 
                "，退款原因：" + refundDTO.getRefundReason());
        preDepositDetailRepository.insert(refundDetail);

        log.info("预存款充值退款成功：depositDetailId={}, refundAmount={}, userId={}, paymentMethod={}, refundExternalTradeNo={}", 
                refundDTO.getDepositDetailId(), refundDTO.getRefundAmount(), detail.getUserId(), 
                paymentMethod, refundExternalTradeNo);
    }
}

