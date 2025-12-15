package com.hrp.contract.mapper;

import com.hrp.common.entity.PactMain;
import org.apache.ibatis.annotations.Param;

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
    int insert(PactMain pactMain);
    int updateById(PactMain pactMain);
    int deleteById(@Param("id") Long id);
}

