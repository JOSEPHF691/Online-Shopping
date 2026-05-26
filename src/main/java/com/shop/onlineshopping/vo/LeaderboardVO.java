package com.shop.onlineshopping.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LeaderboardVO {
    private String productName;
    private String category;
    private Integer totalSold;
    private BigDecimal totalRevenue;
}
