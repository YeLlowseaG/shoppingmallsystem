package com.shoppingmall.common.util;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 字符串工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public class StringUtil {

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }

    /**
     * 判断字符串是否为空（包括空白字符）
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * 判断字符串是否不为空（包括空白字符）
     *
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    /**
     * 去除字符串两端空白
     *
     * @param str 字符串
     * @return 去除空白后的字符串
     */
    public static String trim(String str) {
        return StringUtils.trim(str);
    }

    /**
     * 去除字符串所有空白
     *
     * @param str 字符串
     * @return 去除空白后的字符串
     */
    public static String trimAll(String str) {
        return StringUtils.deleteWhitespace(str);
    }

    /**
     * 字符串截取
     *
     * @param str   字符串
     * @param start 开始位置
     * @param end   结束位置
     * @return 截取后的字符串
     */
    public static String substring(String str, int start, int end) {
        return StringUtils.substring(str, start, end);
    }

    /**
     * 字符串截取（从开始位置到指定长度）
     *
     * @param str   字符串
     * @param start 开始位置
     * @return 截取后的字符串
     */
    public static String substring(String str, int start) {
        return StringUtils.substring(str, start);
    }

    /**
     * 字符串截取（指定长度）
     *
     * @param str    字符串
     * @param length 长度
     * @return 截取后的字符串
     */
    public static String truncate(String str, int length) {
        if (isEmpty(str)) {
            return str;
        }
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length);
    }

    /**
     * 字符串截取（指定长度，超出部分用省略号表示）
     *
     * @param str    字符串
     * @param length 长度
     * @return 截取后的字符串
     */
    public static String truncateWithEllipsis(String str, int length) {
        if (isEmpty(str)) {
            return str;
        }
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length) + "...";
    }

    /**
     * 字符串脱敏（手机号）
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 字符串脱敏（邮箱）
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }

    /**
     * 字符串脱敏（姓名）
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (isEmpty(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }

    /**
     * 驼峰转下划线
     *
     * @param str 驼峰字符串
     * @return 下划线字符串
     */
    public static String camelToUnderline(String str) {
        return StrUtil.toUnderlineCase(str);
    }

    /**
     * 下划线转驼峰
     *
     * @param str 下划线字符串
     * @return 驼峰字符串
     */
    public static String underlineToCamel(String str) {
        return StrUtil.toCamelCase(str);
    }
}

