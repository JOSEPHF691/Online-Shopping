package com.shop.onlineshopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.dto.UserDTO;
import com.shop.onlineshopping.entity.LoginLog;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.mapper.LoginLogMapper;
import com.shop.onlineshopping.mapper.UserMapper;
import com.shop.onlineshopping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Override
    public void register(UserDTO userDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("注册失败：用户名已存在");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setName(userDTO.getName());
        user.setRole(0);
        user.setCreateTime(LocalDateTime.now());
        user.setEmail(userDTO.getEmail());
        user.setRegion(userDTO.getRegion() != null ? userDTO.getRegion() : "未知");

        userMapper.insert(user);
    }

    @Override
    public User login(UserDTO userDTO, String ip) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("登录失败：账号不存在");
        }
        if (!user.getPassword().equals(userDTO.getPassword())) {
            throw new RuntimeException("登录失败：密码错误");
        }

        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        LoginLog log = new LoginLog();
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setIp(ip);
        log.setLoginTime(LocalDateTime.now());
        log.setRole(user.getRole());
        loginLogMapper.insert(log);

        user.setPassword(null);
        return user;
    }

    @Override
    public List<User> listAllUsers() {
        List<User> users = userMapper.selectList(null);
        for (User u : users) {
            u.setPassword(null);
        }
        return users;
    }

    @Override
    public void updateUserRole(Long userId, Integer newRole) {
        if (newRole == null || newRole < 0 || newRole > 2) {
            throw new RuntimeException("无效的角色值，必须为 0/1/2");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setRole(newRole);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(newPassword);
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getRole() == 2) {
            throw new RuntimeException("不能删除管理员账号");
        }
        userMapper.deleteById(userId);
    }
}
