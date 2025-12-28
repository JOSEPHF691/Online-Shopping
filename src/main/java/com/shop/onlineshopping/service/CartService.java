package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.OrderDTO;
import com.shop.onlineshopping.vo.CartVO;

import java.util.List;

public interface CartService {
    // 添加到购物车 (这里复用 OrderDTO，因为它刚好有 userId, productId, count)
    void add(OrderDTO cartDTO);

    // 查看我的购物车
    List<CartVO> myCart(Long userId);

    void remove(Long id);

    void decrease(Long id);
}