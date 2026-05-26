package com.shop.onlineshopping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.entity.*;
import com.shop.onlineshopping.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {

    @Autowired
    private BrowseLogMapper browseLogMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    // ==================== 日志查询 ====================

    public List<BrowseLog> listBrowseLogs(int page, int size) {
        LambdaQueryWrapper<BrowseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BrowseLog::getCreateTime);
        wrapper.last("LIMIT " + offset(page, size) + "," + size);
        return browseLogMapper.selectList(wrapper);
    }

    public List<LoginLog> listLoginLogs(int page, int size) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LoginLog::getLoginTime);
        wrapper.last("LIMIT " + offset(page, size) + "," + size);
        return loginLogMapper.selectList(wrapper);
    }

    public List<OperationLog> listOperationLogs(int page, int size) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreateTime);
        wrapper.last("LIMIT " + offset(page, size) + "," + size);
        return operationLogMapper.selectList(wrapper);
    }

    public void recordOperation(Long operatorId, String operatorName, String operation, String ip) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperation(operation);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    // ==================== 销售仪表板数据 ====================

    public Map<String, Object> getSalesDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        long totalProducts = productMapper.selectCount(null);

        LambdaQueryWrapper<Product> lowStockWrapper = new LambdaQueryWrapper<>();
        lowStockWrapper.le(Product::getStock, 5).eq(Product::getStatus, 1);
        long lowStockCount = productMapper.selectCount(lowStockWrapper);

        LambdaQueryWrapper<Orders> pendingShipWrapper = new LambdaQueryWrapper<>();
        pendingShipWrapper.eq(Orders::getStatus, 2);
        long pendingShip = ordersMapper.selectCount(pendingShipWrapper);

        LambdaQueryWrapper<Orders> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Orders::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        long todayOrders = ordersMapper.selectCount(todayWrapper);

        dashboard.put("totalProducts", totalProducts);
        dashboard.put("lowStockCount", lowStockCount);
        dashboard.put("pendingShip", pendingShip);
        dashboard.put("todayOrders", todayOrders);
        return dashboard;
    }

    // ==================== Admin统计数据 ====================

    public List<User> listSalespersons() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 1);
        List<User> users = userMapper.selectList(wrapper);
        for (User u : users) {
            u.setPassword(null);
        }
        return users;
    }

    // ==================== Admin首页数据卡片 ====================

    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        Long totalRevenue = ordersMapper.getTotalRevenue();
        Long totalOrders = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>().ge(Orders::getStatus, 2));
        long totalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, 0));

        Long stockValue = productMapper.getTotalStockValue();

        dashboard.put("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        dashboard.put("totalOrders", totalOrders);
        dashboard.put("totalUsers", totalUsers);
        dashboard.put("stockValue", stockValue != null ? stockValue : 0);
        return dashboard;
    }

    // ==================== 工具方法 ====================

    private int offset(int page, int size) {
        return Math.max(0, (page - 1) * size);
    }
}
