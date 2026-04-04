package com.javalabs.basics;

import com.javalabs.controller.EmployeeController;
import com.javalabs.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * DI 安全性对比实验
 * 演示在离开 Spring 环境时，注入方式对代码健壮性的影响
 */
class DISafetyTest {

    @Test
    @DisplayName("实验 A：属性注入在单测环境下极易出现 NPE")
    void fieldInjectionTest() {
        // 模拟一个使用了属性注入 (@Autowired) 的 Controller
        class DangerousController {
            // @Autowired (实际上单测环境根本不会处理这个注解)
            EmployeeService employeeService; 

            public void doWork() {
                employeeService.getAllEmployees(); // 👈 这里会崩
            }
        }

        DangerousController controller = new DangerousController();
        
        // 验证：在不启动 Spring 的情况下，controller 内部的 service 永远是 null
        assertThrows(NullPointerException.class, controller::doWork, 
            "属性注入无法在外部被赋值，必然导致 NPE");
    }

    @Test
    @DisplayName("实验 B：构造器注入在单测环境下提供编译级安全")
    void constructorInjectionTest() {
        // 创建 Mock 依赖 (模拟 Node.js 中的 mock)
        EmployeeService mockService = Mockito.mock(EmployeeService.class);
        
        // 验证点：
        // 1. 如果我不传 mockService，编译就会报错 (尝试解开下面注释看看)：
        // EmployeeController controller = new EmployeeController(); 
        
        // 2. 构造器注入强制我必须显式传入依赖
        EmployeeController controller = new EmployeeController(mockService);
        
        assertNotNull(controller, "对象一旦创建，其依赖必然已经就绪");
    }
}
