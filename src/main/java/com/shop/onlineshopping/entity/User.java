package com.shop.onlineshopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    // @TableId: 告诉 Java 这是主键，AUTO 表示是自增的
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String name;
    private Integer role; // 0普通用户 1管理员
    private LocalDateTime createTime;
    private String email;
}