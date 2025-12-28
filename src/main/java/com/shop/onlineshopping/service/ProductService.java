package com.shop.onlineshopping.service;

import com.shop.onlineshopping.entity.Product;
import java.util.List;

public interface ProductService {
    // 1. 查询所有上架商品 (给用户看的)
    List<Product> list();

    // 2. 新增商品 (管理员用)
    void add(Product product);

    // 3. 修改商品 (管理员用)
    void update(Product product);

    // 4. 删除商品 (管理员用)
    void delete(Long id);
}