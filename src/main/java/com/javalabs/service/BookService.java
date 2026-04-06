package com.javalabs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javalabs.entity.Book;
import java.util.Optional;

/**
 * 书籍管理业务接口
 */
public interface BookService {
    /**
     * 分页查询书籍列表
     */
    Page<Book> getBooksByPage(int current, int size, String title, Long categoryId);

    /**
     * 根据 ID 获取书籍详情
     */
    Optional<Book> getBookById(Long id);

    /**
     * 新增书籍
     */
    Book createBook(Book book);

    /**
     * 更新书籍信息
     */
    Book updateBook(Long id, Book book);

    /**
     * 删除书籍
     */
    void deleteBook(Long id);
}
