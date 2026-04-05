package com.javalabs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表: users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 哈希后的密码
     */
    private String password;

    /**
     * 创建时间
     * 对应字段: created_at (由 application.yml 中的 map-underscore-to-camel-case 自动转换)
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 关联的任务列表
     * @TableField(exist = false) 表示该字段非数据库表字段，类比 Prisma 中的 relation
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<Task> tasks;
}
