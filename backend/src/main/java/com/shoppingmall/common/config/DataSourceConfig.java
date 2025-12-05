package com.shoppingmall.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置（手动配置Druid，确保使用Druid而不是HikariCP）
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
@Configuration
public class DataSourceConfig {

    /**
     * 配置Druid数据源
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }
}

