package com.javalabs.service.impl;

import com.javalabs.entity.Book;
import com.javalabs.entity.BorrowRecord;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.mapper.BookMapper;
import com.javalabs.mapper.BorrowRecordMapper;
import com.javalabs.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 借阅管理业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;

    @Override
    @Transactional // 核心！确保减库存和增记录的原子性 (NodeJS 概念: ACID 事务)
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        // 1. 查询书籍详情并检查库存
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new ResourceNotFoundException("书籍不存在，ID: " + bookId);
        }
        
        if (book.getStock() <= 0) {
            throw new RuntimeException("库存不足，无法借阅: " + book.getTitle());
        }

        // 2. 减库存
        book.setStock(book.getStock() - 1);
        bookMapper.updateById(book);

        // 3. 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setStatus(0); // 借出中
        
        borrowRecordMapper.insert(record);
        
        log.info("📢 用户 ID: {} 借阅书籍: {}，已扣减库存", userId, book.getTitle());
        return record;
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new ResourceNotFoundException("借阅记录不存在，ID: " + recordId);
        }
        
        if (record.getStatus() == 1) {
            return record; // 已归还
        }

        // 1. 更新库存
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setStock(book.getStock() + 1);
            bookMapper.updateById(book);
        }

        // 2. 更新记录
        record.setReturnDate(LocalDate.now());
        record.setStatus(1); // 已归还
        borrowRecordMapper.updateById(record);
        
        log.info("📢 书籍归还：ID {}，库存已回填", record.getBookId());
        return record;
    }

    @Override
    public List<BorrowRecord> getMyRecords(Long userId) {
        return borrowRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .orderByDesc(BorrowRecord::getCreatedAt)
        );
    }
}
