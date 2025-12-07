package com.hrp.reimb.mapper;

import com.hrp.common.entity.CtrlPayout;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报账数据访问接口
 */
public interface CtrlPayoutMapper {
    CtrlPayout selectById(@Param("id") Long id);
    CtrlPayout selectByBillcode(@Param("billcode") String billcode);
    List<CtrlPayout> selectByEmpId(@Param("empId") Long empId);
    List<CtrlPayout> selectByStatus(@Param("status") String status);
    List<CtrlPayout> selectAll();
    int insert(CtrlPayout ctrlPayout);
    int updateById(CtrlPayout ctrlPayout);
    int deleteById(@Param("id") Long id);
}

