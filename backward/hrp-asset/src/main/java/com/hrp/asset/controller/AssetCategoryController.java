package com.hrp.asset.controller;

import com.hrp.asset.service.AssetCategoryService;
import com.hrp.common.entity.AssetCategory;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.PageResult;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产分类管理控制器
 */
@RestController
@RequestMapping("/asset/category")
@CrossOrigin
public class AssetCategoryController {

    @Autowired
    private AssetCategoryService assetCategoryService;

    /**
     * 分页查询一级分类
     */
    @GetMapping("/level1/page")
    public Result<PageResult<AssetCategory>> getLevel1Page(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "categoryName", required = false) String categoryName) {
        PageResult<AssetCategory> pageResult = assetCategoryService.getLevel1Page(page, size, status, categoryName);
        return Result.success(pageResult);
    }

    /**
     * 分页查询二级分类
     */
    @GetMapping("/level2/page")
    public Result<PageResult<AssetCategory>> getLevel2Page(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "categoryName", required = false) String categoryName) {
        PageResult<AssetCategory> pageResult = assetCategoryService.getLevel2Page(page, size, parentId, status, categoryName);
        return Result.success(pageResult);
    }

    /**
     * 分页查询三级分类
     */
    @GetMapping("/level3/page")
    public Result<PageResult<AssetCategory>> getLevel3Page(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "level1Id", required = false) Long level1Id,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "categoryName", required = false) String categoryName) {
        PageResult<AssetCategory> pageResult = assetCategoryService.getLevel3Page(page, size, parentId, level1Id, status, categoryName);
        return Result.success(pageResult);
    }

    /**
     * 查询所有一级分类（用于下拉选择）
     */
    @GetMapping("/level1/list")
    public Result<List<AssetCategory>> getLevel1List(@RequestParam(value = "status", required = false) Integer status) {
        List<AssetCategory> list = assetCategoryService.getLevel1List(status);
        return Result.success(list);
    }

    /**
     * 查询指定一级分类下的所有二级分类（用于下拉选择）
     */
    @GetMapping("/level2/list")
    public Result<List<AssetCategory>> getLevel2List(
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "status", required = false) Integer status) {
        List<AssetCategory> list = assetCategoryService.getLevel2List(parentId, status);
        return Result.success(list);
    }

    /**
     * 查询指定二级分类下的所有三级分类（用于下拉选择）
     */
    @GetMapping("/level3/list")
    public Result<List<AssetCategory>> getLevel3List(
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "status", required = false) Integer status) {
        List<AssetCategory> list = assetCategoryService.getLevel3List(parentId, status);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<AssetCategory> getById(@PathVariable("id") Long id) {
        AssetCategory category = assetCategoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 新增一级分类
     */
    @PostMapping("/level1")
    public Result<Void> saveLevel1(@RequestBody AssetCategory category,
                                    @RequestHeader(value = "Authorization", required = false) String token) {
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            category.setCreateUser(createUser);
        } else {
            category.setCreateUser("SYSTEM");
        }
        category.setLevel(1);
        boolean success = assetCategoryService.save(category);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 新增二级分类
     */
    @PostMapping("/level2")
    public Result<Void> saveLevel2(@RequestBody AssetCategory category,
                                   @RequestHeader(value = "Authorization", required = false) String token) {
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            category.setCreateUser(createUser);
        } else {
            category.setCreateUser("SYSTEM");
        }
        category.setLevel(2);
        boolean success = assetCategoryService.save(category);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 新增三级分类
     */
    @PostMapping("/level3")
    public Result<Void> saveLevel3(@RequestBody AssetCategory category,
                                   @RequestHeader(value = "Authorization", required = false) String token) {
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            category.setCreateUser(createUser);
        } else {
            category.setCreateUser("SYSTEM");
        }
        category.setLevel(3);
        boolean success = assetCategoryService.save(category);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 更新分类
     */
    @PutMapping
    public Result<Void> update(@RequestBody AssetCategory category) {
        boolean success = assetCategoryService.update(category);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除分类（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            boolean success = assetCategoryService.delete(id);
            return success ? Result.success() : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 停用分类
     */
    @PostMapping("/{id}/stop")
    public Result<Void> stop(@PathVariable("id") Long id) {
        try {
            boolean success = assetCategoryService.stop(id);
            return success ? Result.success() : Result.error("停用失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启用分类
     */
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable("id") Long id) {
        boolean success = assetCategoryService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }

    /**
     * 从token中获取当前用户账号（emp_code）
     */
    private String getCurrentUserAccount(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return JwtUtil.getAccount(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

