package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.entity.*;
import com.shop.onlineshopping.service.LogService;
import com.shop.onlineshopping.service.UserService;
import com.shop.onlineshopping.vo.SalesVO;
import com.shop.onlineshopping.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private OrdersService ordersService;

    // ==================== 仪表板 ====================

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(logService.getAdminDashboard());
    }

    // ==================== 销售人员管理 ====================

    @GetMapping("/salespersons")
    public Result<List<User>> listSalespersons() {
        return Result.success(logService.listSalespersons());
    }

    @PostMapping("/salesperson/add")
    public Result<Void> addSalesperson(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        try {
            User user = new User();
            user.setUsername(body.get("username"));
            user.setPassword(body.get("password"));
            user.setName(body.get("name"));
            user.setRole(1);
            user.setEmail(body.get("email"));
            com.shop.onlineshopping.dto.UserDTO dto = new com.shop.onlineshopping.dto.UserDTO();
            dto.setUsername(user.getUsername());
            dto.setPassword(user.getPassword());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            userService.register(dto);
            userService.updateUserRole(
                    userService.listAllUsers().stream()
                            .filter(u -> u.getUsername().equals(user.getUsername()))
                            .findFirst().orElseThrow().getId(),
                    1);
            logService.recordOperation(
                    Long.valueOf(body.getOrDefault("operatorId", "0")),
                    "Admin", "新增销售人员[" + user.getUsername() + "]",
                    getClientIp(request));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/salesperson/delete")
    public Result<Void> deleteSalesperson(@RequestParam Long userId,
                                          HttpServletRequest request) {
        try {
            userService.deleteUser(userId);
            logService.recordOperation(0L, "Admin",
                    "删除销售人员ID[" + userId + "]", getClientIp(request));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/salesperson/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body,
                                      HttpServletRequest request) {
        try {
            Long userId = Long.valueOf(body.get("userId"));
            String newPassword = body.get("newPassword");
            userService.resetPassword(userId, newPassword);
            logService.recordOperation(0L, "Admin",
                    "重置销售人员ID[" + userId + "]密码", getClientIp(request));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 销售统计 ====================

    @GetMapping("/stats/sales")
    public Result<List<SalesVO>> salesStats() {
        return Result.success(ordersService.getSalesStats());
    }

    // ==================== 订单管理(全部) ====================

    @GetMapping("/orders")
    public Result<List<com.shop.onlineshopping.vo.OrderVO>> allOrders() {
        return Result.success(ordersService.listAllOrders());
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

    // ==================== 用户管理(全角色) ====================

    @GetMapping("/users")
    public Result<List<User>> allUsers() {
        return Result.success(userService.listAllUsers());
    }

    @PostMapping("/user/update-role")
    public Result<Void> updateUserRole(@RequestBody Map<String, Long> body,
                                       HttpServletRequest request) {
        try {
            Long userId = body.get("userId");
            Integer newRole = body.get("newRole").intValue();
            userService.updateUserRole(userId, newRole);
            logService.recordOperation(0L, "Admin",
                    "修改用户ID[" + userId + "]角色为" + newRole, getClientIp(request));
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
