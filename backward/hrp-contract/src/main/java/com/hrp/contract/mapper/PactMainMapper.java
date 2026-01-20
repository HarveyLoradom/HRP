package com.hrp.contract.mapper;

import com.hrp.common.entity.PactMain;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PactMainMapper {
    PactMain selectById(@Param("id") Long id);
    PactMain selectByContractNo(@Param("contractNo") String contractNo);
    List<PactMain> selectByStatus(@Param("status") String status);
    List<PactMain> selectAll();
    List<PactMain> selectAllPage(@Param("offset") Long offset, @Param("size") Long size);
    Long countAll();
    List<PactMain> selectByStatusPage(@Param("status") String status, @Param("offset") Long offset, @Param("size") Long size);
    Long countByStatus(@Param("status") String status);
    List<PactMain> selectByApprover(@Param("userId") String userId);
    /**
     * 根据条件查询合同列表
     */
    List<PactMain> selectByConditions(@Param("empId") Long empId,
                                     @Param("contractNo") String contractNo,
                                     @Param("contractName") String contractName,
                                     @Param("contractType") String contractType,
                                     @Param("status") String status,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate);
    /**
     * 根据前缀查询最大合同编号
     * @param prefix 前缀（如：PACT20250105）
     * @return 最大合同编号
     */
    String selectMaxContractNoByPrefix(@Param("prefix") String prefix);
    int insert(PactMain pactMain);
    int updateById(PactMain pactMain);
    int deleteById(@Param("id") Long id);
    
    /**
     * 更新执行状态为履约中（只更新非人工修改的合同）
     * 当前时间 >= 开始时间，且执行状态为待履约（PENDING_EXECUTION）的
     */
    int updateExecutionStatusToExecuting(@Param("now") LocalDateTime now);
    
    /**
     * 更新执行状态为已履约（只更新非人工修改的合同）
     * 当前时间 >= 结束时间，且执行状态为履约中（EXECUTING）的
     */
    int updateExecutionStatusToCompleted(@Param("now") LocalDateTime now);
    
    /**
     * 查询已审批的合同（用于合同执行页面）
     */
    List<PactMain> selectApprovedContractsByConditions(@Param("contractNo") String contractNo,
                                                      @Param("contractName") String contractName,
                                                      @Param("contractType") String contractType,
                                                      @Param("executionStatus") String executionStatus);
}



