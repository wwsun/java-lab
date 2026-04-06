package com.javalabs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 演示控制器
 * 用于验证 Redis 配置是否正确，展示 StringRedisTemplate 和 RedisTemplate 的用法。
 */
@RestController
@RequestMapping("/api/redis")
public class RedisDemoController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试 StringRedisTemplate (简单的 Key-Value)
     * 对标 Node.js: await redis.set('msg', 'hello')
     */
    @GetMapping("/string/set")
    public String testStringSet(@RequestParam String key, @RequestParam String value) {
        stringRedisTemplate.opsForValue().set(key, value, 1, TimeUnit.HOURS);
        return "Set String success: " + key + "=" + value;
    }

    @GetMapping("/string/get")
    public String testStringGet(@RequestParam String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 测试 RedisTemplate (自动 JSON 序列化对象)
     * 对标 Node.js: await redis.set('user:1', JSON.stringify(user))
     */
    @PostMapping("/object/set")
    public Map<String, Object> testObjectSet(@RequestBody Map<String, Object> data) {
        String key = "test:obj:" + System.currentTimeMillis();
        // 直接存对象，RedisTemplate 会利用 Jackson 自动转为 JSON 字符串
        redisTemplate.opsForValue().set(key, data, 1, TimeUnit.HOURS);
        
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("data", data);
        result.put("message", "Object stored in Redis as JSON");
        return result;
    }

    @GetMapping("/object/get")
    public Object testObjectGet(@RequestParam String key) {
        // 取出的 JSON 字符串会自动反序列化为 Map 或具体对象
        return redisTemplate.opsForValue().get(key);
    }
}
