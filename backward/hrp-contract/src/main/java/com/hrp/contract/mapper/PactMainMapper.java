package com.hrp.contract.mapper;

import com.hrp.common.entity.PactMain;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PactMainMapper {
    PactMain selectById(@Param("id") Long id);
    PactMain selectByContractNo(@Param("contractNo") String contractNo);
    List<PactMain> selectByStatus(@Param("status") String status);
    List<PactMain> selectAll();
    List<PactMain> selectByApprover(@Param("userId") String userId);
    int insert(PactMain pactMain);
    int updateById(PactMain pactMain);
    int deleteById(@Param("id") Long id);
}

