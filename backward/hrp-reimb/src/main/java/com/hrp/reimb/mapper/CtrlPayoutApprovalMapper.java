package com.hrp.reimb.mapper;

import com.hrp.common.entity.CtrlPayoutApproval;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批记录数据访问接口
 */
public interface CtrlPayoutApprovalMapper {
    CtrlPayoutApproval selectById(@Param("id") Long id);
    List<CtrlPayoutApproval> selectByPayoutId(@Param("payoutId") Long payoutId);
    int insert(CtrlPayoutApproval approval);
    int updateById(CtrlPayoutApproval approval);
    int deleteById(@Param("id") Long id);
}








