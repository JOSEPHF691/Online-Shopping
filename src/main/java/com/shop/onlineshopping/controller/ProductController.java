package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    @GetMapping("/search")
    public Result<List<Product>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(productService.search(keyword, category));
    }

    @GetMapping("/detail")
    public Result<Product> detail(@RequestParam Long id,
                                  @RequestParam(required = false) Long userId,
                                  HttpServletRequest request) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (userId != null) {
            String ip = getClientIp(request);
            productService.recordBrowse(userId, id, product.getCategory(), ip);
        }
        return Result.success(product);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Product product) {
        productService.add(product);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        productService.delete(id);
        return Result.success();
    }

    @PostMapping("/browse-duration")
    public Result<Void> updateBrowseDuration(@RequestParam Long userId,
                                             @RequestParam Long productId,
                                             @RequestParam int durationSeconds) {
        productService.updateBrowseDuration(userId, productId, durationSeconds);
        return Result.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
