package com.shop.onlineshopping.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // 🚫 临时禁用邮件功能（避免 JavaMailSender 注入失败）
    public void sendShipmentEmail(String to, String orderNo) {
        // 什么都不做，直接返回
        return;
    }
}
