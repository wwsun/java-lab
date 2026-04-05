package com.javalabs.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javalabs.entity.Task;
import com.javalabs.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * N+1 查询问题 诊断与实战
 */
@SpringBootTest
@Transactional
class NPlusOneDemoTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        User user1 = new User();
        user1.setUsername("sun");
        user1.setEmail("sun@test.com");
        user1.setPassword("123");
        userMapper.insert(user1);

        User user2 = new User();
        user2.setUsername("moon");
        user2.setEmail("moon@test.com");
        user2.setPassword("123");
        userMapper.insert(user2);

        // 为每个用户创建多个任务
        for (int i = 1; i <= 3; i++) {
            Task task = new Task();
            task.setUserId(user1.getId());
            task.setTitle("Sun's Task " + i);
            taskMapper.insert(task);
        }

        for (int i = 1; i <= 2; i++) {
            Task task = new Task();
            task.setUserId(user2.getId());
            task.setTitle("Moon's Task " + i);
            taskMapper.insert(task);
        }
    }

    @Test
    @DisplayName("场景A：复现 N+1 问题 (通过循环查询)")
    void reproduceNPlusOneProblem() {
        System.out.println(">>> [场景A] 开始查询所有用户...");
        // 1. 发射第 1 条 SQL：查询所有用户 (1 次)
        List<User> users = userMapper.selectList(null);

        System.out.println(">>> [场景A] 循环查询每个用户的任务 (触发 N+1)...");
        for (User user : users) {
            // 2. 发射第 N 条 SQL：查询当前用户的任务 (N 次)
            List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getUserId, user.getId())
            );
            user.setTasks(tasks);
            System.out.println("用户: " + user.getUsername() + ", 任务数: " + tasks.size());
        }
        
        // 结论：如果用户有 100 个，这里会发射 1+100 条 SQL，数据库性能会迅速崩坏
        Assertions.assertTrue(users.size() >= 2);
    }

    @Test
    @DisplayName("场景B：解决 N+1 问题 (通过联表查询 - 1条 SQL 搞定)")
    void solveByJointQuery() {
        System.out.println(">>> [场景B] 开始单次联表查询...");
        
        // 执行自定义的联表查询方法
        // 只有 1 条 SQL：SELECT u.*, t.* FROM users u LEFT JOIN tasks t ON u.id = t.user_id
        List<User> users = userMapper.selectUserWithTasks();

        for (User user : users) {
            System.out.println("用户: " + user.getUsername() + ", 任务数: " + 
                (user.getTasks() != null ? user.getTasks().size() : 0));
        }

        // 验证数据正确性
        Assertions.assertFalse(users.isEmpty());
        for (User u : users) {
            Assertions.assertNotNull(u.getTasks(), "关联任务不应为空");
        }
    }
}
