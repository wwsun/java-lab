package com.javalabs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javalabs.entity.Book;
import com.javalabs.entity.BorrowRecord;
import com.javalabs.dto.Result;
import com.javalabs.service.BookService;
import com.javalabs.service.BorrowRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 书籍业务控制器
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BorrowRecordService borrowRecordService;

    /**
     * 分页查询书籍 (所有人可访问，但需登录)
     */
    @GetMapping
    public Result<Page<Book>> listBooks(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId) {
        
        return Result.success(bookService.getBooksByPage(current, size, title, categoryId));
    }

    /**
     * 新增书籍 (仅限 ADMIN 访问)
     * 使用 @PreAuthorize 注解自动拦截，不再需要手动检查 request 属性
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Book> createBook(@RequestBody Book book) {
        return Result.success(bookService.createBook(book));
    }

    /**
     * 删除书籍 (仅限 ADMIN 访问)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success(null);
    }

    /**
     * 借阅书籍 (所有人需登录)
     */
    @PostMapping("/{id}/borrow")
    public Result<BorrowRecord> borrowBook(@PathVariable Long id) {
        // 模拟从 Token 中解析出的当前用户 ID
        // 注意：后续我们会演示如何通过 SecurityContextHolder 获取真实 ID
        Long currentUserId = 1L; 
        
        return Result.success(borrowRecordService.borrowBook(currentUserId, id));
    }
}
