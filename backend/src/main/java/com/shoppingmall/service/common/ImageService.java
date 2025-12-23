package com.shoppingmall.service.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 图片处理服务接口
 */
public interface ImageService {
    
    /**
     * 解压ZIP文件到临时目录
     * 
     * @param zipFile ZIP文件
     * @return 临时目录路径
     */
    String extractZipToTempDir(MultipartFile zipFile) throws Exception;
    
    /**
     * 查找主图
     * 
     * @param tempDir 临时目录
     * @param productCode 商品编码
     * @return 主图文件，未找到返回null
     */
    File findMainImage(String tempDir, String productCode);
    
    /**
     * 查找详情图
     * 
     * @param tempDir 临时目录
     * @param productCode 商品编码
     * @return 详情图文件列表
     */
    List<File> findDetailImages(String tempDir, String productCode);
    
    /**
     * 上传图片并返回URL
     * 
     * @param imageFile 图片文件
     * @return 图片URL
     */
    String uploadImage(File imageFile) throws Exception;
    
    /**
     * 清理临时目录
     * 
     * @param tempDir 临时目录路径
     */
    void cleanupTempDir(String tempDir);
}
