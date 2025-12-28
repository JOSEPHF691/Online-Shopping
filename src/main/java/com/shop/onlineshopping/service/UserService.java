package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.UserDTO;
import com.shop.onlineshopping.entity.User;

public interface UserService {
    // 注册
    void register(UserDTO userDTO);

    // 登录
    User login(UserDTO userDTO);
}