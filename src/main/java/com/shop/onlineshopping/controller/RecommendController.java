package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.common.Result;
import com.shop.onlineshopping.service.RecommendService;
import com.shop.onlineshopping.vo.RecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @GetMapping("/also-bought")
    public Result<List<RecommendVO>> alsoBought(
            @RequestParam Long productId,
            @RequestParam(required = false) Long userId) {
        return Result.success(recommendService.alsoBought(productId, userId));
    }

    @GetMapping("/also-viewed")
    public Result<List<RecommendVO>> alsoViewed(
            @RequestParam Long productId,
            @RequestParam(required = false) Long userId) {
        return Result.success(recommendService.alsoViewed(productId, userId));
    }

    @GetMapping("/personal")
    public Result<List<RecommendVO>> personal(@RequestParam Long userId) {
        return Result.success(recommendService.collaborativeFilter(userId));
    }
}
