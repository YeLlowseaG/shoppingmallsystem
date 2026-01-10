package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shoppingmall.dto.ProductImportDTO;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.dto.ProductSkuDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductCategory;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.entity.ShippingTemplate;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.repository.website.BrandRepository;
import com.shoppingmall.repository.logistics.ShippingTemplateRepository;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.service.product.ProductImportService;
import com.shoppingmall.service.common.ImageService;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.dto.ProductMemberPriceDTO;
import com.shoppingmall.dto.ProductSkuMemberPriceDTO;
import com.shoppingmall.vo.ProductImportResultVO;
import com.shoppingmall.vo.MemberLevelVO;
import java.util.Set;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImportServiceImpl implements ProductImportService {
    
    /**
     * 单次最大导入商品数量
     */
    private static final int MAX_IMPORT_COUNT = 200;
    
    private final ProductService productService;
    private final ProductSkuService skuService;
    private final ImageService imageService;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ShippingTemplateRepository shippingTemplateRepository;
    private final MemberLevelService memberLevelService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 会员等级名称到ID的映射缓存
    private Map<String, Long> memberLevelNameToIdMap = null;
    
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

            // 3. 检查导入数量限制
            int totalCount = importDataList.size();
            if (totalCount > MAX_IMPORT_COUNT) {
                throw new RuntimeException("单次导入商品数量不能超过 " + MAX_IMPORT_COUNT + " 条，当前数量: " + totalCount + " 条，请分批导入");
            }

            result.setTotalCount(totalCount);
            
            // 4. 验证会员等级名称
            try {
                validateMemberLevelNames(importDataList);
            } catch (Exception e) {
                throw new RuntimeException("会员等级名称验证失败: " + e.getMessage(), e);
            }

            // 5. 按商品编码分组（一个商品可能有多个SKU行）
            Map<String, List<ProductImportDTO>> productGroups = groupByProductCode(importDataList);

            // 6. 逐个导入商品
            for (Map.Entry<String, List<ProductImportDTO>> entry : productGroups.entrySet()) {
                String productCode = entry.getKey();
                List<ProductImportDTO> rows = entry.getValue();

                try {
                    importSingleProduct(rows, tempDir, result, missingCategories);
                    result.incrementSuccess();
                } catch (Exception e) {
                    log.error("导入商品失败: {}", productCode, e);
                    String errorMessage = getErrorMessage(e, productCode);
                    result.addError(rows.get(0).getRowNumber(), productCode, errorMessage);
                }
            }

            // 7. 汇总缺失的分类到警告信息
            if (!missingCategories.isEmpty()) {
                result.addWarning("以下分类在系统中不存在，建议先创建这些分类再导入：" + String.join("、", missingCategories));
            }

            log.info("商品导入完成，成功: {}, 失败: {}", result.getSuccessCount(), result.getFailCount());
            log.info("错误列表大小: {}, 错误详情: {}", result.getErrors().size(), result.getErrors());
            log.info("警告列表大小: {}, 警告详情: {}", result.getWarnings().size(), result.getWarnings());

        } finally {
            // 8. 清理临时目录
            if (tempDir != null) {
                imageService.cleanupTempDir(tempDir);
            }
        }

        return result;
    }
    
    private List<ProductImportDTO> parseCSV(MultipartFile csvFile) throws Exception {
        List<ProductImportDTO> dataList = new ArrayList<>();
        
        // 初始化会员等级映射
        initMemberLevelMap();
        List<MemberLevelVO> memberLevels = memberLevelService.getAllEnabledMemberLevels();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            // 获取表头
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            
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
                    dto.setShippingTemplateId(getLong(record, "运费模板ID"));
                    dto.setBasePrice(getBigDecimal(record, "基础价"));
                    dto.setSuggestedRetailPrice(getBigDecimal(record, "建议零售价"));
                    dto.setMarketRetailPrice(getBigDecimal(record, "市场零售价"));
                    dto.setWarningStock(getInteger(record, "预警库存"));
                    dto.setWeight(getBigDecimal(record, "重量(g)"));
                    dto.setDescription(getStringOrNull(record, "商品描述"));
                    // 批量导入商品默认为草稿状态，除非明确指定
                    String status = record.get("状态");
                    dto.setStatus(StringUtil.isBlank(status) ? "草稿" : status);
                    
                    // 解析启用会员价
                    String enableMemberPriceStr = getStringOrNull(record, "启用会员价");
                    dto.setEnableMemberPrice("是".equals(enableMemberPriceStr));
                    
                    // 解析商品会员价（每个等级一列）
                    Map<String, BigDecimal> productMemberPrices = new HashMap<>();
                    for (MemberLevelVO level : memberLevels) {
                        String headerName = "商品会员价-" + level.getLevelName();
                        if (headerMap.containsKey(headerName)) {
                            BigDecimal price = getBigDecimal(record, headerName);
                            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                                productMemberPrices.put(level.getLevelName(), price);
                            }
                        }
                    }
                    dto.setProductMemberPrices(productMemberPrices);
                    
                    dto.setEnableSpec("是".equals(record.get("启用规格")));
                    dto.setSkuCode(getStringOrNull(record, "SKU编码"));
                    dto.setSpecCombination(getStringOrNull(record, "规格组合"));
                    dto.setSkuPrice(getBigDecimal(record, "SKU价格"));
                    dto.setSkuStock(getInteger(record, "SKU库存"));
                    
                    // 解析启用SKU会员价
                    String enableSkuMemberPriceStr = getStringOrNull(record, "启用SKU会员价");
                    dto.setEnableSkuMemberPrice("是".equals(enableSkuMemberPriceStr));
                    
                    // 解析SKU会员价（每个等级一列）
                    Map<String, BigDecimal> skuMemberPrices = new HashMap<>();
                    for (MemberLevelVO level : memberLevels) {
                        String headerName = "SKU会员价-" + level.getLevelName();
                        if (headerMap.containsKey(headerName)) {
                            BigDecimal price = getBigDecimal(record, headerName);
                            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                                skuMemberPrices.put(level.getLevelName(), price);
                            }
                        }
                    }
                    dto.setSkuMemberPrices(skuMemberPrices);
                    
                    // 兼容旧模板：如果存在旧的"SKU会员价"列（单个价格），也解析
                    if (headerMap.containsKey("SKU会员价")) {
                        BigDecimal oldSkuMemberPrice = getBigDecimal(record, "SKU会员价");
                        if (oldSkuMemberPrice != null && oldSkuMemberPrice.compareTo(BigDecimal.ZERO) > 0) {
                            // 如果新格式没有数据，使用旧格式的数据（需要指定一个默认等级，这里使用第一个等级）
                            if (skuMemberPrices.isEmpty() && !memberLevels.isEmpty()) {
                                skuMemberPrices.put(memberLevels.get(0).getLevelName(), oldSkuMemberPrice);
                                dto.setSkuMemberPrices(skuMemberPrices);
                            }
                        }
                    }
                    
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
        
        // 初始化会员等级映射
        initMemberLevelMap();
        List<MemberLevelVO> memberLevels = memberLevelService.getAllEnabledMemberLevels();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 获取表头行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel文件格式错误：缺少表头");
            }
            
            // 解析表头，找到会员价列的索引
            Map<String, Integer> headerIndexMap = new HashMap<>();
            int lastCellNum = headerRow.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = getCellValue(headerRow, i);
                    if (headerName != null) {
                        headerIndexMap.put(headerName.trim(), i);
                    }
                }
            }
            
            // 计算基础列数（启用会员价之前）
            int baseColumnCount = 15; // 商品编码到启用会员价（包含运费模板ID）
            int enableMemberPriceIndex = headerIndexMap.getOrDefault("启用会员价", baseColumnCount - 1);
            int enableSpecIndex = headerIndexMap.getOrDefault("启用规格", enableMemberPriceIndex + 1);
            
            // 找到商品会员价列和SKU会员价列的起始索引
            Map<String, Integer> productMemberPriceIndexMap = new HashMap<>();
            Map<String, Integer> skuMemberPriceIndexMap = new HashMap<>();
            
            for (MemberLevelVO level : memberLevels) {
                String productHeader = "商品会员价-" + level.getLevelName();
                String skuHeader = "SKU会员价-" + level.getLevelName();
                if (headerIndexMap.containsKey(productHeader)) {
                    productMemberPriceIndexMap.put(level.getLevelName(), headerIndexMap.get(productHeader));
                }
                if (headerIndexMap.containsKey(skuHeader)) {
                    skuMemberPriceIndexMap.put(level.getLevelName(), headerIndexMap.get(skuHeader));
                }
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
                    dto.setShippingTemplateId(getLongFromCell(row, 6));
                    dto.setBasePrice(getBigDecimalFromCell(row, 7));
                    dto.setSuggestedRetailPrice(getBigDecimalFromCell(row, 8));
                    dto.setMarketRetailPrice(getBigDecimalFromCell(row, 9));
                    dto.setWarningStock(getIntegerFromCell(row, 10));
                    dto.setWeight(getBigDecimalFromCell(row, 11));
                    dto.setDescription(getCellValue(row, 12));
                    // 批量导入商品默认为草稿状态，除非明确指定
                    String status = getCellValue(row, 13);
                    dto.setStatus(StringUtil.isBlank(status) ? "草稿" : status);
                    
                    // 解析启用会员价
                    String enableMemberPriceStr = getCellValue(row, enableMemberPriceIndex);
                    dto.setEnableMemberPrice("是".equals(enableMemberPriceStr));
                    
                    // 解析商品会员价（每个等级一列）
                    Map<String, BigDecimal> productMemberPrices = new HashMap<>();
                    for (MemberLevelVO level : memberLevels) {
                        Integer colIndex = productMemberPriceIndexMap.get(level.getLevelName());
                        if (colIndex != null) {
                            BigDecimal price = getBigDecimalFromCell(row, colIndex);
                            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                                productMemberPrices.put(level.getLevelName(), price);
                            }
                        }
                    }
                    dto.setProductMemberPrices(productMemberPrices);
                    
                    // 兼容旧模板：如果表头中有"启用规格"，使用新的列索引；否则使用旧的固定索引
                    int enableSpecColIndex = enableSpecIndex > enableMemberPriceIndex ? enableSpecIndex : 15;
                    dto.setEnableSpec("是".equals(getCellValue(row, enableSpecColIndex)));
                    
                    // 兼容旧模板：SKU相关列的索引需要根据是否有会员价列来调整
                    int skuCodeIndex = enableSpecColIndex + 1;
                    int specCombinationIndex = skuCodeIndex + 1;
                    int skuPriceIndex = specCombinationIndex + 1;
                    int skuStockIndex = skuPriceIndex + 1;
                    int enableSkuMemberPriceIndex = skuStockIndex + 1;
                    
                    dto.setSkuCode(getCellValue(row, skuCodeIndex));
                    dto.setSpecCombination(getCellValue(row, specCombinationIndex));
                    dto.setSkuPrice(getBigDecimalFromCell(row, skuPriceIndex));
                    dto.setSkuStock(getIntegerFromCell(row, skuStockIndex));
                    
                    // 解析启用SKU会员价
                    String enableSkuMemberPriceStr = getCellValue(row, enableSkuMemberPriceIndex);
                    dto.setEnableSkuMemberPrice("是".equals(enableSkuMemberPriceStr));
                    
                    // 解析SKU会员价（每个等级一列）
                    Map<String, BigDecimal> skuMemberPrices = new HashMap<>();
                    for (MemberLevelVO level : memberLevels) {
                        Integer colIndex = skuMemberPriceIndexMap.get(level.getLevelName());
                        if (colIndex != null) {
                            BigDecimal price = getBigDecimalFromCell(row, colIndex);
                            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                                skuMemberPrices.put(level.getLevelName(), price);
                            }
                        }
                    }
                    dto.setSkuMemberPrices(skuMemberPrices);
                    
                    // 兼容旧模板：如果存在旧的"SKU会员价"列（单个价格），也解析
                    if (headerIndexMap.containsKey("SKU会员价")) {
                        Integer oldSkuMemberPriceIndex = headerIndexMap.get("SKU会员价");
                        BigDecimal oldSkuMemberPrice = getBigDecimalFromCell(row, oldSkuMemberPriceIndex);
                        if (oldSkuMemberPrice != null && oldSkuMemberPrice.compareTo(BigDecimal.ZERO) > 0) {
                            // 如果新格式没有数据，使用旧格式的数据（需要指定一个默认等级，这里使用第一个等级）
                            if (skuMemberPrices.isEmpty() && !memberLevels.isEmpty()) {
                                skuMemberPrices.put(memberLevels.get(0).getLevelName(), oldSkuMemberPrice);
                                dto.setSkuMemberPrices(skuMemberPrices);
                            }
                        }
                    }

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

    private Long getLongFromCell(Row row, int cellIndex) {
        String value = getCellValue(row, cellIndex);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
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
        
        // 3. 查找品牌（可选，但如果填写了品牌名称，则必须存在）
        Long brandId = null;
        if (firstRow.getBrandName() != null && !firstRow.getBrandName().trim().isEmpty()) {
            QueryWrapper<Brand> brandQuery = new QueryWrapper<>();
            brandQuery.eq("brand_name", firstRow.getBrandName());
            Brand brand = brandRepository.selectOne(brandQuery);
            if (brand == null) {
                throw new RuntimeException("品牌名称不存在: " + firstRow.getBrandName());
            }
            // 检查品牌是否启用
            if (brand.getStatus() == null || brand.getStatus() == 0) {
                throw new RuntimeException("品牌已禁用: " + firstRow.getBrandName());
            }
            brandId = brand.getId();
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
        productDTO.setShippingTemplateId(firstRow.getShippingTemplateId());
        productDTO.setBasePrice(firstRow.getBasePrice());
        productDTO.setSuggestedRetailPrice(firstRow.getSuggestedRetailPrice());
        productDTO.setMarketRetailPrice(firstRow.getMarketRetailPrice());
        productDTO.setWarningStock(firstRow.getWarningStock());
        productDTO.setWeight(firstRow.getWeight());
        productDTO.setDescription(firstRow.getDescription());
        productDTO.setStatus(firstRow.getStatus());
        productDTO.setMainImage(mainImageUrl != null ? mainImageUrl : "");
        // 将图片列表转换为JSON数组格式（数据库images字段是JSON类型）
        if (detailImageUrls.isEmpty()) {
            productDTO.setImages(null);
        } else {
            try {
                productDTO.setImages(objectMapper.writeValueAsString(detailImageUrls));
            } catch (Exception e) {
                log.error("转换图片列表为JSON失败: {}", detailImageUrls, e);
                productDTO.setImages(null);
            }
        }

        // 如果不启用规格，使用第一行的库存
        if (!firstRow.getEnableSpec()) {
            productDTO.setStock(firstRow.getSkuStock() != null ? firstRow.getSkuStock() : 0);
        } else {
            productDTO.setStock(0); // 启用规格时，总库存由SKU汇总
        }
        
        // 设置启用会员价
        productDTO.setEnableMemberPrice(firstRow.getEnableMemberPrice() != null && firstRow.getEnableMemberPrice() ? 1 : 0);
        
        // 构建商品会员价列表
        if (firstRow.getEnableMemberPrice() != null && firstRow.getEnableMemberPrice() 
                && firstRow.getProductMemberPrices() != null && !firstRow.getProductMemberPrices().isEmpty()) {
            List<ProductMemberPriceDTO> memberPrices = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : firstRow.getProductMemberPrices().entrySet()) {
                if (StringUtil.isNotBlank(entry.getKey()) && entry.getValue() != null 
                        && entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    Long levelId = findMemberLevelIdByName(entry.getKey());
                    if (levelId != null) {
                        ProductMemberPriceDTO mpDTO = new ProductMemberPriceDTO();
                        mpDTO.setMemberLevelId(levelId);
                        mpDTO.setMemberPrice(entry.getValue());
                        memberPrices.add(mpDTO);
                    }
                }
            }
            productDTO.setMemberPrices(memberPrices);
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
                    skuDTO.setEnableMemberPrice(row.getEnableSkuMemberPrice() != null && row.getEnableSkuMemberPrice() ? 1 : 0);
                    skuDTO.setWarningStock(row.getWarningStock());
                    skuDTO.setWeight(row.getWeight());
                    skuDTO.setStatus(1);
                    
                    // 构建SKU会员价列表
                    if (row.getEnableSkuMemberPrice() != null && row.getEnableSkuMemberPrice() 
                            && row.getSkuMemberPrices() != null && !row.getSkuMemberPrices().isEmpty()) {
                        List<ProductSkuMemberPriceDTO> skuMemberPrices = new ArrayList<>();
                        for (Map.Entry<String, BigDecimal> entry : row.getSkuMemberPrices().entrySet()) {
                            if (StringUtil.isNotBlank(entry.getKey()) && entry.getValue() != null 
                                    && entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                                Long levelId = findMemberLevelIdByName(entry.getKey());
                                if (levelId != null) {
                                    ProductSkuMemberPriceDTO mpDTO = new ProductSkuMemberPriceDTO();
                                    mpDTO.setMemberLevelId(levelId);
                                    mpDTO.setMemberPrice(entry.getValue());
                                    skuMemberPrices.add(mpDTO);
                                }
                            }
                        }
                        skuDTO.setMemberPrices(skuMemberPrices);
                    }
                    
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
        // 校验运费模板ID是否存在
        if (dto.getShippingTemplateId() != null) {
            ShippingTemplate template = shippingTemplateRepository.selectById(dto.getShippingTemplateId());
            if (template == null) {
                throw new RuntimeException("运费模板ID不存在: " + dto.getShippingTemplateId());
            }
            if (template.getStatus() == null || template.getStatus() == 0) {
                throw new RuntimeException("运费模板已禁用: " + dto.getShippingTemplateId());
            }
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
            return objectMapper.writeValueAsString(specMap);
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

    private Long getLong(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 初始化会员等级名称到ID的映射
     */
    private void initMemberLevelMap() {
        if (memberLevelNameToIdMap == null) {
            memberLevelNameToIdMap = new HashMap<>();
            try {
                List<MemberLevelVO> levels = memberLevelService.getAllEnabledMemberLevels();
                for (MemberLevelVO level : levels) {
                    if (level.getLevelName() != null && level.getId() != null) {
                        memberLevelNameToIdMap.put(level.getLevelName().trim(), level.getId());
                    }
                }
                log.info("初始化会员等级映射完成，共 {} 个等级", memberLevelNameToIdMap.size());
            } catch (Exception e) {
                log.error("初始化会员等级映射失败", e);
                memberLevelNameToIdMap = new HashMap<>();
            }
        }
    }
    
    /**
     * 根据会员等级名称查找ID
     */
    private Long findMemberLevelIdByName(String levelName) {
        if (StringUtil.isBlank(levelName)) {
            return null;
        }
        
        initMemberLevelMap();
        Long id = memberLevelNameToIdMap.get(levelName.trim());
        
        if (id == null) {
            throw new RuntimeException("会员等级名称不存在: " + levelName);
        }
        
        return id;
    }
    
    /**
     * 验证会员等级名称是否存在
     */
    private void validateMemberLevelNames(List<ProductImportDTO> importDataList) {
        initMemberLevelMap();
        Set<String> allLevelNames = memberLevelNameToIdMap.keySet();
        
        for (ProductImportDTO dto : importDataList) {
            // 验证商品会员价中的等级名称
            if (dto.getProductMemberPrices() != null && !dto.getProductMemberPrices().isEmpty()) {
                for (String levelName : dto.getProductMemberPrices().keySet()) {
                    if (StringUtil.isNotBlank(levelName) && !allLevelNames.contains(levelName.trim())) {
                        throw new RuntimeException(
                            String.format("第%d行，会员等级名称不存在: %s", 
                                dto.getRowNumber(), levelName));
                    }
                }
            }
            
            // 验证SKU会员价中的等级名称
            if (dto.getSkuMemberPrices() != null && !dto.getSkuMemberPrices().isEmpty()) {
                for (String levelName : dto.getSkuMemberPrices().keySet()) {
                    if (StringUtil.isNotBlank(levelName) && !allLevelNames.contains(levelName.trim())) {
                        throw new RuntimeException(
                            String.format("第%d行，SKU会员等级名称不存在: %s", 
                                dto.getRowNumber(), levelName));
                    }
                }
            }
        }
    }
    
    /**
     * 提取友好的错误信息
     */
    private String getErrorMessage(Exception e, String productCode) {
        // 检查是否是数据库唯一约束冲突（商品编码重复）
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                String message = cause.getMessage();
                if (message != null && message.contains("Duplicate entry") && message.contains("uk_product_code")) {
                    return "商品编码已存在: " + productCode + "，请检查是否重复导入或数据库中已存在该商品";
                }
            }
            cause = cause.getCause();
        }
        
        // 返回原始错误信息
        return e.getMessage() != null ? e.getMessage() : "导入失败";
    }
}
