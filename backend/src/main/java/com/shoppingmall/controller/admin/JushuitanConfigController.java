package com.shoppingmall.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.JushuitanConfigDTO;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 聚水潭配置管理Controller
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/erp/config")
public class JushuitanConfigController {

    @Resource
    private JushuitanConfigService jushuitanConfigService;

    /**
     * 获取聚水潭配置
     */
    @GetMapping
    public Result<JushuitanConfigVO> getConfig() {
        JushuitanConfigVO config = jushuitanConfigService.getConfig();
        return Result.success(config);
    }

    /**
     * 保存或更新聚水潭配置
     */
    @PostMapping
    public Result<JushuitanConfigVO> saveConfig(@RequestBody JushuitanConfigDTO dto) {
        JushuitanConfigVO config = jushuitanConfigService.saveOrUpdateConfig(dto);
        return Result.success("配置保存成功", config);
    }

    /**
     * 测试聚水潭连接
     */
    @PostMapping("/test")
    public Result<String> testConnection() {
        String result = jushuitanConfigService.testConnection();
        return Result.success(result);
    }
}
