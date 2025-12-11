package com.hrp.user.controller;

import com.hrp.common.entity.Code;
import com.hrp.common.entity.Result;
import com.hrp.user.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统字典管理控制器
 */
@RestController
@RequestMapping("/user/code")
@CrossOrigin
public class CodeController {

    @Autowired
    private CodeService codeService;

    /**
     * 根据ID查询字典
     */
    @GetMapping("/{id}")
    public Result<Code> getById(@PathVariable String id) {
        Code code = codeService.getById(id);
        if (code != null) {
            return Result.success(code);
        }
        return Result.error("字典不存在");
    }

    /**
     * 根据类型查询字典列表
     */
    @GetMapping("/type/{codeType}")
    public Result<List<Code>> getByType(@PathVariable String codeType) {
        List<Code> list = codeService.getByType(codeType);
        return Result.success(list);
    }

    /**
     * 查询所有字典
     */
    @GetMapping("/list")
    public Result<List<Code>> getAll() {
        List<Code> list = codeService.getAll();
        return Result.success(list);
    }

    /**
     * 新增字典
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Code code) {
        // ID为空时，服务层会自动生成UUID
        boolean success = codeService.save(code);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新字典
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Code code) {
        if (code.getId() == null || code.getId().isEmpty()) {
            return Result.error("字典ID不能为空");
        }
        boolean success = codeService.update(code);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除字典
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        boolean success = codeService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
