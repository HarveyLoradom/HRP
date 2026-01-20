package com.hrp.auth.controller;

import com.hrp.auth.service.CodeService;
import com.hrp.common.entity.Code;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.JwtUtil;
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
    public Result<List<Code>> getAll(@RequestParam(value = "isStop", required = false) Long isStop) {
        List<Code> list = codeService.getAll(isStop);
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<Code>> getAllPage(
            @RequestParam(value = "isStop", required = false) Long isStop,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<Code> pageResult = codeService.getAllPage(isStop, page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据类型查询字典列表
     */
    @GetMapping("/type/{codeType}")
    public Result<List<Code>> getByType(@PathVariable("codeType") String codeType, @RequestParam(value = "isStop", required = false) Long isStop) {
        List<Code> list = codeService.getByType(codeType, isStop);
        return Result.success(list);
    }

    @GetMapping("/type/{codeType}/page")
    public Result<com.hrp.common.entity.PageResult<Code>> getByTypePage(
            @PathVariable("codeType") String codeType,
            @RequestParam(value = "isStop", required = false) Long isStop,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<Code> pageResult = codeService.getByTypePage(codeType, isStop, page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增字典
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Code code,
                                @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户账号作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            code.setCreateUser(createUser);
        }
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
    public Result<String> saveBatch(@RequestBody List<Code> codeList,
                                     @RequestHeader(value = "Authorization", required = false) String token) {
        if (codeList == null || codeList.isEmpty()) {
            throw new BusinessException(400, "参数列表不能为空");
        }
        // 从token中获取当前用户账号作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            for (Code code : codeList) {
                if (code.getCreateUser() == null || code.getCreateUser().isEmpty()) {
                    code.setCreateUser(createUser);
                }
            }
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
    public Result<Code> getById(@PathVariable("id") String id) {
        Code code = codeService.getById(id);
        if (code == null) {
            throw new BusinessException(404, "字典不存在");
        }
        return Result.success(code);
    }

    /**
     * 根据codeName查询字典
     */
    @GetMapping("/name/{codeName}")
    public Result<Code> getByCodeName(@PathVariable("codeName") String codeName) {
        Code code = codeService.getByCodeName(codeName);
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
     * 删除字典（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable("id") String id) {
        // 系统参数不允许删除
        Code code = codeService.getById(id);
        if (code != null && "SYSTEM_PARAM".equals(code.getCodeType())) {
            throw new BusinessException("系统参数不允许删除");
        }
        boolean success = codeService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success("删除成功");
    }

    /**
     * 停用/启用字典
     */
    @PostMapping("/{id}/toggle-status")
    public Result<String> toggleStatus(@PathVariable("id") String id) {
        // 系统参数不允许停用/启用
        Code code = codeService.getById(id);
        if (code != null && "SYSTEM_PARAM".equals(code.getCodeType())) {
            throw new BusinessException("系统参数不允许停用/启用");
        }
        boolean success = codeService.toggleStatus(id);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        code = codeService.getById(id);
        String msg = (code != null && code.getIsStop() != null && code.getIsStop() == 1L) ? "停用成功" : "启用成功";
        return Result.success(msg);
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

