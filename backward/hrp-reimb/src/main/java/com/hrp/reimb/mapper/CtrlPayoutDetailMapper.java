package com.hrp.reimb.mapper;

import com.hrp.common.entity.CtrlPayoutDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CtrlPayoutDetailMapper {
    CtrlPayoutDetail selectById(@Param("id") Long id);
    List<CtrlPayoutDetail> selectByPayoutId(@Param("payoutId") Long payoutId);
    int insert(CtrlPayoutDetail detail);
    int updateById(CtrlPayoutDetail detail);
    int deleteById(@Param("id") Long id);
    int deleteByPayoutId(@Param("payoutId") Long payoutId);
}

