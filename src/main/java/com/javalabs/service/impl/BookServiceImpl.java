package com.javalabs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javalabs.entity.Book;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.mapper.BookMapper;
import com.javalabs.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 书籍管理业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_BOOK_KEY = "book:cache:";
    private static final long CACHE_TTL = 30; // 缓存过期时间（分钟）

    @Override
    public Page<Book> getBooksByPage(int current, int size, String title, Long categoryId) {
        Page<Book> page = new Page<>(current, size);
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        
        // 模糊搜索标题 (NodeJS 类比: LIKE %title%)
        if (StringUtils.hasText(title)) {
            queryWrapper.like(Book::getTitle, title);
        }
        
        // 分类过滤
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq(Book::getCategoryId, categoryId);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Book::getCreatedAt);
        
        return bookMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        String key = CACHE_BOOK_KEY + id;

        // 1. 尝试从 Redis 缓存获取
        Book cachedBook = (Book) redisTemplate.opsForValue().get(key);
        if (cachedBook != null) {
            log.info("🚀 缓存命中 (Cache Hit): {}", key);
            return Optional.of(cachedBook);
        }

        // 2. 缓存缺失，查询数据库
        log.info("🔍 缓存缺失 (Cache Miss)，从 DB 查询 ID: {}", id);
        Book book = bookMapper.selectById(id);

        // 3. 将结果回填至缓存
        if (book != null) {
            redisTemplate.opsForValue().set(key, book, CACHE_TTL, TimeUnit.MINUTES);
            log.info("📥 已回填缓存: {}", key);
        } else {
            // 特别提示：如果 DB 也没有，理论上可以缓存一个空值防止穿透，这里简单返回 Optional.empty()
            log.warn("⚠️ 数据库中不存在 ID 为 {} 的书籍", id);
        }

        return Optional.ofNullable(book);
    }

    @Override
    public Book createBook(Book book) {
        bookMapper.insert(book);
        log.info("📖 已新增书籍：{}", book.getTitle());
        return book;
    }

    @Override
    public Book updateBook(Long id, Book book) {
        book.setId(id);
        if (bookMapper.updateById(book) > 0) {
            // 写操作后删除缓存 (Cache Evict)
            redisTemplate.delete(CACHE_BOOK_KEY + id);
            log.info("🔥 数据已更新，已清理缓存 key: {}{}", CACHE_BOOK_KEY, id);
            return book;
        }
        throw new ResourceNotFoundException("未找到 ID 为 " + id + " 的书籍");
    }

    @Override
    public void deleteBook(Long id) {
        if (bookMapper.deleteById(id) > 0) {
            // 删除后同时清理缓存
            redisTemplate.delete(CACHE_BOOK_KEY + id);
            log.info("🗑️ 书籍 ID: {} 已删除，并清理相关缓存", id);
        }
    }
}
