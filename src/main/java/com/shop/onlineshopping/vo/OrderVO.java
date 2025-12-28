package com.shop.onlineshopping.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    // 1. 订单原本的信息
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private Integer count;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;

    // 2. 商品的详细信息 (新增的)
    private String productName;
    private String productImage;
    private BigDecimal price; // 商品单价
}