package com.javalabs.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Callable 与 Future 的异步获取验证")
class CallableDemoTest {

    @Test
    @DisplayName("验证 Future 是否能正确获取 Callable 的计算结果 (42)")
    void testCallableResultSuccess() throws ExecutionException, InterruptedException {
        // 1. Arrange
        CallableDemo demo = new CallableDemo();

        // 2. Act
        Integer result = demo.calculateWithResult();

        // 3. Assert
        assertEquals(42, result, "Future 拿到的结果必须是 Callable 返回的 42");
    }
}
