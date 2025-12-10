package com.shoppingmall.repository.help;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.HelpCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帮助中心分类数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Mapper
public interface HelpCategoryRepository extends BaseMapper<HelpCategory> {

}

