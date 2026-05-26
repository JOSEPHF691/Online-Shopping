package com.shop.onlineshopping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.entity.*;
import com.shop.onlineshopping.mapper.*;
import com.shop.onlineshopping.vo.RecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {

    private static final int TOP_N = 5;
    private static final int K_NEIGHBORS = 5;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BrowseLogMapper browseLogMapper;

    // ==================== 简单推荐 ====================

    public List<RecommendVO> alsoBought(Long productId, Long currentUserId) {
        // 找出购买过该商品的所有用户
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getProductId, productId).ge(Orders::getStatus, 2);
        List<Orders> orders = ordersMapper.selectList(wrapper);
        Set<Long> buyerIds = orders.stream()
                .filter(o -> !o.getUserId().equals(currentUserId))
                .map(Orders::getUserId)
                .collect(Collectors.toSet());

        if (buyerIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查找这些用户还买了什么
        LambdaQueryWrapper<Orders> coBuyWrapper = new LambdaQueryWrapper<>();
        coBuyWrapper.in(Orders::getUserId, buyerIds)
                .ne(Orders::getProductId, productId)
                .ge(Orders::getStatus, 2);
        List<Orders> coBuyOrders = ordersMapper.selectList(coBuyWrapper);

        // 聚合计数
        Map<Long, Long> productScore = new HashMap<>();
        for (Orders o : coBuyOrders) {
            productScore.merge(o.getProductId(), (long) o.getCount(), Long::sum);
        }

        return productScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(TOP_N)
                .map(e -> productToVO(productMapper.selectById(e.getKey()), "购买过此商品的用户也买了"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<RecommendVO> alsoViewed(Long productId, Long currentUserId) {
        LambdaQueryWrapper<BrowseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseLog::getProductId, productId)
                .ne(currentUserId != null, BrowseLog::getUserId, currentUserId);
        List<BrowseLog> logs = browseLogMapper.selectList(wrapper);
        Set<Long> viewerIds = logs.stream()
                .map(BrowseLog::getUserId)
                .collect(Collectors.toSet());

        if (viewerIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Orders> buyWrapper = new LambdaQueryWrapper<>();
        buyWrapper.in(Orders::getUserId, viewerIds)
                .ne(Orders::getProductId, productId)
                .ge(Orders::getStatus, 2);
        List<Orders> orders = ordersMapper.selectList(buyWrapper);

        Map<Long, Long> productScore = new HashMap<>();
        for (Orders o : orders) {
            productScore.merge(o.getProductId(), (long) o.getCount(), Long::sum);
        }

        return productScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(TOP_N)
                .map(e -> productToVO(productMapper.selectById(e.getKey()), "浏览过此商品的用户也买了"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== 协同过滤推荐 (UserCF) ====================

    public List<RecommendVO> collaborativeFilter(Long userId) {
        // 1. 获取所有付费订单
        List<Orders> allOrders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>().ge(Orders::getStatus, 2));
        if (allOrders.isEmpty()) {
            return alsoViewed(null, userId); // fallback
        }

        // 2. 构建用户-商品交互矩阵 (user -> product -> 购买次数)
        Set<Long> allUserIds = allOrders.stream().map(Orders::getUserId).collect(Collectors.toSet());
        Map<Long, Map<Long, Double>> userItemMatrix = new HashMap<>();
        for (Orders o : allOrders) {
            userItemMatrix.computeIfAbsent(o.getUserId(), k -> new HashMap<>())
                    .merge(o.getProductId(), (double) o.getCount(), Double::sum);
        }

        // 目标用户的向量
        Map<Long, Double> targetVector = userItemMatrix.getOrDefault(userId, new HashMap<>());
        if (targetVector.isEmpty()) {
            return alsoViewed(null, userId);
        }

        // 3. 计算余弦相似度
        Map<Long, Double> similarities = new HashMap<>();
        for (Long otherUserId : allUserIds) {
            if (otherUserId.equals(userId)) continue;
            Map<Long, Double> otherVector = userItemMatrix.get(otherUserId);
            double sim = cosineSimilarity(targetVector, otherVector);
            if (sim > 0) {
                similarities.put(otherUserId, sim);
            }
        }

        // 4. 取Top K最相似用户
        List<Long> neighbors = similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(K_NEIGHBORS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (neighbors.isEmpty()) {
            return alsoViewed(null, userId);
        }

        // 5. 聚合推荐商品
        Set<Long> userOwned = targetVector.keySet();
        Map<Long, Double> recommendScores = new HashMap<>();
        for (Long neighborId : neighbors) {
            Map<Long, Double> neighborVec = userItemMatrix.get(neighborId);
            double sim = similarities.get(neighborId);
            for (Map.Entry<Long, Double> entry : neighborVec.entrySet()) {
                Long productId = entry.getKey();
                if (userOwned.contains(productId)) continue;
                recommendScores.merge(productId, entry.getValue() * sim, Double::sum);
            }
        }

        return recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(TOP_N)
                .map(e -> productToVO(productMapper.selectById(e.getKey()), "与您相似的用户也购买了"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== 余弦相似度 ====================

    private double cosineSimilarity(Map<Long, Double> v1, Map<Long, Double> v2) {
        if (v1.isEmpty() || v2.isEmpty()) return 0;

        Set<Long> allKeys = new HashSet<>(v1.keySet());
        allKeys.addAll(v2.keySet());

        double dot = 0, norm1 = 0, norm2 = 0;
        for (Long key : allKeys) {
            double a = v1.getOrDefault(key, 0.0);
            double b = v2.getOrDefault(key, 0.0);
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denominator == 0 ? 0 : dot / denominator;
    }

    // ==================== 帮助方法 ====================

    private RecommendVO productToVO(Product p, String reason) {
        if (p == null || p.getStatus() == null || p.getStatus() == 0) return null;
        RecommendVO vo = new RecommendVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setCategory(p.getCategory());
        vo.setPrice(p.getPrice());
        vo.setImage(p.getImage());
        vo.setReason(reason);
        return vo;
    }
}
