package com.hrp.asset.controller;

import com.hrp.common.entity.ProcurementRequirement;
import com.hrp.common.entity.ProcurementRequirementDTO;
import com.hrp.common.entity.ProcurementRequirementApproval;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.User;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.JwtUtil;
import com.hrp.asset.service.ProcurementRequirementService;
import com.hrp.asset.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset/procurement")
@CrossOrigin
public class ProcurementRequirementController {

    @Autowired
    private ProcurementRequirementService procurementRequirementService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取我的列表（自己发起的+待审批的）
     * 如果是管理员，返回所有数据
     */
    @GetMapping("/my-list")
    public Result<List<ProcurementRequirement>> getMyList(
            @RequestHeader(value = "Authorization", required = false) String token,
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
            List<ProcurementRequirement> requirements = procurementRequirementService.getAll();
            return Result.success(requirements);
        } else {
            // 普通用户，返回自己发起的和待审批的
            List<ProcurementRequirement> requirements = procurementRequirementService.getMyList(userId, empId);
            return Result.success(requirements);
        }
    }

    @GetMapping("/my-list/page")
    public Result<com.hrp.common.entity.PageResult<ProcurementRequirement>> getMyListPage(
            @RequestHeader(value = "Authorization", required = false) String token,
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
        com.hrp.common.entity.PageResult<ProcurementRequirement> pageResult;
        if (user != null && user.getType() != null && user.getType() == 1L) {
            // 管理员，返回所有数据的分页
            pageResult = procurementRequirementService.getAllPage(page, size);
        } else {
            // 普通用户，返回自己发起的和待审批的分页
            pageResult = procurementRequirementService.getMyListPage(userId, empId, page, size);
        }
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    public Result<List<ProcurementRequirement>> getAll() {
        List<ProcurementRequirement> requirements = procurementRequirementService.getAll();
        return Result.success(requirements);
    }

    @GetMapping("/status/{status}")
    public Result<List<ProcurementRequirement>> getByStatus(@PathVariable String status) {
        List<ProcurementRequirement> requirements = procurementRequirementService.getByStatus(status);
        return Result.success(requirements);
    }

    @GetMapping("/applicant/{applicantId}")
    public Result<List<ProcurementRequirement>> getByApplicant(@PathVariable Long applicantId) {
        List<ProcurementRequirement> list = procurementRequirementService.getByApplicant(applicantId);
        return Result.success(list);
    }

    @GetMapping("/approver/{approverId}")
    public Result<List<ProcurementRequirement>> getByApprover(@PathVariable String approverId) {
        List<ProcurementRequirement> list = procurementRequirementService.getByApprover(approverId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ProcurementRequirement> getById(@PathVariable Long id) {
        ProcurementRequirement requirement = procurementRequirementService.getById(id);
        return Result.success(requirement);
    }

    @GetMapping("/detail/{id}")
    public Result<ProcurementRequirementDTO> getDetail(@PathVariable Long id) {
        ProcurementRequirementDTO dto = procurementRequirementService.getDetail(id);
        return Result.success(dto);
    }

    @PostMapping
    public Result<Void> save(@RequestBody ProcurementRequirementDTO dto) {
        boolean success = procurementRequirementService.save(dto);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody ProcurementRequirementDTO dto) {
        boolean success = procurementRequirementService.save(dto);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        boolean success = procurementRequirementService.submit(id);
        if (!success) {
            throw new BusinessException("提交失败");
        }
        return Result.success();
    }

    @PostMapping("/withdraw/{id}")
    public Result<Void> withdraw(@PathVariable Long id) {
        boolean success = procurementRequirementService.withdraw(id);
        if (!success) {
            throw new BusinessException("撤回失败");
        }
        return Result.success();
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody Map<String, Object> params) {
        Long requirementId = Long.valueOf(params.get("requirementId").toString());
        String userId = params.get("userId").toString();
        String opinion = params.get("opinion") != null ? params.get("opinion").toString() : "";
        boolean success = procurementRequirementService.approve(requirementId, userId, opinion);
        if (!success) {
            throw new BusinessException("审批失败");
        }
        return Result.success();
    }

    @PostMapping("/reject")
    public Result<Void> reject(@RequestBody Map<String, Object> params) {
        Long requirementId = Long.valueOf(params.get("requirementId").toString());
        String userId = params.get("userId").toString();
        String opinion = params.get("opinion") != null ? params.get("opinion").toString() : "";
        boolean success = procurementRequirementService.reject(requirementId, userId, opinion);
        if (!success) {
            throw new BusinessException("驳回失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = procurementRequirementService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @GetMapping("/approvals/{requirementId}")
    public Result<List<com.hrp.common.entity.ProcurementRequirementApproval>> getApprovals(@PathVariable Long requirementId) {
        ProcurementRequirementDTO dto = procurementRequirementService.getDetail(requirementId);
        return Result.success(dto != null ? dto.getApprovals() : null);
    }
}

