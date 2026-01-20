package com.hrp.auth.feign;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.HrApply;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * HR服务Feign客户端
 * 用于调用hr服务的接口
 */
@FeignClient(name = "hrp-hr", path = "/hr")
public interface HrServiceClient {
    
    /**
     * 根据申请单号获取业务申请信息
     * 
     * @param applyNo 申请单号
     * @return 业务申请信息
     */
    @GetMapping("/apply/apply-no/{applyNo}")
    Result<HrApply> getHrApplyByApplyNo(@PathVariable("applyNo") String applyNo);
    
    /**
     * 根据ID获取业务申请信息
     * 
     * @param applyId 申请ID
     * @return 业务申请信息
     */
    @GetMapping("/apply/{applyId}")
    Result<HrApply> getHrApplyById(@PathVariable("applyId") Long applyId);
    
    /**
     * 更新业务申请信息
     * 
     * @param hrApply 业务申请信息
     * @return 更新后的业务申请信息
     */
    @org.springframework.web.bind.annotation.PutMapping("/apply")
    Result<HrApply> updateHrApply(@org.springframework.web.bind.annotation.RequestBody HrApply hrApply);
}

