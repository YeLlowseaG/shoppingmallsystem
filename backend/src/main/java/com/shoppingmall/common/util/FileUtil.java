package com.shoppingmall.common.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public class FileUtil {

    /**
     * 默认文件上传路径
     */
    private static final String DEFAULT_UPLOAD_PATH = "D:/uploads";

    /**
     * 图片文件扩展名
     */
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};

    /**
     * 文档文件扩展名
     */
    private static final String[] DOCUMENT_EXTENSIONS = {"pdf", "doc", "docx", "xls", "xlsx", "txt"};

    /**
     * 保存文件
     *
     * @param file     文件
     * @param basePath 基础路径
     * @return 文件相对路径
     * @throws IOException IO异常
     */
    public static String saveFile(MultipartFile file, String basePath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = generateFileName(extension);

        // 生成日期目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileDir = basePath + "/" + dateDir;

        // 创建目录
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        String filePath = fileDir + "/" + fileName;
        File destFile = new File(filePath);
        file.transferTo(destFile);

        // 返回相对路径
        return "/" + dateDir + "/" + fileName;
    }

    /**
     * 保存图片文件
     *
     * @param file     图片文件
     * @param basePath 基础路径
     * @return 文件相对路径
     * @throws IOException IO异常
     */
    public static String saveImage(MultipartFile file, String basePath) throws IOException {
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!isImageFile(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        return saveFile(file, basePath);
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return false;
        }

        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（小写）
     */
    public static String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 判断是否为图片文件
     *
     * @param extension 文件扩展名
     * @return 是否为图片文件
     */
    public static boolean isImageFile(String extension) {
        if (StrUtil.isBlank(extension)) {
            return false;
        }
        for (String ext : IMAGE_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为文档文件
     *
     * @param extension 文件扩展名
     * @return 是否为文档文件
     */
    public static boolean isDocumentFile(String extension) {
        if (StrUtil.isBlank(extension)) {
            return false;
        }
        for (String ext : DOCUMENT_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成文件名
     *
     * @param extension 文件扩展名
     * @return 文件名
     */
    private static String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }

    /**
     * 获取文件大小（格式化）
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2fGB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 验证文件大小
     *
     * @param fileSize 文件大小（字节）
     * @param maxSize  最大大小（字节）
     * @return 是否有效
     */
    public static boolean validateFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }
}

