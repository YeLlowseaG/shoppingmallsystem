package com.shoppingmall.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

    /**
     * 验证码缓存（5分钟过期）
     */
    @Bean("captchaCache")
    public Cache<String, String> captchaCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)                    // 最大缓存条目数
                .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后5分钟过期
                .build();
    }

    /**
     * 地区数据缓存（长期缓存，地区数据很少变化）
     * 使用较大的缓存容量和较长的过期时间
     */
    @Bean("regionCache")
    public Cache<String, Object> regionCache() {
        return Caffeine.newBuilder()
                .maximumSize(50000)                      // 最大缓存条目数（足够存储所有地区数据）
                .expireAfterWrite(24, TimeUnit.HOURS)   // 写入后24小时过期（地区数据很少变化）
                .expireAfterAccess(12, TimeUnit.HOURS)  // 访问后12小时过期
                .recordStats()                           // 启用统计
                .build();
    }

    /**
     * 地区树形结构缓存（完整树形数据）
     */
    @Bean("regionTreeCache")
    public Cache<String, Object> regionTreeCache() {
        return Caffeine.newBuilder()
                .maximumSize(10)                         // 只缓存几种树形结构（全部、按级别等）
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
    }
}
