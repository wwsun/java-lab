package com.javalabs.mapper;

import com.javalabs.entity.Task;
import com.javalabs.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户映射器集成测试
 * 使用 @SpringBootTest 启动完整的 Spring 容器环境进行数据验证
 */
@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Test
    void testFullCrud() {
        // --- 1. Insert (增加) ---
        // Arrange
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPassword("hashed_password");

        // Act
        int rows = userMapper.insert(user);

        // Assert
        Assertions.assertEquals(1, rows);
        Assertions.assertNotNull(user.getId(), "插入后自增 ID 应当回填");
        Long userId = user.getId();

        // --- 2. Select (读取) ---
        // Act
        User selectedUser = userMapper.selectById(userId);

        // Assert
        Assertions.assertNotNull(selectedUser);
        Assertions.assertEquals("test_user", selectedUser.getUsername());

        // --- 3. Update (修改) ---
        // Arrange
        selectedUser.setEmail("new_email@example.com");

        // Act
        int updateRows = userMapper.updateById(selectedUser);

        // Assert
        Assertions.assertEquals(1, updateRows);
        User updatedUser = userMapper.selectById(userId);
        Assertions.assertEquals("new_email@example.com", updatedUser.getEmail());

        // --- 4. Delete (删除) ---
        // Act
        int deleteRows = userMapper.deleteById(userId);

        // Assert
        Assertions.assertEquals(1, deleteRows);
        User deletedUser = userMapper.selectById(userId);
        Assertions.assertNull(deletedUser, "查询已删除的数据应当返回 null");
    }

    @Test
    void testDeleteTasksByUsernameLike() {
        // 1. 准备用户和任务数据
        User user = new User();
        user.setUsername("delete_test_user");
        user.setEmail("delete_test@example.com");
        user.setPassword("pass");
        userMapper.insert(user);
        
        Task task = new Task();
        task.setUserId(user.getId());
        task.setTitle("Delete Me Task");
        task.setStatus("PENDING");
        taskMapper.insert(task);

        // 2. 执行模糊删除
        int deletedRows = userMapper.deleteTasksByUsernameLike("delete_test");
        
        // 3. 验证结果
        Assertions.assertEquals(1, deletedRows);
        Task deletedTask = taskMapper.selectById(task.getId());
        Assertions.assertNull(deletedTask, "任务应当已被模糊删除");
        
        // 清理环境：删除测试用户
        userMapper.deleteById(user.getId());
    }
}
