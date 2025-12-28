package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.OrderDTO;
import com.shop.onlineshopping.vo.OrderVO;
import com.shop.onlineshopping.vo.SalesVO;

import java.util.List;

public interface OrdersService {

    // 单个商品直接下单
    OrderVO createOrder(OrderDTO orderDTO);

    // 购物车一键结账 (返回生成的订单号)
    String checkout(Long userId);

    // 查看我的订单历史
    List<OrderVO> myOrders(Long userId);

    // 支付订单 (根据订单号)
    void pay(String orderNo);

    // 管理员：查看所有订单 (不分用户)
    List<OrderVO> listAllOrders();

    // 管理员：发货
    void ship(String orderNo);

    //获取销售报表
    List<SalesVO> getSalesStats();
}