package com.shop.onlineshopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.onlineshopping.entity.Orders;
import com.shop.onlineshopping.vo.SalesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT p.name as productName, SUM(o.count) as totalSold, SUM(o.total_amount) as totalRevenue " +
            "FROM orders o " +
            "LEFT JOIN product p ON o.product_id = p.id " +
            "WHERE o.status >= 2 " +
            "GROUP BY o.product_id, p.name " +
            "ORDER BY totalRevenue DESC")
    List<SalesVO> getSalesStats();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status >= 2")
    Long getTotalRevenue();
}
