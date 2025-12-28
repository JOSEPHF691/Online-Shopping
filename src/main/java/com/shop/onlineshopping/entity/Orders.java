package com.shop.onlineshopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;    // 订单号
    private Long userId;       // 用户ID
    private Long productId;    // 商品ID
    private Integer count;     // 购买数量
    private BigDecimal totalAmount; // 总金额
    private Integer status;    // 订单状态
    private LocalDateTime createTime;
}