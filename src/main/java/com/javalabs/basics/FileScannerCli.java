package com.javalabs.basics;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * 第一周终极实战：极速文件内容扫描器 (CLI Tool)
 * 利用 Java 21 虚拟线程极速扫描指定目录下的关键词。
 */
public class FileScannerCli {

    private final String keyword;
    private final Path rootPath;

    // 并发容器：存储 文件路径 -> 匹配次数
    private final ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();
    
    // 原子计数：统计总文件数与总匹配数
    private final AtomicLong totalFilesScanned = new AtomicLong(0);
    private final AtomicInteger totalMatchesFound = new AtomicInteger(0);

    public FileScannerCli(String path, String keyword) {
        this.rootPath = Paths.get(path);
        this.keyword = keyword.toLowerCase(); // 忽略大小写
    }

    /**
     * 执行扫描主逻辑
     */
    public void scan() throws IOException, InterruptedException {
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            System.err.printf("❌ 错误：路径 %s 不存在或不是目录。\n", rootPath);
            return;
        }

        System.out.printf("🚀 启动扫描... [目录: %s, 关键词: %s]\n", rootPath, keyword);
        Instant start = Instant.now();

        // 💡 核心：使用 Java 21 虚拟线程执行器
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            // 💡 使用 NIO Files.walk 递归获取所有文件
            try (Stream<Path> paths = Files.walk(rootPath)) {
                paths.filter(Files::isRegularFile)
                     .filter(this::isTextFile) // 仅扫描文本文件
                     .forEach(file -> executor.submit(() -> scanFile(file)));
            }
            
            // executor 在 try-with-resources 结束时会自动等待所有线程完成
        }

        Instant finish = Instant.now();
        printReport(Duration.between(start, finish).toMillis());
    }

    /**
     * 单个文件的扫描任务 (运行在虚拟线程中)
     */
    private void scanFile(Path file) {
        try {
            int count = 0;
            // 💡 逐行读取，对内存极致友好
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                if (line.toLowerCase().contains(keyword)) {
                    count++;
                }
            }

            if (count > 0) {
                results.put(file.toString(), count);
                totalMatchesFound.addAndGet(count);
            }
            totalFilesScanned.incrementAndGet();

        } catch (IOException e) {
            // 忽略读取失败的文件（如权限问题），保持系统健壮
            // System.err.printf("⚠️ 无法读取文件: %s\n", file);
        }
    }

    /**
     * 简单的文件类型判定（排除二进制、git 等）
     */
    private boolean isTextFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        // 排除常见的二进制目录与非文本后缀
        if (path.toString().contains(".git") || path.toString().contains("/target/")) return false;
        
        return name.endsWith(".java") || name.endsWith(".md") || name.endsWith(".xml") 
            || name.endsWith(".txt") || name.endsWith(".yml") || name.endsWith(".json");
    }

    /**
     * 输出精美报表
     */
    private void printReport(long timeMs) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 极速扫描报表");
        System.out.println("=".repeat(50));
        System.out.printf("⏱️ 总耗时:      %d ms\n", timeMs);
        System.out.printf("📁 扫描文件数:  %d\n", totalFilesScanned.get());
        System.out.printf("🔍 总匹配频次:  %d\n", totalMatchesFound.get());
        System.out.println("-".repeat(50));
        
        System.out.println("🔥 命中次数 Top 10 文件:");
        results.entrySet().stream()
               .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
               .limit(10)
               .forEach(entry -> {
                   String displayPath = entry.getKey().replace(rootPath.toString(), ".");
                   System.out.printf("[%d 次] %s\n", entry.getValue(), displayPath);
               });
        System.out.println("=".repeat(50));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.out.println("📖 用法: java FileScannerCli <路径> <关键词>");
            return;
        }

        new FileScannerCli(args[0], args[1]).scan();
    }
}
