package com.shoppingmall.payment.exception;

import com.shoppingmall.common.exception.BusinessException;

/**
 * 支付异常
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public class PaymentException extends BusinessException {

    public PaymentException(String message) {
        super(500, message);
    }

    public PaymentException(Integer code, String message) {
        super(code, message);
    }

    public PaymentException(String message, Throwable cause) {
        super(500, message, cause);
    }

    public PaymentException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}


