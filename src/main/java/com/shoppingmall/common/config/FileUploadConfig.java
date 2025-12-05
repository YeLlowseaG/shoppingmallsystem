package com.shoppingmall.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 文件上传根路径
     */
    private String path = "D:/uploads";

    /**
     * 单文件最大大小（字节）
     */
    private Long maxSize = 10 * 1024 * 1024L; // 10MB

    /**
     * 允许的文件类型（逗号分隔）
     */
    private String allowedTypes = "jpg,jpeg,png,gif,pdf";

    /**
     * 图片存储路径
     */
    private String imagePath = "D:/uploads/images";

    /**
     * 文档存储路径
     */
    private String documentPath = "D:/uploads/documents";

    /**
     * 获取图片存储路径
     */
    public String getImagePath() {
        return imagePath != null ? imagePath : path + "/images";
    }

    /**
     * 获取文档存储路径
     */
    public String getDocumentPath() {
        return documentPath != null ? documentPath : path + "/documents";
    }
}

