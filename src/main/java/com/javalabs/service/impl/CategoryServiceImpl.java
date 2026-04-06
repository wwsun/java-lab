package com.javalabs.service.impl;

import com.javalabs.entity.Category;
import com.javalabs.mapper.CategoryMapper;
import com.javalabs.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> getAllCategories() {
        // 由于暂不涉及无限极递归，直接返回平铺列表
        // 前端或业务层可根据 parentId 过滤
        return categoryMapper.selectList(null);
    }

    @Override
    public Category createCategory(Category category) {
        categoryMapper.insert(category);
        return category;
    }
}
