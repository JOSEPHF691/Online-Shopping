package com.shop.onlineshopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.dto.OrderDTO;
import com.shop.onlineshopping.entity.Cart;
import com.shop.onlineshopping.entity.Orders;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.mapper.CartMapper;
import com.shop.onlineshopping.mapper.OrdersMapper;
import com.shop.onlineshopping.mapper.ProductMapper;
import com.shop.onlineshopping.mapper.UserMapper;
import com.shop.onlineshopping.service.EmailService;
import com.shop.onlineshopping.service.OrdersService;
import com.shop.onlineshopping.vo.OrderVO;
import com.shop.onlineshopping.vo.SalesVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    // ★★★ 1. 注入发邮件工具 (记得刷新Maven否则会爆红) ★★★
    @Autowired
    private EmailService emailService;

    // ★★★ 2. 注入用户Mapper (为了根据ID查用户的邮箱) ★★★
    @Autowired
    private UserMapper userMapper;

    // 1. 单个商品直接下单
    @Override
    public OrderVO createOrder(OrderDTO orderDTO) {
        Product product = productMapper.selectById(orderDTO.getProductId());
        if (product == null) {
            throw new RuntimeException("下单失败：商品不存在");
        }

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(orderDTO.getCount()));


        Orders orders = new Orders();
        orders.setOrderNo(UUID.randomUUID().toString());
        orders.setUserId(orderDTO.getUserId());
        orders.setProductId(orderDTO.getProductId());
        orders.setCount(orderDTO.getCount());
        orders.setTotalAmount(totalAmount);
        orders.setStatus(1); // 1:待付款
        orders.setCreateTime(LocalDateTime.now());

        ordersMapper.insert(orders);

        return convertToVO(orders, product);
    }

    // 2. 购物车结账
    @Override
    @Transactional // 开启事务
    public String checkout(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> cartList = cartMapper.selectList(wrapper);

        if (cartList == null || cartList.isEmpty()) {
            throw new RuntimeException("购物车是空的，不能结账！");
        }

        String orderNo = UUID.randomUUID().toString();

        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) continue;

            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(cart.getCount()));



            Orders order = new Orders();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setProductId(cart.getProductId());
            order.setCount(cart.getCount());
            order.setTotalAmount(totalAmount);
            order.setStatus(1); // 1:待付款
            order.setCreateTime(LocalDateTime.now());

            ordersMapper.insert(order);

            cartMapper.deleteById(cart.getId());
        }

        return orderNo;
    }

    // 3. 查看我的订单列表
    @Override
    public List<OrderVO> myOrders(Long userId) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        wrapper.orderByDesc(Orders::getCreateTime);

        List<Orders> ordersList = ordersMapper.selectList(wrapper);
        List<OrderVO> voList = new ArrayList<>();

        for (Orders order : ordersList) {
            Product product = productMapper.selectById(order.getProductId());
            voList.add(convertToVO(order, product));
        }

        return voList;
    }

    // 4. 支付订单
    @Override
    public void pay(String orderNo) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        List<Orders> orders = ordersMapper.selectList(wrapper);

        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("订单不存在");
        }

        for (Orders order : orders) {
            order.setStatus(2); // 2: 已支付
            ordersMapper.updateById(order);
        }
    }

    // 5. 管理员查看所有订单
    @Override
    public List<OrderVO> listAllOrders() {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Orders::getCreateTime);
        List<Orders> ordersList = ordersMapper.selectList(wrapper);

        List<OrderVO> voList = new ArrayList<>();
        for (Orders order : ordersList) {
            Product product = productMapper.selectById(order.getProductId());
            voList.add(convertToVO(order, product));
        }
        return voList;
    }

    // 6. 管理员发货 (带邮件通知功能)
    @Override
    public void ship(String orderNo) {
        // 第一步：根据订单号查出所有商品记录
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        List<Orders> orders = ordersMapper.selectList(wrapper);

        if (orders == null || orders.isEmpty()) {
            // 如果查不到订单，直接结束
            return;
        }

        // 第二步：循环把每一个商品的状态都改成 3 (已发货)
        for (Orders order : orders) {
            order.setStatus(3);
            ordersMapper.updateById(order);
        }

        // ★★★ 第三步：发邮件 (重点：写在循环外面，只发一次) ★★★

        // 1. 取出买家的 ID (因为是同一个订单号，取第一条数据的 userId 即可)
        Long userId = orders.get(0).getUserId();

        // 2. 去数据库查这个买家的详细信息 (主要是为了拿邮箱)
        User user = userMapper.selectById(userId);

        // 3. 检查用户是否存在，以及有没有填邮箱
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            // 4. 调用邮件服务发送
            // (注意：这里如果邮件发送失败，控制台会报错，但不影响发货状态的修改)
            emailService.sendShipmentEmail(user.getEmail(), orderNo);
        }
    }

    // 辅助方法：把 Entity 转成 VO
    private OrderVO convertToVO(Orders order, Product product) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImage(product.getDescription());
            vo.setPrice(product.getPrice());
        }
        return vo;
    }

    @Override
    public List<SalesVO> getSalesStats() {
        return ordersMapper.getSalesStats();
    }
}