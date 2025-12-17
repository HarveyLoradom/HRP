package com.hrp.reimb.mapper;

import com.hrp.common.entity.CtrlPayoutPayment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 支付清单数据访问接口
 */
public interface CtrlPayoutPaymentMapper {
    CtrlPayoutPayment selectById(@Param("id") Long id);
    List<CtrlPayoutPayment> selectByPayoutId(@Param("payoutId") Long payoutId);
    int insert(CtrlPayoutPayment payment);
    int updateById(CtrlPayoutPayment payment);
    int deleteById(@Param("id") Long id);
    int deleteByPayoutId(@Param("payoutId") Long payoutId);
}













