package com.javalabs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javalabs.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户表 Mapper 接口
 * 继承 BaseMapper 后，自动拥有增删改查的基本方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询所有用户及其关联的任务列表 (联表查询方案)
     * MyBatis 将通过 XML 中的 ResultMap 自动完成数据组装
     */
    List<User> selectUserWithTasks();

    /**
     * 根据用户名模糊匹配，删除关联的所有任务
     * @param username 用户名关键字
     * @return 删除的任务行数
     */
    int deleteTasksByUsernameLike(@Param("username") String username);

    /**
     * 精确根据用户名查找用户
     */
    default User selectByUsername(String username) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }
}
