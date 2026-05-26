package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.dto.UserDTO;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserDTO userDTO) {
        try {
            userService.register(userDTO);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        try {
            String ip = getClientIp(request);
            User loginUser = userService.login(userDTO, ip);
            return Result.success(loginUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    public Result<List<User>> adminListUsers() {
        try {
            return Result.success(userService.listAllUsers());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/admin/update-role")
    public Result<Void> adminUpdateRole(@RequestBody Map<String, Long> body) {
        try {
            Long userId = body.get("userId");
            Integer newRole = body.get("newRole").intValue();
            userService.updateUserRole(userId, newRole);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/admin/reset-password")
    public Result<Void> adminResetPassword(@RequestBody Map<String, String> body) {
        try {
            Long userId = Long.valueOf(body.get("userId"));
            String newPassword = body.get("newPassword");
            userService.resetPassword(userId, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/admin/delete")
    public Result<Void> adminDeleteUser(@RequestParam Long userId) {
        try {
            userService.deleteUser(userId);
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
