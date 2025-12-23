package com.shoppingmall.service.common.impl;

import com.shoppingmall.service.common.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 图片处理服务实现类
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    
    @Value("${upload.path:/tmp/uploads}")
    private String uploadBasePath;
    
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".gif"};
    
    @Override
    public String extractZipToTempDir(MultipartFile zipFile) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir") + "/product_import_" + UUID.randomUUID();
        File tempDirFile = new File(tempDir);
        
        if (!tempDirFile.mkdirs()) {
            throw new IOException("无法创建临时目录: " + tempDir);
        }
        
        File tempZipFile = new File(tempDir, "images.zip");
        zipFile.transferTo(tempZipFile);
        
        try (ZipFile zip = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                
                if (entry.isDirectory()) {
                    continue;
                }
                
                String fileName = new File(entry.getName()).getName();
                
                if (fileName.startsWith(".") || fileName.startsWith("__MACOSX")) {
                    continue;
                }
                
                File outputFile = new File(tempDir, fileName);
                
                try (InputStream in = zip.getInputStream(entry);
                     FileOutputStream out = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = in.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }
                }
            }
        }
        
        tempZipFile.delete();
        
        log.info("ZIP文件解压完成，临时目录: {}", tempDir);
        return tempDir;
    }
    
    @Override
    public File findMainImage(String tempDir, String productCode) {
        File dir = new File(tempDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        
        for (String ext : IMAGE_EXTENSIONS) {
            File file = new File(tempDir, productCode + ext);
            if (file.exists() && file.isFile()) {
                log.debug("找到主图: {}", file.getName());
                return file;
            }
        }
        
        return null;
    }
    
    @Override
    public List<File> findDetailImages(String tempDir, String productCode) {
        File dir = new File(tempDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }
        
        File[] files = dir.listFiles((d, name) -> {
            String lowerName = name.toLowerCase();
            boolean startsWithCode = lowerName.startsWith(productCode.toLowerCase() + "_");
            boolean isImage = Arrays.stream(IMAGE_EXTENSIONS)
                    .anyMatch(lowerName::endsWith);
            return startsWithCode && isImage;
        });
        
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        
        List<File> sortedFiles = Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
        
        log.debug("找到 {} 张详情图", sortedFiles.size());
        return sortedFiles;
    }
    
    @Override
    public String uploadImage(File imageFile) throws Exception {
        if (imageFile == null || !imageFile.exists()) {
            return null;
        }
        
        String fileName = UUID.randomUUID() + "_" + imageFile.getName();
        String datePath = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String targetDir = uploadBasePath + "/products/" + datePath;
        
        Path targetDirPath = Paths.get(targetDir);
        if (!Files.exists(targetDirPath)) {
            Files.createDirectories(targetDirPath);
        }
        
        Path targetFilePath = Paths.get(targetDir, fileName);
        Files.copy(imageFile.toPath(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        String imageUrl = "/uploads/products/" + datePath + "/" + fileName;
        
        log.debug("图片上传成功: {}", imageUrl);
        return imageUrl;
    }
    
    @Override
    public void cleanupTempDir(String tempDir) {
        if (tempDir == null || tempDir.isEmpty()) {
            return;
        }
        
        try {
            File dir = new File(tempDir);
            if (dir.exists()) {
                deleteDirectory(dir);
                log.info("临时目录已清理: {}", tempDir);
            }
        } catch (Exception e) {
            log.error("清理临时目录失败: {}", tempDir, e);
        }
    }
    
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
