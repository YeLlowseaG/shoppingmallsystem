package com.shoppingmall.common.util;

import com.shoppingmall.common.config.FileUploadConfig;
import com.shoppingmall.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    /**
     * 上传图片文件
     *
     * @param file 文件
     * @return 文件相对路径
     * @throws IOException IO异常
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // 验证文件类型
        String extension = FileUtil.getFileExtension(file.getOriginalFilename());
        if (!FileUtil.isImageFile(extension)) {
            throw new BusinessException(400, "不支持的文件类型，仅支持图片文件");
        }

        // 验证文件大小
        if (!FileUtil.validateFileSize(file.getSize(), fileUploadConfig.getMaxSize())) {
            throw new BusinessException(400, "文件大小超过限制：" + FileUtil.formatFileSize(fileUploadConfig.getMaxSize()));
        }

        return uploadFile(file, fileUploadConfig.getImagePath());
    }

    /**
     * 上传文档文件
     *
     * @param file 文件
     * @return 文件相对路径
     * @throws IOException IO异常
     */
    public String uploadDocument(MultipartFile file) throws IOException {
        // 验证文件类型
        String extension = FileUtil.getFileExtension(file.getOriginalFilename());
        if (!FileUtil.isDocumentFile(extension)) {
            throw new BusinessException(400, "不支持的文件类型，仅支持文档文件");
        }

        // 验证文件大小
        if (!FileUtil.validateFileSize(file.getSize(), fileUploadConfig.getMaxSize())) {
            throw new BusinessException(400, "文件大小超过限制：" + FileUtil.formatFileSize(fileUploadConfig.getMaxSize()));
        }

        return uploadFile(file, fileUploadConfig.getDocumentPath());
    }

    /**
     * 上传文件（通用方法）
     *
     * @param file     文件
     * @param basePath 基础路径
     * @return 文件相对路径
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file, String basePath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.getFileExtension(originalFilename);
        String fileName = generateFileName(extension);

        // 生成日期目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileDir = basePath + "/" + dateDir;

        // 创建目录
        Path dirPath = Paths.get(fileDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            log.info("创建目录: {}", fileDir);
        }

        // 保存文件
        String filePath = fileDir + "/" + fileName;
        File destFile = new File(filePath);
        file.transferTo(destFile);

        log.info("文件上传成功: {}", filePath);

        // 返回相对路径（用于数据库存储）
        return "/uploads/" + (basePath.equals(fileUploadConfig.getImagePath()) ? "images" : "documents") + "/" + dateDir + "/" + fileName;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径（相对路径或绝对路径）
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        try {
            // 如果是相对路径，转换为绝对路径
            String absolutePath;
            if (filePath.startsWith("/uploads/")) {
                // 相对路径，转换为绝对路径
                absolutePath = fileUploadConfig.getPath() + filePath.replace("/uploads", "");
            } else {
                // 已经是绝对路径
                absolutePath = filePath;
            }

            File file = new File(absolutePath);
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", absolutePath);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 生成文件名
     *
     * @param extension 文件扩展名
     * @return 文件名
     */
    private String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }

    /**
     * 验证文件类型是否允许
     *
     * @param extension 文件扩展名
     * @return 是否允许
     */
    public boolean isAllowedType(String extension) {
        if (StringUtil.isBlank(extension)) {
            return false;
        }
        List<String> allowedTypes = Arrays.asList(fileUploadConfig.getAllowedTypes().split(","));
        return allowedTypes.contains(extension.toLowerCase());
    }
}

