package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.OrderMessageHandleDTO;
import com.shoppingmall.dto.OrderMessageQueryDTO;
import com.shoppingmall.service.admin.OrderMessageService;
import com.shoppingmall.vo.OrderMessageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单问题/消息控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@RestController("adminOrderMessageController")
@RequestMapping("/api/admin/order-messages")
@RequiredArgsConstructor
public class OrderMessageController {

    private final OrderMessageService orderMessageService;
    private final HttpServletRequest request;

    /**
     * 分页查询订单问题列表
     */
    @GetMapping("/page")
    public Result<IPage<OrderMessageVO>> getOrderMessagePage(OrderMessageQueryDTO queryDTO) {
        IPage<OrderMessageVO> page = orderMessageService.getOrderMessagePage(queryDTO);
        return Result.success(page);
    }

    /**
     * 根据ID获取订单问题详情
     */
    @GetMapping("/{id}")
    public Result<OrderMessageVO> getOrderMessageById(@PathVariable Long id) {
        OrderMessageVO vo = orderMessageService.getOrderMessageById(id);
        return Result.success(vo);
    }

    /**
     * 处理订单问题
     */
    @PutMapping("/handle")
    public Result<?> handleOrderMessage(@Valid @RequestBody OrderMessageHandleDTO handleDTO) {
        // 从请求中获取管理员信息（实际应该从JWT token中获取）
        Long handlerId = (Long) request.getAttribute("adminId");
        String handlerName = (String) request.getAttribute("adminName");
        
        // 如果JWT中没有，暂时使用默认值（实际项目中应该从认证信息中获取）
        if (handlerId == null) {
            handlerId = 1L;
        }
        if (handlerName == null) {
            handlerName = "管理员";
        }

        orderMessageService.handleOrderMessage(handleDTO, handlerId, handlerName);
        return Result.success("处理成功");
    }
}

























