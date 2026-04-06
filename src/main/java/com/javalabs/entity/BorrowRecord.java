package com.javalabs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 */
@Data
@TableName("borrow_records")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 书籍ID
     */
    private Long bookId;
    
    /**
     * 借书日期
     */
    private LocalDate borrowDate;
    
    /**
     * 归还日期
     */
    private LocalDate returnDate;
    
    /**
     * 状态: 0-借出, 1-已还
     */
    private Integer status;
    
    private LocalDateTime createTime;
}
