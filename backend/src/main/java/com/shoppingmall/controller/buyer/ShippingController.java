package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ShippingFeeCalculateDTO;
import com.shoppingmall.service.logistics.ShippingService;
import com.shoppingmall.vo.ShippingMethodVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配送方式控制器（买家端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@RestController("buyerShippingController")
@RequestMapping("/api/buyer/shipping")
@RequiredArgsConstructor
@Slf4j
public class ShippingController {

    private final ShippingService shippingService;

    /**
     * 获取所有启用的配送方式列表
     */
    @GetMapping("/methods")
    public Result<List<ShippingMethodVO>> getEnabledShippingMethods() {
        List<ShippingMethodVO> methods = shippingService.getEnabledShippingMethods();
        return Result.success(methods);
    }

    /**
     * 计算运费
     */
    @PostMapping("/calculate")
    public Result<BigDecimal> calculateShippingFee(@RequestBody ShippingFeeCalculateDTO calculateDTO) {
        BigDecimal fee = shippingService.calculateShippingFee(calculateDTO);
        return Result.success(fee);
    }

    /**
     * 根据运费模板ID计算运费（用于订单结算）
     */
    @PostMapping("/calculate-by-template/{templateId}")
    public Result<BigDecimal> calculateShippingFeeByTemplate(
            @PathVariable Long templateId,
            @RequestBody ShippingFeeCalculateDTO calculateDTO) {
        log.info("========== 收到运费计算请求 ==========");
        log.info("运费模板ID: {}", templateId);
        log.info("计算参数: province={}, city={}, district={}, totalWeight={}kg, totalAmount={}, totalQuantity={}",
            calculateDTO.getProvince(), calculateDTO.getCity(), calculateDTO.getDistrict(),
            calculateDTO.getTotalWeight(), calculateDTO.getTotalAmount(), calculateDTO.getTotalQuantity());
        
        BigDecimal fee = shippingService.calculateShippingFeeByTemplate(templateId, calculateDTO);
        
        log.info("========== 运费计算结果: {} ==========", fee);
        return Result.success(fee);
    }
}

