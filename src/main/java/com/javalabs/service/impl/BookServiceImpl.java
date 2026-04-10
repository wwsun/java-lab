package com.javalabs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javalabs.entity.Book;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.mapper.BookMapper;
import com.javalabs.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 书籍管理业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

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
    @Cacheable(value = "book", key = "#id", unless = "#result == null")
    public Optional<Book> getBookById(Long id) {
        log.info("🔍 缓存未命中或不适用，从 DB 查询书籍 ID: {}", id);
        return Optional.ofNullable(bookMapper.selectById(id));
    }

    @Override
    public Book createBook(Book book) {
        bookMapper.insert(book);
        log.info("📖 已新增书籍：{}", book.getTitle());
        return book;
    }

    @Override
    @CacheEvict(value = "book", key = "#id")
    public Book updateBook(Long id, Book book) {
        book.setId(id);
        if (bookMapper.updateById(book) > 0) {
            log.info("🔥 数据已更新，已清理缓存 key: book::{}", id);
            return book;
        }
        throw new ResourceNotFoundException("未找到 ID 为 " + id + " 的书籍");
    }

    @Override
    @CacheEvict(value = "book", key = "#id")
    public void deleteBook(Long id) {
        if (bookMapper.deleteById(id) > 0) {
            log.info("🗑️ 书籍 ID: {} 已删除，相关缓存已清理", id);
        }
    }
}
