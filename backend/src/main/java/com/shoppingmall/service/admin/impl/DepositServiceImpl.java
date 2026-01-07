package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.constant.DepositStatus;
import com.shoppingmall.common.constant.DepositType;
import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.AdminDepositQueryDTO;
import com.shoppingmall.dto.RefundRequestDTO;
import com.shoppingmall.entity.PreDeposit;
import com.shoppingmall.entity.PreDepositDetail;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.deposit.PreDepositDetailRepository;
import com.shoppingmall.repository.deposit.PreDepositRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.entity.PaymentApiLog;
import com.shoppingmall.mapper.PaymentApiLogMapper;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.service.PaymentGatewayService;
import com.shoppingmall.payment.strategy.PaymentStrategy;
import com.shoppingmall.payment.util.AlipayUtil;
import com.shoppingmall.service.admin.DepositService;
import com.shoppingmall.service.payment.PaymentLogService;
import com.shoppingmall.vo.AdminDepositRecordVO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    private final PaymentConfigService paymentConfigService;
    private final PaymentLogService paymentLogService;
    private final PaymentApiLogMapper paymentApiLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 支付状态查询频率限制缓存（30秒过期）
     */
    private final Cache<String, Long> paymentQueryCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.SECONDS) // 30秒过期
            .build();

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

            // 如果是充值记录，计算已退款金额和可退款金额
            if (DepositType.RECHARGE.equals(detail.getType()) && detail.getStatus() == 1) {
                BigDecimal refundedAmount = calculateRefundedAmount(detail.getId());
                vo.setRefundedAmount(refundedAmount);
                
                BigDecimal depositAmount = detail.getDepositAmount() != null 
                        ? detail.getDepositAmount() 
                        : detail.getAmount();
                BigDecimal refundableAmount = depositAmount.subtract(refundedAmount);
                if (refundableAmount.compareTo(BigDecimal.ZERO) < 0) {
                    refundableAmount = BigDecimal.ZERO;
                }
                vo.setRefundableAmount(refundableAmount);
            } else {
                vo.setRefundedAmount(BigDecimal.ZERO);
                vo.setRefundableAmount(BigDecimal.ZERO);
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

        // 如果是充值记录，计算已退款金额和可退款金额
        if (DepositType.RECHARGE.equals(detail.getType()) && detail.getStatus() == 1) {
            BigDecimal refundedAmount = calculateRefundedAmount(detail.getId());
            vo.setRefundedAmount(refundedAmount);
            
            BigDecimal depositAmount = detail.getDepositAmount() != null 
                    ? detail.getDepositAmount() 
                    : detail.getAmount();
            BigDecimal refundableAmount = depositAmount.subtract(refundedAmount);
            if (refundableAmount.compareTo(BigDecimal.ZERO) < 0) {
                refundableAmount = BigDecimal.ZERO;
            }
            vo.setRefundableAmount(refundableAmount);
        } else {
            vo.setRefundedAmount(BigDecimal.ZERO);
            vo.setRefundableAmount(BigDecimal.ZERO);
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
                // 支付宝预存款退款（直接调用AlipayUtil，不经过PaymentGatewayService）
                // 预存款退款和订单退款使用不同的逻辑路径，避免混合
                if (internalOrderNo == null || internalOrderNo.isEmpty()) {
                    throw new BusinessException(400, "内部订单号不存在，无法退款");
                }
                
                try {
                    log.info("开始调用支付宝预存款退款接口，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，退款原因：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), refundDTO.getRefundReason());
                    
                    // 获取支付宝配置
                    AlipayConfig alipayConfig = paymentConfigService.getAlipayConfig();
                    if (alipayConfig == null || !Boolean.TRUE.equals(alipayConfig.getEnabled())) {
                        throw new BusinessException(400, "支付宝未启用");
                    }
                    
                    AlipayConfig.AlipayEnvConfig envConfig = "production".equals(alipayConfig.getEnv())
                            ? alipayConfig.getProduction()
                            : alipayConfig.getSandbox();
                    
                    if (envConfig == null) {
                        throw new BusinessException(400, "支付宝环境配置不存在");
                    }
                    
                    String refundNo = "ALI_REFUND_" + System.currentTimeMillis();
                    String refundAmountStr = String.format("%.2f", refundDTO.getRefundAmount().doubleValue());
                    
                    // 先尝试使用商户订单号（out_trade_no）进行退款
                    try {
                        log.info("尝试使用商户订单号进行预存款退款，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，退款单号：{}",
                                detail.getId(), internalOrderNo, externalTradeNo, refundAmountStr, refundNo);
                        
                        long startTime = System.currentTimeMillis();
                        Map<String, String> result = AlipayUtil.refund(
                                envConfig,
                                internalOrderNo, // 使用内部订单号（out_trade_no）
                                refundNo,
                                refundAmountStr,
                                false // 使用out_trade_no
                        );
                        long executionTime = System.currentTimeMillis() - startTime;
                        
                        String code = result.get("code");
                        String subCode = result.get("sub_code");
                        String subMsg = result.get("sub_msg");
                        String msg = result.get("msg");
                        
                        log.info("支付宝预存款退款响应，充值记录ID：{}，内部订单号：{}，响应码：{}，子错误码：{}，错误信息：{}",
                                detail.getId(), internalOrderNo, code, subCode, subMsg != null ? subMsg : msg);
                        
                        // 记录退款日志
                        try {
                            PaymentApiLog apiLog = new PaymentApiLog();
                            apiLog.setPaymentMethod("ALIPAY");
                            apiLog.setApiType("REFUND");
                            apiLog.setBusinessType("DEPOSIT"); // 预存款退款
                            apiLog.setOrderNo(internalOrderNo);
                            apiLog.setPaymentNo(internalOrderNo);
                            apiLog.setExternalTradeNo(externalTradeNo);
                            String apiUrl = envConfig.getGateway() != null ? envConfig.getGateway() : 
                                ("sandbox".equals(alipayConfig.getEnv()) ? "https://openapi.alipaydev.com/gateway.do" : "https://openapi.alipay.com/gateway.do");
                            apiLog.setApiUrl(apiUrl);
                            apiLog.setRequestMethod("POST");
                            apiLog.setExecutionTime((int) executionTime);
                            
                            if ("10000".equals(code)) {
                                apiLog.setApiStatus(1); // 成功
                            } else {
                                apiLog.setApiStatus(0); // 失败
                                apiLog.setErrorCode(code);
                                apiLog.setErrorMessage(subMsg != null && !subMsg.isEmpty() ? subMsg : (msg != null ? msg : "退款失败"));
                            }
                            
                            try {
                                Map<String, Object> requestData = new HashMap<>();
                                requestData.put("paymentNo", internalOrderNo);
                                requestData.put("refundNo", refundNo);
                                requestData.put("refundAmount", refundAmountStr);
                                requestData.put("refundReason", refundDTO.getRefundReason());
                                requestData.put("useTradeNo", false);
                                apiLog.setRequestData(objectMapper.writeValueAsString(requestData));
                                apiLog.setResponseData(objectMapper.writeValueAsString(result));
                            } catch (Exception ex) {
                                log.warn("序列化退款数据失败", ex);
                            }
                            
                            paymentLogService.savePaymentLog(apiLog);
                        } catch (Exception ex) {
                            log.warn("记录退款接口日志失败", ex);
                        }
                        
                        if ("10000".equals(code)) {
                            refundExternalTradeNo = refundNo;
                            log.info("支付宝预存款退款成功（使用商户订单号）：充值记录ID={}, internalOrderNo={}, externalTradeNo={}, refundAmount={}, refundExternalTradeNo={}", 
                                    detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), refundExternalTradeNo);
                        } else {
                            String errorMsg = subMsg != null && !subMsg.isEmpty() ? subMsg : (msg != null ? msg : "退款失败");
                            
                            // 如果是系统错误，尝试使用trade_no
                            if ("20000".equals(code) && "aop.ACQ.SYSTEM_ERROR".equals(subCode)) {
                                if (externalTradeNo != null && !externalTradeNo.isEmpty()) {
                                    log.warn("使用商户订单号退款失败（系统错误），尝试使用支付宝交易号进行退款，充值记录ID：{}，内部订单号：{}，外部交易号：{}，错误信息：{}",
                                            detail.getId(), internalOrderNo, externalTradeNo, errorMsg);
                                    throw new PaymentException(500, errorMsg); // 抛出异常，触发重试逻辑
                                } else {
                                    log.error("使用商户订单号退款失败，但没有外部交易号，无法重试，充值记录ID：{}，内部订单号：{}，错误信息：{}",
                                            detail.getId(), internalOrderNo, errorMsg);
                                    throw new PaymentException(500, errorMsg);
                                }
                            } else {
                                log.error("使用商户订单号退款失败（非系统错误），充值记录ID：{}，内部订单号：{}，错误码：{}，子错误码：{}，错误信息：{}",
                                        detail.getId(), internalOrderNo, code, subCode, errorMsg);
                                throw new PaymentException(500, errorMsg);
                            }
                        }
                    } catch (PaymentException e) {
                        // 如果使用商户订单号退款失败，且错误码是系统错误，尝试使用支付宝交易号（trade_no）
                        String errorMessage = e.getMessage();
                        if (errorMessage != null && (errorMessage.contains("系统异常") || errorMessage.contains("SYSTEM_ERROR") || errorMessage.contains("订单不存在"))) {
                            if (externalTradeNo != null && !externalTradeNo.isEmpty()) {
                                log.warn("使用商户订单号退款失败，尝试使用支付宝交易号进行预存款退款，充值记录ID：{}，内部订单号：{}，外部交易号：{}，错误信息：{}",
                                        detail.getId(), internalOrderNo, externalTradeNo, errorMessage);
                                
                                // 使用支付宝交易号（trade_no）进行退款
                                try {
                                    String retryRefundNo = "ALI_REFUND_" + System.currentTimeMillis();
                                    
                                    log.info("准备使用支付宝交易号进行预存款退款，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，退款单号：{}",
                                            detail.getId(), internalOrderNo, externalTradeNo, refundAmountStr, retryRefundNo);
                                    
                                    long retryStartTime = System.currentTimeMillis();
                                    Map<String, String> result = AlipayUtil.refund(
                                            envConfig,
                                            externalTradeNo, // 使用外部交易号（trade_no）
                                            retryRefundNo,
                                            refundAmountStr,
                                            true // 使用trade_no
                                    );
                                    long retryExecutionTime = System.currentTimeMillis() - retryStartTime;
                                    
                                    String code = result.get("code");
                                    String subCode = result.get("sub_code");
                                    String subMsg = result.get("sub_msg");
                                    String msg = result.get("msg");
                                    
                                    log.info("支付宝预存款退款响应（使用trade_no），充值记录ID：{}，外部交易号：{}，响应码：{}，子错误码：{}，错误信息：{}",
                                            detail.getId(), externalTradeNo, code, subCode, subMsg != null ? subMsg : msg);
                                    
                                    // 记录退款日志（使用trade_no重试）
                                    try {
                                        PaymentApiLog apiLog = new PaymentApiLog();
                                        apiLog.setPaymentMethod("ALIPAY");
                                        apiLog.setApiType("REFUND");
                                        apiLog.setBusinessType("DEPOSIT"); // 预存款退款
                                        apiLog.setOrderNo(internalOrderNo);
                                        apiLog.setPaymentNo(internalOrderNo);
                                        apiLog.setExternalTradeNo(externalTradeNo);
                                        String apiUrl = envConfig.getGateway() != null ? envConfig.getGateway() : 
                                            ("sandbox".equals(alipayConfig.getEnv()) ? "https://openapi.alipaydev.com/gateway.do" : "https://openapi.alipay.com/gateway.do");
                                        apiLog.setApiUrl(apiUrl);
                                        apiLog.setRequestMethod("POST");
                                        apiLog.setExecutionTime((int) retryExecutionTime);
                                        
                                        if ("10000".equals(code)) {
                                            apiLog.setApiStatus(1); // 成功
                                        } else {
                                            apiLog.setApiStatus(0); // 失败
                                            apiLog.setErrorCode(code);
                                            apiLog.setErrorMessage(subMsg != null && !subMsg.isEmpty() ? subMsg : (msg != null ? msg : "退款失败"));
                                        }
                                        
                                        try {
                                            Map<String, Object> requestData = new HashMap<>();
                                            requestData.put("paymentNo", externalTradeNo);
                                            requestData.put("refundNo", retryRefundNo);
                                            requestData.put("refundAmount", refundAmountStr);
                                            requestData.put("refundReason", refundDTO.getRefundReason());
                                            requestData.put("useTradeNo", true);
                                            apiLog.setRequestData(objectMapper.writeValueAsString(requestData));
                                            apiLog.setResponseData(objectMapper.writeValueAsString(result));
                                        } catch (Exception ex) {
                                            log.warn("序列化退款数据失败", ex);
                                        }
                                        
                                        paymentLogService.savePaymentLog(apiLog);
                                    } catch (Exception ex) {
                                        log.warn("记录退款接口日志失败", ex);
                                    }
                                    
                                    if ("10000".equals(code)) {
                                        refundExternalTradeNo = retryRefundNo;
                                        log.info("支付宝预存款退款成功（使用支付宝交易号）：充值记录ID={}, internalOrderNo={}, externalTradeNo={}, refundAmount={}, refundExternalTradeNo={}", 
                                                detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), refundExternalTradeNo);
                                    } else {
                                        String errorMsg = subMsg != null && !subMsg.isEmpty() ? subMsg : (msg != null ? msg : "退款失败");
                                        log.error("使用支付宝交易号退款失败，充值记录ID：{}，内部订单号：{}，外部交易号：{}，错误码：{}，子错误码：{}，错误信息：{}",
                                                detail.getId(), internalOrderNo, externalTradeNo, code, subCode, errorMsg);
                                        throw new PaymentException(500, "使用支付宝交易号退款失败：" + errorMsg);
                                    }
                                } catch (PaymentException e2) {
                                    log.error("使用支付宝交易号退款也失败，充值记录ID：{}，内部订单号：{}，外部交易号：{}，错误信息：{}",
                                            detail.getId(), internalOrderNo, externalTradeNo, e2.getMessage(), e2);
                                    throw e2; // 抛出重试失败的异常
                                } catch (Exception e2) {
                                    log.error("使用支付宝交易号退款异常，充值记录ID：{}，内部订单号：{}，外部交易号：{}，错误信息：{}",
                                            detail.getId(), internalOrderNo, externalTradeNo, e2.getMessage(), e2);
                                    throw new PaymentException(500, "使用支付宝交易号退款异常：" + e2.getMessage(), e2);
                                }
                            } else {
                                // 没有外部交易号，抛出原始异常
                                throw e;
                            }
                        } else {
                            // 不是系统错误，直接抛出异常
                            throw e;
                        }
                    }
                } catch (PaymentException e) {
                    log.error("支付宝预存款退款失败，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，错误信息：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), e.getMessage(), e);
                    // 如果退款失败，需要回滚预存款余额
                    BigDecimal rollbackBalance = preDeposit.getBalance().add(refundDTO.getRefundAmount());
                    BigDecimal rollbackAvailableBalance = preDeposit.getAvailableBalance().add(refundDTO.getRefundAmount());
                    preDeposit.setBalance(rollbackBalance);
                    preDeposit.setAvailableBalance(rollbackAvailableBalance);
                    preDepositRepository.updateById(preDeposit);
                    throw new BusinessException(500, "支付宝退款失败：" + e.getMessage() + "，已回滚预存款余额");
                } catch (Exception e) {
                    log.error("支付宝预存款退款异常，充值记录ID：{}，内部订单号：{}，外部交易号：{}，退款金额：{}，错误信息：{}",
                            detail.getId(), internalOrderNo, externalTradeNo, refundDTO.getRefundAmount(), e.getMessage(), e);
                    // 如果退款失败，需要回滚预存款余额
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
        // 转换支付方式为中文名称
        String paymentMethodName = getPaymentMethodName(paymentMethod);
        refundDetail.setRemark("充值退款，原充值记录ID：" + detail.getId() + 
                "，原支付方式：" + paymentMethodName + 
                "，退款原因：" + refundDTO.getRefundReason());
        preDepositDetailRepository.insert(refundDetail);

        log.info("预存款充值退款成功：depositDetailId={}, refundAmount={}, userId={}, paymentMethod={}, refundExternalTradeNo={}", 
                refundDTO.getDepositDetailId(), refundDTO.getRefundAmount(), detail.getUserId(), 
                paymentMethod, refundExternalTradeNo);
    }

    /**
     * 计算指定充值记录的已退款金额
     * 查询所有退款记录，remark中包含"原充值记录ID：{充值记录ID}"的退款记录，累加退款金额
     *
     * @param rechargeDetailId 充值记录ID
     * @return 已退款金额
     */
    private BigDecimal calculateRefundedAmount(Long rechargeDetailId) {
        // 查询所有退款记录，remark中包含"原充值记录ID：{充值记录ID}"
        String remarkPattern = "原充值记录ID：" + rechargeDetailId;
        List<PreDepositDetail> refundRecords = preDepositDetailRepository.selectList(
                new LambdaQueryWrapper<PreDepositDetail>()
                        .eq(PreDepositDetail::getType, DepositType.REFUND) // 退款类型
                        .eq(PreDepositDetail::getStatus, 1) // 已通过状态
                        .like(PreDepositDetail::getRemark, remarkPattern) // remark中包含原充值记录ID
        );

        // 累加退款金额
        BigDecimal totalRefunded = BigDecimal.ZERO;
        for (PreDepositDetail refundRecord : refundRecords) {
            BigDecimal refundAmount = refundRecord.getExpenseAmount() != null 
                    ? refundRecord.getExpenseAmount() 
                    : refundRecord.getAmount();
            if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                totalRefunded = totalRefunded.add(refundAmount);
            }
        }

        return totalRefunded;
    }

    /**
     * 获取支付方式的中文名称
     *
     * @param paymentMethod 支付方式（英文）
     * @return 支付方式中文名称
     */
    private String getPaymentMethodName(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return "未知";
        }
        
        String methodUpper = paymentMethod.toUpperCase();
        switch (methodUpper) {
            case "ALIPAY":
                return "支付宝";
            case "WECHAT":
            case "WECHATPAY":
                return "微信";
            case "PRE_DEPOSIT":
                return "预存款";
            case "OFFLINE":
                return "线下支付";
            default:
                // 如果包含alipay关键字，返回支付宝
                if (methodUpper.contains("ALIPAY")) {
                    return "支付宝";
                }
                // 如果包含wechat或weixin关键字，返回微信
                if (methodUpper.contains("WECHAT") || methodUpper.contains("WEIXIN")) {
                    return "微信";
                }
                return paymentMethod;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPaymentStatus(Long id) {
        // 1. 查询记录
        PreDepositDetail detail = preDepositDetailRepository.selectById(id);
        if (detail == null) {
            throw new BusinessException(404, "记录不存在");
        }

        // 2. 验证状态和支付方式
        if (!DepositStatus.PAYING.equals(detail.getStatus())) {
            throw new BusinessException(400, "只有支付中状态的记录才能查询支付状态");
        }

        String paymentMethod = detail.getPaymentMethod();
        if (paymentMethod == null || 
            (!"alipay".equalsIgnoreCase(paymentMethod) && !"wechat".equalsIgnoreCase(paymentMethod))) {
            throw new BusinessException(400, "只有支付宝或微信支付的记录才能查询支付状态");
        }

        // 3. 频率限制检查（30秒内只能查询一次）
        String cacheKey = "sync_payment_status:" + id;
        Long lastQueryTime = paymentQueryCache.getIfPresent(cacheKey);
        if (lastQueryTime != null) {
            long elapsed = System.currentTimeMillis() - lastQueryTime;
            if (elapsed < 30000) { // 30秒 = 30000毫秒
                long remainingSeconds = (30000 - elapsed) / 1000;
                throw new BusinessException(429, String.format("查询过于频繁，请等待%d秒后重试", remainingSeconds));
            }
        }

        // 4. 更新缓存时间戳
        paymentQueryCache.put(cacheKey, System.currentTimeMillis());

        // 5. 获取内部订单号
        String internalOrderNo = detail.getInternalOrderNo();
        if (internalOrderNo == null || internalOrderNo.isEmpty()) {
            throw new BusinessException(400, "内部订单号不存在，无法查询支付状态");
        }

        // 6. 调用支付网关查询状态（会自动记录日志到payment_api_log表）
        String paymentMethodUpper = paymentMethod.toUpperCase();
        Integer paymentStatus;
        try {
            // 获取支付策略
            PaymentStrategy strategy = paymentGatewayService.getPaymentStrategy(paymentMethodUpper);
            if (strategy == null) {
                throw new BusinessException(400, "不支持的支付方式：" + paymentMethodUpper);
            }
            // 调用策略查询支付状态
            paymentStatus = strategy.queryPaymentStatus(internalOrderNo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询支付状态失败，记录ID：{}，内部订单号：{}", id, internalOrderNo, e);
            throw new BusinessException(500, "查询支付状态失败：" + e.getMessage());
        }

        // 7. 根据查询结果更新状态
        if (PaymentStatus.PAID.equals(paymentStatus)) {
            // 支付成功，更新充值记录状态和余额
            detail.setStatus(DepositStatus.APPROVED);
            detail.setAuditTime(LocalDateTime.now());
            
            // 查询外部交易号（从支付日志中获取）
            String externalTradeNo = getExternalTradeNoFromLog(internalOrderNo, paymentMethodUpper);
            if (externalTradeNo != null) {
                detail.setExternalTradeNo(externalTradeNo);
                detail.setRemark("预存款充值:外部交易号(" + externalTradeNo + ")");
            }

            // 更新预存款余额（使用悲观锁，防止并发充值导致余额不一致）
            PreDeposit preDeposit = preDepositRepository.selectOne(
                new LambdaQueryWrapper<PreDeposit>()
                    .eq(PreDeposit::getUserId, detail.getUserId())
                    .last("FOR UPDATE")
            );

            if (preDeposit != null) {
                BigDecimal currentBalance = preDeposit.getBalance() != null ? preDeposit.getBalance() : BigDecimal.ZERO;
                BigDecimal currentAvailableBalance = preDeposit.getAvailableBalance() != null 
                    ? preDeposit.getAvailableBalance() : BigDecimal.ZERO;
                BigDecimal depositAmount = detail.getDepositAmount() != null 
                    ? detail.getDepositAmount() 
                    : detail.getAmount();
                BigDecimal newBalance = currentBalance.add(depositAmount);
                BigDecimal newAvailableBalance = currentAvailableBalance.add(depositAmount);

                preDeposit.setBalance(newBalance);
                preDeposit.setAvailableBalance(newAvailableBalance);
                preDepositRepository.updateById(preDeposit);

                detail.setCurrentBalance(newBalance);
                detail.setAvailableBalance(newAvailableBalance);
            }

            preDepositDetailRepository.updateById(detail);
            log.info("手动查询支付状态成功，记录ID：{}，内部订单号：{}，状态已更新为已通过", id, internalOrderNo);
        } else if (PaymentStatus.CLOSED.equals(paymentStatus)) {
            // 订单已关闭
            detail.setStatus(DepositStatus.REJECTED);
            preDepositDetailRepository.updateById(detail);
            log.info("手动查询支付状态，记录ID：{}，内部订单号：{}，订单已关闭", id, internalOrderNo);
        } else if (PaymentStatus.FAILED.equals(paymentStatus)) {
            // 支付失败
            detail.setStatus(DepositStatus.REJECTED);
            preDepositDetailRepository.updateById(detail);
            log.info("手动查询支付状态，记录ID：{}，内部订单号：{}，支付失败", id, internalOrderNo);
        } else {
            // 仍然是支付中状态，不更新
            log.info("手动查询支付状态，记录ID：{}，内部订单号：{}，状态仍为支付中", id, internalOrderNo);
        }
    }

    /**
     * 从支付日志中获取外部交易号
     *
     * @param internalOrderNo 内部订单号
     * @param paymentMethod 支付方式（ALIPAY/WECHAT）
     * @return 外部交易号
     */
    private String getExternalTradeNoFromLog(String internalOrderNo, String paymentMethod) {
        try {
            LambdaQueryWrapper<PaymentApiLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PaymentApiLog::getOrderNo, internalOrderNo);
            queryWrapper.eq(PaymentApiLog::getApiType, "QUERY_ORDER");
            queryWrapper.eq(PaymentApiLog::getPaymentMethod, paymentMethod);
            queryWrapper.orderByDesc(PaymentApiLog::getCreateTime);
            queryWrapper.last("LIMIT 1");
            
            PaymentApiLog log = paymentApiLogMapper.selectOne(queryWrapper);
            return log != null ? log.getExternalTradeNo() : null;
        } catch (Exception e) {
            log.warn("从支付日志获取外部交易号失败", e);
            return null;
        }
    }
}

