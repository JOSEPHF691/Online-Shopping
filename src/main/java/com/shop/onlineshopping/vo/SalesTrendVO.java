package com.shop.onlineshopping.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesTrendVO {
    private String date;
    private BigDecimal revenue;
    private Integer quantity;
}
