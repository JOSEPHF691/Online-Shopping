package com.shop.onlineshopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.onlineshopping.entity.BrowseLog;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.mapper.BrowseLogMapper;
import com.shop.onlineshopping.mapper.ProductMapper;
import com.shop.onlineshopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BrowseLogMapper browseLogMapper;

    @Override
    public List<Product> list() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> search(String keyword, String category) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);

        if (category != null && !category.isEmpty() && !"全部".equals(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getName, keyword.trim());
        }
        wrapper.orderByDesc(Product::getPrice);
        return productMapper.selectList(wrapper);
    }

    @Override
    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public void add(Product product) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        productMapper.insert(product);
    }

    @Override
    public void update(Product product) {
        productMapper.updateById(product);
    }

    @Override
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public void recordBrowse(Long userId, Long productId, String category, String ip) {
        BrowseLog log = new BrowseLog();
        log.setUserId(userId);
        log.setProductId(productId);
        log.setCategory(category);
        log.setDurationSeconds(0);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        browseLogMapper.insert(log);
    }

    @Override
    public void updateBrowseDuration(Long userId, Long productId, int durationSeconds) {
        LambdaQueryWrapper<BrowseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseLog::getUserId, userId)
               .eq(BrowseLog::getProductId, productId)
               .orderByDesc(BrowseLog::getCreateTime)
               .last("LIMIT 1");
        BrowseLog log = browseLogMapper.selectOne(wrapper);
        if (log != null) {
            log.setDurationSeconds(durationSeconds);
            browseLogMapper.updateById(log);
        }
    }
}
