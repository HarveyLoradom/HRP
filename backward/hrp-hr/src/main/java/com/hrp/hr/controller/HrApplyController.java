package com.hrp.hr.controller;

import com.hrp.common.entity.HrApply;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.hr.service.HrApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/apply")
@CrossOrigin
public class HrApplyController {

    @Autowired
    private HrApplyService hrApplyService;

    /**
     * 分页查询业务申请（支持我的申请和审批人视角）
     */
    @GetMapping("/page")
    public Result<PageResult<HrApply>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "applyNo", required = false) String applyNo,
            @RequestParam(value = "empId", required = false) Long empId,
            @RequestParam(value = "empName", required = false) String empName,
            @RequestParam(value = "hrApplyType", required = false) String hrApplyType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "approver", required = false) Boolean approver,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 如果是审批人视角，使用getPageByApprover
        if (Boolean.TRUE.equals(approver)) {
            String currentUserId = null;
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                currentUserId = com.hrp.common.util.JwtUtil.getUserId(token);
            }
            if (currentUserId == null || currentUserId.trim().isEmpty()) {
                return Result.error("未获取到当前用户信息");
            }
            PageResult<HrApply> pageResult = hrApplyService.getPageByApprover(
                    page, size, currentUserId, applyNo, empName, hrApplyType, status, startDate, endDate);
            return Result.success(pageResult);
        } else {
            // 查询列表：显示所有数据或我的申请
            PageResult<HrApply> pageResult = hrApplyService.getPage(
                    page, size, applyNo, empId, hrApplyType, status, startDate, endDate);
            return Result.success(pageResult);
        }
    }
    
    /**
     * 我的申请列表
     */
    @GetMapping("/my/{empId}")
    public Result<List<HrApply>> getMyApplies(@PathVariable("empId") Long empId) {
        List<HrApply> applies = hrApplyService.getMyApplies(empId);
        return Result.success(applies);
    }
    
    /**
     * 我的申请列表（分页）
     */
    @GetMapping("/my/{empId}/page")
    public Result<PageResult<HrApply>> getMyAppliesPage(
            @PathVariable("empId") Long empId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        PageResult<HrApply> pageResult = hrApplyService.getMyAppliesPage(empId, page, size);
        return Result.success(pageResult);
    }
    
    /**
     * 我的审批列表
     */
    @GetMapping("/my-approval/{userId}")
    public Result<List<HrApply>> getMyApprovalApplies(@PathVariable("userId") String userId) {
        List<HrApply> applies = hrApplyService.getMyApprovalApplies(userId);
        return Result.success(applies);
    }
    
    /**
     * 我的审批列表（分页）
     */
    @GetMapping("/my-approval/{userId}/page")
    public Result<PageResult<HrApply>> getMyApprovalAppliesPage(
            @PathVariable("userId") String userId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        PageResult<HrApply> pageResult = hrApplyService.getMyApprovalAppliesPage(userId, page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询业务申请详情
     */
    @GetMapping("/{id}")
    public Result<HrApply> getById(@PathVariable("id") Long id) {
        HrApply hrApply = hrApplyService.getById(id);
        if (hrApply == null) {
            return Result.error("业务申请不存在");
        }
        return Result.success(hrApply);
    }

    /**
     * 根据申请编码查询业务申请详情
     */
    @GetMapping("/apply-no/{applyNo}")
    public Result<HrApply> getByApplyNo(@PathVariable("applyNo") String applyNo) {
        HrApply hrApply = hrApplyService.getByApplyNo(applyNo);
        if (hrApply == null) {
            return Result.error("业务申请不存在");
        }
        return Result.success(hrApply);
    }

    /**
     * 保存业务申请
     */
    @PostMapping
    public Result<HrApply> save(@RequestBody HrApply hrApply, 
                                 @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token获取当前用户账号并设置createUser
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String account = com.hrp.common.util.JwtUtil.getAccount(token);
                if (account != null && (hrApply.getCreateUser() == null || hrApply.getCreateUser().isEmpty())) {
                    hrApply.setCreateUser(account);
                }
            }
            boolean success = hrApplyService.save(hrApply);
            if (success) {
                return Result.success(hrApply);
            }
            return Result.error("保存失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 更新业务申请
     */
    @PutMapping
    public Result<HrApply> update(@RequestBody HrApply hrApply) {
        try {
            boolean success = hrApplyService.update(hrApply);
            if (success) {
                HrApply updated = hrApplyService.getById(hrApply.getApplyId());
                return Result.success(updated);
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 提交申请（启动流程）
     */
    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable("id") Long id) {
        boolean success = hrApplyService.submit(id);
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
        boolean success = hrApplyService.withdraw(id);
        return success ? Result.success() : Result.error("撤回失败");
    }
    
    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String userId = request != null ? request.get("userId") : null;
        String opinion = request != null ? request.get("opinion") : null;
        String approverSignature = request != null ? request.get("approverSignature") : null;
        boolean success = hrApplyService.approve(id, userId, opinion, approverSignature);
        return success ? Result.success() : Result.error("审批失败");
    }
    
    /**
     * 审批驳回
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable("id") Long id, 
                                @RequestParam(value = "userId") String userId, 
                                @RequestParam(value = "opinion", required = false) String opinion) {
        boolean success = hrApplyService.reject(id, userId, opinion);
        return success ? Result.success() : Result.error("驳回失败");
    }
    
    /**
     * 退回申请
     */
    @PostMapping("/{id}/return")
    public Result<Void> returnApply(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String returnType = request != null ? request.get("returnType") : "RETURN_TO_CURRENT";
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = hrApplyService.returnApply(id, returnType, opinion);
        return success ? Result.success() : Result.error("退回失败");
    }

    /**
     * 删除业务申请
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean deleted = hrApplyService.delete(id);
        if (deleted) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 生成申请编码
     */
    @GetMapping("/generate-apply-no")
    public Result<String> generateApplyNo(@RequestParam(value = "hrApplyType") String hrApplyType) {
        String applyNo = hrApplyService.generateApplyNo(hrApplyType);
        return Result.success(applyNo);
    }
}

