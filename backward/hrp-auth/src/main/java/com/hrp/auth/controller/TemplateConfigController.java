package com.hrp.auth.controller;

import com.hrp.auth.service.TemplateConfigService;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.TemplateConfig;
import com.hrp.common.util.JwtUtil;
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
    public Result<TemplateConfig> getById(@PathVariable("id") Long id) {
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
            @RequestParam(value = "businessType") String businessType,
            @RequestParam(value = "businessTypeValue") String businessTypeValue) {
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
    public Result<List<TemplateConfig>> getByBusinessTypeOnly(@PathVariable("businessType") String businessType) {
        List<TemplateConfig> list = templateConfigService.getByBusinessTypeOnly(businessType);
        return Result.success(list);
    }

    /**
     * 查询所有模板设置
     */
    @GetMapping("/list")
    public Result<List<TemplateConfig>> getAll(@RequestParam(value = "isActive", required = false) Integer isActive) {
        List<TemplateConfig> list = templateConfigService.getAll(isActive);
        return Result.success(list);
    }

    /**
     * 分页查询所有模板设置
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<TemplateConfig>> getAllPage(
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "isActive", required = false) Integer isActive,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<TemplateConfig> pageResult = templateConfigService.getAllPage(businessType,isActive, page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增模板设置
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody TemplateConfig config,
                                @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户账号作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            config.setCreateUser(createUser);
        } else {
            config.setCreateUser("SYSTEM");
        }
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
    public Result<String> delete(@PathVariable("id") Long id) {
        boolean success = templateConfigService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
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

