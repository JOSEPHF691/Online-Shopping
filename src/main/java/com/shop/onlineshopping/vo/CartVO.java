package com.shop.onlineshopping.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartVO {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer count;
    private String productName;
    private String productImage; // 新增
    private BigDecimal price;
    private BigDecimal totalMoney;
}
