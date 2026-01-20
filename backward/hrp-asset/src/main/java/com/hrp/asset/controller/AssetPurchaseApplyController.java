package com.hrp.asset.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.AssetPurchaseApplyMain;
import com.hrp.common.entity.AssetPurchaseApplyDetail;
import com.hrp.asset.service.AssetPurchaseApplyService;
import com.hrp.asset.feign.AuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset/purchase/apply")
@CrossOrigin
public class AssetPurchaseApplyController {

    @Autowired
    private AssetPurchaseApplyService assetPurchaseApplyService;

    @Autowired(required = false)
    private AuthServiceClient authServiceClient;

    /**
     * 采购申请列表（我的申请）
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<AssetPurchaseApplyMain>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "applyNo", required = false) String applyNo,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户的empId
        Long currentEmpId = null;
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String tokenStr = token.substring(7);
                String account = com.hrp.common.util.JwtUtil.getAccount(tokenStr);
                if (account != null && !account.trim().isEmpty() && authServiceClient != null) {
                    com.hrp.common.entity.Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserByAccount(account);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        com.hrp.common.entity.User user = userResult.getData();
                        currentEmpId = user.getEmpId();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取当前用户empId失败: " + e.getMessage());
            }
        }
        
        // 如果获取到empId，只查询当前用户的申请
        if (currentEmpId != null) {
            List<AssetPurchaseApplyMain> myApplies = assetPurchaseApplyService.getByEmpId(currentEmpId);
            // 前端过滤
            if (applyNo != null && !applyNo.isEmpty()) {
                myApplies = myApplies.stream()
                    .filter(a -> a.getApplyNo() != null && a.getApplyNo().contains(applyNo))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                myApplies = myApplies.stream()
                    .filter(a -> status.equals(String.valueOf(a.getStatus())))
                    .collect(java.util.stream.Collectors.toList());
            }
            // 分页处理
            int start = (page.intValue() - 1) * size.intValue();
            int end = Math.min(start + size.intValue(), myApplies.size());
            List<AssetPurchaseApplyMain> pageList = start < myApplies.size() ? myApplies.subList(start, end) : new java.util.ArrayList<>();
            return Result.success(new com.hrp.common.entity.PageResult<>(pageList, (long) myApplies.size(), size, page));
        } else {
            // 如果没有获取到empId，使用通用查询（但应该不会到这里，因为前端会传token）
            com.hrp.common.entity.PageResult<AssetPurchaseApplyMain> pageResult = assetPurchaseApplyService.getPage(
                    page, size, applyNo, null, status, startDate, endDate);
            return Result.success(pageResult);
        }
    }

    /**
     * 采购审批列表
     */
    @GetMapping("/approval/page")
    public Result<com.hrp.common.entity.PageResult<AssetPurchaseApplyMain>> getApprovalPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "applyNo", required = false) String applyNo,
            @RequestParam(value = "applyEmpName", required = false) String applyEmpName,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestHeader(value = "Authorization", required = false) String token) {
        String currentUserId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            currentUserId = com.hrp.common.util.JwtUtil.getUserId(token);
        }
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return Result.error("未获取到当前用户信息");
        }
        com.hrp.common.entity.PageResult<AssetPurchaseApplyMain> pageResult = assetPurchaseApplyService.getPageByApprover(
                page, size, currentUserId, applyNo, applyEmpName, status, startDate, endDate);
        return Result.success(pageResult);
    }

    /**
     * 采购查询列表
     */
    @GetMapping("/query/page")
    public Result<com.hrp.common.entity.PageResult<AssetPurchaseApplyMain>> getQueryPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "applyNo", required = false) String applyNo,
            @RequestParam(value = "applyEmpName", required = false) String applyEmpName,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        com.hrp.common.entity.PageResult<AssetPurchaseApplyMain> pageResult = assetPurchaseApplyService.getPage(
                page, size, applyNo, applyEmpName, status, startDate, endDate);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取申请详情（包括明细）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable("id") Long id) {
        AssetPurchaseApplyMain apply = assetPurchaseApplyService.getById(id);
        if (apply == null) {
            return Result.error("申请不存在");
        }
        List<AssetPurchaseApplyDetail> details = assetPurchaseApplyService.getDetailsByApplyId(id);
        
        // 优先使用SQL查询时已获取的申请人、部门信息（通过JOIN获取）
        String applyEmpName = apply.getApplyEmpName();
        String applyDeptName = apply.getApplyDeptName();
        
        // 如果SQL查询没有获取到，尝试通过其他方式补充
        if ((applyEmpName == null || applyEmpName.isEmpty()) && authServiceClient != null && apply.getApplyEmpCode() != null && !apply.getApplyEmpCode().isEmpty()) {
            try {
                // 通过工号查询用户信息
                com.hrp.common.entity.Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserByAccount(apply.getApplyEmpCode());
                if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                    com.hrp.common.entity.User user = userResult.getData();
                    if (applyEmpName == null || applyEmpName.isEmpty()) {
                        applyEmpName = user.getName();
                    }
                    if ((applyDeptName == null || applyDeptName.isEmpty()) && user.getDeptName() != null && !user.getDeptName().isEmpty()) {
                        applyDeptName = user.getDeptName();
                    }
                }
            } catch (Exception e) {
                System.err.println("通过工号获取用户信息失败: " + e.getMessage());
            }
        }
        
        // 如果部门名称仍为空，尝试通过部门编码或部门ID查询
        if ((applyDeptName == null || applyDeptName.isEmpty()) && authServiceClient != null && 
            (apply.getApplyDeptCode() != null && !apply.getApplyDeptCode().isEmpty() || apply.getApplyDeptId() != null && !apply.getApplyDeptId().isEmpty())) {
            // 这里可以通过部门服务查询部门名称，但由于是跨服务调用，暂时跳过
            // 实际上SQL查询中已经通过JOIN获取了部门名称，如果为空可能是数据不存在
        }
        
        // 设置申请人、部门名称
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", apply.getId());
        result.put("applyNo", apply.getApplyNo());
        result.put("applyDeptId", apply.getApplyDeptId());
        result.put("applyEmpId", apply.getApplyEmpId());
        result.put("applyEmpName", applyEmpName);
        result.put("applyEmpCode", apply.getApplyEmpCode()); // 申请人工号
        result.put("applyDeptName", applyDeptName);
        result.put("applyDeptCode", apply.getApplyDeptCode()); // 申请部门编码
        result.put("applyTime", apply.getApplyTime());
        result.put("demandDate", apply.getDemandDate());
        result.put("applyReason", apply.getApplyReason());
        result.put("applyMoney", apply.getApplyMoney());
        result.put("status", apply.getStatus());
        result.put("templateConfigId", apply.getTemplateConfigId());
        result.put("mainAttachId", apply.getMainAttachId());
        result.put("createUser", apply.getCreateUser());
        result.put("createTime", apply.getCreateTime());
        result.put("updateTime", apply.getUpdateTime());
        result.put("details", details);
        
        return Result.success(result);
    }

    /**
     * 根据申请单号获取申请详情
     */
    @GetMapping("/no/{applyNo}")
    public Result<AssetPurchaseApplyMain> getByApplyNo(@PathVariable("applyNo") String applyNo) {
        AssetPurchaseApplyMain apply = assetPurchaseApplyService.getByApplyNo(applyNo);
        if (apply == null) {
            return Result.error("申请不存在");
        }
        return Result.success(apply);
    }

    /**
     * 根据申请单号获取申请明细列表
     */
    @GetMapping("/details/{applyNo}")
    public Result<List<AssetPurchaseApplyDetail>> getDetailsByApplyNo(@PathVariable("applyNo") String applyNo) {
        AssetPurchaseApplyMain apply = assetPurchaseApplyService.getByApplyNo(applyNo);
        if (apply == null) {
            return Result.error("申请不存在");
        }
        List<AssetPurchaseApplyDetail> details = assetPurchaseApplyService.getDetailsByApplyId(apply.getId());
        return Result.success(details);
    }

    /**
     * 新增申请
     */
    @PostMapping
    public Result<AssetPurchaseApplyMain> save(@RequestBody Map<String, Object> request, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            AssetPurchaseApplyMain apply = convertToApplyMain(request);
            // 从token获取当前用户信息并设置createUser、applyEmpId、applyDeptId
            if (token != null && token.startsWith("Bearer ")) {
                String tokenStr = token.substring(7);
                String account = com.hrp.common.util.JwtUtil.getAccount(tokenStr);
                if (account != null) {
                    apply.setCreateUser(account);
                    
                    // 如果applyEmpId或applyDeptId为空，从token获取当前用户信息并设置
                    if ((apply.getApplyEmpId() == null || apply.getApplyDeptId() == null || apply.getApplyDeptId().isEmpty()) && authServiceClient != null) {
                        try {
                            com.hrp.common.entity.Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserByAccount(account);
                            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                                com.hrp.common.entity.User user = userResult.getData();
                                if (apply.getApplyEmpId() == null && user.getEmpId() != null) {
                                    apply.setApplyEmpId(user.getEmpId());
                                }
                                if ((apply.getApplyDeptId() == null || apply.getApplyDeptId().isEmpty()) && user.getDeptId() != null) {
                                    apply.setApplyDeptId(user.getDeptId().toString());
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("通过账号获取用户信息失败: " + e.getMessage());
                        }
                    }
                }
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) request.get("details");
            List<AssetPurchaseApplyDetail> details = convertToDetails(detailsList);
            AssetPurchaseApplyMain saved = assetPurchaseApplyService.save(apply, details);
            if (saved != null) {
                return Result.success(saved);
            }
            return Result.error("新增失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 更新申请
     */
    @PutMapping
    public Result<AssetPurchaseApplyMain> update(@RequestBody Map<String, Object> request) {
        try {
            AssetPurchaseApplyMain apply = convertToApplyMain(request);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) request.get("details");
            List<AssetPurchaseApplyDetail> details = convertToDetails(detailsList);
            AssetPurchaseApplyMain updated = assetPurchaseApplyService.update(apply, details);
            if (updated != null) {
                return Result.success(updated);
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新采购申请明细表（用于流程实例变量更新）
     * 注意：此接口只更新明细表，不更新主表
     */
    @PutMapping("/update-details")
    public Result<Boolean> updateAssetPurchaseApplyDetails(@RequestBody Map<String, Object> request) {
        try {
            Long applyId = null;
            if (request.get("applyId") != null) {
                if (request.get("applyId") instanceof Number) {
                    applyId = ((Number) request.get("applyId")).longValue();
                } else {
                    applyId = Long.parseLong(request.get("applyId").toString());
                }
            }
            if (applyId == null) {
                return Result.error("申请ID不能为空");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) request.get("details");
            List<AssetPurchaseApplyDetail> details = convertToDetails(detailsList);
            
            // 获取主表信息（确保申请存在）
            AssetPurchaseApplyMain apply = assetPurchaseApplyService.getById(applyId);
            if (apply == null) {
                return Result.error("申请不存在");
            }
            
            // 调用更新方法（使用现有的主表信息，只更新明细表）
            // update 方法会先删除旧明细，然后保存新明细，主表字段不会被修改（因为我们使用的是现有的主表对象）
            AssetPurchaseApplyMain updated = assetPurchaseApplyService.update(apply, details);
            if (updated != null) {
                return Result.success(true);
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除申请
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean success = assetPurchaseApplyService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 提交申请
     */
    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable("id") Long id) {
        boolean success = assetPurchaseApplyService.submit(id);
        if (success) {
            return Result.success("提交成功");
        }
        return Result.error("提交失败");
    }

    /**
     * 撤回申请
     */
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable("id") Long id) {
        boolean success = assetPurchaseApplyService.withdraw(id);
        return success ? Result.success() : Result.error("撤回失败");
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String userId = request != null ? request.get("userId") : null;
        String opinion = request != null ? request.get("opinion") : null;
        String signature = request != null ? request.get("signature") : null;
        boolean success = assetPurchaseApplyService.approve(id, userId, opinion, signature);
        return success ? Result.success() : Result.error("审批失败");
    }

    /**
     * 审批驳回
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable("id") Long id, @RequestParam(value = "userId") String userId, @RequestParam(value = "opinion", required = false) String opinion) {
        boolean success = assetPurchaseApplyService.reject(id, userId, opinion);
        return success ? Result.success() : Result.error("驳回失败");
    }

    /**
     * 退回申请
     */
    @PostMapping("/{id}/return")
    public Result<Void> returnApply(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String returnType = request != null ? request.get("returnType") : "RETURN_TO_CURRENT";
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = assetPurchaseApplyService.returnApply(id, returnType, opinion);
        return success ? Result.success() : Result.error("退回失败");
    }

    /**
     * 将Map转换为AssetPurchaseApplyMain对象
     */
    private AssetPurchaseApplyMain convertToApplyMain(Map<String, Object> map) {
        AssetPurchaseApplyMain apply = new AssetPurchaseApplyMain();
        if (map.get("id") != null) apply.setId(Long.valueOf(map.get("id").toString()));
        if (map.get("applyNo") != null) apply.setApplyNo(map.get("applyNo").toString());
        if (map.get("applyDeptId") != null) apply.setApplyDeptId(map.get("applyDeptId").toString());
        if (map.get("applyEmpId") != null) apply.setApplyEmpId(Long.valueOf(map.get("applyEmpId").toString()));
        if (map.get("applyTime") != null) {
            try {
                apply.setApplyTime(java.time.LocalDateTime.parse(map.get("applyTime").toString()));
            } catch (Exception e) {
                apply.setApplyTime(java.time.LocalDateTime.now());
            }
        }
        if (map.get("demandDate") != null) {
            try {
                apply.setDemandDate(java.time.LocalDate.parse(map.get("demandDate").toString()));
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        if (map.get("applyReason") != null) apply.setApplyReason(map.get("applyReason").toString());
        if (map.get("applyMoney") != null) {
            try {
                apply.setApplyMoney(new java.math.BigDecimal(map.get("applyMoney").toString()));
            } catch (Exception e) {
                apply.setApplyMoney(java.math.BigDecimal.ZERO);
            }
        }
        if (map.get("status") != null) {
            // 状态值直接使用字符串，对应sys_code的APPLY_STATUS的code_value
            apply.setStatus(map.get("status").toString());
        }
        if (map.get("templateConfigId") != null) apply.setTemplateConfigId(Long.valueOf(map.get("templateConfigId").toString()));
        if (map.get("mainAttachId") != null) apply.setMainAttachId(map.get("mainAttachId").toString());
        if (map.get("createUser") != null) apply.setCreateUser(map.get("createUser").toString());
        return apply;
    }

    /**
     * 将Map列表转换为AssetPurchaseApplyDetail列表
     */
    private List<AssetPurchaseApplyDetail> convertToDetails(List<Map<String, Object>> detailsList) {
        List<AssetPurchaseApplyDetail> details = new java.util.ArrayList<>();
        if (detailsList != null) {
            for (Map<String, Object> detailMap : detailsList) {
                AssetPurchaseApplyDetail detail = new AssetPurchaseApplyDetail();
                if (detailMap.get("id") != null) detail.setId(Long.valueOf(detailMap.get("id").toString()));
                
                // assetCode是必填字段，必须是字符串
                Object assetCodeObj = detailMap.get("assetCode");
                if (assetCodeObj != null) {
                    String assetCodeStr = assetCodeObj.toString().trim();
                    if (!assetCodeStr.isEmpty()) {
                        detail.setAssetCode(assetCodeStr);
                    } else {
                        throw new IllegalArgumentException("资产编码不能为空");
                    }
                } else {
                    throw new IllegalArgumentException("资产编码不能为空");
                }
                
                if (detailMap.get("assetName") != null) detail.setAssetName(detailMap.get("assetName").toString());
                if (detailMap.get("spec") != null) detail.setSpec(detailMap.get("spec").toString());
                if (detailMap.get("manufacturer") != null) detail.setManufacturer(detailMap.get("manufacturer").toString());
                if (detailMap.get("unit") != null) detail.setUnit(detailMap.get("unit").toString());
                if (detailMap.get("applyQuantity") != null) detail.setApplyQuantity(Integer.valueOf(detailMap.get("applyQuantity").toString()));
                if (detailMap.get("price") != null) detail.setPrice(new java.math.BigDecimal(detailMap.get("price").toString()));
                if (detailMap.get("totalPrice") != null) detail.setTotalPrice(new java.math.BigDecimal(detailMap.get("totalPrice").toString()));
                if (detailMap.get("remark") != null) detail.setRemark(detailMap.get("remark").toString());
                details.add(detail);
            }
        }
        return details;
    }
}


