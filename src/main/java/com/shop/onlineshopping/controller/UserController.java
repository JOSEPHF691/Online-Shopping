package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.dto.UserDTO;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        try {
            userService.register(userDTO);
            return Result.success("注册成功！");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ↓↓↓↓↓ 新增的登录接口 ↓↓↓↓↓
    @PostMapping("/login")
    public Result<User> login(@RequestBody UserDTO userDTO) {
        try {
            User loginUser = userService.login(userDTO);
            return Result.success(loginUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}