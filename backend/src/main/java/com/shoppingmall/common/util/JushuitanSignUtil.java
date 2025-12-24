package com.shoppingmall.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * 聚水潭API签名工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public class JushuitanSignUtil {

    /**
     * 生成聚水潭API签名
     * 算法：MD5(appSecret + key1 + value1 + key2 + value2 + ... + appSecret).toUpperCase()
     *
     * @param appSecret 应用密钥
     * @param params 请求参数
     * @return 签名字符串
     */
    public static String generateSign(String appSecret, Map<String, String> params) {
        // 1. 参数按key排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        // 2. 拼接字符串：appSecret + key1 + value1 + key2 + value2 + ... + appSecret
        StringBuilder sb = new StringBuilder(appSecret);
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 跳过空值和sign字段
            if (value != null && !value.isEmpty() && !"sign".equals(key)) {
                sb.append(key).append(value);
            }
        }
        sb.append(appSecret);

        // 3. MD5加密并转大写
        return DigestUtils.md5Hex(sb.toString()).toUpperCase();
    }

    /**
     * 验证签名是否正确
     *
     * @param appSecret 应用密钥
     * @param params 请求参数（包含sign字段）
     * @return 是否验证通过
     */
    public static boolean verifySign(String appSecret, Map<String, String> params) {
        String receivedSign = params.get("sign");
        if (receivedSign == null || receivedSign.isEmpty()) {
            return false;
        }

        // 移除sign字段后重新计算签名
        Map<String, String> paramsWithoutSign = new TreeMap<>(params);
        paramsWithoutSign.remove("sign");

        String calculatedSign = generateSign(appSecret, paramsWithoutSign);
        return receivedSign.equals(calculatedSign);
    }
}
