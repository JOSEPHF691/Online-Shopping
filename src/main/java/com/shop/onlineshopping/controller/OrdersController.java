package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.dto.OrderDTO;
import com.shop.onlineshopping.service.OrdersService;
import com.shop.onlineshopping.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    // 1. 单个商品下单
    @PostMapping("/create")
    public Result<OrderVO> create(@RequestBody OrderDTO orderDTO) {
        try {
            OrderVO orderVO = ordersService.createOrder(orderDTO);
            return Result.success(orderVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 购物车一键结账
    @PostMapping("/checkout")
    public Result<String> checkout(@RequestParam Long userId) {
        try {
            String orderNo = ordersService.checkout(userId);
            return Result.success("下单成功，订单号：" + orderNo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 3. 查看我的订单列表
    @GetMapping("/list")
    public Result<List<OrderVO>> list(@RequestParam Long userId) {
        return Result.success(ordersService.myOrders(userId));
    }

    // 4. 支付接口
    @PostMapping("/pay")
    public Result pay(@RequestParam String orderNo) {
        try {
            ordersService.pay(orderNo);
            return Result.success("支付成功！");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 管理员：查看所有订单
    @GetMapping("/admin/list")
    public Result<List<OrderVO>> adminList() {
        return Result.success(ordersService.listAllOrders());
    }

    // 管理员：发货
    @PostMapping("/ship")
    public Result ship(@RequestParam String orderNo) {
        ordersService.ship(orderNo);
        return Result.success("发货成功！");
    }

    @GetMapping("/admin/stats")
    public Result<List<com.shop.onlineshopping.vo.SalesVO>> getStats() {
        return Result.success(ordersService.getSalesStats());
    }
}