package com.javalabs.service;

import com.javalabs.entity.BorrowRecord;
import java.util.List;

/**
 * 借阅管理业务接口
 */
public interface BorrowRecordService {
    /**
     * 借阅书籍 (关联减库存事务)
     *
     * @param userId 用户 ID
     * @param bookId 书籍 ID
     * @return 借阅记录
     */
    BorrowRecord borrowBook(Long userId, Long bookId);

    /**
     * 还书
     *
     * @param recordId 记录 ID
     * @return 更新后的记录
     */
    BorrowRecord returnBook(Long recordId);

    /**
     * 获取用户本人的借阅历史
     */
    List<BorrowRecord> getMyRecords(Long userId);
}
