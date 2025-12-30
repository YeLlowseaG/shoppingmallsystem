package com.shoppingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ScheduledTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务Mapper接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Mapper
public interface ScheduledTaskMapper extends BaseMapper<ScheduledTask> {
}


