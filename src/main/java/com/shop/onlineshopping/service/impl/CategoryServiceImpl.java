package com.shop.onlineshopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.entity.Category;
import com.shop.onlineshopping.mapper.CategoryMapper;
import com.shop.onlineshopping.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getCreateTime)
        );
    }

    @Override
    public void add(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
        }
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name.trim());
        if (categoryMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("分类已存在");
        }
        Category category = new Category();
        category.setName(name.trim());
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
    }

    @Override
    public void delete(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new RuntimeException("分类不存在");
        }
        categoryMapper.deleteById(id);
    }
}
