package com.shoppingmall.common.exception;

import com.shoppingmall.common.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理客户端断开连接异常（静默处理，不记录错误日志）
     * 这种情况通常发生在客户端提前关闭连接时，属于正常情况
     */
    @ExceptionHandler(IOException.class)
    public Result<?> handleIOException(IOException e, HttpServletRequest request, HttpServletResponse response) {
        // 检查是否是客户端断开连接相关的异常
        if (isClientAbortException(e)) {
            // 检查响应是否已提交
            if (response.isCommitted()) {
                // 响应已提交，无法返回，直接返回null
                return null;
            }
            // 静默处理，只记录debug级别日志
            log.debug("客户端断开连接: {} - {}", request.getRequestURI(), e.getMessage());
            return null;
        }
        // 其他IOException继续处理
        log.error("IO异常", e);
        return Result.error(500, "系统异常: " + e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request, HttpServletResponse response) {
        // 检查是否是客户端断开连接异常
        Throwable cause = e.getCause();
        if (cause != null && (cause instanceof IOException && isClientAbortException((IOException) cause))) {
            if (response.isCommitted()) {
                return null;
            }
            log.debug("客户端断开连接（运行时异常）: {} - {}", request.getRequestURI(), cause.getMessage());
            return null;
        }
        log.error("运行时异常", e);
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        // 检查是否是客户端断开连接异常
        Throwable cause = e.getCause();
        if (cause != null && (cause instanceof IOException && isClientAbortException((IOException) cause))) {
            if (response.isCommitted()) {
                return null;
            }
            log.debug("客户端断开连接（系统异常）: {} - {}", request.getRequestURI(), cause.getMessage());
            return null;
        }
        log.error("系统异常", e);
        return Result.error(500, "系统异常: " + e.getMessage());
    }

    /**
     * 判断是否是客户端断开连接异常
     */
    private boolean isClientAbortException(IOException e) {
        if (e == null) {
            return false;
        }
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        // 检查异常类型名称（支持 ClientAbortException）
        String className = e.getClass().getName();
        if (className.contains("ClientAbortException")) {
            return true;
        }
        // 检查异常消息
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("connection reset by peer") ||
               lowerMessage.contains("broken pipe") ||
               lowerMessage.contains("connection aborted");
    }
}

