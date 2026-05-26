package com.shop.onlineshopping.service;

import com.shop.onlineshopping.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> list();

    List<Product> search(String keyword, String category);

    Product getById(Long id);

    void add(Product product);

    void update(Product product);

    void delete(Long id);

    void recordBrowse(Long userId, Long productId, String category, String ip);

    void updateBrowseDuration(Long userId, Long productId, int durationSeconds);
}
