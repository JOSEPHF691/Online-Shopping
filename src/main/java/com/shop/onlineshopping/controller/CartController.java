package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.dto.OrderDTO;
import com.shop.onlineshopping.service.CartService;
import com.shop.onlineshopping.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 添加到购物车
    @PostMapping("/add")
    public Result add(@RequestBody OrderDTO cartDTO) {
        cartService.add(cartDTO);
        return Result.success();
    }

    // 查看购物车
    // 返回值类型变成了 Result<List<CartVO>>，这样前端就能看到商品名了
    @GetMapping("/list")
    public Result<List<CartVO>> list(@RequestParam Long userId) {
        List<CartVO> list = cartService.myCart(userId);
        return Result.success(list);
    }

    // 删除购物车项
    // 这里的 id 是购物车表中那一行记录的 id (不是商品id)
    @DeleteMapping("/remove")
    public Result remove(@RequestParam Long id) {
        cartService.remove(id);
        return Result.success();
    }

    // 购物车数量减 1
    @PostMapping("/decrease")
    public Result decrease(@RequestParam Long id) {
        cartService.decrease(id);
        return Result.success();
    }

}