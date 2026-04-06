package com.javalabs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 书籍信息实体类
 */
@Data
@TableName("books")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 书名
     */
    private String title;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * ISBN 编号
     */
    private String isbn;
    
    /**
     * 分类ID (逻辑外键)
     */
    private Long categoryId;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 书籍简介
     */
    private String description;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
