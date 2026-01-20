package com.hrp.asset.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.AssetPurchase;
import com.hrp.common.entity.AssetPurchaseDetail;
import com.hrp.common.entity.AssetPurchaseApplyMain;
import com.hrp.common.entity.AssetPurchaseApplyDetail;
import com.hrp.asset.service.AssetPurchaseService;
import com.hrp.asset.service.AssetPurchaseApplyService;
import com.hrp.asset.feign.AuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset/purchase")
@CrossOrigin
public class AssetPurchaseController {

    @Autowired
    private AssetPurchaseService assetPurchaseService;
    
    @Autowired
    private AssetPurchaseApplyService assetPurchaseApplyService;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;

    /**
     * 采购单列表（分页查询）
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<AssetPurchase>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "applyNo", required = false) String applyNo,
            @RequestParam(value = "purchaseStatus", required = false) String purchaseStatus,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        com.hrp.common.entity.PageResult<AssetPurchase> pageResult = assetPurchaseService.getPage(
                page, size, orderNo, applyNo, purchaseStatus, startDate, endDate);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询采购单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable("id") Long id) {
        AssetPurchase purchase = assetPurchaseService.getById(id);
        if (purchase == null) {
            return Result.error("采购单不存在");
        }
        
        // 查询明细
        List<AssetPurchaseDetail> details = assetPurchaseService.getDetailsByPurchaseId(id);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("purchase", purchase);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 根据采购单号查询采购单详情
     */
    @GetMapping("/order-no/{orderNo}")
    public Result<Map<String, Object>> getByOrderNo(@PathVariable("orderNo") String orderNo) {
        AssetPurchase purchase = assetPurchaseService.getByOrderNo(orderNo);
        if (purchase == null) {
            return Result.error("采购单不存在");
        }
        
        // 查询明细
        List<AssetPurchaseDetail> details = assetPurchaseService.getDetailsByOrderNo(orderNo);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("purchase", purchase);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 根据申请单号查询已创建的采购单
     */
    @GetMapping("/apply-no/{applyNo}")
    public Result<List<AssetPurchase>> getByApplyNo(@PathVariable("applyNo") String applyNo) {
        List<AssetPurchase> purchases = assetPurchaseService.getByApplyNo(applyNo);
        return Result.success(purchases);
    }

    /**
     * 查询可用的申请单（状态为已审批的）
     */
    @GetMapping("/available-applies")
    public Result<List<AssetPurchaseApplyMain>> getAvailableApplies() {
        List<AssetPurchaseApplyMain> applies = assetPurchaseApplyService.getByStatus("APPROVED");
        return Result.success(applies);
    }

    /**
     * 根据申请单号查询申请单信息（包括明细）
     */
    @GetMapping("/apply-by-no/{applyNo}")
    public Result<Map<String, Object>> getApplyByNo(@PathVariable("applyNo") String applyNo) {
        AssetPurchaseApplyMain apply = assetPurchaseApplyService.getByApplyNo(applyNo);
        if (apply == null) {
            return Result.error("申请单不存在");
        }
        
        List<AssetPurchaseApplyDetail> details = assetPurchaseApplyService.getDetailsByApplyId(apply.getId());
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("apply", apply);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 新增采购单
     */
    @PostMapping
    public Result<AssetPurchase> save(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取当前用户信息
            String currentUser = null;
            Long currentEmpId = null;
            String currentEmpName = null;
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String tokenStr = token.substring(7);
                    String account = com.hrp.common.util.JwtUtil.getAccount(tokenStr);
                    if (account != null && !account.trim().isEmpty() && authServiceClient != null) {
                        com.hrp.common.entity.Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserByAccount(account);
                        if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                            com.hrp.common.entity.User user = userResult.getData();
                            currentUser = account;
                            currentEmpId = user.getEmpId();
                            currentEmpName = user.getName();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("获取当前用户信息失败: " + e.getMessage());
                }
            }
            
            // 解析请求数据
            Map<String, Object> purchaseMap = (Map<String, Object>) request.get("purchase");
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) request.get("details");
            
            // 构建采购主表对象
            AssetPurchase purchase = convertToPurchase(purchaseMap);
            purchase.setCreateUser(currentUser);
            purchase.setOperatorId(currentEmpId);
            purchase.setOperatorName(currentEmpName);
            
            // 构建采购明细列表
            List<AssetPurchaseDetail> details = null;
            if (detailsList != null && !detailsList.isEmpty()) {
                details = detailsList.stream()
                        .map(this::convertToPurchaseDetail)
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // 保存
            AssetPurchase saved = assetPurchaseService.save(purchase, details);
            if (saved != null) {
                return Result.success(saved);
            } else {
                return Result.error("保存失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 更新采购单
     */
    @PutMapping
    public Result<AssetPurchase> update(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 解析请求数据
            Map<String, Object> purchaseMap = (Map<String, Object>) request.get("purchase");
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) request.get("details");
            
            // 构建采购主表对象
            AssetPurchase purchase = convertToPurchase(purchaseMap);
            
            // 构建采购明细列表
            List<AssetPurchaseDetail> details = null;
            if (detailsList != null && !detailsList.isEmpty()) {
                details = detailsList.stream()
                        .map(this::convertToPurchaseDetail)
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // 更新
            AssetPurchase updated = assetPurchaseService.update(purchase, details);
            if (updated != null) {
                return Result.success(updated);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除采购单（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean result = assetPurchaseService.delete(id);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 完成采购（生成入库单）
     */
    @PostMapping("/{id}/complete")
    public Result<AssetPurchase> completePurchase(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> request) {
        try {
            String contractNo = request.get("contractNo");
            if (contractNo == null || contractNo.trim().isEmpty()) {
                return Result.error("完成采购时，合同号必填");
            }
            
            // TODO: 验证合同类型必须是采购合同
            // 可以调用合同服务验证合同类型
            
            AssetPurchase purchase = assetPurchaseService.completePurchase(id, contractNo);
            if (purchase != null) {
                return Result.success(purchase);
            } else {
                return Result.error("完成采购失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("完成采购失败: " + e.getMessage());
        }
    }

    /**
     * 将Map转换为AssetPurchase对象
     */
    private AssetPurchase convertToPurchase(Map<String, Object> map) {
        AssetPurchase purchase = new AssetPurchase();
        if (map.get("id") != null) {
            purchase.setId(Long.parseLong(map.get("id").toString()));
        }
        if (map.get("orderNo") != null) {
            purchase.setOrderNo(map.get("orderNo").toString());
        }
        if (map.get("applyNo") != null) {
            purchase.setApplyNo(map.get("applyNo").toString());
        }
        if (map.get("applyId") != null) {
            purchase.setApplyId(Long.parseLong(map.get("applyId").toString()));
        }
        if (map.get("purchaseDate") != null) {
            purchase.setPurchaseDate(java.time.LocalDate.parse(map.get("purchaseDate").toString()));
        }
        if (map.get("purchaseStatus") != null) {
            purchase.setPurchaseStatus(map.get("purchaseStatus").toString());
        }
        if (map.get("contractNo") != null) {
            purchase.setContractNo(map.get("contractNo").toString());
        }
        if (map.get("supplierId") != null) {
            purchase.setSupplierId(Long.parseLong(map.get("supplierId").toString()));
        }
        if (map.get("supplierName") != null) {
            purchase.setSupplierName(map.get("supplierName").toString());
        }
        if (map.get("totalAmount") != null) {
            try {
                purchase.setTotalAmount(new java.math.BigDecimal(map.get("totalAmount").toString()));
            } catch (Exception e) {
                // 如果转换失败，使用默认值0
                purchase.setTotalAmount(java.math.BigDecimal.ZERO);
            }
        }
        if (map.get("operatorId") != null) {
            purchase.setOperatorId(Long.parseLong(map.get("operatorId").toString()));
        }
        if (map.get("operatorName") != null) {
            purchase.setOperatorName(map.get("operatorName").toString());
        }
        if (map.get("remark") != null) {
            purchase.setRemark(map.get("remark").toString());
        }
        return purchase;
    }

    /**
     * 将Map转换为AssetPurchaseDetail对象
     */
    private AssetPurchaseDetail convertToPurchaseDetail(Map<String, Object> map) {
        AssetPurchaseDetail detail = new AssetPurchaseDetail();
        if (map.get("id") != null) {
            detail.setId(Long.parseLong(map.get("id").toString()));
        }
        if (map.get("applyDetailId") != null) {
            detail.setApplyDetailId(Long.parseLong(map.get("applyDetailId").toString()));
        }
        if (map.get("assetCode") != null) {
            detail.setAssetCode(map.get("assetCode").toString());
        }
        if (map.get("assetName") != null) {
            detail.setAssetName(map.get("assetName").toString());
        }
        if (map.get("spec") != null) {
            detail.setSpec(map.get("spec").toString());
        }
        if (map.get("manufacturer") != null) {
            detail.setManufacturer(map.get("manufacturer").toString());
        }
        if (map.get("unit") != null) {
            detail.setUnit(map.get("unit").toString());
        }
        if (map.get("applyQuantity") != null) {
            detail.setApplyQuantity(Integer.parseInt(map.get("applyQuantity").toString()));
        }
        if (map.get("purchaseQuantity") != null) {
            detail.setPurchaseQuantity(Integer.parseInt(map.get("purchaseQuantity").toString()));
        }
        if (map.get("price") != null) {
            detail.setPrice(new java.math.BigDecimal(map.get("price").toString()));
        }
        if (map.get("remark") != null) {
            detail.setRemark(map.get("remark").toString());
        }
        return detail;
    }
}

