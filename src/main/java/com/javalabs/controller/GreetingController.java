package com.javalabs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 您的第一个 Spring Boot Controller
 * @RestController = @Controller + @ResponseBody
 */
@RestController
public class GreetingController {

    /**
     * 实现一个简单的 GET 接口
     * 访问路径: http://localhost:8080/api/greeting?name=Java
     */
    @GetMapping("/api/greeting")
    public GreetingResponse sayHello(@RequestParam(defaultValue = "Modern Java") String name) {
        // 返回一个对象，Spring 默认使用 Jackson 库将其转为 JSON
        return new GreetingResponse("Hello, " + name + "!", System.currentTimeMillis());
    }

    /**
     * 使用 Java 17 Record 快速定义 DTO (Data Transfer Object)
     * 对标 TypeScript 的 interface / Type
     */
    public record GreetingResponse(String message, long timestamp) {}
}
