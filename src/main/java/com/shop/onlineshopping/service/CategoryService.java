package com.shop.onlineshopping.service;

import com.shop.onlineshopping.entity.Category;
import java.util.List;

public interface CategoryService {

    List<Category> listAll();

    void add(String name);

    void delete(Long id);
}
