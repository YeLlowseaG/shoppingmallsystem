package com.shoppingmall;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * B2B成人用品采购系统启动类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ShoppingMallApplication implements CommandLineRunner {

    private final Environment environment;

    public ShoppingMallApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(ShoppingMallApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String host = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        
        log.info("=================================================================");
        log.info("后端服务启动成功！Backend service started successfully!");
        log.info("=================================================================");
    }
}

