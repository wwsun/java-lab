package com.javalabs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java 现代时间 API (java.time.*) 功能验证")
class TimeDemoTest {

    @Test
    @DisplayName("验证 Instant 的 UTC 属性与毫秒转换")
    void testInstantAndEpoch() {
        // 1. Arrange: 从特定的 ISO 字符串创建 Instant (类比 JS: new Date('...').toISOString())
        String iso = "2024-05-20T13:14:00Z";
        Instant instant = Instant.parse(iso);

        // 2. Act
        long millis = instant.toEpochMilli();

        // 3. Assert
        assertEquals(1716210840000L, millis, "ISO 时间转换毫秒数应一致");
        assertEquals(iso, instant.toString(), "toString 应该返回标准 ISO 8601 格式");
    }

    @Test
    @DisplayName("验证时间的创建、格式化分析与不可变性")
    void testTimeCreationAndImmutability() {
        // 1. Arrange: 定义一个特定的时间点
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        String inputStr = "2024/05/20 13:14";

        // 2. Act: 解析与属性获取
        LocalDateTime dt = LocalDateTime.parse(inputStr, formatter);
        
        // 修改测试
        LocalDateTime dt2 = dt.plusWeeks(1);

        // 3. Assert
        assertEquals(2024, dt.getYear());
        assertEquals(5, dt.getMonthValue());
        assertEquals(20, dt.getDayOfMonth());
        
        // 验证不可变性 (dt 应该保持不变)
        assertEquals(13, dt.getHour());
        assertEquals(27, dt2.getDayOfMonth(), "dt2 应该是一周后的日期 (20+7)");
    }

    @Test
    @DisplayName("验证跨月日期的间隔计算逻辑")
    void testDateDiffAcrossMonths() {
        // 1. Arrange: 模拟活动开始与结束时间
        LocalDate startDate = LocalDate.of(2024, 1, 30);
        LocalDate endDate = LocalDate.of(2024, 2, 2);

        // 2. Act: 计算天数差
        long days = ChronoUnit.DAYS.between(startDate, endDate);

        // 3. Assert: 验证各月不同的天数处理 (2024是闰年，2月有29天)
        assertEquals(3, days, "从 1月30日 到 2月2日 应该是 3 天");
    }

    @Test
    @DisplayName("验证日期边界情况：如 1月31日 加一个月会变成几号？")
    void testDateBoundaryPlusMonths() {
        // 1. Arrange: 1月31日
        LocalDate d1 = LocalDate.of(2024, 1, 31);

        // 2. Act: 加一个月
        LocalDate d2 = d1.plusMonths(1);

        // 3. Assert: Java 会智能地调整到该月的最后一天 (2月29日)
        assertEquals(2, d2.getMonthValue());
        assertEquals(29, d2.getDayOfMonth(), "由于2024是闰年，1月31日加一个月应变为 2月29日");
    }
}
