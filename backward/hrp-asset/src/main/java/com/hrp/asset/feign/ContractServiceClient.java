package com.hrp.asset.feign;

import com.hrp.common.entity.PactMain;
import com.hrp.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 合同服务Feign客户端
 * 用于调用合同服务的接口
 */
@FeignClient(name = "hrp-contract", path = "/contract")
public interface ContractServiceClient {
    
    /**
     * 根据合同编号查询合同信息
     * 
     * @param contractNo 合同编号
     * @return 合同信息
     */
    @GetMapping("/contract-no/{contractNo}")
    Result<PactMain> getByContractNo(@PathVariable("contractNo") String contractNo);
    
    /**
     * 查询采购合同列表（只查询合同类型为PURCHASE的合同）
     * 
     * @param contractNo 合同编号（可选，用于搜索）
     * @return 采购合同列表
     */
    @GetMapping("/purchase/list")
    Result<java.util.List<PactMain>> getPurchaseContracts(@RequestParam(value = "contractNo", required = false) String contractNo);
}

