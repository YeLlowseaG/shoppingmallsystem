package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shoppingmall.dto.ProductImportDTO;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.dto.ProductSkuDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductCategory;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.repository.website.BrandRepository;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.service.product.ProductImportService;
import com.shoppingmall.service.common.ImageService;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.vo.ProductImportResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImportServiceImpl implements ProductImportService {
    
    private final ProductService productService;
    private final ProductSkuService skuService;
    private final ImageService imageService;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    
    @Override
    public ProductImportResultVO importProducts(MultipartFile csvFile, MultipartFile imageZip) throws Exception {
        ProductImportResultVO result = new ProductImportResultVO();
        String tempDir = null;
        Set<String> missingCategories = new LinkedHashSet<>();  // 收集缺失的分类

        try {
            // 1. 解压图片ZIP（如果有）
            if (imageZip != null && !imageZip.isEmpty()) {
                log.info("开始解压图片ZIP文件: {}", imageZip.getOriginalFilename());
                tempDir = imageService.extractZipToTempDir(imageZip);
            }

            // 2. 解析文件（CSV 或 Excel）
            String fileName = csvFile.getOriginalFilename();
            log.info("开始解析文件: {}", fileName);
            List<ProductImportDTO> importDataList;

            if (fileName != null && (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
                importDataList = parseExcel(csvFile);
            } else {
                importDataList = parseCSV(csvFile);
            }

            result.setTotalCount(importDataList.size());

            // 3. 按商品编码分组（一个商品可能有多个SKU行）
            Map<String, List<ProductImportDTO>> productGroups = groupByProductCode(importDataList);

            // 4. 逐个导入商品
            for (Map.Entry<String, List<ProductImportDTO>> entry : productGroups.entrySet()) {
                String productCode = entry.getKey();
                List<ProductImportDTO> rows = entry.getValue();

                try {
                    importSingleProduct(rows, tempDir, result, missingCategories);
                    result.incrementSuccess();
                } catch (Exception e) {
                    log.error("导入商品失败: {}", productCode, e);
                    result.addError(rows.get(0).getRowNumber(), productCode, e.getMessage());
                }
            }

            // 5. 汇总缺失的分类到警告信息
            if (!missingCategories.isEmpty()) {
                result.addWarning("以下分类在系统中不存在，建议先创建这些分类再导入：" + String.join("、", missingCategories));
            }

            log.info("商品导入完成，成功: {}, 失败: {}", result.getSuccessCount(), result.getFailCount());
            log.info("错误列表大小: {}, 错误详情: {}", result.getErrors().size(), result.getErrors());
            log.info("警告列表大小: {}, 警告详情: {}", result.getWarnings().size(), result.getWarnings());

        } finally {
            // 6. 清理临时目录
            if (tempDir != null) {
                imageService.cleanupTempDir(tempDir);
            }
        }

        return result;
    }
    
    private List<ProductImportDTO> parseCSV(MultipartFile csvFile) throws Exception {
        List<ProductImportDTO> dataList = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            int rowNum = 1; // 第1行是表头
            for (CSVRecord record : csvParser) {
                rowNum++;
                ProductImportDTO dto = new ProductImportDTO();
                dto.setRowNumber(rowNum);
                
                try {
                    dto.setProductCode(record.get("商品编码"));
                    dto.setBarcode(getStringOrNull(record, "条码"));
                    dto.setUnit(getStringOrNull(record, "计量单位"));
                    dto.setProductName(record.get("商品名称"));
                    dto.setCategoryName(record.get("分类名称"));
                    dto.setBrandName(getStringOrNull(record, "品牌名称"));
                    dto.setBasePrice(getBigDecimal(record, "基础价"));
                    dto.setSuggestedRetailPrice(getBigDecimal(record, "建议零售价"));
                    dto.setMarketRetailPrice(getBigDecimal(record, "市场零售价"));
                    dto.setWarningStock(getInteger(record, "预警库存"));
                    dto.setWeight(getBigDecimal(record, "重量(g)"));
                    dto.setDescription(getStringOrNull(record, "商品描述"));
                    // 批量导入商品默认为草稿状态，除非明确指定
                    String status = record.get("状态");
                    dto.setStatus(StringUtil.isBlank(status) ? "草稿" : status);
                    dto.setEnableSpec("是".equals(record.get("启用规格")));
                    dto.setSkuCode(getStringOrNull(record, "SKU编码"));
                    dto.setSpecCombination(getStringOrNull(record, "规格组合"));
                    dto.setSkuPrice(getBigDecimal(record, "SKU价格"));
                    dto.setSkuStock(getInteger(record, "SKU库存"));
                    dto.setSkuMemberPrice(getBigDecimal(record, "SKU会员价"));
                    dto.setEnableSkuMemberPrice("是".equals(getStringOrNull(record, "启用SKU会员价")));
                    
                    dataList.add(dto);
                } catch (Exception e) {
                    log.warn("解析CSV第{}行失败: {}", rowNum, e.getMessage());
                }
            }
        }
        
        log.info("CSV解析完成，共 {} 行数据", dataList.size());
        return dataList;
    }

    private List<ProductImportDTO> parseExcel(MultipartFile excelFile) throws Exception {
        List<ProductImportDTO> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 获取表头行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel文件格式错误：缺少表头");
            }

            // 解析数据行
            int rowCount = sheet.getLastRowNum();
            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                ProductImportDTO dto = new ProductImportDTO();
                dto.setRowNumber(i + 1);

                try {
                    dto.setProductCode(getCellValue(row, 0));
                    dto.setBarcode(getCellValue(row, 1));
                    dto.setUnit(getCellValue(row, 2));
                    dto.setProductName(getCellValue(row, 3));
                    dto.setCategoryName(getCellValue(row, 4));
                    dto.setBrandName(getCellValue(row, 5));
                    dto.setBasePrice(getBigDecimalFromCell(row, 6));
                    dto.setSuggestedRetailPrice(getBigDecimalFromCell(row, 7));
                    dto.setMarketRetailPrice(getBigDecimalFromCell(row, 8));
                    dto.setWarningStock(getIntegerFromCell(row, 9));
                    dto.setWeight(getBigDecimalFromCell(row, 10));
                    dto.setDescription(getCellValue(row, 11));
                    // 批量导入商品默认为草稿状态，除非明确指定
                    String status = getCellValue(row, 12);
                    dto.setStatus(StringUtil.isBlank(status) ? "草稿" : status);
                    dto.setEnableSpec("是".equals(getCellValue(row, 13)));
                    dto.setSkuCode(getCellValue(row, 14));
                    dto.setSpecCombination(getCellValue(row, 15));
                    dto.setSkuPrice(getBigDecimalFromCell(row, 16));
                    dto.setSkuStock(getIntegerFromCell(row, 17));
                    dto.setSkuMemberPrice(getBigDecimalFromCell(row, 18));
                    dto.setEnableSkuMemberPrice("是".equals(getCellValue(row, 19)));

                    dataList.add(dto);
                } catch (Exception e) {
                    log.warn("解析Excel第{}行失败: {}", i + 1, e.getMessage());
                }
            }
        }

        log.info("Excel解析完成，共 {} 行数据", dataList.size());
        return dataList;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue();
                return (value == null || value.trim().isEmpty()) ? null : value.trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private BigDecimal getBigDecimalFromCell(Row row, int cellIndex) {
        String value = getCellValue(row, cellIndex);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getIntegerFromCell(Row row, int cellIndex) {
        String value = getCellValue(row, cellIndex);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, List<ProductImportDTO>> groupByProductCode(List<ProductImportDTO> dataList) {
        Map<String, List<ProductImportDTO>> groups = new LinkedHashMap<>();
        for (ProductImportDTO dto : dataList) {
            groups.computeIfAbsent(dto.getProductCode(), k -> new ArrayList<>()).add(dto);
        }
        return groups;
    }
    
    private void importSingleProduct(List<ProductImportDTO> rows, String tempDir, ProductImportResultVO result, Set<String> missingCategories) throws Exception {
        ProductImportDTO firstRow = rows.get(0);
        String productCode = firstRow.getProductCode();

        // 1. 验证数据
        validateProductData(firstRow);

        // 2. 查找或创建分类
        Long categoryId = findOrCreateCategory(firstRow.getCategoryName());
        if (categoryId == null) {
            // 记录缺失的分类，并跳过该商品
            missingCategories.add(firstRow.getCategoryName());
            throw new RuntimeException("分类不存在: " + firstRow.getCategoryName());
        }
        
        // 3. 查找品牌（可选）
        Long brandId = null;
        if (firstRow.getBrandName() != null && !firstRow.getBrandName().trim().isEmpty()) {
            QueryWrapper<Brand> brandQuery = new QueryWrapper<>();
            brandQuery.eq("brand_name", firstRow.getBrandName());
            Brand brand = brandRepository.selectOne(brandQuery);
            if (brand != null) {
                brandId = brand.getId();
            }
        }
        
        // 4. 处理图片
        String mainImageUrl = null;
        List<String> detailImageUrls = new ArrayList<>();
        
        if (tempDir != null) {
            File mainImage = imageService.findMainImage(tempDir, productCode);
            if (mainImage != null) {
                mainImageUrl = imageService.uploadImage(mainImage);
            } else {
                result.addWarning("商品 " + productCode + " 缺少主图");
            }
            
            List<File> detailImages = imageService.findDetailImages(tempDir, productCode);
            for (File img : detailImages) {
                String url = imageService.uploadImage(img);
                if (url != null) {
                    detailImageUrls.add(url);
                }
            }
        }
        
        // 5. 检查商品是否已存在
        QueryWrapper<Product> productQuery = new QueryWrapper<>();
        productQuery.eq("product_code", productCode);
        Product existingProduct = productRepository.selectOne(productQuery);
        if (existingProduct != null) {
            throw new RuntimeException("商品编码已存在: " + productCode);
        }
        
        // 6. 创建商品
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductCode(productCode);
        productDTO.setBarcode(firstRow.getBarcode());
        productDTO.setUnit(firstRow.getUnit());
        productDTO.setProductName(firstRow.getProductName());
        productDTO.setCategoryId(categoryId);
        productDTO.setBrandId(brandId);
        productDTO.setBasePrice(firstRow.getBasePrice());
        productDTO.setSuggestedRetailPrice(firstRow.getSuggestedRetailPrice());
        productDTO.setMarketRetailPrice(firstRow.getMarketRetailPrice());
        productDTO.setWarningStock(firstRow.getWarningStock());
        productDTO.setWeight(firstRow.getWeight());
        productDTO.setDescription(firstRow.getDescription());
        productDTO.setStatus(firstRow.getStatus());
        productDTO.setMainImage(mainImageUrl != null ? mainImageUrl : "");
        productDTO.setImages(detailImageUrls.isEmpty() ? null : String.join(",", detailImageUrls));

        // 如果不启用规格，使用第一行的库存
        if (!firstRow.getEnableSpec()) {
            productDTO.setStock(firstRow.getSkuStock() != null ? firstRow.getSkuStock() : 0);
        } else {
            productDTO.setStock(0); // 启用规格时，总库存由SKU汇总
        }
        
        Long productId = productService.createProduct(productDTO);
        
        // 7. 创建SKU（如果启用规格）
        if (firstRow.getEnableSpec() && rows.size() > 0) {
            List<ProductSkuDTO> skuDTOs = new ArrayList<>();
            
            for (ProductImportDTO row : rows) {
                if (row.getSkuCode() != null && !row.getSkuCode().trim().isEmpty()) {
                    ProductSkuDTO skuDTO = new ProductSkuDTO();
                    skuDTO.setProductId(productId);
                    skuDTO.setSkuCode(row.getSkuCode());
                    skuDTO.setSpecCombination(convertSpecCombinationToJson(row.getSpecCombination()));
                    skuDTO.setPrice(row.getSkuPrice() != null ? row.getSkuPrice() : row.getBasePrice());
                    skuDTO.setStock(row.getSkuStock() != null ? row.getSkuStock() : 0);
                    skuDTO.setSuggestedRetailPrice(row.getSuggestedRetailPrice());
                    skuDTO.setMarketRetailPrice(row.getMarketRetailPrice());
                    skuDTO.setMemberPrice(row.getSkuMemberPrice());
                    skuDTO.setEnableMemberPrice(row.getEnableSkuMemberPrice() ? 1 : 0);
                    skuDTO.setWarningStock(row.getWarningStock());
                    skuDTO.setWeight(row.getWeight());
                    skuDTO.setStatus(1);
                    
                    skuDTOs.add(skuDTO);
                }
            }
            
            if (!skuDTOs.isEmpty()) {
                skuService.batchCreateSkus(skuDTOs);
            }
        }
        
        log.info("成功导入商品: {}", productCode);
    }
    
    private void validateProductData(ProductImportDTO dto) {
        if (dto.getProductCode() == null || dto.getProductCode().trim().isEmpty()) {
            throw new RuntimeException("商品编码不能为空");
        }
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new RuntimeException("商品名称不能为空");
        }
        if (dto.getCategoryName() == null || dto.getCategoryName().trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
        }
        if (dto.getBasePrice() == null || dto.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("基础价必须大于0");
        }
    }
    
    private Long findOrCreateCategory(String categoryName) {
        QueryWrapper<ProductCategory> categoryQuery = new QueryWrapper<>();
        categoryQuery.eq("category_name", categoryName);
        ProductCategory category = categoryRepository.selectOne(categoryQuery);
        return category != null ? category.getId() : null;
    }
    
    private String convertSpecCombinationToJson(String specCombination) {
        if (specCombination == null || specCombination.trim().isEmpty()) {
            return "{}";
        }
        
        // 格式: "颜色:红色;尺寸:M" -> {"颜色":"红色","尺寸":"M"}
        Map<String, String> specMap = new LinkedHashMap<>();
        String[] pairs = specCombination.split(";");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                specMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(specMap);
        } catch (Exception e) {
            log.error("转换规格组合失败: {}", specCombination, e);
            return "{}";
        }
    }
    
    private String getStringOrNull(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            return (value == null || value.trim().isEmpty()) ? null : value.trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    private BigDecimal getBigDecimal(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    private Integer getInteger(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
