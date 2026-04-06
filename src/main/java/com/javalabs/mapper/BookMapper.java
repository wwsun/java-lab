package com.javalabs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javalabs.entity.Book;
import org.apache.ibatis.annotations.Mapper;

/**
 * 书籍信息 Mapper 接口
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
