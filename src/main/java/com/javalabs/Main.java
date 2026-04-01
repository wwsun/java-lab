package com.javalabs;

public class Main {
    public static void main(String[] args) {
        // 1. var 关键字：局部变量类型推断（感受下 TS 的 let/const 回归）
        var framework = "Spring Boot 3";
        var version = 21;

        // 2. 多行文本块：完美替代拼接（感受下 TS 的反引号模板字符串 ``）
        var userJson = """
            {
              "name": "Alex",
              "role": "Frontend Architect"
            }
            """;

        // 3. Records 记录类：专门承载数据的不可变结构 (类似 TS 里的 Interface)
        record User(String name, String role) {}
        var user = new User("Alex", "Node Developer");

        // 4. Switch 模式匹配带来的强力返回 (像极了更高级一点的 switch 表达式)
        Object unknownType = "I am a String";
        var typeDescription = switch (unknownType) {
            case String s -> "这是一个字符串: " + s;
            case Integer i -> "这是一个数字: " + i;
            case User u -> "这是一个用户对象: " + u.name();
            default -> "未知类型";
        };

        System.out.println(typeDescription);
    }
}
