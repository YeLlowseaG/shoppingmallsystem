package com.shoppingmall.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息Repository
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Mapper
public interface MessageRepository extends BaseMapper<Message> {
}


