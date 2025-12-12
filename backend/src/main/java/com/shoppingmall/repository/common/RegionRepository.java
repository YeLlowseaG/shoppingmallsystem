package com.shoppingmall.repository.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Mapper
public interface RegionRepository extends BaseMapper<Region> {

    /**
     * 根据级别查询（批量查询优化）
     */
    @Select("SELECT * FROM region WHERE level = #{level} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<Region> selectByLevel(@Param("level") Integer level);

    /**
     * 根据父级ID查询子级（批量查询）
     */
    @Select("SELECT * FROM region WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<Region> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据编码查询（唯一查询）
     */
    @Select("SELECT * FROM region WHERE code = #{code} AND status = 1 LIMIT 1")
    Region selectByCode(@Param("code") String code);

    /**
     * 查询所有启用的地区（用于预加载）
     */
    @Select("SELECT * FROM region WHERE status = 1 ORDER BY level ASC, sort_order ASC, id ASC")
    List<Region> selectAllEnabled();
}

