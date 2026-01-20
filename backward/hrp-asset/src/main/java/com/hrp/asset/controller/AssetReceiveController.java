package com.hrp.asset.controller;

import com.hrp.asset.service.AssetReceiveService;
import com.hrp.common.entity.AssetReceiveMain;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 资产领用管理控制器
 */
@RestController
@RequestMapping("/asset/receive")
@CrossOrigin
public class AssetReceiveController {

    @Autowired
    private AssetReceiveService assetReceiveService;
    
    @Autowired(required = false)
    private com.hrp.asset.feign.AuthServiceClient authServiceClient;

    /**
     * 分页查询资产领用列表
     */
    @GetMapping("/page")
    public Result<PageResult<AssetReceiveMain>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "receiveNo", required = false) String receiveNo,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "applyEmpId", required = false) String applyEmpId,
            @RequestParam(value = "operatorCode", required = false) String operatorCode,
            @RequestHeader(value = "Authorization", required = false) String token) {
        PageResult<AssetReceiveMain> pageResult = assetReceiveService.getPage(page, size, receiveNo, deptId, applyEmpId, operatorCode);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询资产领用详情
     */
    @GetMapping("/{id}")
    public Result<AssetReceiveMain> getById(@PathVariable("id") Long id) {
        AssetReceiveMain receiveMain = assetReceiveService.getById(id);
        if (receiveMain == null) {
            return Result.error("领用单不存在");
        }
        return Result.success(receiveMain);
    }

    /**
     * 根据领用单号查询资产领用详情
     */
    @GetMapping("/receive-no/{receiveNo}")
    public Result<AssetReceiveMain> getByReceiveNo(@PathVariable("receiveNo") String receiveNo) {
        AssetReceiveMain receiveMain = assetReceiveService.getByReceiveNo(receiveNo);
        if (receiveMain == null) {
            return Result.error("领用单不存在");
        }
        return Result.success(receiveMain);
    }

    /**
     * 保存资产领用（主表和明细表）
     */
    @PostMapping
    public Result<AssetReceiveMain> save(@RequestBody Map<String, Object> request,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从请求中提取主表和明细表数据
            Map<String, Object> receiveMainMap = (Map<String, Object>) request.get("receiveMain");
            java.util.List<Map<String, Object>> detailsList = (java.util.List<Map<String, Object>>) request.get("details");
            
            // 转换为实体对象
            AssetReceiveMain receiveMain = convertToReceiveMain(receiveMainMap);
            java.util.List<com.hrp.common.entity.AssetReceiveDetail> details = convertToReceiveDetails(detailsList);
            
            // 获取当前用户ID
            String currentUserId = getCurrentUserId(token);
            
            // 如果没有设置操作人代码，从token中获取
            if (receiveMain.getOperatorCode() == null && currentUserId != null) {
                // 从用户信息中获取 empCode
                if (authServiceClient != null) {
                    try {
                        com.hrp.common.entity.Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserById(currentUserId);
                        if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                            com.hrp.common.entity.User user = userResult.getData();
                            // 尝试从用户信息中获取 empCode
                            // 根据系统设计，account 通常就是 empCode（工号），或者需要通过 empId 查询
                            String empCode = null;
                            if (user.getAccount() != null && !user.getAccount().trim().isEmpty()) {
                                // account 可能就是 empCode（字符串格式）
                                empCode = user.getAccount().trim();
                            } else if (user.getEmpId() != null) {
                                // 如果 account 为空，尝试通过 empId 查询员工信息获取 empCode
                                // 注意：这里需要调用员工查询接口，但 AuthServiceClient 可能没有这个方法
                                // 暂时使用 account，如果 account 也为空则抛出异常
                                throw new RuntimeException("无法获取操作人工号(empCode)，用户账号(account)和员工ID(empId)都为空");
                            }
                            
                            if (empCode != null && !empCode.isEmpty()) {
                                receiveMain.setOperatorCode(empCode);
                            } else {
                                throw new RuntimeException("无法获取操作人工号(empCode)，用户账号(account)为空");
                            }
                        }
                    } catch (Exception e) {
                        // 如果获取失败，抛出异常
                        System.err.println("获取操作人信息失败: " + e.getMessage());
                        throw new RuntimeException("获取操作人信息失败: " + e.getMessage());
                    }
                } else {
                    throw new RuntimeException("无法获取操作人工号(empCode)，authServiceClient未配置");
                }
            }
            
            // 最终检查：如果 operatorCode 仍然为 null，抛出异常
            if (receiveMain.getOperatorCode() == null) {
                throw new RuntimeException("操作人工号(operatorCode)不能为空");
            }
            
            AssetReceiveMain saved = assetReceiveService.save(receiveMain, details, currentUserId);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 生成领用单号
     */
    @GetMapping("/generate-receive-no")
    public Result<String> generateReceiveNo() {
        String receiveNo = assetReceiveService.generateReceiveNo();
        return Result.success(receiveNo);
    }

    /**
     * 从token中获取当前用户ID
     */
    private String getCurrentUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return JwtUtil.getUserId(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 转换Map为AssetReceiveMain实体
     */
    private AssetReceiveMain convertToReceiveMain(Map<String, Object> map) {
        AssetReceiveMain receiveMain = new AssetReceiveMain();
        if (map.get("receiveNo") != null) {
            receiveMain.setReceiveNo(map.get("receiveNo").toString());
        }
        if (map.get("applyEmpId") != null) {
            receiveMain.setApplyEmpId(map.get("applyEmpId").toString());
        }
        if (map.get("deptId") != null) {
            receiveMain.setDeptId(Long.parseLong(map.get("deptId").toString()));
        }
        if (map.get("receiveDate") != null) {
            receiveMain.setReceiveDate(java.time.LocalDate.parse(map.get("receiveDate").toString()));
        }
        if (map.get("receiveReason") != null) {
            receiveMain.setReceiveReason(map.get("receiveReason").toString());
        }
        if (map.get("operatorCode") != null && !map.get("operatorCode").toString().trim().isEmpty()) {
            receiveMain.setOperatorCode(map.get("operatorCode").toString().trim());
        }
        return receiveMain;
    }

    /**
     * 转换List为AssetReceiveDetail实体列表
     */
    private java.util.List<com.hrp.common.entity.AssetReceiveDetail> convertToReceiveDetails(java.util.List<Map<String, Object>> list) {
        java.util.List<com.hrp.common.entity.AssetReceiveDetail> details = new java.util.ArrayList<>();
        for (Map<String, Object> map : list) {
            com.hrp.common.entity.AssetReceiveDetail detail = new com.hrp.common.entity.AssetReceiveDetail();
            if (map.get("assetCode") != null) {
                detail.setAssetCode(map.get("assetCode").toString());
            }
            if (map.get("assetName") != null) {
                detail.setAssetName(map.get("assetName").toString());
            }
            if (map.get("spec") != null) {
                detail.setSpec(map.get("spec").toString());
            }
            if (map.get("unit") != null) {
                detail.setUnit(map.get("unit").toString());
            }
            if (map.get("receiveNum") != null) {
                detail.setReceiveNum(Integer.parseInt(map.get("receiveNum").toString()));
            }
            if (map.get("manufacturer") != null) {
                detail.setManufacturer(map.get("manufacturer").toString());
            }
            details.add(detail);
        }
        return details;
    }
}

