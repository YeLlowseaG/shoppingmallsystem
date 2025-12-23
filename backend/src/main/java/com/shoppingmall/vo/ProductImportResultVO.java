package com.shoppingmall.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品导入结果VO
 */
@Data
public class ProductImportResultVO {
    
    private Integer totalCount = 0;
    
    private Integer successCount = 0;
    
    private Integer failCount = 0;
    
    private List<ImportError> errors = new ArrayList<>();
    
    private List<String> warnings = new ArrayList<>();
    
    @Data
    public static class ImportError {
        
        private Integer row;
        
        private String productCode;
        
        private String error;
        
        public ImportError(Integer row, String productCode, String error) {
            this.row = row;
            this.productCode = productCode;
            this.error = error;
        }
    }
    
    public void addError(Integer row, String productCode, String error) {
        this.errors.add(new ImportError(row, productCode, error));
        this.failCount++;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public void incrementSuccess() {
        this.successCount++;
    }
}
