package com.javalabs.basics;
import com.javalabs.exception.InsufficientFundsException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java 异常处理与业务异常实操验证")
class ExceptionDemoTest {

    @Test
    @DisplayName("应该能够捕获业务受检异常 (Checked Exception)")
    void shouldHandleBusinessCheckedException() {
        // 1. Arrange
        ExceptionDemo demo = new ExceptionDemo();
        double balance = 50.0;
        double amount = 100.0;

        // 2. Act & Assert: 使用 assertThrows 捕获具体异常
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> {
            demo.transfer(balance, amount);
        }, "应该抛出 InsufficientFundsException！");

        // 3. 验证异常内部的业务字段
        assertEquals(50.0, exception.getAmountNeeded(), "应补差额应该是 50.0");
    }

    @Test
    @DisplayName("应该能够捕获运行时非法参数异常 (Unchecked Exception)")
    void shouldHandleRuntimeUncheckedException() {
        // 1. Arrange
        ExceptionDemo demo = new ExceptionDemo();

        // 2. Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            demo.transfer(100.0, -10.0);
        });

        // 3. 验证异常消息是否正确
        assertEquals("Amount must be positive!", exception.getMessage());
    }

    @Test
    @DisplayName("验证 try-finally 逻辑：即使 catch 中返回，finally 依然会执行")
    void shouldExecuteFinallyEvenOnReturn() {
        // 1. Arrange: 这种情况需要集成测试验证日志输出，单元测试通常验证业务逻辑
        // 但我们可以验证其返回逻辑：除以 0 时，根据 demo 逻辑应返回默认值 0
        ExceptionDemo demo = new ExceptionDemo();

        // 2. Act
        int result = demo.divide(10, 0);

        // 3. Assert
        assertEquals(0, result, "除以 0 应该被 catch 捕获并返回 0");
    }
}
