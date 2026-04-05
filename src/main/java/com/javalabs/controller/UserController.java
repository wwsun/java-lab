package com.javalabs.controller;

import com.javalabs.entity.User;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.model.Result;
import com.javalabs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理 REST 控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users - 获取所有用户
     */
    @GetMapping
    public Result<List<User>> getAll() {
        return Result.success(userService.getAllUsers());
    }

    /**
     * GET /api/users/{id} - 根据 ID 获取用户
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 为 " + id + " 的用户"));
        return Result.success(user);
    }

    /**
     * GET /api/users/with-tasks - 获取所有用户及其任务
     */
    @GetMapping("/with-tasks")
    public Result<List<User>> getUsersWithTasks() {
        return Result.success(userService.getUsersWithTasks());
    }

    /**
     * POST /api/users - 新增用户
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<User> create(@Valid @RequestBody User user) {
        User saved = userService.createUser(user);
        return Result.success(saved);
    }

    /**
     * PUT /api/users/{id} - 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return Result.success(updated);
    }

    /**
     * DELETE /api/users/{id} - 删除用户
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
