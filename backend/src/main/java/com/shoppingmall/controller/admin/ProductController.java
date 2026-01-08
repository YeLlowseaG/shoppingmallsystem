package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.service.product.ProductImportService;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.vo.ProductImportResultVO;
import com.shoppingmall.vo.ProductVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("商品导入模板");

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
            String[] headers = {
                "商品编码", "条码", "计量单位", "商品名称", "分类名称", "品牌名称",
                "基础价", "建议零售价", "市场零售价", "预警库存", "重量(g)", "商品描述",
                "状态", "启用规格", "SKU编码", "规格组合", "SKU价格", "SKU库存", "SKU会员价", "启用SKU会员价"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000); // 设置列宽
            }

            // 添加示例数据（第一行：不启用规格）
            Row example1 = sheet.createRow(1);
            example1.createCell(0).setCellValue("P001");
            example1.createCell(1).setCellValue("6901234567890");
            example1.createCell(2).setCellValue("个");
            example1.createCell(3).setCellValue("示例商品A");
            example1.createCell(4).setCellValue("情趣用品");
            example1.createCell(5).setCellValue("示例品牌");
            example1.createCell(6).setCellValue(99.00);
            example1.createCell(7).setCellValue(129.00);
            example1.createCell(8).setCellValue(119.00);
            example1.createCell(9).setCellValue(10);
            example1.createCell(10).setCellValue(500);
            example1.createCell(11).setCellValue("这是商品描述");
            example1.createCell(12).setCellValue("上架");
            example1.createCell(13).setCellValue("否");
            example1.createCell(14).setCellValue("");
            example1.createCell(15).setCellValue("");
            example1.createCell(16).setCellValue(99.00);
            example1.createCell(17).setCellValue(100);
            example1.createCell(18).setCellValue("");
            example1.createCell(19).setCellValue("否");

            // 添加示例数据（启用规格的商品，第一个SKU）
            Row example2 = sheet.createRow(2);
            example2.createCell(0).setCellValue("P002");
            example2.createCell(1).setCellValue("6901234567891");
            example2.createCell(2).setCellValue("盒");
            example2.createCell(3).setCellValue("示例商品B");
            example2.createCell(4).setCellValue("健康护理");
            example2.createCell(5).setCellValue("示例品牌");
            example2.createCell(6).setCellValue(49.00);
            example2.createCell(7).setCellValue(69.00);
            example2.createCell(8).setCellValue(59.00);
            example2.createCell(9).setCellValue(20);
            example2.createCell(10).setCellValue(200);
            example2.createCell(11).setCellValue("这是商品描述");
            example2.createCell(12).setCellValue("上架");
            example2.createCell(13).setCellValue("是");
            example2.createCell(14).setCellValue("P002-S");
            example2.createCell(15).setCellValue("颜色:红色;尺寸:S");
            example2.createCell(16).setCellValue(49.00);
            example2.createCell(17).setCellValue(50);
            example2.createCell(18).setCellValue(45.00);
            example2.createCell(19).setCellValue("是");

            // 第二个SKU（只填SKU相关字段）
            Row example3 = sheet.createRow(3);
            example3.createCell(0).setCellValue("P002");
            example3.createCell(13).setCellValue("是");
            example3.createCell(14).setCellValue("P002-M");
            example3.createCell(15).setCellValue("颜色:红色;尺寸:M");
            example3.createCell(16).setCellValue(52.00);
            example3.createCell(17).setCellValue(60);
            example3.createCell(18).setCellValue(48.00);
            example3.createCell(19).setCellValue("是");

            // 第三个SKU
            Row example4 = sheet.createRow(4);
            example4.createCell(0).setCellValue("P002");
            example4.createCell(13).setCellValue("是");
            example4.createCell(14).setCellValue("P002-L");
            example4.createCell(15).setCellValue("颜色:蓝色;尺寸:L");
            example4.createCell(16).setCellValue(55.00);
            example4.createCell(17).setCellValue(40);
            example4.createCell(18).setCellValue(50.00);
            example4.createCell(19).setCellValue("是");

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
}
