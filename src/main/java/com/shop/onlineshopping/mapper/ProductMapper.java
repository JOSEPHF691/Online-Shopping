package com.shop.onlineshopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.onlineshopping.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}