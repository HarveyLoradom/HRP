package com.hrp.user.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.User;
import com.hrp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable String id) {
        User user = userService.getById(id);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 根据账号查询用户
     */
    @GetMapping("/account/{account}")
    public Result<User> getByAccount(@PathVariable String account) {
        User user = userService.getByAccount(account);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 查询所有用户
     */
    @GetMapping("/list")
    public Result<List<User>> getAll() {
        List<User> list = userService.getAll();
        return Result.success(list);
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody User user) {
        // 检查账号是否已存在
        User existUser = userService.getByAccount(user.getAccount());
        if (existUser != null) {
            return Result.error("账号已存在");
        }
        boolean success = userService.save(user);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            return Result.error("用户ID不能为空");
        }
        boolean success = userService.update(user);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        boolean success = userService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 启用/停用用户
     */
    @PutMapping("/toggle-status/{id}")
    public Result<String> toggleStatus(@PathVariable String id) {
        boolean success = userService.toggleStatus(id);
        if (success) {
            return Result.success("操作成功");
        }
        return Result.error("操作失败");
    }

    /**
     * 重置用户密码（使用系统配置的原始密码）
     */
    @PutMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable String id) {
        boolean success = userService.resetPassword(id);
        if (success) {
            return Result.success("密码重置成功");
        }
        return Result.error("密码重置失败");
    }
}
