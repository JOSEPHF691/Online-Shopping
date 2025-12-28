package com.shop.onlineshopping.dto;
import lombok.Data;

@Data
public class OrderDTO {
    private Long userId;    // 谁买的
    private Long productId; // 买哪个
    private Integer count;  // 买几个
}