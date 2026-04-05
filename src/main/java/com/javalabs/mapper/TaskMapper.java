package com.javalabs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javalabs.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务表 Mapper 接口
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    // 基础 CRUD 由 MyBatis-Plus 提供
}
