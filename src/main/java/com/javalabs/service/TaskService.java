package com.javalabs.service;

import com.javalabs.entity.Task;
import java.util.List;
import java.util.Optional;

/**
 * 任务管理业务接口
 */
public interface TaskService {
    List<Task> getAllTasks();
    
    Optional<Task> getTaskById(Long id);
    
    List<Task> getTasksByUserId(Long userId);
    
    Task createTask(Task task);
    
    Task updateTask(Long id, Task task);
    
    void deleteTask(Long id);
}
