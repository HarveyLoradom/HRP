package com.hrp.reimb.mapper;

import com.hrp.common.entity.CtrlPayoutInvoice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发票数据访问接口
 */
public interface CtrlPayoutInvoiceMapper {
    CtrlPayoutInvoice selectById(@Param("id") Long id);
    List<CtrlPayoutInvoice> selectByPayoutId(@Param("payoutId") Long payoutId);
    int insert(CtrlPayoutInvoice invoice);
    int updateById(CtrlPayoutInvoice invoice);
    int deleteById(@Param("id") Long id);
    int deleteByPayoutId(@Param("payoutId") Long payoutId);
}













