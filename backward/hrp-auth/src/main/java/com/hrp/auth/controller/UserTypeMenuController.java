package com.hrp.auth.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.UserTypeMenu;
import com.hrp.auth.service.UserTypeMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户类型菜单管理控制器
 */
@RestController
@RequestMapping("/auth/user-type-menu")
@CrossOrigin
public class UserTypeMenuController {

    @Autowired
    private UserTypeMenuService userTypeMenuService;

    /**
     * 根据用户类型查询菜单列表
     */
    @GetMapping("/user-type/{userType}")
    public Result<List<UserTypeMenu>> getByUserType(@PathVariable String userType) {
        List<UserTypeMenu> list = userTypeMenuService.getByUserType(userType);
        return Result.success(list);
    }

    /**
     * 分配用户类型菜单
     */
    @PostMapping("/assign")
    public Result<String> assignMenus(@RequestParam String userType, @RequestBody List<Long> menuIds) {
        boolean success = userTypeMenuService.assignMenus(userType, menuIds);
        if (success) {
            return Result.success("分配成功");
        }
        return Result.error("分配失败");
    }

    /**
     * 移除用户类型菜单
     */
    @DeleteMapping("/remove")
    public Result<String> removeMenu(@RequestParam String userType, @RequestParam Long menuId) {
        boolean success = userTypeMenuService.removeMenu(userType, menuId);
        if (success) {
            return Result.success("移除成功");
        }
        return Result.error("移除失败");
    }

    /**
     * 清空用户类型所有菜单
     */
    @DeleteMapping("/clear/{userType}")
    public Result<String> clearMenus(@PathVariable String userType) {
        boolean success = userTypeMenuService.clearMenus(userType);
        if (success) {
            return Result.success("清空成功");
        }
        return Result.error("清空失败");
    }
}

