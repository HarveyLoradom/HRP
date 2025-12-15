package com.hrp.auth.controller;

import com.hrp.auth.service.AddSignService;
import com.hrp.common.entity.AddSignRecord;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 加签管理控制器
 */
@RestController
@RequestMapping("/auth/addsign")
@CrossOrigin
public class AddSignController {

    @Autowired
    private AddSignService addsignService;

    /**
     * 创建加签任务
     */
    @PostMapping("/create")
    public Result<Long> createAddSign(@RequestBody AddSignRequest request) {
        try {
            Long addsignId = addsignService.createAddSign(
                request.getParentTaskId(),
                request.getTargetUserId(),
                request.getTargetUserName(),
                request.getTargetEmpCode(),
                request.getAddsignUserId(),
                request.getAddsignUserName(),
                request.getAddsignEmpCode(),
                request.getAddsignReason()
            );
            return Result.success("加签成功", addsignId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成加签任务
     */
    @PostMapping("/complete")
    public Result<String> completeAddSign(@RequestBody CompleteAddSignRequest request) {
        boolean success = addsignService.completeAddSign(
            request.getAddsignTaskId(),
            request.getApprovalResult(),
            request.getApprovalOpinion()
        );
        if (success) {
            return Result.success("操作成功");
        }
        return Result.error("操作失败");
    }

    /**
     * 根据父任务ID查询加签记录
     */
    @GetMapping("/parent-task/{parentTaskId}")
    public Result<List<AddSignRecord>> getAddSignRecords(@PathVariable Long parentTaskId) {
        List<AddSignRecord> records = addsignService.getAddSignRecordsByParentTaskId(parentTaskId);
        return Result.success(records);
    }

    /**
     * 加签请求对象
     */
    public static class AddSignRequest {
        private Long parentTaskId;
        private String targetUserId;
        private String targetUserName;
        private String targetEmpCode;
        private String addsignUserId;
        private String addsignUserName;
        private String addsignEmpCode;
        private String addsignReason;

        // Getters and Setters
        public Long getParentTaskId() { return parentTaskId; }
        public void setParentTaskId(Long parentTaskId) { this.parentTaskId = parentTaskId; }
        public String getTargetUserId() { return targetUserId; }
        public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
        public String getTargetUserName() { return targetUserName; }
        public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
        public String getTargetEmpCode() { return targetEmpCode; }
        public void setTargetEmpCode(String targetEmpCode) { this.targetEmpCode = targetEmpCode; }
        public String getAddsignUserId() { return addsignUserId; }
        public void setAddsignUserId(String addsignUserId) { this.addsignUserId = addsignUserId; }
        public String getAddsignUserName() { return addsignUserName; }
        public void setAddsignUserName(String addsignUserName) { this.addsignUserName = addsignUserName; }
        public String getAddsignEmpCode() { return addsignEmpCode; }
        public void setAddsignEmpCode(String addsignEmpCode) { this.addsignEmpCode = addsignEmpCode; }
        public String getAddsignReason() { return addsignReason; }
        public void setAddsignReason(String addsignReason) { this.addsignReason = addsignReason; }
    }

    /**
     * 完成加签请求对象
     */
    public static class CompleteAddSignRequest {
        private Long addsignTaskId;
        private String approvalResult;
        private String approvalOpinion;

        // Getters and Setters
        public Long getAddsignTaskId() { return addsignTaskId; }
        public void setAddsignTaskId(Long addsignTaskId) { this.addsignTaskId = addsignTaskId; }
        public String getApprovalResult() { return approvalResult; }
        public void setApprovalResult(String approvalResult) { this.approvalResult = approvalResult; }
        public String getApprovalOpinion() { return approvalOpinion; }
        public void setApprovalOpinion(String approvalOpinion) { this.approvalOpinion = approvalOpinion; }
    }
}

