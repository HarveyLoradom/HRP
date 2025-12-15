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
    List<CtrlPayout> selectByEmpIdPage(@Param("empId") Long empId, @Param("offset") Long offset, @Param("size") Long size);
    Long countByEmpId(@Param("empId") Long empId);
    List<CtrlPayout> selectByStatus(@Param("status") String status);
    List<CtrlPayout> selectByStatusPage(@Param("status") String status, @Param("offset") Long offset, @Param("size") Long size);
    Long countByStatus(@Param("status") String status);
    List<CtrlPayout> selectAll();
    List<CtrlPayout> selectAllPage(@Param("offset") Long offset, @Param("size") Long size);
    Long countAll();
    List<CtrlPayout> selectByApprover(@Param("userId") String userId);
    List<CtrlPayout> selectByApproverPage(@Param("userId") String userId, @Param("offset") Long offset, @Param("size") Long size);
    Long countByApprover(@Param("userId") String userId);
    int insert(CtrlPayout ctrlPayout);
    int updateById(CtrlPayout ctrlPayout);
    int deleteById(@Param("id") Long id);
}

