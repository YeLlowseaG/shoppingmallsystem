package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.DepositType;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.DepositQueryDTO;
import com.shoppingmall.dto.DepositRechargeDTO;
import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.entity.PreDeposit;
import com.shoppingmall.entity.PreDepositDetail;
import com.shoppingmall.repository.deposit.PreDepositDetailRepository;
import com.shoppingmall.repository.deposit.PreDepositRepository;
import com.shoppingmall.service.buyer.DepositService;
import com.shoppingmall.service.payment.PaymentService;
import com.shoppingmall.vo.DepositBalanceVO;
import com.shoppingmall.vo.DepositRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预存款服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service("buyerDepositServiceImpl")
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final PreDepositRepository preDepositRepository;
    private final PreDepositDetailRepository preDepositDetailRepository;
    private final PaymentService paymentService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponseDTO recharge(Long userId, DepositRechargeDTO rechargeDTO) {
        // 验证充值金额
        if (rechargeDTO.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            throw new BusinessException("充值金额不能小于0.01元");
        }

        // 获取或创建预存款账户
        PreDeposit preDeposit = preDepositRepository.selectOne(
                new LambdaQueryWrapper<PreDeposit>()
                        .eq(PreDeposit::getUserId, userId)
        );

        if (preDeposit == null) {
            // 创建新的预存款账户
            preDeposit = new PreDeposit();
            preDeposit.setUserId(userId);
            preDeposit.setBalance(BigDecimal.ZERO);
            preDeposit.setAvailableBalance(BigDecimal.ZERO);
            preDeposit.setFrozenBalance(BigDecimal.ZERO);
            preDepositRepository.insert(preDeposit);
        }

        // 生成内部充值订单号（用于跟踪，不是外部交易号）
        String internalOrderNo = "DEPOSIT_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 创建充值记录（状态为待审核，等待支付回调）
        PreDepositDetail detail = new PreDepositDetail();
        detail.setUserId(userId);
        detail.setAmount(rechargeDTO.getAmount());
        detail.setDepositAmount(rechargeDTO.getAmount());
        detail.setExpenseAmount(BigDecimal.ZERO);
        detail.setFrozenAmount(BigDecimal.ZERO);
        detail.setUnfrozenAmount(BigDecimal.ZERO);
        detail.setCurrentBalance(preDeposit.getBalance());
        detail.setAvailableBalance(preDeposit.getAvailableBalance());
        detail.setType(DepositType.RECHARGE);
        detail.setStatus(0); // 待审核
        detail.setPaymentMethod(rechargeDTO.getPaymentMethod());
        detail.setEvent("在线充值");
        // 初始备注使用内部订单号，支付回调后会更新为真实外部交易号
        detail.setRemark("预存款充值:外部交易号(" + internalOrderNo + ")");
        // 外部交易号初始为null，等待支付回调更新
        detail.setExternalTradeNo(null);
        // 保存内部订单号，用于精确查找
        detail.setInternalOrderNo(internalOrderNo);

        // 如果是线上充值（微信/支付宝），状态保持为待审核，等待支付回调
        // 不要在这里直接更新余额，应该等待支付回调确认后再更新
        // 如果是线下充值或其他方式，可以根据实际情况处理

        preDepositDetailRepository.insert(detail);

        log.info("用户{}发起预存款充值，金额：{}，支付方式：{}，内部订单号：{}", userId, rechargeDTO.getAmount(), rechargeDTO.getPaymentMethod(), internalOrderNo);

        // 如果是线上充值（微信/支付宝），调用支付服务创建支付订单
        PaymentResponseDTO paymentResponse = null;
        if ("wechat".equals(rechargeDTO.getPaymentMethod()) || "alipay".equals(rechargeDTO.getPaymentMethod())) {
            // 构建支付请求
            PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
            paymentRequest.setInternalOrderNo(internalOrderNo);
            paymentRequest.setAmount(rechargeDTO.getAmount());
            paymentRequest.setPaymentMethod(rechargeDTO.getPaymentMethod());
            paymentRequest.setCurrency(rechargeDTO.getCurrency());
            paymentRequest.setDescription("预存款充值");
            paymentRequest.setUserId(userId);
            paymentRequest.setNotifyUrl("/api/buyer/member/deposit/payment/callback");

            // 调用支付服务创建支付订单
            paymentResponse = paymentService.createPayment(paymentRequest);

            // 如果是模拟支付，直接模拟支付成功（方便测试）
            if (Boolean.TRUE.equals(paymentResponse.getIsMock()) && paymentResponse.getMockExternalTradeNo() != null) {
                // 提取为final变量，供lambda表达式使用
                final String mockExternalTradeNo = paymentResponse.getMockExternalTradeNo();
                
                log.info("模拟支付模式，自动模拟支付成功，内部订单号：{}，模拟外部交易号：{}", 
                        internalOrderNo, mockExternalTradeNo);
                
                // 延迟1秒后模拟支付回调（模拟支付平台的异步回调）
                new Thread(() -> {
                    try {
                        Thread.sleep(1000); // 延迟1秒，模拟支付处理时间
                        handlePaymentCallback(internalOrderNo, mockExternalTradeNo, true);
                        log.info("模拟支付回调处理完成，内部订单号：{}", internalOrderNo);
                    } catch (Exception e) {
                        log.error("模拟支付回调处理失败，内部订单号：{}", internalOrderNo, e);
                    }
                }).start();
            }
        }

        // 返回支付响应信息
        if (paymentResponse == null) {
            // 非线上支付，返回空的支付响应
            paymentResponse = new PaymentResponseDTO();
            paymentResponse.setInternalOrderNo(internalOrderNo);
            paymentResponse.setIsMock(false);
        }

        return paymentResponse;
    }

    @Override
    public DepositBalanceVO getBalanceAndRecords(Long userId, DepositQueryDTO queryDTO) {
        // 获取预存款余额
        PreDeposit preDeposit = preDepositRepository.selectOne(
                new LambdaQueryWrapper<PreDeposit>()
                        .eq(PreDeposit::getUserId, userId)
        );

        BigDecimal depositBalance = BigDecimal.ZERO;
        BigDecimal availableBalance = BigDecimal.ZERO;

        if (preDeposit != null) {
            depositBalance = preDeposit.getBalance();
            availableBalance = preDeposit.getAvailableBalance();
        }

        // 构建查询条件
        LambdaQueryWrapper<PreDepositDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreDepositDetail::getUserId, userId);

        // 操作类型筛选
        if (queryDTO.getOperationType() != null && !queryDTO.getOperationType().isEmpty()) {
            switch (queryDTO.getOperationType()) {
                case "deposit_payment":
                    queryWrapper.eq(PreDepositDetail::getEvent, "预存款支付");
                    break;
                case "online_recharge":
                    queryWrapper.eq(PreDepositDetail::getEvent, "在线充值");
                    break;
                case "deposit_refund":
                    queryWrapper.eq(PreDepositDetail::getEvent, "预存款退款");
                    break;
                case "agent_recharge":
                    queryWrapper.like(PreDepositDetail::getEvent, "代充值");
                    break;
            }
        }

        // 时间范围筛选
        if (queryDTO.getStartDate() != null && !queryDTO.getStartDate().isEmpty()) {
            LocalDate startDate = LocalDate.parse(queryDTO.getStartDate(), DATE_FORMATTER);
            queryWrapper.ge(PreDepositDetail::getCreateTime, startDate.atStartOfDay());
        }
        if (queryDTO.getEndDate() != null && !queryDTO.getEndDate().isEmpty()) {
            LocalDate endDate = LocalDate.parse(queryDTO.getEndDate(), DATE_FORMATTER);
            queryWrapper.le(PreDepositDetail::getCreateTime, endDate.atTime(23, 59, 59));
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(PreDepositDetail::getCreateTime);

        // 分页查询
        Page<PreDepositDetail> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PreDepositDetail> pageResult = preDepositDetailRepository.selectPage(page, queryWrapper);

        // 转换为VO
        List<DepositRecordVO> records = pageResult.getRecords().stream()
                .map(this::convertToRecordVO)
                .collect(Collectors.toList());

        DepositBalanceVO vo = new DepositBalanceVO();
        vo.setDepositBalance(depositBalance);
        vo.setAvailableBalance(availableBalance);
        vo.setRecords(records);
        vo.setTotal(pageResult.getTotal());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentCallback(String internalOrderNo, String externalTradeNo, boolean success) {
        // 根据内部订单号精确查找充值记录
        PreDepositDetail detail = preDepositDetailRepository.selectOne(
                new LambdaQueryWrapper<PreDepositDetail>()
                        .eq(PreDepositDetail::getInternalOrderNo, internalOrderNo)
                        .eq(PreDepositDetail::getType, DepositType.RECHARGE)
                        .orderByDesc(PreDepositDetail::getCreateTime)
                        .last("LIMIT 1")
        );

        if (detail == null) {
            log.warn("未找到对应的充值记录，内部订单号：{}", internalOrderNo);
            throw new BusinessException("未找到对应的充值记录，内部订单号：" + internalOrderNo);
        }

        // 防止重复回调处理
        if (detail.getStatus() == 1) {
            // 已通过状态，检查外部交易号是否一致
            if (externalTradeNo != null && externalTradeNo.equals(detail.getExternalTradeNo())) {
                log.info("充值记录已处理，忽略重复回调，内部订单号：{}，外部交易号：{}", internalOrderNo, externalTradeNo);
                return; // 重复回调，直接返回
            } else {
                log.warn("充值记录已处理，但外部交易号不一致，内部订单号：{}，已有外部交易号：{}，新外部交易号：{}", 
                        internalOrderNo, detail.getExternalTradeNo(), externalTradeNo);
                throw new BusinessException("充值记录已处理，但外部交易号不一致");
            }
        }

        if (detail.getStatus() == 2) {
            log.warn("充值记录已拒绝，无法处理回调，内部订单号：{}", internalOrderNo);
            throw new BusinessException("充值记录已拒绝，无法处理回调");
        }

        // 更新外部交易号和备注
        detail.setExternalTradeNo(externalTradeNo);
        detail.setRemark("预存款充值:外部交易号(" + externalTradeNo + ")");

        if (success) {
            // 支付成功，更新状态和余额
            detail.setStatus(1); // 已通过
            detail.setAuditTime(LocalDateTime.now());

            // 获取预存款账户
            PreDeposit preDeposit = preDepositRepository.selectOne(
                    new LambdaQueryWrapper<PreDeposit>()
                            .eq(PreDeposit::getUserId, detail.getUserId())
            );

            if (preDeposit != null) {
                // 更新预存款余额
                BigDecimal newBalance = preDeposit.getBalance().add(detail.getDepositAmount());
                BigDecimal newAvailableBalance = preDeposit.getAvailableBalance().add(detail.getDepositAmount());

                preDeposit.setBalance(newBalance);
                preDeposit.setAvailableBalance(newAvailableBalance);
                preDepositRepository.updateById(preDeposit);

                // 更新明细中的余额
                detail.setCurrentBalance(newBalance);
                detail.setAvailableBalance(newAvailableBalance);
            }
        } else {
            // 支付失败
            detail.setStatus(2); // 已拒绝
        }

        preDepositDetailRepository.updateById(detail);

        log.info("支付回调处理完成，内部订单号：{}，外部交易号：{}，支付结果：{}", internalOrderNo, externalTradeNo, success ? "成功" : "失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void depositPayment(Long userId, Long orderId, String orderNo, BigDecimal amount) {
        // 获取预存款账户
        PreDeposit preDeposit = preDepositRepository.selectOne(
                new LambdaQueryWrapper<PreDeposit>()
                        .eq(PreDeposit::getUserId, userId)
        );

        if (preDeposit == null) {
            throw new BusinessException("预存款账户不存在，请先充值");
        }

        // 处理余额为null的情况
        BigDecimal currentBalance = preDeposit.getBalance() != null ? preDeposit.getBalance() : BigDecimal.ZERO;
        BigDecimal currentAvailableBalance = preDeposit.getAvailableBalance() != null ? preDeposit.getAvailableBalance() : BigDecimal.ZERO;

        // 检查余额是否充足
        if (currentAvailableBalance.compareTo(amount) < 0) {
            throw new BusinessException("预存款余额不足，当前余额：" + currentAvailableBalance + "，需要支付：" + amount);
        }

        // 更新预存款余额
        BigDecimal newBalance = currentBalance.subtract(amount);
        BigDecimal newAvailableBalance = currentAvailableBalance.subtract(amount);

        preDeposit.setBalance(newBalance);
        preDeposit.setAvailableBalance(newAvailableBalance);
        preDepositRepository.updateById(preDeposit);

        // 创建消费记录
        PreDepositDetail detail = new PreDepositDetail();
        detail.setUserId(userId);
        detail.setAmount(amount);
        detail.setDepositAmount(BigDecimal.ZERO);
        detail.setExpenseAmount(amount);
        detail.setFrozenAmount(BigDecimal.ZERO);
        detail.setUnfrozenAmount(BigDecimal.ZERO);
        detail.setCurrentBalance(newBalance);
        detail.setAvailableBalance(newAvailableBalance);
        detail.setType(DepositType.CONSUME);
        detail.setStatus(1); // 已通过
        detail.setEvent("预存款支付");
        detail.setOrderId(orderId);
        detail.setOrderNo(orderNo);
        detail.setRemark("预存款支付:订单号{" + orderNo + "}");

        preDepositDetailRepository.insert(detail);

        log.info("用户{}使用预存款支付订单，订单号：{}，金额：{}，扣减后余额：{}", userId, orderNo, amount, newAvailableBalance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void depositRefund(Long userId, Long orderId, String orderNo, BigDecimal amount) {
        // 获取预存款账户
        PreDeposit preDeposit = preDepositRepository.selectOne(
                new LambdaQueryWrapper<PreDeposit>()
                        .eq(PreDeposit::getUserId, userId)
        );

        if (preDeposit == null) {
            // 创建新的预存款账户
            preDeposit = new PreDeposit();
            preDeposit.setUserId(userId);
            preDeposit.setBalance(BigDecimal.ZERO);
            preDeposit.setAvailableBalance(BigDecimal.ZERO);
            preDeposit.setFrozenBalance(BigDecimal.ZERO);
            preDepositRepository.insert(preDeposit);
        }

        // 更新预存款余额
        BigDecimal newBalance = preDeposit.getBalance().add(amount);
        BigDecimal newAvailableBalance = preDeposit.getAvailableBalance().add(amount);

        preDeposit.setBalance(newBalance);
        preDeposit.setAvailableBalance(newAvailableBalance);
        preDepositRepository.updateById(preDeposit);

        // 创建退款记录
        PreDepositDetail detail = new PreDepositDetail();
        detail.setUserId(userId);
        detail.setAmount(amount);
        detail.setDepositAmount(amount);
        detail.setExpenseAmount(BigDecimal.ZERO);
        detail.setFrozenAmount(BigDecimal.ZERO);
        detail.setUnfrozenAmount(BigDecimal.ZERO);
        detail.setCurrentBalance(newBalance);
        detail.setAvailableBalance(newAvailableBalance);
        detail.setType(DepositType.REFUND);
        detail.setStatus(1); // 已通过
        detail.setEvent("预存款退款");
        detail.setOrderId(orderId);
        detail.setOrderNo(orderNo);
        detail.setRemark("预存款退款:订单号{" + orderNo + "}");

        preDepositDetailRepository.insert(detail);

        log.info("用户{}预存款退款，订单号：{}，金额：{}", userId, orderNo, amount);
    }

    /**
     * 转换为记录VO
     */
    private DepositRecordVO convertToRecordVO(PreDepositDetail detail) {
        DepositRecordVO vo = new DepositRecordVO();
        vo.setId(detail.getId());
        vo.setEvent(detail.getEvent());
        vo.setDepositAmount(detail.getDepositAmount() != null ? detail.getDepositAmount() : BigDecimal.ZERO);
        vo.setExpenseAmount(detail.getExpenseAmount() != null ? detail.getExpenseAmount() : BigDecimal.ZERO);
        vo.setFrozenAmount(detail.getFrozenAmount() != null ? detail.getFrozenAmount() : BigDecimal.ZERO);
        vo.setUnfrozenAmount(detail.getUnfrozenAmount() != null ? detail.getUnfrozenAmount() : BigDecimal.ZERO);
        vo.setCurrentBalance(detail.getCurrentBalance() != null ? detail.getCurrentBalance() : BigDecimal.ZERO);
        vo.setAvailableBalance(detail.getAvailableBalance() != null ? detail.getAvailableBalance() : BigDecimal.ZERO);
        vo.setCreateTime(detail.getCreateTime());
        vo.setRemark(detail.getRemark());
        vo.setOrderNo(detail.getOrderNo());
        return vo;
    }
}

