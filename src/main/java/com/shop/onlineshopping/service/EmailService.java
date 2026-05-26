package com.shop.onlineshopping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendShipmentEmail(String to, String orderNo) {
        if (mailSender == null) {
            log.warn("JavaMailSender未配置，跳过发送邮件。订单号: {}", orderNo);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("CS黑市商城 - 订单发货通知");
            message.setText(String.format(
                    "亲爱的CS玩家，\n\n" +
                    "您的订单 [%s] 已发货！\n" +
                    "感谢您在CS黑市商城的购买，祝您游戏愉快！\n\n" +
                    "CS黑市商城",
                    orderNo));
            mailSender.send(message);
            log.info("发货邮件已发送至: {}, 订单号: {}", to, orderNo);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
        }
    }
}
