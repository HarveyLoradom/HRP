package com.hrp.menu.controller;

import com.hrp.common.entity.Menu;
import com.hrp.common.entity.Result;
import com.hrp.menu.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@CrossOrigin
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取所有菜单（树形结构）
     */
    @GetMapping("/list")
    public Result<List<Menu>> getAllMenus() {
        List<Menu> menus = menuService.getAllMenus();
        return Result.success(menus);
    }

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public Result<List<Menu>> getMenuTree() {
        List<Menu> menus = menuService.getAllMenus();
        return Result.success(menus);
    }

    /**
     * 根据用户ID获取菜单列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Menu>> getMenusByUserId(@PathVariable String userId) {
        List<Menu> menus = menuService.getMenusByUserId(userId);
        return Result.success(menus);
    }

    /**
     * 根据ID获取菜单
     */
    @GetMapping("/{id}")
    public Result<Menu> getById(@PathVariable Long id) {
        Menu menu = menuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @PostMapping
    public Result<Void> save(@RequestBody Menu menu) {
        boolean success = menuService.save(menu);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 更新菜单
     */
    @PutMapping
    public Result<Void> update(@RequestBody Menu menu) {
        boolean success = menuService.update(menu);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = menuService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}
