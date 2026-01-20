package com.hrp.hr.mapper;

import com.hrp.common.entity.HrApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务申请表数据访问接口（参照报账模块）
 */
public interface HrApplyMapper {
    HrApply selectById(@Param("applyId") Long applyId);
    HrApply selectByApplyNo(@Param("applyNo") String applyNo);
    List<HrApply> selectByEmpId(@Param("empId") Long empId);
    List<HrApply> selectByEmpIdPage(@Param("empId") Long empId, @Param("offset") Long offset, @Param("size") Long size);
    Long countByEmpId(@Param("empId") Long empId);
    List<HrApply> selectByStatus(@Param("status") String status);
    List<HrApply> selectByStatusPage(@Param("status") String status, @Param("offset") Long offset, @Param("size") Long size);
    Long countByStatus(@Param("status") String status);
    List<HrApply> selectAll();
    List<HrApply> selectAllPage(@Param("offset") Long offset, @Param("size") Long size);
    Long countAll();
    List<HrApply> selectByApprover(@Param("userId") String userId);
    List<HrApply> selectByApproverPage(@Param("userId") String userId, @Param("status") String status, @Param("offset") Long offset, @Param("size") Long size);
    Long countByApprover(@Param("userId") String userId, @Param("status") String status);
    List<HrApply> selectByConditions(@Param("applyNo") String applyNo,
                                     @Param("empId") Long empId,
                                     @Param("empName") String empName,
                                     @Param("hrApplyType") String hrApplyType,
                                     @Param("status") String status,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate);
    String selectMaxApplyNoByPrefix(@Param("prefix") String prefix);
    int insert(HrApply hrApply);
    int updateById(HrApply hrApply);
    int deleteById(@Param("applyId") Long applyId);
}
