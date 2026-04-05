package com.javalabs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3 到 50 之间")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 哈希后的密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为 6 位")
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
