package com.shop.onlineshopping.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecommendVO {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private String image;
    private String reason;
}
