package com.javalabs.exception;
import com.javalabs.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 (AOP)
 * 统一处理全站 API 异常，返回规范的 JSON 响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 JSON 解析异常 (如非法 JSON 格式)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("❌ JSON 解析异常: {}", ex.getMessage());
        return Result.error(HttpStatus.BAD_REQUEST.value(), "请求体格式错误，请检查 JSON 语法: " + ex.getMessage());
    }

    /**
     * 处理入参校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("❌ 校验失败: {}", message);
        return Result.error(HttpStatus.BAD_REQUEST.value(), "校验失败: " + message);
    }

    /**
     * 处理自定义的 ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFoundException(ResourceNotFoundException ex) {
        log.warn("❌ 资源未找到: {}", ex.getMessage());
        return Result.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    /**
     * 处理兜底的系统异常 (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGlobalException(Exception ex) {
        log.error("💥 系统内部错误", ex);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "抱歉，服务器开小差了: " + ex.getMessage());
    }
}
