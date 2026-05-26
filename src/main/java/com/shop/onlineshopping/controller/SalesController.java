package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.entity.*;
import com.shop.onlineshopping.service.CategoryService;
import com.shop.onlineshopping.service.LogService;
import com.shop.onlineshopping.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LogService logService;

    // ==================== 仪表板 ====================

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(logService.getSalesDashboard());
    }

    // ==================== 商品管理 (查全部,含下架) ====================

    @GetMapping("/products")
    public Result<List<Product>> allProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(productService.search(
                keyword != null ? keyword : "", category != null ? category : ""));
    }

    @PostMapping("/product/toggle-status")
    public Result<Void> toggleStatus(@RequestBody Map<String, Long> body,
                                     HttpServletRequest request) {
        try {
            Long productId = body.get("productId");
            Product product = productService.getById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            product.setStatus(product.getStatus() == 1 ? 0 : 1);
            productService.update(product);
            logService.recordOperation(body.get("operatorId") != null ? body.get("operatorId") : 0L,
                    "Sales", "切换商品[" + product.getName() + "]状态为" + (product.getStatus() == 1 ? "上架" : "下架"),
                    getClientIp(request));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/product/update")
    public Result<Void> updateProduct(@RequestBody Product product,
                                      HttpServletRequest request) {
        try {
            productService.update(product);
            logService.recordOperation(0L, "Sales",
                    "修改商品[" + product.getId() + "]信息", getClientIp(request));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 日志查看 ====================

    @GetMapping("/logs/browse")
    public Result<List<BrowseLog>> browseLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(logService.listBrowseLogs(page, size));
    }

    @GetMapping("/logs/login")
    public Result<List<LoginLog>> loginLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(logService.listLoginLogs(page, size));
    }

    @GetMapping("/logs/operation")
    public Result<List<OperationLog>> operationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(logService.listOperationLogs(page, size));
    }

    // ==================== 分类管理 (Sales也可以管理分类) ====================

    @PostMapping("/category/add")
    public Result<Void> addCategory(@RequestBody Map<String, String> body,
                                    HttpServletRequest request) {
        try {
            categoryService.add(body.get("name"));
            logService.recordOperation(0L, "Sales",
                    "新增分类[" + body.get("name") + "]", getClientIp(request));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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
