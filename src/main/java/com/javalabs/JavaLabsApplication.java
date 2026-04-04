package com.javalabs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动主类
 * @SpringBootApplication 是一个复合注解，包含了：
 * 1. @SpringBootConfiguration (配置类)
 * 2. @EnableAutoConfiguration (开启自动配置 - Spring Boot 的魔法来源)
 * 3. @ComponentScan (扫描当前包及其子包下的组件，如 Controller, Service)
 */
@SpringBootApplication
public class JavaLabsApplication {

    public static void main(String[] args) {
        // 启动 Spring 应用上下文
        SpringApplication.run(JavaLabsApplication.class, args);
        System.out.println("🚀 Java Labs Spring Boot 应用启动成功！");
    }
}
