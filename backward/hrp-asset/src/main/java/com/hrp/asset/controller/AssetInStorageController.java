package com.hrp.asset.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.AssetInStorage;
import com.hrp.common.entity.AssetInStorageDetail;
import com.hrp.asset.service.AssetInStorageService;
import com.hrp.asset.feign.AuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset/storage")
@CrossOrigin
public class AssetInStorageController {

    @Autowired
    private AssetInStorageService assetInStorageService;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;

    /**
     * 入库单列表（分页查询）
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<AssetInStorage>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "storageNo", required = false) String storageNo,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "applyNo", required = false) String applyNo,
            @RequestParam(value = "storageStatus", required = false) String storageStatus,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        com.hrp.common.entity.PageResult<AssetInStorage> pageResult = assetInStorageService.getPage(
                page, size, storageNo, orderNo, applyNo, storageStatus, startDate, endDate);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询入库单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable("id") Long id) {
        AssetInStorage storage = assetInStorageService.getById(id);
        if (storage == null) {
            return Result.error("入库单不存在");
        }
        
        // 查询明细
        List<AssetInStorageDetail> details = assetInStorageService.getDetailsByStorageId(id);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("storage", storage);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 根据入库单号查询入库单详情
     */
    @GetMapping("/storage-no/{storageNo}")
    public Result<Map<String, Object>> getByStorageNo(@PathVariable("storageNo") String storageNo) {
        AssetInStorage storage = assetInStorageService.getByStorageNo(storageNo);
        if (storage == null) {
            return Result.error("入库单不存在");
        }
        
        // 查询明细
        List<AssetInStorageDetail> details = assetInStorageService.getDetailsByStorageNo(storageNo);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("storage", storage);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 根据采购单号查询入库单
     */
    @GetMapping("/order-no/{orderNo}")
    public Result<List<AssetInStorage>> getByOrderNo(@PathVariable("orderNo") String orderNo) {
        List<AssetInStorage> storages = assetInStorageService.getByOrderNo(orderNo);
        return Result.success(storages);
    }

    /**
     * 根据采购ID查询入库单
     */
    @GetMapping("/purchase-id/{purchaseId}")
    public Result<List<AssetInStorage>> getByPurchaseId(@PathVariable("purchaseId") Long purchaseId) {
        List<AssetInStorage> storages = assetInStorageService.getByPurchaseId(purchaseId);
        return Result.success(storages);
    }

    /**
     * 新增入库单
     */
    @PostMapping
    public Result<AssetInStorage> save(
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
            Map<String, Object> storageMap = (Map<String, Object>) request.get("storage");
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) request.get("details");
            
            // 构建入库主表对象
            AssetInStorage storage = convertToStorage(storageMap);
            storage.setCreateUser(currentUser);
            storage.setOperatorId(currentEmpId);
            storage.setOperatorName(currentEmpName);
            
            // 构建入库明细列表
            List<AssetInStorageDetail> details = null;
            if (detailsList != null && !detailsList.isEmpty()) {
                details = detailsList.stream()
                        .map(this::convertToStorageDetail)
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // 保存
            AssetInStorage saved = assetInStorageService.save(storage, details);
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
     * 更新入库单
     */
    @PutMapping
    public Result<AssetInStorage> update(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 解析请求数据
            Map<String, Object> storageMap = (Map<String, Object>) request.get("storage");
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) request.get("details");
            
            // 构建入库主表对象
            AssetInStorage storage = convertToStorage(storageMap);
            
            // 构建入库明细列表
            List<AssetInStorageDetail> details = null;
            if (detailsList != null && !detailsList.isEmpty()) {
                details = detailsList.stream()
                        .map(this::convertToStorageDetail)
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // 更新
            AssetInStorage updated = assetInStorageService.update(storage, details);
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
     * 删除入库单（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean result = assetInStorageService.delete(id);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 完成入库（更新资产账户库存数量）
     */
    @PostMapping("/{id}/complete")
    public Result<AssetInStorage> completeStorage(@PathVariable Long id) {
        try {
            AssetInStorage storage = assetInStorageService.completeStorage(id);
            if (storage != null) {
                return Result.success(storage);
            } else {
                return Result.error("入库失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("入库失败: " + e.getMessage());
        }
    }

    /**
     * 将Map转换为AssetInStorage对象
     */
    private AssetInStorage convertToStorage(Map<String, Object> map) {
        AssetInStorage storage = new AssetInStorage();
        if (map.get("id") != null) {
            storage.setId(Long.parseLong(map.get("id").toString()));
        }
        if (map.get("storageNo") != null) {
            storage.setStorageNo(map.get("storageNo").toString());
        }
        if (map.get("purchaseId") != null) {
            storage.setPurchaseId(Long.parseLong(map.get("purchaseId").toString()));
        }
        if (map.get("orderNo") != null) {
            storage.setOrderNo(map.get("orderNo").toString());
        }
        if (map.get("applyNo") != null) {
            storage.setApplyNo(map.get("applyNo").toString());
        }
        if (map.get("storageDate") != null) {
            storage.setStorageDate(java.time.LocalDate.parse(map.get("storageDate").toString()));
        }
        if (map.get("storageStatus") != null) {
            storage.setStorageStatus(map.get("storageStatus").toString());
        }
        if (map.get("operatorId") != null) {
            storage.setOperatorId(Long.parseLong(map.get("operatorId").toString()));
        }
        if (map.get("operatorName") != null) {
            storage.setOperatorName(map.get("operatorName").toString());
        }
        if (map.get("remark") != null) {
            storage.setRemark(map.get("remark").toString());
        }
        return storage;
    }

    /**
     * 将Map转换为AssetInStorageDetail对象
     */
    private AssetInStorageDetail convertToStorageDetail(Map<String, Object> map) {
        AssetInStorageDetail detail = new AssetInStorageDetail();
        if (map.get("id") != null) {
            detail.setId(Long.parseLong(map.get("id").toString()));
        }
        if (map.get("purchaseDetailId") != null) {
            detail.setPurchaseDetailId(Long.parseLong(map.get("purchaseDetailId").toString()));
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
        if (map.get("storageQuantity") != null) {
            detail.setStorageQuantity(Integer.parseInt(map.get("storageQuantity").toString()));
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

