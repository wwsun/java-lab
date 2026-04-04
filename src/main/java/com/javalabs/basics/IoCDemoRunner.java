package com.javalabs.basics;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * IoC 容器盘点员
 * 实现 CommandLineRunner 接口，意味着这段代码会在 Spring Boot 启动完成后立即执行
 */
@Component // 👈 关键：必须加这个注解，Spring 才会把它当成自己的“员工”管理起来
public class IoCDemoRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;

    // 👈 声明构造函数，Spring 会自动把容器（ApplicationContext）传进来
    public IoCDemoRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n--- 🕵️ IoC 容器 Bean 盘点开始 ---");

        // 获取容器中所有 Bean 的名字
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        System.out.println("容器中 Bean 总数: " + beanNames.length);

        // 过滤出我们自己定义的 Bean (通常在 com.javalabs 包下)
        System.out.println("\n[自定义业务 Bean 列表]:");
        Arrays.stream(beanNames)
                .filter(name -> name.contains("employee") || name.contains("greeting") || name.contains("javaLabs"))
                .forEach(name -> System.out.println("-> " + name + " [" + applicationContext.getBean(name).getClass().getSimpleName() + "]"));

        System.out.println("\n[关键基础设施 Bean 示例]:");
        System.out.println("-> jacksonObjectMapper (负责 JSON 转换)");
        System.out.println("-> employeeController (您的控制器)");

        System.out.println("--- 🔎 盘点结束 ---\n");
    }
}
