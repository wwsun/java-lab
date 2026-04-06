package com.javalabs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javalabs.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工映射器接口 (继承 MyBatis-Plus BaseMapper)
 * 只要定义这个接口，就拥有了内置的所有 CRUD 方法
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
    // 这里不需要写 SQL，只需继承即可
}
