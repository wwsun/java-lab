package com.javalabs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javalabs.entity.Book;
import com.javalabs.entity.BorrowRecord;
import com.javalabs.dto.Result;
import com.javalabs.service.BookService;
import com.javalabs.service.BorrowRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
     * 分页查询书籍
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
     * 新增书籍 (仅限管理员)
     */
    @PostMapping
    public Result<Book> createBook(@RequestBody Book book, HttpServletRequest request) {
        // 从拦截器存入的 Request Attributes 中获取角色信息
        String role = (String) request.getAttribute("role");
        
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "Forbidden: Only ADMIN can add books");
        }
        
        return Result.success(bookService.createBook(book));
    }

    /**
     * 借阅书籍
     */
    @PostMapping("/{id}/borrow")
    public Result<BorrowRecord> borrowBook(@PathVariable Long id, HttpServletRequest request) {
        // 模拟从 Token 中解析出的当前用户 ID (实际项目中应从 Token 解析或数据库查询)
        // 这里为了演示，我们先假设一个当前登录的用户 ID 为 1
        // 注意：生产代码中应使用 SecurityContextHolder 获取当前用户
        Long currentUserId = 1L; 
        
        return Result.success(borrowRecordService.borrowBook(currentUserId, id));
    }
}
