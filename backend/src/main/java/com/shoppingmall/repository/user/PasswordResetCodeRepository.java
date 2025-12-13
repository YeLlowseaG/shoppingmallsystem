package com.shoppingmall.repository.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.PasswordResetCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 密码重置验证码数据访问层
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Mapper
public interface PasswordResetCodeRepository extends BaseMapper<PasswordResetCode> {

}

