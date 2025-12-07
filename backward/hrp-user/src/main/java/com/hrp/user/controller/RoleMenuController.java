package com.hrp.user.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.RoleMenu;
import com.hrp.user.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色菜单管理控制器
 */
@RestController
@RequestMapping("/role-menu")
@CrossOrigin
public class RoleMenuController {

    @Autowired
    private RoleMenuService roleMenuService;

    /**
     * 根据角色ID查询菜单列表
     */
    @GetMapping("/role/{roleId}")
    public Result<List<RoleMenu>> getByRoleId(@PathVariable Long roleId) {
        List<RoleMenu> list = roleMenuService.getByRoleId(roleId);
        return Result.success(list);
    }

    /**
     * 分配角色菜单
     */
    @PostMapping("/assign")
    public Result<String> assignMenus(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        boolean success = roleMenuService.assignMenus(roleId, menuIds);
        if (success) {
            return Result.success("分配成功");
        }
        return Result.error("分配失败");
    }

    /**
     * 移除角色菜单
     */
    @DeleteMapping("/remove")
    public Result<String> removeMenu(@RequestParam Long roleId, @RequestParam Long menuId) {
        boolean success = roleMenuService.removeMenu(roleId, menuId);
        if (success) {
            return Result.success("移除成功");
        }
        return Result.error("移除失败");
    }

    /**
     * 清空角色所有菜单
     */
    @DeleteMapping("/clear/{roleId}")
    public Result<String> clearMenus(@PathVariable Long roleId) {
        boolean success = roleMenuService.clearMenus(roleId);
        if (success) {
            return Result.success("清空成功");
        }
        return Result.error("清空失败");
    }
}
