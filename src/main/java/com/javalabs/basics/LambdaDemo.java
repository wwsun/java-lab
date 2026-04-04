package com.javalabs.basics;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 演示 Java Lambda 核心语法与心智模型。
 * 重点：理解如何将“逻辑”作为参数传递。
 */
public class LambdaDemo {

    /**
     * 演示 Effectively Final 变量捕获限制。
     * 核心原理：Java 为了保证多线程安全，Lambda 内部只能访问不可变的外部变量。
     */
    public void captureVariable() {
        // 虽然没写 final，但这个变量在声明后不能再被修改
        String prefix = "Event: ";
        
        // ❌ 错误示范：如果取消下面这一行的注释，Lambda 表达式将会编译报错
        // prefix = "Modified: "; 
        
        // 这里的 Lambda 捕获并锁定了 prefix 的当前值
        Runnable r = () -> System.out.println(prefix + "Runner is active!");
        r.run();
    }

    /**
     * 演示如何“绕过”变量修改限制。
     * 技巧：修改数组的内容（引用未变），而不是修改变量本身的引用。
     */
    public int bypassFinalLimit() {
        // counter 是一个最终引用，指向一个可以修改的数组空间
        // final 标记了该引用不能被重新赋值
        // 创建一个包含 1 个元素的 int 数组，初始值是 0
        // 等同于 int[] counter = new int[]{0}

        final int[] counter = {0};

        Runnable r = () -> counter[0]++;
        r.run();
        r.run();
        
        return counter[0];
    }

    /**
     * 演示系统内置的“四大天王”接口与方法引用。
     */
    public void functionalInterfaces() {
        List<String> names = List.of("Alice", "Bob", "Charlie", "David");

        // 1. Predicate (断言)：接收一个对象，返回布尔值。常用于 filter。
        Predicate<String> startWithA = s -> s.startsWith("A");
        
        // 2. Function (转换)：接收 T，返回 R。常用于 map。
        // 这里使用了「方法引用」写法 String::length，等同于 s -> s.length()
        Function<String, Integer> lengthMapper = String::length; 
        
        // 3. Consumer (消费)：接收对象，无返回值。常用于 forEach。
        // 这里使用了「方法引用」写法 System.out::println
        Consumer<String> printer = System.out::println;

        printer.accept("Hello Lambda");

        System.out.println("--- Lambda 实战演练 (Stream API) ---");
        names.stream()
             .filter(startWithA) // 使用 Predicate 过滤
             .map(lengthMapper) // 使用 Function 转换
             .forEach(len -> System.out.println("A-名 字符长度: " + len));
    }

    public static void main(String[] args) {
        var demo = new LambdaDemo();
        
        System.out.println("=== 1. 变量捕获 (Effectively Final) ===");
        demo.captureVariable();
        
        System.out.println("\n=== 2. 内置接口与方法引用 (Method References) ===");
        demo.functionalInterfaces();
    }
}
