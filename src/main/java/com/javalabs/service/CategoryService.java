package com.javalabs.service;

import com.javalabs.entity.Category;
import java.util.List;

/**
 * 书籍分类业务接口
 */
public interface CategoryService {
    /**
     * 获取所有分类 (两层结构)
     */
    List<Category> getAllCategories();

    /**
     * 新增分类
     */
    Category createCategory(Category category);
}
