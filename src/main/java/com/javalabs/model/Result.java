package com.javalabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装实体 (对标 Node.js 中的标准化 JSON 返回格式)
 * @param <T> 数据负载类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码 (200 表示成功，非 200 表示业务失败)
     */
    private int code;

    /**
     * 响应消息 (成功或错误的语义化描述)
     */
    private String message;

    /**
     * 数据负载 (核心业务数据)
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 快速成功静态方法
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 快速成功静态方法 (无数据)
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 快速失败静态方法
     */
    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 默认失败方法 (500)
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
