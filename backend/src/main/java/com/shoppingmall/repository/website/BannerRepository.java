package com.shoppingmall.repository.website;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.Banner;
import org.apache.ibatis.annotations.Mapper;

/**
 * 轮播图数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Mapper
public interface BannerRepository extends BaseMapper<Banner> {

}
