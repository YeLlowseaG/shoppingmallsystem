package com.shoppingmall.controller.common;

import com.shoppingmall.common.util.FileUploadUtil;
import com.shoppingmall.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Slf4j
@RestController
@RequestMapping("/api/common/upload")
public class FileUploadController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadUtil.uploadImage(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", filePath);
            result.put("fileName", file.getOriginalFilename());
            return Result.success("图片上传成功", result);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文档
     */
    @PostMapping("/document")
    public Result<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadUtil.uploadDocument(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", filePath);
            result.put("fileName", file.getOriginalFilename());
            return Result.success("文档上传成功", result);
        } catch (IOException e) {
            log.error("文档上传失败", e);
            return Result.error("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<?> deleteFile(@RequestParam("filePath") String filePath) {
        boolean deleted = fileUploadUtil.deleteFile(filePath);
        if (deleted) {
            return Result.success("文件删除成功");
        } else {
            return Result.error("文件删除失败");
        }
    }
}

