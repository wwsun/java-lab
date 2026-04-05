package com.javalabs.controller;

import com.javalabs.entity.Task;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.model.Result;
import com.javalabs.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理 REST 控制器
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * GET /api/tasks - 获取所有任务
     */
    @GetMapping
    public Result<List<Task>> getAll() {
        return Result.success(taskService.getAllTasks());
    }

    /**
     * GET /api/tasks/{id} - 根据 ID 获取任务
     */
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 为 " + id + " 的任务"));
        return Result.success(task);
    }

    /**
     * GET /api/tasks/user/{userId} - 获取特定用户的所有任务
     */
    @GetMapping("/user/{userId}")
    public Result<List<Task>> getByUserId(@PathVariable Long userId) {
        return Result.success(taskService.getTasksByUserId(userId));
    }

    /**
     * POST /api/tasks - 新增任务
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Task> create(@Valid @RequestBody Task task) {
        Task saved = taskService.createTask(task);
        return Result.success(saved);
    }

    /**
     * PUT /api/tasks/{id} - 更新任务信息
     */
    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @Valid @RequestBody Task task) {
        Task updated = taskService.updateTask(id, task);
        return Result.success(updated);
    }

    /**
     * DELETE /api/tasks/{id} - 删除任务
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }
}
