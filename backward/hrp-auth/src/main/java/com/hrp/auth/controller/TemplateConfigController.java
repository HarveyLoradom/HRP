package com.hrp.auth.controller;

import com.hrp.auth.service.TemplateConfigService;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.TemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板设置管理控制器
 */
@RestController
@RequestMapping("/auth/template-config")
@CrossOrigin
public class TemplateConfigController {

    @Autowired
    private TemplateConfigService templateConfigService;

    /**
     * 根据ID查询模板设置
     */
    @GetMapping("/{id}")
    public Result<TemplateConfig> getById(@PathVariable Long id) {
        TemplateConfig config = templateConfigService.getById(id);
        if (config != null) {
            return Result.success(config);
        }
        return Result.error("模板设置不存在");
    }

    /**
     * 根据业务类型和业务类型值查询模板设置
     */
    @GetMapping("/business-type")
    public Result<TemplateConfig> getByBusinessType(
            @RequestParam String businessType,
            @RequestParam String businessTypeValue) {
        TemplateConfig config = templateConfigService.getByBusinessType(businessType, businessTypeValue);
        if (config != null) {
            return Result.success(config);
        }
        return Result.error("未找到对应的模板设置");
    }

    /**
     * 根据业务类型查询模板设置列表
     */
    @GetMapping("/business-type/{businessType}")
    public Result<List<TemplateConfig>> getByBusinessTypeOnly(@PathVariable String businessType) {
        List<TemplateConfig> list = templateConfigService.getByBusinessTypeOnly(businessType);
        return Result.success(list);
    }

    /**
     * 查询所有模板设置
     */
    @GetMapping("/list")
    public Result<List<TemplateConfig>> getAll() {
        List<TemplateConfig> list = templateConfigService.getAll();
        return Result.success(list);
    }

    /**
     * 分页查询所有模板设置
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<TemplateConfig>> getAllPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<TemplateConfig> pageResult = templateConfigService.getAllPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增模板设置
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody TemplateConfig config) {
        // 检查是否已存在相同的业务类型和业务类型值
        TemplateConfig existConfig = templateConfigService.getByBusinessType(
            config.getBusinessType(), config.getBusinessTypeValue());
        if (existConfig != null) {
            return Result.error("该业务类型和业务类型值的配置已存在");
        }
        boolean success = templateConfigService.save(config);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新模板设置
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody TemplateConfig config) {
        if (config.getConfigId() == null) {
            return Result.error("配置ID不能为空");
        }
        boolean success = templateConfigService.update(config);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除模板设置
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = templateConfigService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}

