package com.javalabs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务实体类
 * 对应数据库表: tasks
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tasks")
public class Task {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户 ID (外键)
     * 对应字段: user_id
     */
    @NotNull(message = "任务所属用户 ID 不能为空")
    private Long userId;

    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过 200 个字符")
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务状态: PENDING, DOING, DONE
     */
    @NotBlank(message = "任务状态不能为空")
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
