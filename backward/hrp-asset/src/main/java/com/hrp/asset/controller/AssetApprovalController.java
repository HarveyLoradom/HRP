package com.hrp.asset.controller;

import com.hrp.asset.service.AssetApprovalService;
import com.hrp.asset.mapper.UserMapper;
import com.hrp.common.entity.AssetApproval;
import com.hrp.common.entity.AssetApprovalDTO;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.User;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset/approval")
@CrossOrigin
public class AssetApprovalController {

    @Autowired
    private AssetApprovalService assetApprovalService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取我的列表（自己发起的+待审批的）
     * 如果是管理员，返回所有数据
     */
    @GetMapping("/my-list")
    public Result<List<AssetApproval>> getMyList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String approvalType,
            @RequestParam(required = false) Long empId) {
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            userId = JwtUtil.getUserId(token.substring(7));
        }
        
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        
        // 获取用户信息，判断是否是管理员
        User user = userMapper.selectById(userId);
        if (user != null && user.getType() != null && user.getType() == 1L) {
            // 管理员，返回所有数据
            List<AssetApproval> approvals;
            if (approvalType != null && !approvalType.isEmpty()) {
                approvals = assetApprovalService.getByType(approvalType);
            } else {
                approvals = assetApprovalService.getAll();
            }
            return Result.success(approvals);
        } else {
            // 普通用户，返回自己发起的和待审批的
            List<AssetApproval> approvals = assetApprovalService.getMyList(userId, empId, approvalType);
            return Result.success(approvals);
        }
    }

    @GetMapping("/my-list/page")
    public Result<com.hrp.common.entity.PageResult<AssetApproval>> getMyListPage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String approvalType,
            @RequestParam(required = false) Long empId,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            userId = JwtUtil.getUserId(token.substring(7));
        }
        
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        
        // 获取用户信息，判断是否是管理员
        User user = userMapper.selectById(userId);
        com.hrp.common.entity.PageResult<AssetApproval> pageResult;
        if (user != null && user.getType() != null && user.getType() == 1L) {
            // 管理员，返回所有数据的分页
            pageResult = assetApprovalService.getAllPage(page, size);
        } else {
            // 普通用户，返回自己发起的和待审批的分页
            pageResult = assetApprovalService.getMyListPage(userId, empId, approvalType, page, size);
        }
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<AssetApproval>> getAll() {
        List<AssetApproval> approvals = assetApprovalService.getAll();
        return Result.success(approvals);
    }

    @GetMapping("/type/{type}")
    public Result<List<AssetApproval>> getByType(@PathVariable String type) {
        List<AssetApproval> approvals = assetApprovalService.getByType(type);
        return Result.success(approvals);
    }

    @GetMapping("/applicant/{applicantId}")
    public Result<List<AssetApproval>> getByApplicant(@PathVariable Long applicantId) {
        List<AssetApproval> approvals = assetApprovalService.getByApplicant(applicantId);
        return Result.success(approvals);
    }

    @GetMapping("/approver/{approverId}")
    public Result<List<AssetApproval>> getByApprover(@PathVariable String approverId) {
        List<AssetApproval> approvals = assetApprovalService.getByApprover(approverId);
        return Result.success(approvals);
    }

    @GetMapping("/status/{status}")
    public Result<List<AssetApproval>> getByStatus(@PathVariable String status) {
        List<AssetApproval> approvals = assetApprovalService.getByStatus(status);
        return Result.success(approvals);
    }

    @GetMapping("/{id}")
    public Result<AssetApproval> getById(@PathVariable Long id) {
        AssetApproval approval = assetApprovalService.getById(id);
        return Result.success(approval);
    }

    @GetMapping("/detail/{id}")
    public Result<AssetApprovalDTO> getDetail(@PathVariable Long id) {
        AssetApprovalDTO dto = assetApprovalService.getDetail(id);
        return Result.success(dto);
    }

    @PostMapping
    public Result<Void> save(@RequestBody AssetApprovalDTO dto) {
        boolean success = assetApprovalService.save(dto);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody AssetApprovalDTO dto) {
        boolean success = assetApprovalService.update(dto);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = assetApprovalService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        boolean success = assetApprovalService.submit(id);
        if (!success) {
            throw new BusinessException("提交失败");
        }
        return Result.success();
    }

    @PostMapping("/withdraw/{id}")
    public Result<Void> withdraw(@PathVariable Long id) {
        boolean success = assetApprovalService.withdraw(id);
        if (!success) {
            throw new BusinessException("撤回失败");
        }
        return Result.success();
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody Map<String, String> params) {
        Long approvalId = Long.parseLong(params.get("approvalId"));
        String userId = params.get("userId");
        String opinion = params.get("opinion");
        boolean success = assetApprovalService.approve(approvalId, userId, opinion);
        if (!success) {
            throw new BusinessException("审批失败");
        }
        return Result.success();
    }

    @PostMapping("/reject")
    public Result<Void> reject(@RequestBody Map<String, String> params) {
        Long approvalId = Long.parseLong(params.get("approvalId"));
        String userId = params.get("userId");
        String opinion = params.get("opinion");
        boolean success = assetApprovalService.reject(approvalId, userId, opinion);
        if (!success) {
            throw new BusinessException("驳回失败");
        }
        return Result.success();
    }

    @GetMapping("/records/{approvalId}")
    public Result<List<com.hrp.common.entity.AssetApprovalRecord>> getRecords(@PathVariable Long approvalId) {
        AssetApprovalDTO dto = assetApprovalService.getDetail(approvalId);
        return Result.success(dto != null ? dto.getRecords() : null);
    }
}
