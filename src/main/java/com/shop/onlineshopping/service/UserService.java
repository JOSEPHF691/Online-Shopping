package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.UserDTO;
import com.shop.onlineshopping.entity.User;

import java.util.List;

public interface UserService {

    void register(UserDTO userDTO);

    User login(UserDTO userDTO, String ip);

    List<User> listAllUsers();

    void updateUserRole(Long userId, Integer newRole);

    void resetPassword(Long userId, String newPassword);

    void deleteUser(Long userId);
}
