package com.javalabs.service;

import com.javalabs.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * 用户管理业务接口
 */
public interface UserService {
    List<User> getAllUsers();
    
    Optional<User> getUserById(Long id);
    
    User createUser(User user);
    
    User updateUser(Long id, User user);
    
    void deleteUser(Long id);
    
    /**
     * 根据用户名查询用户
     */
    Optional<User> getUserByUsername(String username);

    /**
     * 查询用户及其任务 (自定义复杂查询)
     */
    List<User> getUsersWithTasks();
}
