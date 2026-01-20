package com.hrp.reimb.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.Supplier;
import com.hrp.common.util.JwtUtil;
import com.hrp.reimb.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商控制器
 */
@RestController
@RequestMapping("/reimb/supplier")
@CrossOrigin
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 获取所有供应商
     */
    @GetMapping("/list")
    public Result<List<Supplier>> getAll(
            @RequestParam(value = "supplierCode", required = false) String supplierCode,
            @RequestParam(value = "supplierName", required = false) String supplierName,
            @RequestParam(value = "isStop", required = false) Long isStop) {
        List<Supplier> list = supplierService.getByConditions(supplierCode, supplierName, isStop);
        return Result.success(list);
    }

    /**
     * 根据ID获取供应商
     */
    @GetMapping("/{id}")
    public Result<Supplier> getById(@PathVariable("id") Long id) {
        Supplier supplier = supplierService.getById(id);
        return Result.success(supplier);
    }

    /**
     * 新增供应商
     */
    @PostMapping
    public Result<Void> save(@RequestBody Supplier supplier,
                             @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户账号作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            supplier.setCreateUser(createUser);
        } else {
            supplier.setCreateUser("SYSTEM");
        }
        boolean success = supplierService.save(supplier);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 更新供应商
     */
    @PutMapping
    public Result<Void> update(@RequestBody Supplier supplier) {
        boolean success = supplierService.update(supplier);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除供应商（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = supplierService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 停用供应商
     */
    @PostMapping("/stop/{id}")
    public Result<Void> stop(@PathVariable("id") Long id) {
        boolean success = supplierService.stop(id);
        return success ? Result.success() : Result.error("停用失败");
    }

    /**
     * 启用供应商
     */
    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable("id") Long id) {
        boolean success = supplierService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }

    /**
     * 从token中获取当前用户账号
     */
    private String getCurrentUserAccount(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 解析token获取账号
            String account = JwtUtil.getAccount(token);
            return account;
        } catch (Exception e) {
            return null;
        }
    }
}

