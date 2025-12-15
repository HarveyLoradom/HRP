package com.hrp.auth.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.UserRole;
import com.hrp.auth.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户角色管理控制器
 */
@RestController
@RequestMapping("/auth/user-role")
@CrossOrigin
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 根据用户ID查询角色列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<UserRole>> getByUserId(@PathVariable String userId) {
        List<UserRole> list = userRoleService.getByUserId(userId);
        return Result.success(list);
    }

    /**
     * 分配用户角色
     */
    @PostMapping("/assign")
    public Result<String> assignRoles(@RequestParam String userId, @RequestBody List<Long> roleIds) {
        boolean success = userRoleService.assignRoles(userId, roleIds);
        if (success) {
            return Result.success("分配成功");
        }
        return Result.error("分配失败");
    }

    /**
     * 移除用户角色
     */
    @DeleteMapping("/remove")
    public Result<String> removeRole(@RequestParam String userId, @RequestParam Long roleId) {
        boolean success = userRoleService.removeRole(userId, roleId);
        if (success) {
            return Result.success("移除成功");
        }
        return Result.error("移除失败");
    }

    /**
     * 清空用户所有角色
     */
    @DeleteMapping("/clear/{userId}")
    public Result<String> clearRoles(@PathVariable String userId) {
        boolean success = userRoleService.clearRoles(userId);
        if (success) {
            return Result.success("清空成功");
        }
        return Result.error("清空失败");
    }

    /**
     * 批量分配用户角色
     */
    @PostMapping("/batch-assign")
    public Result<String> batchAssignRoles(@RequestBody java.util.Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) params.get("userIds");
        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) params.get("roleIds");
        
        if (userIds == null || userIds.isEmpty()) {
            return Result.error("请选择用户");
        }
        
        boolean success = userRoleService.batchAssignRoles(userIds, roleIds);
        if (success) {
            return Result.success("批量分配成功");
        }
        return Result.error("批量分配失败");
    }
}

