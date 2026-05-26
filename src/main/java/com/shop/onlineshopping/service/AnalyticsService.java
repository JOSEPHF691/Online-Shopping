package com.shop.onlineshopping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.entity.*;
import com.shop.onlineshopping.mapper.*;
import com.shop.onlineshopping.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private BrowseLogMapper browseLogMapper;

    // ==================== 用户画像 ====================

    public Map<String, List<UserProfileVO>> userProfiles() {
        Map<String, List<UserProfileVO>> profiles = new LinkedHashMap<>();

        // 1. 地域分布
        List<User> users = userMapper.selectList(null);
        Map<String, Long> regionMap = users.stream()
                .filter(u -> u.getRole() == 0)
                .collect(Collectors.groupingBy(
                        u -> u.getRegion() != null ? u.getRegion() : "未知",
                        Collectors.counting()));
        List<UserProfileVO> regions = regionMap.entrySet().stream()
                .map(e -> { UserProfileVO vo = new UserProfileVO(); vo.setLabel(e.getKey()); vo.setValue(e.getValue()); return vo; })
                .collect(Collectors.toList());
        profiles.put("地域分布", regions);

        // 2. 购买力分档
        List<Orders> allPaid = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>().ge(Orders::getStatus, 2));
        Map<Long, BigDecimal> userSpent = new HashMap<>();
        for (Orders o : allPaid) {
            userSpent.merge(o.getUserId(), o.getTotalAmount(), BigDecimal::add);
        }
        long high = userSpent.values().stream().filter(v -> v.compareTo(new BigDecimal("3000")) >= 0).count();
        long mid = userSpent.values().stream().filter(v -> v.compareTo(new BigDecimal("500")) >= 0 && v.compareTo(new BigDecimal("3000")) < 0).count();
        long low = userSpent.values().stream().filter(v -> v.compareTo(new BigDecimal("500")) < 0).count();

        List<UserProfileVO> powers = new ArrayList<>();
        UserProfileVO h = new UserProfileVO(); h.setLabel("高(≥¥3000)"); h.setValue(high); powers.add(h);
        UserProfileVO m = new UserProfileVO(); m.setLabel("中(¥500-3000)"); m.setValue(mid); powers.add(m);
        UserProfileVO l = new UserProfileVO(); l.setLabel("低(<¥500)"); l.setValue(low); powers.add(l);
        profiles.put("购买力分布", powers);

        // 3. 偏好分类
        Map<String, Long> catPref = new HashMap<>();
        for (Orders o : allPaid) {
            Product p = productMapper.selectById(o.getProductId());
            if (p != null && p.getCategory() != null) {
                catPref.merge(p.getCategory(), (long) o.getCount(), Long::sum);
            }
        }
        List<UserProfileVO> prefs = catPref.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> { UserProfileVO vo = new UserProfileVO(); vo.setLabel(e.getKey()); vo.setValue(e.getValue()); return vo; })
                .collect(Collectors.toList());
        profiles.put("偏好分类", prefs);

        return profiles;
    }

    // ==================== 销售趋势 ====================

    public Map<String, List<SalesTrendVO>> salesTrends(String period) {
        Map<String, List<SalesTrendVO>> trends = new LinkedHashMap<>();

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Orders::getStatus, 2);

        if ("daily".equals(period)) {
            wrapper.ge(Orders::getCreateTime, LocalDateTime.now().minusDays(7).withHour(0).withMinute(0));
            trends.put("近7天日趋势", buildDailyTrend(wrapper));
        } else if ("weekly".equals(period)) {
            wrapper.ge(Orders::getCreateTime, LocalDateTime.now().minusWeeks(4).with(DayOfWeek.MONDAY).withHour(0).withMinute(0));
            trends.put("近4周周趋势", buildWeeklyTrend(wrapper));
        } else {
            wrapper.ge(Orders::getCreateTime, LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0));
            trends.put("近6月月趋势", buildMonthlyTrend(wrapper));
        }

        return trends;
    }

    private List<SalesTrendVO> buildDailyTrend(LambdaQueryWrapper<Orders> baseWrapper) {
        List<Orders> orders = ordersMapper.selectList(baseWrapper);
        Map<String, SalesTrendVO> map = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            String key = LocalDate.now().minusDays(i).format(fmt);
            SalesTrendVO vo = new SalesTrendVO();
            vo.setDate(key);
            vo.setRevenue(BigDecimal.ZERO);
            vo.setQuantity(0);
            map.put(key, vo);
        }
        for (Orders o : orders) {
            String key = o.getCreateTime().toLocalDate().format(fmt);
            SalesTrendVO vo = map.get(key);
            if (vo != null) {
                vo.setRevenue(vo.getRevenue().add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO));
                vo.setQuantity(vo.getQuantity() + (o.getCount() != null ? o.getCount() : 0));
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<SalesTrendVO> buildWeeklyTrend(LambdaQueryWrapper<Orders> baseWrapper) {
        List<Orders> orders = ordersMapper.selectList(baseWrapper);
        Map<String, SalesTrendVO> map = new LinkedHashMap<>();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        for (int i = 3; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusWeeks(i);
            String key = "W" + d.get(wf.weekOfYear());
            SalesTrendVO vo = new SalesTrendVO();
            vo.setDate(key);
            vo.setRevenue(BigDecimal.ZERO);
            vo.setQuantity(0);
            map.put(key, vo);
        }
        for (Orders o : orders) {
            String key = "W" + o.getCreateTime().toLocalDate().get(wf.weekOfYear());
            SalesTrendVO vo = map.get(key);
            if (vo != null) {
                vo.setRevenue(vo.getRevenue().add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO));
                vo.setQuantity(vo.getQuantity() + (o.getCount() != null ? o.getCount() : 0));
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<SalesTrendVO> buildMonthlyTrend(LambdaQueryWrapper<Orders> baseWrapper) {
        List<Orders> orders = ordersMapper.selectList(baseWrapper);
        Map<String, SalesTrendVO> map = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            String key = LocalDate.now().minusMonths(i).format(fmt);
            SalesTrendVO vo = new SalesTrendVO();
            vo.setDate(key);
            vo.setRevenue(BigDecimal.ZERO);
            vo.setQuantity(0);
            map.put(key, vo);
        }
        for (Orders o : orders) {
            String key = o.getCreateTime().toLocalDate().format(fmt);
            SalesTrendVO vo = map.get(key);
            if (vo != null) {
                vo.setRevenue(vo.getRevenue().add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO));
                vo.setQuantity(vo.getQuantity() + (o.getCount() != null ? o.getCount() : 0));
            }
        }
        return new ArrayList<>(map.values());
    }

    // ==================== 异常检测 ====================

    public List<AnomalyVO> anomalyDetection() {
        List<AnomalyVO> anomalies = new ArrayList<>();

        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(7).atStartOfDay();

        for (Product p : products) {
            // 今天销量
            LambdaQueryWrapper<Orders> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.eq(Orders::getProductId, p.getId())
                    .ge(Orders::getStatus, 2)
                    .ge(Orders::getCreateTime, todayStart);
            List<Orders> todayOrders = ordersMapper.selectList(todayWrapper);
            long todayQty = todayOrders.stream().mapToLong(Orders::getCount).sum();

            // 前7天(不含今天)销量
            LambdaQueryWrapper<Orders> histWrapper = new LambdaQueryWrapper<>();
            histWrapper.eq(Orders::getProductId, p.getId())
                    .ge(Orders::getStatus, 2)
                    .ge(Orders::getCreateTime, sevenDaysAgo)
                    .lt(Orders::getCreateTime, todayStart);
            List<Orders> histOrders = ordersMapper.selectList(histWrapper);

            List<Long> dailyQtys = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate d = LocalDate.now().minusDays(i + 1);
                long dayQty = histOrders.stream()
                        .filter(o -> o.getCreateTime().toLocalDate().equals(d))
                        .mapToLong(Orders::getCount).sum();
                dailyQtys.add(dayQty);
            }

            double avg = dailyQtys.stream().mapToLong(Long::longValue).average().orElse(0);
            double variance = dailyQtys.stream()
                    .mapToDouble(q -> Math.pow(q - avg, 2))
                    .average().orElse(0);
            double stdDev = Math.sqrt(variance);

            String status = "正常";
            if (avg > 0.1 && todayQty > avg + 2 * Math.max(stdDev, 0.5)) {
                status = "异常骤增";
            } else if (avg > 0.1 && todayQty < avg - 2 * Math.max(stdDev, 0.5)) {
                status = "异常骤降";
            }

            if (!"正常".equals(status)) {
                AnomalyVO vo = new AnomalyVO();
                vo.setProductName(p.getName());
                vo.setTodayQty(todayQty);
                vo.setAvgQty(Math.round(avg * 100.0) / 100.0);
                vo.setAnomaly(status);
                anomalies.add(vo);
            }
        }

        return anomalies;
    }

    // ==================== 排行榜 ====================

    public Map<String, List<LeaderboardVO>> leaderboard(String period) {
        Map<String, List<LeaderboardVO>> boards = new LinkedHashMap<>();

        LocalDateTime since;
        switch (period) {
            case "daily": since = LocalDate.now().atStartOfDay(); break;
            case "weekly": since = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay(); break;
            case "monthly": since = LocalDate.now().withDayOfMonth(1).atStartOfDay(); break;
            default: since = LocalDate.now().minusYears(100).atStartOfDay();
        }

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Orders::getStatus, 2).ge(Orders::getCreateTime, since);
        List<Orders> orders = ordersMapper.selectList(wrapper);

        Map<Long, LeaderboardVO> agg = new LinkedHashMap<>();
        for (Orders o : orders) {
            LeaderboardVO vo = agg.computeIfAbsent(o.getProductId(), k -> {
                LeaderboardVO v = new LeaderboardVO();
                Product p = productMapper.selectById(o.getProductId());
                v.setProductName(p != null ? p.getName() : "未知");
                v.setCategory(p != null ? p.getCategory() : "-");
                v.setTotalSold(0);
                v.setTotalRevenue(BigDecimal.ZERO);
                return v;
            });
            vo.setTotalSold(vo.getTotalSold() + (o.getCount() != null ? o.getCount() : 0));
            vo.setTotalRevenue(vo.getTotalRevenue().add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO));
        }

        List<LeaderboardVO> list = agg.values().stream()
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .limit(20)
                .collect(Collectors.toList());
        boards.put("排行榜", list);
        return boards;
    }

    // ==================== 销售趋势预测 (简单移动平均) ====================

    public Map<String, List<SalesTrendVO>> salesPrediction(int forecastDays) {
        Map<String, List<SalesTrendVO>> result = new LinkedHashMap<>();

        // 获取近30天日销售数据
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Orders::getStatus, 2)
               .ge(Orders::getCreateTime, LocalDateTime.now().minusDays(30).withHour(0).withMinute(0));
        List<Orders> orders = ordersMapper.selectList(wrapper);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, BigDecimal[]> dailyMap = new LinkedHashMap<>();

        // 聚合每日销售额和销量
        for (int i = 29; i >= 0; i--) {
            String key = LocalDate.now().minusDays(i).format(fmt);
            dailyMap.put(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }
        for (Orders o : orders) {
            String key = o.getCreateTime().toLocalDate().format(fmt);
            BigDecimal[] arr = dailyMap.get(key);
            if (arr != null) {
                arr[0] = arr[0].add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
                arr[1] = arr[1].add(BigDecimal.valueOf(o.getCount() != null ? o.getCount() : 0));
            }
        }

        List<SalesTrendVO> history = dailyMap.entrySet().stream()
                .map(e -> {
                    SalesTrendVO vo = new SalesTrendVO();
                    vo.setDate(e.getKey());
                    vo.setRevenue(e.getValue()[0]);
                    vo.setQuantity(e.getValue()[1].intValue());
                    return vo;
                }).collect(java.util.stream.Collectors.toList());

        // 用最后7天的移动平均斜率做预测
        int window = 7;
        int dataSize = history.size();
        if (dataSize < window) {
            result.put("历史", history);
            result.put("预测", new ArrayList<>());
            return result;
        }

        List<SalesTrendVO> recentHistory = history.subList(dataSize - 14, dataSize);
        result.put("历史", recentHistory);

        // 计算近7天日均值和趋势斜率
        List<SalesTrendVO> recent7 = history.subList(dataSize - window, dataSize);
        double avgRevenue = recent7.stream().mapToDouble(v -> v.getRevenue().doubleValue()).average().orElse(0);
        double avgQty = recent7.stream().mapToDouble(SalesTrendVO::getQuantity).average().orElse(0);

        // 简单线性回归计算斜率 (用最近7天)
        double sumX = 0, sumYr = 0, sumYq = 0, sumX2 = 0;
        for (int i = 0; i < window; i++) {
            double x = i - (window - 1) / 2.0;
            sumX += x;
            sumYr += x * recent7.get(i).getRevenue().doubleValue();
            sumYq += x * recent7.get(i).getQuantity();
            sumX2 += x * x;
        }
        double slopeRevenue = sumX2 != 0 ? sumYr / sumX2 : 0;
        double slopeQty = sumX2 != 0 ? sumYq / sumX2 : 0;

        // 生成未来预测
        List<SalesTrendVO> forecast = new ArrayList<>();
        for (int i = 1; i <= forecastDays; i++) {
            SalesTrendVO vo = new SalesTrendVO();
            vo.setDate(LocalDate.now().plusDays(i).format(fmt));
            double rev = Math.max(0, avgRevenue + slopeRevenue * (window / 2.0 + i));
            int qty = Math.max(0, (int) Math.round(avgQty + slopeQty * (window / 2.0 + i)));
            vo.setRevenue(BigDecimal.valueOf(Math.round(rev * 100.0) / 100.0));
            vo.setQuantity(qty);
            forecast.add(vo);
        }
        result.put("预测", forecast);

        return result;
    }

    // ==================== 类别销售占比 ====================

    public List<UserProfileVO> categoryRevenue() {
        List<Product> products = productMapper.selectList(null);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Orders::getStatus, 2);
        List<Orders> orders = ordersMapper.selectList(wrapper);

        Map<String, BigDecimal> catRev = new LinkedHashMap<>();
        for (Orders o : orders) {
            Product p = products.stream().filter(pr -> pr.getId().equals(o.getProductId())).findFirst().orElse(null);
            String cat = p != null && p.getCategory() != null ? p.getCategory() : "未分类";
            catRev.merge(cat, o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO, BigDecimal::add);
        }

        return catRev.entrySet().stream()
                .map(e -> { UserProfileVO vo = new UserProfileVO(); vo.setLabel(e.getKey()); vo.setValue(e.getValue().longValue()); return vo; })
                .collect(Collectors.toList());
    }
}
