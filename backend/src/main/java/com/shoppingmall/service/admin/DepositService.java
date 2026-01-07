package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.AdminDepositQueryDTO;
import com.shoppingmall.dto.RefundRequestDTO;
import com.shoppingmall.vo.AdminDepositRecordVO;

/**
 * 管理后台预存款服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface DepositService {

    /**
     * 分页查询预存款交易记录
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<AdminDepositRecordVO> getDepositRecordList(AdminDepositQueryDTO queryDTO);

    /**
     * 根据ID获取预存款交易记录详情
     *
     * @param id 记录ID
     * @return 交易记录详情
     */
    AdminDepositRecordVO getDepositRecordById(Long id);

    /**
     * 预存款充值退款
     *
     * @param refundDTO 退款请求
     */
    void refundDepositRecharge(RefundRequestDTO refundDTO);
}






































































