package com.hrp.auth.feign;

import com.hrp.common.entity.AssetPurchaseApplyMain;
import com.hrp.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Asset服务Feign客户端
 * 用于调用asset服务的接口
 */
@FeignClient(name = "hrp-asset", path = "/asset/purchase/apply")
public interface AssetServiceClient {
    
    /**
     * 根据申请单号获取采购申请信息
     * 
     * @param applyNo 申请单号
     * @return 采购申请信息
     */
    @GetMapping("/no/{applyNo}")
    Result<AssetPurchaseApplyMain> getAssetPurchaseApplyByNo(@PathVariable("applyNo") String applyNo);
    
    /**
     * 根据申请单号获取采购申请明细列表
     * 
     * @param applyNo 申请单号
     * @return 采购申请明细列表
     */
    @GetMapping("/details/{applyNo}")
    Result<java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail>> getAssetPurchaseApplyDetailsByNo(@PathVariable("applyNo") String applyNo);
    
    /**
     * 批量更新采购申请明细表（用于流程实例变量更新）
     * 
     * @param request 包含主表ID和明细表列表的请求体
     * @return 更新结果
     */
    @PutMapping("/update-details")
    Result<Boolean> updateAssetPurchaseApplyDetails(@RequestBody Map<String, Object> request);
    
    /**
     * 更新采购申请（主表和明细表一起更新）
     * 
     * @param request 包含主表和明细表的请求体
     * @return 更新后的采购申请
     */
    @PutMapping
    Result<AssetPurchaseApplyMain> updateAssetPurchaseApply(@RequestBody Map<String, Object> request);
}

