package com.hrp.auth.controller;

import com.hrp.auth.service.CodeService;
import com.hrp.common.entity.Code;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统字典管理控制器
 */
@RestController
@RequestMapping("/auth/code")
@CrossOrigin
public class CodeController {

    @Autowired
    private CodeService codeService;

    /**
     * 查询所有字典
     */
    @GetMapping("/list")
    public Result<List<Code>> getAll() {
        List<Code> list = codeService.getAll();
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<Code>> getAllPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<Code> pageResult = codeService.getAllPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据类型查询字典列表
     */
    @GetMapping("/type/{codeType}")
    public Result<List<Code>> getByType(@PathVariable String codeType) {
        List<Code> list = codeService.getByType(codeType);
        return Result.success(list);
    }

    @GetMapping("/type/{codeType}/page")
    public Result<com.hrp.common.entity.PageResult<Code>> getByTypePage(
            @PathVariable String codeType,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<Code> pageResult = codeService.getByTypePage(codeType, page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增字典
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Code code) {
        // ID为空时，服务层会自动生成UUID
        boolean success = codeService.save(code);
        if (!success) {
            throw new BusinessException("新增失败");
        }
        return Result.success("新增成功");
    }

    /**
     * 批量新增字典
     */
    @PostMapping("/saveBatch")
    public Result<String> saveBatch(@RequestBody List<Code> codeList) {
        if (codeList == null || codeList.isEmpty()) {
            throw new BusinessException(400, "参数列表不能为空");
        }
        boolean success = codeService.saveBatch(codeList);
        if (!success) {
            throw new BusinessException("批量新增失败");
        }
        return Result.success("批量新增成功，共创建" + codeList.size() + "个参数");
    }

    /**
     * 根据ID查询字典
     */
    @GetMapping("/{id}")
    public Result<Code> getById(@PathVariable String id) {
        Code code = codeService.getById(id);
        if (code == null) {
            throw new BusinessException(404, "字典不存在");
        }
        return Result.success(code);
    }

    /**
     * 更新字典
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Code code) {
        if (code.getId() == null || code.getId().isEmpty()) {
            throw new BusinessException(400, "字典ID不能为空");
        }
        boolean success = codeService.update(code);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success("更新成功");
    }

    /**
     * 删除字典
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        boolean success = codeService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success("删除成功");
    }
}

