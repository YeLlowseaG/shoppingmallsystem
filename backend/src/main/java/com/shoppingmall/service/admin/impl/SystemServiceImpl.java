package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.PaymentApiLogQueryDTO;
import com.shoppingmall.dto.ScheduledTaskExecutionLogQueryDTO;
import com.shoppingmall.dto.ScheduledTaskQueryDTO;
import com.shoppingmall.entity.PaymentApiLog;
import com.shoppingmall.entity.ScheduledTask;
import com.shoppingmall.entity.ScheduledTaskExecutionLog;
import com.shoppingmall.mapper.PaymentApiLogMapper;
import com.shoppingmall.mapper.ScheduledTaskExecutionLogMapper;
import com.shoppingmall.mapper.ScheduledTaskMapper;
import com.shoppingmall.service.admin.SystemService;
import com.shoppingmall.vo.PaymentApiLogVO;
import com.shoppingmall.vo.ScheduledTaskExecutionLogVO;
import com.shoppingmall.vo.ScheduledTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统管理服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final PaymentApiLogMapper paymentApiLogMapper;
    private final ScheduledTaskMapper scheduledTaskMapper;
    private final ScheduledTaskExecutionLogMapper scheduledTaskExecutionLogMapper;

    @Autowired
    private ApplicationContext applicationContext;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<PaymentApiLogVO> getPaymentApiLogList(PaymentApiLogQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        // 构建查询条件
        LambdaQueryWrapper<PaymentApiLog> wrapper = new LambdaQueryWrapper<>();

        // 支付方式
        if (StringUtils.hasText(queryDTO.getPaymentMethod())) {
            wrapper.eq(PaymentApiLog::getPaymentMethod, queryDTO.getPaymentMethod());
        }

        // 接口类型
        if (StringUtils.hasText(queryDTO.getApiType())) {
            wrapper.eq(PaymentApiLog::getApiType, queryDTO.getApiType());
        }

        // 业务类型
        if (StringUtils.hasText(queryDTO.getBusinessType())) {
            wrapper.eq(PaymentApiLog::getBusinessType, queryDTO.getBusinessType());
        }

        // 订单号
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.like(PaymentApiLog::getOrderNo, queryDTO.getOrderNo());
        }

        // 支付流水号
        if (StringUtils.hasText(queryDTO.getPaymentNo())) {
            wrapper.like(PaymentApiLog::getPaymentNo, queryDTO.getPaymentNo());
        }

        // 接口状态
        if (queryDTO.getApiStatus() != null) {
            wrapper.eq(PaymentApiLog::getApiStatus, queryDTO.getApiStatus());
        }

        // 时间范围
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(queryDTO.getStartTime(), DATETIME_FORMATTER);
                wrapper.ge(PaymentApiLog::getCreateTime, startTime);
            } catch (Exception e) {
                log.warn("开始时间格式错误: {}", queryDTO.getStartTime());
            }
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            try {
                LocalDateTime endTime = LocalDateTime.parse(queryDTO.getEndTime(), DATETIME_FORMATTER);
                wrapper.le(PaymentApiLog::getCreateTime, endTime);
            } catch (Exception e) {
                log.warn("结束时间格式错误: {}", queryDTO.getEndTime());
            }
        }

        // 按创建时间倒序
        wrapper.orderByDesc(PaymentApiLog::getCreateTime);

        // 分页查询
        Page<PaymentApiLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<PaymentApiLog> resultPage = paymentApiLogMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<PaymentApiLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<PaymentApiLogVO> voList = resultPage.getRecords().stream().map(log -> {
            PaymentApiLogVO vo = new PaymentApiLogVO();
            BeanUtils.copyProperties(log, vo);
            
            // 设置描述字段
            vo.setApiTypeDesc(getApiTypeDesc(log.getApiType()));
            vo.setBusinessTypeDesc(getBusinessTypeDesc(log.getBusinessType()));
            vo.setApiStatusDesc(getApiStatusDesc(log.getApiStatus()));
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取接口类型描述
     */
    private String getApiTypeDesc(String apiType) {
        if (apiType == null) {
            return "";
        }
        return switch (apiType) {
            case "CREATE_PAYMENT" -> "创建支付";
            case "REFUND" -> "退款";
            case "QUERY_ORDER" -> "查询订单";
            case "CALLBACK" -> "回调通知";
            default -> apiType;
        };
    }

    /**
     * 获取业务类型描述
     */
    private String getBusinessTypeDesc(String businessType) {
        if (businessType == null) {
            return "";
        }
        return switch (businessType) {
            case "ORDER" -> "订单支付";
            case "DEPOSIT" -> "预存款充值";
            default -> businessType;
        };
    }

    /**
     * 获取接口状态描述
     */
    private String getApiStatusDesc(Integer apiStatus) {
        if (apiStatus == null) {
            return "";
        }
        return switch (apiStatus) {
            case 0 -> "失败";
            case 1 -> "成功";
            case 2 -> "处理中";
            default -> String.valueOf(apiStatus);
        };
    }

    @Override
    public IPage<ScheduledTaskVO> getScheduledTaskList(ScheduledTaskQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        // 构建查询条件
        LambdaQueryWrapper<ScheduledTask> wrapper = new LambdaQueryWrapper<>();

        // 任务名称（模糊查询）
        if (StringUtils.hasText(queryDTO.getTaskName())) {
            wrapper.like(ScheduledTask::getTaskName, queryDTO.getTaskName());
        }

        // 状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(ScheduledTask::getStatus, queryDTO.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(ScheduledTask::getCreateTime);

        // 分页查询
        Page<ScheduledTask> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ScheduledTask> resultPage = scheduledTaskMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<ScheduledTaskVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ScheduledTaskVO> voList = resultPage.getRecords().stream().map(task -> {
            ScheduledTaskVO vo = new ScheduledTaskVO();
            BeanUtils.copyProperties(task, vo);
            
            // 设置状态描述
            vo.setStatusDesc(getTaskStatusDesc(task.getStatus()));
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取任务状态描述
     */
    private String getTaskStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "已停止";
            case 1 -> "运行中";
            default -> String.valueOf(status);
        };
    }

    /**
     * 立即执行定时任务（手动执行）
     * 注意：只有手动执行的任务才会记录执行日志，自动执行的定时任务不会记录日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTaskNow(Long taskId) {
        // 查询任务信息
        ScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        ScheduledTaskExecutionLog executionLog = new ScheduledTaskExecutionLog();
        executionLog.setTaskId(taskId);
        executionLog.setTaskName(task.getTaskName());
        executionLog.setTaskGroup(task.getTaskGroup());
        executionLog.setBeanName(task.getBeanName());
        executionLog.setMethodName(task.getMethodName());
        executionLog.setStartTime(startTime);
        executionLog.setExecuteType(2); // 2-手动执行（固定值，不记录自动执行的日志）
        executionLog.setExecuteStatus(0); // 默认失败，执行成功后更新

        String errorMessage = null;
        try {
            // 从Spring容器中获取Bean
            Object bean = applicationContext.getBean(task.getBeanName());

            // 通过反射调用方法（getMethod如果找不到方法会抛出NoSuchMethodException）
            Method method = bean.getClass().getMethod(task.getMethodName());

            log.info("开始手动执行定时任务: taskId={}, taskName={}, beanName={}, methodName={}", 
                    taskId, task.getTaskName(), task.getBeanName(), task.getMethodName());

            // 执行方法
            method.invoke(bean);

            // 记录成功
            executionLog.setExecuteStatus(1); // 1-成功
            log.info("定时任务执行成功: taskId={}, taskName={}", taskId, task.getTaskName());

            // 更新任务的最后执行时间
            task.setLastExecuteTime(startTime);
            scheduledTaskMapper.updateById(task);

        } catch (Exception e) {
            errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getName();
            }
            log.error("定时任务执行失败: taskId={}, taskName={}, error={}", 
                    taskId, task.getTaskName(), errorMessage, e);
            executionLog.setExecuteStatus(0); // 0-失败
            executionLog.setErrorMessage(errorMessage);
        } finally {
            // 记录结束时间和耗时
            LocalDateTime endTime = LocalDateTime.now();
            executionLog.setEndTime(endTime);
            long duration = java.time.Duration.between(startTime, endTime).toMillis();
            executionLog.setDuration(duration);

            // 保存执行日志
            scheduledTaskExecutionLogMapper.insert(executionLog);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTaskStatus(Long taskId, Integer status) {
        ScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        task.setStatus(status);
        scheduledTaskMapper.updateById(task);

        log.info("定时任务状态已更新: taskId={}, taskName={}, status={}", 
                taskId, task.getTaskName(), status == 1 ? "运行中" : "已停止");
    }

    @Override
    public IPage<ScheduledTaskExecutionLogVO> getTaskExecutionLogList(ScheduledTaskExecutionLogQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        // 构建查询条件
        LambdaQueryWrapper<ScheduledTaskExecutionLog> wrapper = new LambdaQueryWrapper<>();

        // 任务ID
        if (queryDTO.getTaskId() != null) {
            wrapper.eq(ScheduledTaskExecutionLog::getTaskId, queryDTO.getTaskId());
        }

        // 开始时间范围查询
        if (queryDTO.getStartTime() != null && !queryDTO.getStartTime().trim().isEmpty()) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(queryDTO.getStartTime(), 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                wrapper.ge(ScheduledTaskExecutionLog::getStartTime, startTime);
            } catch (Exception e) {
                log.warn("开始时间格式错误，忽略该条件: {}", queryDTO.getStartTime(), e);
            }
        }
        if (queryDTO.getEndTime() != null && !queryDTO.getEndTime().trim().isEmpty()) {
            try {
                LocalDateTime endTime = LocalDateTime.parse(queryDTO.getEndTime(), 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                wrapper.le(ScheduledTaskExecutionLog::getStartTime, endTime);
            } catch (Exception e) {
                log.warn("结束时间格式错误，忽略该条件: {}", queryDTO.getEndTime(), e);
            }
        }

        // 按开始时间倒序
        wrapper.orderByDesc(ScheduledTaskExecutionLog::getStartTime);

        // 分页查询
        Page<ScheduledTaskExecutionLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ScheduledTaskExecutionLog> resultPage = scheduledTaskExecutionLogMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<ScheduledTaskExecutionLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ScheduledTaskExecutionLogVO> voList = resultPage.getRecords().stream().map(log -> {
            ScheduledTaskExecutionLogVO vo = new ScheduledTaskExecutionLogVO();
            BeanUtils.copyProperties(log, vo);
            
            // 设置描述字段
            vo.setExecuteStatusDesc(getExecuteStatusDesc(log.getExecuteStatus()));
            vo.setExecuteTypeDesc(getExecuteTypeDesc(log.getExecuteType()));
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取执行状态描述
     */
    private String getExecuteStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "失败";
            case 1 -> "成功";
            default -> String.valueOf(status);
        };
    }

    /**
     * 获取执行类型描述
     * 注意：目前只记录手动执行的日志，所以此方法主要用于显示
     */
    private String getExecuteTypeDesc(Integer type) {
        if (type == null) {
            return "";
        }
        // 虽然数据库设计支持1-定时执行，但实际只记录手动执行（类型2）的日志
        return switch (type) {
            case 1 -> "定时执行";
            case 2 -> "手动执行";
            default -> String.valueOf(type);
        };
    }
}

