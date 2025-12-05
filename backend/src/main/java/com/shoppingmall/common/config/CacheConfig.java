package com.shoppingmall.common.config;

import com.github.ben-manes.caffeine.cache.Cache;
import com.github.ben-manes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine缓存配置
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Configuration
public class CacheConfig {

    /**
     * 本地缓存配置
     */
    @Bean
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)                    // 最大缓存条目数
                .expireAfterWrite(10, TimeUnit.MINUTES) // 写入后10分钟过期
                .expireAfterAccess(5, TimeUnit.MINUTES)  // 访问后5分钟过期
                .recordStats()                          // 启用统计
                .build();
    }
}

