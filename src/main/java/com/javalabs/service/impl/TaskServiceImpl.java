package com.javalabs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javalabs.entity.Task;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.mapper.TaskMapper;
import com.javalabs.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 任务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    @Override
    public List<Task> getAllTasks() {
        return taskMapper.selectList(null);
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        return Optional.ofNullable(taskMapper.selectById(id));
    }

    @Override
    public List<Task> getTasksByUserId(Long userId) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getUserId, userId);
        return taskMapper.selectList(queryWrapper);
    }

    @Override
    public Task createTask(Task task) {
        taskMapper.insert(task);
        log.info("🌟 已持久化新任务：{}，自动回填 ID：{}", task.getTitle(), task.getId());
        return task;
    }

    @Override
    public Task updateTask(Long id, Task task) {
        task.setId(id);
        int rows = taskMapper.updateById(task);
        if (rows > 0) {
            return task;
        }
        throw new ResourceNotFoundException("找不到 ID 为 " + id + " 的任务");
    }

    @Override
    public void deleteTask(Long id) {
        taskMapper.deleteById(id);
        log.info("🌟 已从数据库删除任务 ID：{}", id);
    }
}
