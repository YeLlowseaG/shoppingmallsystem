package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.OrderSyncLog;
import com.shoppingmall.mapper.OrderSyncLogMapper;
import com.shoppingmall.service.erp.JushuitanLogisticsService;
import com.shoppingmall.service.erp.JushuitanOrderService;
import com.shoppingmall.vo.OrderSyncLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单同步Controller
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/erp/order")
public class OrderSyncController {

    @Resource
    private JushuitanOrderService jushuitanOrderService;

    @Resource
    private JushuitanLogisticsService jushuitanLogisticsService;

    @Resource
    private OrderSyncLogMapper orderSyncLogMapper;

    /**
     * 手动推送订单到聚水潭
     */
    @PostMapping("/push/{orderId}")
    public Result<String> pushOrder(@PathVariable Long orderId) {
        boolean success = jushuitanOrderService.pushOrder(orderId);
        if (success) {
            return Result.success("订单推送成功");
        } else {
            return Result.error("订单推送失败");
        }
    }

    /**
     * 批量推送订单
     */
    @PostMapping("/push/batch")
    public Result<Map<String, Object>> batchPushOrders(@RequestBody List<Long> orderIds) {
        int successCount = jushuitanOrderService.batchPushOrders(orderIds);

        Map<String, Object> result = new HashMap<>();
        result.put("total", orderIds.size());
        result.put("success", successCount);
        result.put("failed", orderIds.size() - successCount);

        return Result.success(String.format("批量推送完成，成功%d个，失败%d个",
            successCount, orderIds.size() - successCount), result);
    }

    /**
     * 重试失败的订单推送
     */
    @PostMapping("/push/retry/{orderId}")
    public Result<String> retryPushOrder(@PathVariable Long orderId) {
        boolean success = jushuitanOrderService.retryPushOrder(orderId);
        if (success) {
            return Result.success("订单重新推送成功");
        } else {
            return Result.error("订单重新推送失败");
        }
    }

    /**
     * 查询订单推送状态
     */
    @GetMapping("/push/status/{orderId}")
    public Result<String> queryPushStatus(@PathVariable Long orderId) {
        String status = jushuitanOrderService.queryPushStatus(orderId);
        return Result.success(status);
    }

    /**
     * 手动拉取订单物流信息
     */
    @PostMapping("/logistics/pull/{orderId}")
    public Result<String> pullLogistics(@PathVariable Long orderId) {
        boolean success = jushuitanLogisticsService.pullLogistics(orderId);
        if (success) {
            return Result.success("物流信息拉取成功");
        } else {
            return Result.error("物流信息拉取失败");
        }
    }

    /**
     * 批量拉取物流信息
     */
    @PostMapping("/logistics/pull/batch")
    public Result<Map<String, Object>> batchPullLogistics(@RequestBody List<Long> orderIds) {
        int successCount = jushuitanLogisticsService.batchPullLogistics(orderIds);

        Map<String, Object> result = new HashMap<>();
        result.put("total", orderIds.size());
        result.put("success", successCount);
        result.put("failed", orderIds.size() - successCount);

        return Result.success(String.format("批量拉取完成，成功%d个，失败%d个",
            successCount, orderIds.size() - successCount), result);
    }

    /**
     * 拉取所有待发货订单的物流信息
     */
    @PostMapping("/logistics/pull/pending")
    public Result<Integer> pullPendingLogistics() {
        int successCount = jushuitanLogisticsService.pullPendingLogistics();
        return Result.success(String.format("成功拉取%d个订单的物流信息", successCount), successCount);
    }

    /**
     * 查询订单物流状态
     */
    @GetMapping("/logistics/status/{orderId}")
    public Result<String> queryLogisticsStatus(@PathVariable Long orderId) {
        String status = jushuitanLogisticsService.queryLogisticsStatus(orderId);
        return Result.success(status);
    }

    /**
     * 分页查询订单同步日志
     */
    @GetMapping("/sync-log")
    public Result<Page<OrderSyncLogVO>> getSyncLogs(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize,
        @RequestParam(required = false) Long orderId,
        @RequestParam(required = false) String syncType,
        @RequestParam(required = false) Integer syncStatus
    ) {
        Page<OrderSyncLog> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderSyncLog> queryWrapper = new QueryWrapper<>();

        if (orderId != null) {
            queryWrapper.eq("order_id", orderId);
        }
        if (syncType != null && !syncType.isEmpty()) {
            queryWrapper.eq("sync_type", syncType);
        }
        if (syncStatus != null) {
            queryWrapper.eq("sync_status", syncStatus);
        }

        queryWrapper.orderByDesc("create_time");

        Page<OrderSyncLog> logPage = orderSyncLogMapper.selectPage(page, queryWrapper);

        // 转换为VO
        Page<OrderSyncLogVO> voPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        List<OrderSyncLogVO> voList = new ArrayList<>();

        for (OrderSyncLog log : logPage.getRecords()) {
            OrderSyncLogVO vo = new OrderSyncLogVO();
            BeanUtils.copyProperties(log, vo);

            // 状态描述
            if (log.getSyncStatus() != null) {
                switch (log.getSyncStatus()) {
                    case 0:
                        vo.setSyncStatusDesc("失败");
                        break;
                    case 1:
                        vo.setSyncStatusDesc("成功");
                        break;
                    case 2:
                        vo.setSyncStatusDesc("处理中");
                        break;
                    default:
                        vo.setSyncStatusDesc("未知");
                }
            }

            // 同步类型描述
            if (log.getSyncType() != null) {
                switch (log.getSyncType()) {
                    case "PUSH_ORDER":
                        vo.setSyncTypeDesc("订单推送");
                        break;
                    case "PULL_LOGISTICS":
                        vo.setSyncTypeDesc("物流拉取");
                        break;
                    case "QUERY_ORDER":
                        vo.setSyncTypeDesc("订单查询");
                        break;
                    default:
                        vo.setSyncTypeDesc(log.getSyncType());
                }
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    /**
     * 查询指定订单的同步日志
     */
    @GetMapping("/sync-log/{orderId}")
    public Result<List<OrderSyncLogVO>> getOrderSyncLogs(@PathVariable Long orderId) {
        List<OrderSyncLog> logs = orderSyncLogMapper.selectList(
            new QueryWrapper<OrderSyncLog>()
                .eq("order_id", orderId)
                .orderByDesc("create_time")
        );

        List<OrderSyncLogVO> voList = new ArrayList<>();
        for (OrderSyncLog log : logs) {
            OrderSyncLogVO vo = new OrderSyncLogVO();
            BeanUtils.copyProperties(log, vo);

            // 状态描述
            if (log.getSyncStatus() != null) {
                switch (log.getSyncStatus()) {
                    case 0:
                        vo.setSyncStatusDesc("失败");
                        break;
                    case 1:
                        vo.setSyncStatusDesc("成功");
                        break;
                    case 2:
                        vo.setSyncStatusDesc("处理中");
                        break;
                }
            }

            // 同步类型描述
            if (log.getSyncType() != null) {
                switch (log.getSyncType()) {
                    case "PUSH_ORDER":
                        vo.setSyncTypeDesc("订单推送");
                        break;
                    case "PULL_LOGISTICS":
                        vo.setSyncTypeDesc("物流拉取");
                        break;
                    case "QUERY_ORDER":
                        vo.setSyncTypeDesc("订单查询");
                        break;
                }
            }

            voList.add(vo);
        }

        return Result.success(voList);
    }
}
