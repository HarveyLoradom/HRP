package com.hrp.user.controller;

import com.hrp.common.entity.Result;
import com.hrp.user.service.UserMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户菜单权限管理控制器
 */
@RestController
@RequestMapping("/user/user-menu")
@CrossOrigin
public class UserMenuController {

    @Autowired
    private UserMenuService userMenuService;

    /**
     * 根据用户ID查询菜单ID列表
     */
    @GetMapping("/{userId}")
    public Result<List<Long>> getMenuIdsByUserId(@PathVariable String userId) {
        List<Long> menuIds = userMenuService.getMenuIdsByUserId(userId);
        return Result.success(menuIds);
    }

    /**
     * 保存用户菜单权限
     */
    @PostMapping("/assign")
    public Result<String> assignUserMenus(@RequestBody AssignUserMenuRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            return Result.error("用户ID不能为空");
        }
        boolean success = userMenuService.saveUserMenus(request.getUserId(), request.getMenuIds());
        if (success) {
            return Result.success("分配成功");
        }
        return Result.error("分配失败");
    }

    /**
     * 用户菜单权限分配请求对象
     */
    public static class AssignUserMenuRequest {
        private String userId;
        private java.util.List<Long> menuIds;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public java.util.List<Long> getMenuIds() {
            return menuIds;
        }

        public void setMenuIds(java.util.List<Long> menuIds) {
            this.menuIds = menuIds;
        }
    }
}
