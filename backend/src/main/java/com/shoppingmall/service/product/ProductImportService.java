package com.shoppingmall.service.product;

import com.shoppingmall.vo.ProductImportResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImportService {
    
    ProductImportResultVO importProducts(MultipartFile csvFile, MultipartFile imageZip) throws Exception;
}
