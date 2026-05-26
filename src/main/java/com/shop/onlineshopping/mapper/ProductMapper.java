package com.shop.onlineshopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.onlineshopping.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT COALESCE(SUM(price * stock), 0) FROM product WHERE status = 1")
    Long getTotalStockValue();
}
