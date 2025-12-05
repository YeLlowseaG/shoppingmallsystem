package com.shoppingmall.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public class DateUtil {

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 格式化日期时间
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDateTime(Date date) {
        return cn.hutool.core.date.DateUtil.formatDateTime(date);
    }

    /**
     * 格式化日期时间
     *
     * @param date   日期
     * @param format 格式
     * @return 格式化后的字符串
     */
    public static String formatDateTime(Date date, String format) {
        return cn.hutool.core.date.DateUtil.format(date, format);
    }

    /**
     * 格式化LocalDateTime
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }

    /**
     * 格式化LocalDateTime
     *
     * @param dateTime 日期时间
     * @param format   格式
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime, String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateStr 日期字符串
     * @return Date对象
     */
    public static Date parseDateTime(String dateStr) {
        return cn.hutool.core.date.DateUtil.parseDateTime(dateStr);
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateStr 日期字符串
     * @param format  格式
     * @return Date对象
     */
    public static Date parseDateTime(String dateStr, String format) {
        return cn.hutool.core.date.DateUtil.parse(dateStr, format);
    }

    /**
     * 获取当前日期时间字符串
     *
     * @return 当前日期时间字符串
     */
    public static String now() {
        return cn.hutool.core.date.DateUtil.now();
    }

    /**
     * 获取当前日期字符串
     *
     * @return 当前日期字符串
     */
    public static String today() {
        return cn.hutool.core.date.DateUtil.today();
    }

    /**
     * 获取当前时间戳（秒）
     *
     * @return 时间戳
     */
    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}

