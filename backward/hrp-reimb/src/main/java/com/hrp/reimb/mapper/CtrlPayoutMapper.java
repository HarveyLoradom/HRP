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
    List<CtrlPayout> selectByItemId(@Param("itemId") Long itemId); // 用于申请冲销：查询APPLY类型的申请单
    List<CtrlPayout> selectPayoutByItemId(@Param("itemId") Long itemId); // 用于报账冲销：查询PAYOUT类型的报账单
    List<CtrlPayout> selectByApprover(@Param("userId") String userId);
    List<CtrlPayout> selectByApproverPage(@Param("userId") String userId, @Param("status") String status, @Param("offset") Long offset, @Param("size") Long size);
    Long countByApprover(@Param("userId") String userId, @Param("status") String status);
    String selectMaxBillcodeByPrefix(@Param("prefix") String prefix);
    List<CtrlPayout> selectByConditions(@Param("payoutBillcode") String payoutBillcode, 
                                        @Param("empName") String empName,
                                        @Param("payoutTypeId") String payoutTypeId,
                                        @Param("status") String status,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("billTypePrefix") String billTypePrefix);
    /**
     * 检查来源申请单号是否已被使用
     * @param sourceApplyNo 来源申请单号
     * @param excludePayoutId 排除的报账单ID（用于更新时排除自己）
     * @return 如果已被使用，返回使用该申请单号的报账单
     */
    CtrlPayout selectBySourceApplyNo(@Param("sourceApplyNo") String sourceApplyNo, @Param("excludePayoutId") Long excludePayoutId);
    
    /**
     * 检查合同编号是否已被使用
     * @param contractNo 合同编号
     * @param excludePayoutId 排除的报账单ID（用于更新时排除自己）
     * @return 如果已被使用，返回使用该合同编号的报账单
     */
    CtrlPayout selectByContractNo(@Param("contractNo") String contractNo, @Param("excludePayoutId") Long excludePayoutId);
    int insert(CtrlPayout ctrlPayout);
    int updateById(CtrlPayout ctrlPayout);
    int deleteById(@Param("id") Long id);
}

