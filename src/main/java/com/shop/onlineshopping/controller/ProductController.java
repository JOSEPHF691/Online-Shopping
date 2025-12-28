package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 用户接口：看列表
    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    // --- 下面是管理员接口 ---

    // 1. 新增商品
    @PostMapping("/add")
    public Result add(@RequestBody Product product) {
        productService.add(product);
        return Result.success("商品上架成功！");
    }

    // 2. 修改商品 (比如改价格、改库存)
    @PostMapping("/update")
    public Result update(@RequestBody Product product) {
        productService.update(product);
        return Result.success("商品信息修改成功！");
    }

    // 3. 删除商品
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Long id) {
        productService.delete(id);
        return Result.success("商品已删除！");
    }
}