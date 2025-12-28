package com.shop.onlineshopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.onlineshopping.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 继承 BaseMapper 后，你不需要写 SQL，
 * 自动拥有 insert, update, selectById 等几十种方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}