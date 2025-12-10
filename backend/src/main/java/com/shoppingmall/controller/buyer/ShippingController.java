package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ShippingFeeCalculateDTO;
import com.shoppingmall.service.logistics.ShippingService;
import com.shoppingmall.vo.ShippingMethodVO;
import lombok.RequiredArgsConstructor;
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
}

