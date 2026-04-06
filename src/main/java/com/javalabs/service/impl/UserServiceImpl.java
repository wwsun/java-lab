package com.javalabs.service.impl;

import com.javalabs.entity.User;
import com.javalabs.exception.ResourceNotFoundException;
import com.javalabs.mapper.UserMapper;
import com.javalabs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> getUserByUsername(String username) {
        // Optional<User> 是一个包装容器，告诉调用者：我返回的可能为空，请务必显示处理这种情况
        // 如果 user 有值，它返回一个包含该对象的 Optional
        // 如果 user 是 null，返回 Optional.empty()，避免抛出 NullPointerException
        return Optional.ofNullable(userMapper.selectByUsername(username));
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    @Transactional
    public User createUser(User user) {
        // @Transactional 注解表示该方法是一个事务方法
        // 默认情况下，如果该方法抛出任何 RuntimeException，事务将自动回滚

        // 使用 BCrypt 加密密码后再存储 (NodeJS 类比: bcrypt.hash)
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
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
