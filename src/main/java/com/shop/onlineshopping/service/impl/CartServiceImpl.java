package com.shop.onlineshopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.dto.OrderDTO;
import com.shop.onlineshopping.entity.Cart;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.mapper.CartMapper;
import com.shop.onlineshopping.mapper.ProductMapper;
import com.shop.onlineshopping.service.CartService;
import com.shop.onlineshopping.vo.CartVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void add(OrderDTO cartDTO) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, cartDTO.getUserId());
        wrapper.eq(Cart::getProductId, cartDTO.getProductId());

        Cart existsCart = cartMapper.selectOne(wrapper);

        if (existsCart != null) {
            existsCart.setCount(existsCart.getCount() + cartDTO.getCount());
            cartMapper.updateById(existsCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(cartDTO.getUserId());
            cart.setProductId(cartDTO.getProductId());
            cart.setCount(cartDTO.getCount());
            cart.setCreateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
    }

    @Override
    public List<CartVO> myCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> cartList = cartMapper.selectList(wrapper);

        List<CartVO> voList = new ArrayList<>();

        for (Cart cart : cartList) {
            CartVO vo = new CartVO();
            BeanUtils.copyProperties(cart, vo);

            Product product = productMapper.selectById(cart.getProductId());

            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getImage()); // 这里填充图片
                vo.setPrice(product.getPrice());

                BigDecimal total = product.getPrice().multiply(new BigDecimal(cart.getCount()));
                vo.setTotalMoney(total);
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public void remove(Long id) {
        cartMapper.deleteById(id);
    }

    @Override
    public void decrease(Long id) {
        Cart cart = cartMapper.selectById(id);
        if (cart != null) {
            if (cart.getCount() > 1) {
                cart.setCount(cart.getCount() - 1);
                cartMapper.updateById(cart);
            } else {
                cartMapper.deleteById(id);
            }
        }
    }
}
