package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.service.product.ProductImportService;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.vo.ProductImportResultVO;
import com.shoppingmall.vo.ProductVO;
import com.shoppingmall.vo.MemberLevelVO;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 商品控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@RestController("adminProductController")
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImportService productImportService;
    private final MemberLevelService memberLevelService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public Result<Page<ProductVO>> getProductPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "create_time_desc") String sortBy) {
        
        Page<ProductVO> page = productService.getProductPage(current, size, categoryId, keyword, brand, status, sortBy, null, false);
        return Result.success("获取成功", page);
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        ProductVO product = productService.getProductById(id, null);
        return Result.success("获取成功", product);
    }

    /**
     * 创建商品
     */
    @PostMapping
    public Result<Long> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Long id = productService.createProduct(productDTO);
        return Result.success("创建成功", id);
    }

    /**
     * 更新商品
     */
    @PutMapping
    public Result<?> updateProduct(@Valid @RequestBody ProductDTO productDTO) {
        productService.updateProduct(productDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        productService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @PostMapping("/import")
    public Result<ProductImportResultVO> importProducts(
            @RequestParam("csvFile") MultipartFile csvFile,
            @RequestParam(value = "imageZip", required = false) MultipartFile imageZip) {
        try {
            ProductImportResultVO result = productImportService.importProducts(csvFile, imageZip);
            return Result.success("导入完成", result);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 下载Excel导入模板
     */
    @GetMapping("/template/excel")
    public void downloadExcelTemplate(HttpServletResponse response) {
        try {
            // 获取所有启用的会员等级
            List<MemberLevelVO> memberLevels = memberLevelService.getAllEnabledMemberLevels();
            
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("商品导入模板");
            CreationHelper factory = workbook.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            // 设置表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            List<String> headers = new ArrayList<>();
            
            // 基础列
            headers.add("商品编码");
            headers.add("条码");
            headers.add("计量单位");
            headers.add("商品名称");
            headers.add("分类名称");
            headers.add("品牌名称");
            headers.add("运费模板ID");
            headers.add("基础价");
            headers.add("建议零售价");
            headers.add("市场零售价");
            headers.add("预警库存");
            headers.add("重量(g)");
            headers.add("商品描述");
            headers.add("状态");
            headers.add("启用会员价");
            
            // 商品会员价列（每个会员等级一列）
            for (MemberLevelVO level : memberLevels) {
                headers.add("商品会员价-" + level.getLevelName());
            }
            
            // SKU相关列
            headers.add("启用规格");
            headers.add("SKU编码");
            headers.add("规格组合");
            headers.add("SKU价格");
            headers.add("SKU库存");
            headers.add("启用SKU会员价");
            
            // SKU会员价列（每个会员等级一列）
            for (MemberLevelVO level : memberLevels) {
                headers.add("SKU会员价-" + level.getLevelName());
            }

            // 创建表头并添加批注
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000); // 设置列宽
                
                // 为表头添加批注说明
                String comment = getFieldComment(headers.get(i), memberLevels);
                if (comment != null && !comment.isEmpty()) {
                    XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 
                        (short) i, 0, (short) Math.min(i + 2, headers.size() - 1), 4);
                    Comment cellComment = drawing.createCellComment(anchor);
                    RichTextString str = factory.createRichTextString(comment);
                    cellComment.setString(str);
                    cellComment.setAuthor("系统提示");
                    cell.setCellComment(cellComment);
                }
            }

            // 计算列索引
            int baseColCount = 15; // 基础列数（到启用会员价，包含运费模板ID）
            int shippingTemplateIdIndex = 6; // 运费模板ID列索引
            int enableMemberPriceIndex = 14; // 启用会员价列索引
            int productMemberPriceStartIndex = 15; // 商品会员价列起始索引
            int enableSpecIndex = baseColCount + memberLevels.size(); // 启用规格列索引
            int skuCodeIndex = enableSpecIndex + 1;
            int specCombinationIndex = skuCodeIndex + 1;
            int skuPriceIndex = specCombinationIndex + 1;
            int skuStockIndex = skuPriceIndex + 1;
            int enableSkuMemberPriceIndex = skuStockIndex + 1;
            int skuMemberPriceStartIndex = enableSkuMemberPriceIndex + 1;
            
            // 添加示例数据（第一行：不启用规格）
            Row example1 = sheet.createRow(1);
            example1.createCell(0).setCellValue("P001");
            example1.createCell(1).setCellValue("6901234567890");
            example1.createCell(2).setCellValue("个");
            example1.createCell(3).setCellValue("示例商品A");
            example1.createCell(4).setCellValue("情趣用品");
            example1.createCell(5).setCellValue("示例品牌");
            example1.createCell(shippingTemplateIdIndex).setCellValue(""); // 运费模板ID，可选
            example1.createCell(7).setCellValue(99.00);
            example1.createCell(8).setCellValue(129.00);
            example1.createCell(9).setCellValue(119.00);
            example1.createCell(10).setCellValue(10);
            example1.createCell(11).setCellValue(500);
            example1.createCell(12).setCellValue("这是商品描述");
            example1.createCell(13).setCellValue("上架");
            example1.createCell(enableMemberPriceIndex).setCellValue("是");
            
            // 商品会员价示例（为第一个会员等级设置价格）
            if (!memberLevels.isEmpty()) {
                example1.createCell(productMemberPriceStartIndex).setCellValue(89.00);
            }
            
            example1.createCell(enableSpecIndex).setCellValue("否");
            example1.createCell(skuCodeIndex).setCellValue("");
            example1.createCell(specCombinationIndex).setCellValue("");
            example1.createCell(skuPriceIndex).setCellValue(99.00);
            example1.createCell(skuStockIndex).setCellValue(100);
            example1.createCell(enableSkuMemberPriceIndex).setCellValue("否");

            // 添加示例数据（启用规格的商品，第一个SKU）
            Row example2 = sheet.createRow(2);
            example2.createCell(0).setCellValue("P002");
            example2.createCell(1).setCellValue("6901234567891");
            example2.createCell(2).setCellValue("盒");
            example2.createCell(3).setCellValue("示例商品B");
            example2.createCell(4).setCellValue("健康护理");
            example2.createCell(5).setCellValue("示例品牌");
            example2.createCell(shippingTemplateIdIndex).setCellValue(""); // 运费模板ID，可选
            example2.createCell(7).setCellValue(49.00);
            example2.createCell(8).setCellValue(69.00);
            example2.createCell(9).setCellValue(59.00);
            example2.createCell(10).setCellValue(20);
            example2.createCell(11).setCellValue(200);
            example2.createCell(12).setCellValue("这是商品描述");
            example2.createCell(13).setCellValue("上架");
            example2.createCell(enableMemberPriceIndex).setCellValue("是");
            
            // 商品会员价示例（为第一个会员等级设置价格）
            if (!memberLevels.isEmpty()) {
                example2.createCell(productMemberPriceStartIndex).setCellValue(44.00);
            }
            
            example2.createCell(enableSpecIndex).setCellValue("是");
            example2.createCell(skuCodeIndex).setCellValue("P002-S");
            example2.createCell(specCombinationIndex).setCellValue("颜色:红色;尺寸:S");
            example2.createCell(skuPriceIndex).setCellValue(49.00);
            example2.createCell(skuStockIndex).setCellValue(50);
            example2.createCell(enableSkuMemberPriceIndex).setCellValue("是");
            
            // SKU会员价示例（为第一个会员等级设置价格）
            if (!memberLevels.isEmpty()) {
                example2.createCell(skuMemberPriceStartIndex).setCellValue(45.00);
            }

            // 第二个SKU（只填SKU相关字段）
            Row example3 = sheet.createRow(3);
            example3.createCell(0).setCellValue("P002");
            example3.createCell(enableSpecIndex).setCellValue("是");
            example3.createCell(skuCodeIndex).setCellValue("P002-M");
            example3.createCell(specCombinationIndex).setCellValue("颜色:红色;尺寸:M");
            example3.createCell(skuPriceIndex).setCellValue(52.00);
            example3.createCell(skuStockIndex).setCellValue(60);
            example3.createCell(enableSkuMemberPriceIndex).setCellValue("是");
            
            // SKU会员价示例
            if (!memberLevels.isEmpty()) {
                example3.createCell(skuMemberPriceStartIndex).setCellValue(48.00);
            }

            // 第三个SKU
            Row example4 = sheet.createRow(4);
            example4.createCell(0).setCellValue("P002");
            example4.createCell(enableSpecIndex).setCellValue("是");
            example4.createCell(skuCodeIndex).setCellValue("P002-L");
            example4.createCell(specCombinationIndex).setCellValue("颜色:蓝色;尺寸:L");
            example4.createCell(skuPriceIndex).setCellValue(55.00);
            example4.createCell(skuStockIndex).setCellValue(40);
            example4.createCell(enableSkuMemberPriceIndex).setCellValue("是");
            
            // SKU会员价示例
            if (!memberLevels.isEmpty()) {
                example4.createCell(skuMemberPriceStartIndex).setCellValue(50.00);
            }

            // 设置响应头
            String fileName = URLEncoder.encode("商品批量导入模板.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // 写入响应
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            throw new RuntimeException("生成Excel模板失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取字段的填写说明（用于批注）
     */
    private String getFieldComment(String headerName, List<MemberLevelVO> memberLevels) {
        switch (headerName) {
            case "商品编码":
                return "必填，商品唯一标识，不能重复，建议使用字母+数字组合";
            case "条码":
                return "可选，商品条码，如：6901234567890";
            case "计量单位":
                return "可选，商品计量单位，如：个、盒、箱、包等";
            case "商品名称":
                return "必填，商品名称，建议简洁明了";
            case "分类名称":
                return "必填，商品分类名称，必须在系统中存在。如果分类不存在，导入会失败";
            case "品牌名称":
                return "可选，品牌名称。如果填写，则必须在系统中存在且已启用，否则导入会失败";
            case "运费模板ID":
                return "可选，运费模板ID，整数。\n" +
                       "如果填写，则必须在系统中存在且已启用，否则导入会失败。\n" +
                       "如果不填写，商品将不关联运费模板";
            case "基础价":
                return "必填，商品基础价格，必须大于0，支持小数，如：99.00";
            case "建议零售价":
                return "可选，建议零售价，支持小数";
            case "市场零售价":
                return "可选，市场零售价，支持小数";
            case "预警库存":
                return "可选，库存预警阈值，整数。当库存低于此值时系统会提醒";
            case "重量(g)":
                return "可选，商品重量，单位：克，支持小数，如：500.5";
            case "商品描述":
                return "可选，商品详细描述信息，支持HTML格式";
            case "状态":
                return "可选，商品状态：上架、下架、草稿。如果不填写，默认为草稿状态";
            case "启用会员价":
                return "可选，是否启用商品会员价：是/否。\n" +
                       "启用后，可以为不同会员等级设置不同的会员价。\n" +
                       "如果填写\"是\"，建议至少为一个会员等级设置会员价";
            case "启用规格":
                return "可选，是否启用商品规格：是/否。\n" +
                       "启用后，需要填写SKU编码、规格组合、SKU价格、SKU库存等字段。\n" +
                       "一个商品可以有多个SKU，每个SKU占一行，商品编码相同";
            case "SKU编码":
                return "启用规格时必填，SKU唯一标识，建议格式：商品编码-规格标识，如：P002-S";
            case "规格组合":
                return "启用规格时必填，规格组合格式：属性名:属性值;属性名:属性值\n" +
                       "示例：颜色:红色;尺寸:S\n" +
                       "注意：属性名和属性值之间用冒号(:)分隔，多个属性之间用分号(;)分隔";
            case "SKU价格":
                return "启用规格时必填，SKU价格，支持小数。如果为空，则使用商品基础价";
            case "SKU库存":
                return "启用规格时必填，SKU库存数量，整数，如：100";
            case "启用SKU会员价":
                return "可选，是否启用SKU会员价：是/否。\n" +
                       "启用后，可以为不同会员等级设置不同的SKU会员价。\n" +
                       "如果填写\"是\"，建议至少为一个会员等级设置SKU会员价";
            default:
                // 会员价列说明
                if (headerName.startsWith("商品会员价-")) {
                    String levelName = headerName.substring("商品会员价-".length());
                    return "启用会员价时可选，为" + levelName + "设置的会员价。\n" +
                           "如果填写，必须是大于0的数字，支持小数，如：89.00\n" +
                           "如果为空，表示该等级不享受会员价，将显示基础价格";
                }
                if (headerName.startsWith("SKU会员价-")) {
                    String levelName = headerName.substring("SKU会员价-".length());
                    return "启用SKU会员价时可选，为" + levelName + "设置的SKU会员价。\n" +
                           "如果填写，必须是大于0的数字，支持小数，如：45.00\n" +
                           "如果为空，表示该等级不享受会员价，将显示SKU基础价格";
                }
                return null;
        }
    }
}
