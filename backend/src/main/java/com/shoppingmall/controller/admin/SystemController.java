package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.PaymentApiLogQueryDTO;
import com.shoppingmall.dto.ScheduledTaskExecutionLogQueryDTO;
import com.shoppingmall.dto.ScheduledTaskQueryDTO;
import com.shoppingmall.service.admin.SystemService;
import com.shoppingmall.vo.PaymentApiLogVO;
import com.shoppingmall.vo.ScheduledTaskExecutionLogVO;
import com.shoppingmall.vo.ScheduledTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 系统管理控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    /**
     * 分页查询支付接口日志
     */
    @GetMapping("/payment-api-log")
    public Result<IPage<PaymentApiLogVO>> getPaymentApiLogList(PaymentApiLogQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        IPage<PaymentApiLogVO> result = systemService.getPaymentApiLogList(queryDTO);
        return Result.success(result);
    }

    /**
     * 分页查询定时任务列表
     */
    @GetMapping("/scheduled-task")
    public Result<IPage<ScheduledTaskVO>> getScheduledTaskList(ScheduledTaskQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        IPage<ScheduledTaskVO> result = systemService.getScheduledTaskList(queryDTO);
        return Result.success(result);
    }

    /**
     * 切换任务状态
     */
    @PutMapping("/scheduled-task/{taskId}/status")
    public Result<Void> toggleTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Integer status
    ) {
        systemService.toggleTaskStatus(taskId, status);
        return Result.success();
    }

    /**
     * 立即执行任务
     */
    @PostMapping("/scheduled-task/{taskId}/execute")
    public Result<Void> executeTaskNow(@PathVariable Long taskId) {
        systemService.executeTaskNow(taskId);
        return Result.success();
    }

    /**
     * 分页查询任务执行日志
     */
    @GetMapping("/scheduled-task/log")
    public Result<IPage<ScheduledTaskExecutionLogVO>> getTaskLogs(ScheduledTaskExecutionLogQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        IPage<ScheduledTaskExecutionLogVO> result = systemService.getTaskExecutionLogList(queryDTO);
        return Result.success(result);
    }
}

