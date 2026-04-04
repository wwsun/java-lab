package com.javalabs.basics;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 演示 Java 现代时间 API (java.time.*) 的核心使用。
 * 
 * 核心考点：不可变性 (Immutability) 与 线程安全 (Thread-Safe)。
 */
public class TimeDemo {

    /**
     * 演示 Instant (绝对时间/时间戳)
     * 对应 Node.js: Date.now() / new Date().toISOString()
     */
    public void showInstant() {
        // 1. 获取当前绝对时间点 (UTC)
        Instant now = Instant.now();
        System.out.println("Current Instant (UTC): " + now);

        // 2. 转换为本地时区时间 (类似 JS: dayjs(timestamp))
        LocalDateTime localAtZone = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        System.out.println("Instant to Local (System Default): " + localAtZone);

        // 3. 时间戳转换 (类比 JS: Date.now())
        long epochMillis = now.toEpochMilli();
        System.out.println("Epoch Millis: " + epochMillis);

        // 4. 从时间戳恢复
        Instant restored = Instant.ofEpochMilli(epochMillis);
        System.out.println("Restored from Millis: " + restored);
    }

    /**
     * 演示如何创建和解析时间
     */
    public void createAndFormat() {
        // 1. 获取当前时间 (类比 JS: dayjs())
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Current local date-time: " + now);

        // 2. 指定特定的日期和时间
        LocalDate today = LocalDate.now();
        LocalTime lunchTime = LocalTime.of(12, 30, 0);
        LocalDateTime lunchToday = LocalDateTime.of(today, lunchTime);
        System.out.println("Lunch time today: " + lunchToday);

        // 3. 格式化输出 (类比 JS: .format('YYYY-MM-DD'))
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatted = now.format(formatter);
        System.out.println("Formatted now: " + formatted);

        // 4. 解析字符串
        LocalDateTime parsed = LocalDateTime.parse("2024-05-20 13:14:00", formatter);
        System.out.println("Parsed time: " + parsed);
    }

    /**
     * 演示日期的加减运算
     * 注意：所有的修改都会返回一个新的对象！
     */
    public void calculate() {
        LocalDate today = LocalDate.now();
        
        // 1. 三天后
        LocalDate threeDaysLater = today.plusDays(3);
        
        // 2. 一个月前
        LocalDate lastMonth = today.minusMonths(1);
        
        System.out.println("Today is " + today);
        System.out.println("Check: today is still the same: " + today); // 验证不可变性
        System.out.println("Modified dates: " + threeDaysLater + ", " + lastMonth);
        
        // 3. 计算日期间隔
        long diffDays = ChronoUnit.DAYS.between(lastMonth, threeDaysLater);
        System.out.println("Difference in days: " + diffDays);
    }

    public static void main(String[] args) {
        TimeDemo demo = new TimeDemo();
        
        System.out.println("--- Instant (UTC & Epoch) ---");
        demo.showInstant();

        System.out.println("\n--- Creation & Formatting ---");
        demo.createAndFormat();
        
        System.out.println("\n--- Calculations ---");
        demo.calculate();
    }
}
