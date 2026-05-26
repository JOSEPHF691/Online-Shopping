package com.shop.onlineshopping.vo;

import lombok.Data;

@Data
public class AnomalyVO {
    private String productName;
    private Long todayQty;
    private Double avgQty;
    private String anomaly;
}
