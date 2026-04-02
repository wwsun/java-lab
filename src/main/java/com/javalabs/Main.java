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

        // 4. instanceof 模式匹配 (JDK 16+)：告别强制类型转换
        // 类似 TS 中的类型守卫 (Type Guard)，匹配后自动绑定变量
        Object unknownType = "I am a String";
        String typeDescription;
        if (unknownType instanceof String s) {
            typeDescription = "这是一个字符串: " + s;
        } else if (unknownType instanceof Integer i) {
            typeDescription = "这是一个数字: " + i;
        } else if (unknownType instanceof User u) {
            typeDescription = "这是一个用户对象: " + u.name();
        } else {
            typeDescription = "未知类型";
        }

        System.out.println(typeDescription);

        // 5. Switch 表达式 (JDK 14+)：箭头语法 + 直接返回值
        // 注意：JDK 17 的 switch 表达式支持常量/枚举匹配，但不支持类型模式匹配
        // 类型模式匹配 (case String s ->) 需要 JDK 21 才正式可用
        var day = "MONDAY";
        var dayType = switch (day) {
            case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "工作日";
            case "SATURDAY", "SUNDAY" -> "周末";
            default -> "未知";
        };
        System.out.println(day + " 是 " + dayType);
    }
}
