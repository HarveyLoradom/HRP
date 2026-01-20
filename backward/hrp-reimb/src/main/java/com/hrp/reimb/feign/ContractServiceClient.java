package com.hrp.reimb.feign;

import com.hrp.common.entity.PactMain;
import com.hrp.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Contract服务Feign客户端
 * 用于调用contract服务的接口
 */
@FeignClient(name = "hrp-contract", path = "/contract")
public interface ContractServiceClient {
    
    /**
     * 根据合同编号获取合同信息
     * 
     * @param contractNo 合同编号
     * @return 合同信息
     */
    @GetMapping("/contract-no/{contractNo}")
    Result<PactMain> getContractByContractNo(@PathVariable("contractNo") String contractNo);
}

