package com.javalabs.service.impl;

import com.javalabs.entity.User;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.mapper.UserMapper;
import com.javalabs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public User createUser(User user) {
        userMapper.insert(user);
        log.info("🌟 已持久化新用户：{}，自动回填 ID：{}", user.getUsername(), user.getId());
        return user;
    }

    @Override
    public User updateUser(Long id, User user) {
        user.setId(id);
        int rows = userMapper.updateById(user);
        if (rows > 0) {
            return user;
        }
        throw new ResourceNotFoundException("找不到 ID 为 " + id + " 的用户");
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
        log.info("🌟 已从数据库删除用户 ID：{}", id);
    }

    @Override
    public List<User> getUsersWithTasks() {
        return userMapper.selectUserWithTasks();
    }
}
