package com.shoppingmall.repository.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {

}



