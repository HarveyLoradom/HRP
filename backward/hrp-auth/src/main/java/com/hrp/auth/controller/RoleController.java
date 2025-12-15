package com.hrp.auth.controller;

import com.hrp.common.entity.Role;
import com.hrp.common.entity.Result;
import com.hrp.auth.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/auth/role")
@CrossOrigin
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{roleId}")
    public Result<Role> getById(@PathVariable Long roleId) {
        Role role = roleService.getById(roleId);
        if (role != null) {
            return Result.success(role);
        }
        return Result.error("角色不存在");
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/list")
    public Result<List<Role>> getAll() {
        List<Role> list = roleService.getAll();
        return Result.success(list);
    }

    /**
     * 分页查询所有角色
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<Role>> getAllPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<Role> pageResult = roleService.getAllPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增角色
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Role role) {
        // 检查编码是否已存在
        Role existRole = roleService.getByCode(role.getRoleCode());
        if (existRole != null) {
            return Result.error("角色编码已存在");
        }
        boolean success = roleService.save(role);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新角色
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Role role) {
        if (role.getRoleId() == null) {
            return Result.error("角色ID不能为空");
        }
        boolean success = roleService.update(role);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public Result<String> delete(@PathVariable Long roleId) {
        boolean success = roleService.delete(roleId);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}

