package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.service.AnalyticsService;
import com.shop.onlineshopping.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/user-profiles")
    public Result<Map<String, List<UserProfileVO>>> userProfiles() {
        return Result.success(analyticsService.userProfiles());
    }

    @GetMapping("/sales-trends")
    public Result<Map<String, List<SalesTrendVO>>> salesTrends(
            @RequestParam(defaultValue = "daily") String period) {
        return Result.success(analyticsService.salesTrends(period));
    }

    @GetMapping("/anomalies")
    public Result<List<AnomalyVO>> anomalies() {
        return Result.success(analyticsService.anomalyDetection());
    }

    @GetMapping("/leaderboard")
    public Result<Map<String, List<LeaderboardVO>>> leaderboard(
            @RequestParam(defaultValue = "all") String period) {
        return Result.success(analyticsService.leaderboard(period));
    }

    @GetMapping("/category-revenue")
    public Result<List<UserProfileVO>> categoryRevenue() {
        return Result.success(analyticsService.categoryRevenue());
    }

    @GetMapping("/sales-prediction")
    public Result<Map<String, List<SalesTrendVO>>> salesPrediction(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(analyticsService.salesPrediction(days));
    }
}
