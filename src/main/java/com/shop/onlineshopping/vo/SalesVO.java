package com.shop.onlineshopping.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesVO {
    private String productName;      // 商品名称
    private Integer totalSold;       // 卖了多少个
    private BigDecimal totalRevenue; // 一共赚了多少钱
}