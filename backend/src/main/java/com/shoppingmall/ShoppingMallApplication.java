package com.shoppingmall;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

/**
 * B2B成人用品采购系统启动类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration.class
})
@EnableScheduling // 启用定时任务
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
        String port = environment.getProperty("server.port", "8081");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        
        log.info("=================================================================");
        log.info("后端服务启动成功！Backend service started successfully!");
        log.info("=================================================================");
    }
}

