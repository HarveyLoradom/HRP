package com.hrp.auth.controller;

import com.hrp.auth.service.PasswordChangeLogService;
import com.hrp.common.entity.PasswordChangeLog;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 密码修改日志控制器
 */
@RestController
@RequestMapping("/auth/password-change-log")
@CrossOrigin
public class PasswordChangeLogController {

    @Autowired
    private PasswordChangeLogService passwordChangeLogService;

    /**
     * 根据ID查询日志详情
     */
    @GetMapping("/{logId}")
    public Result<PasswordChangeLog> getById(@PathVariable("logId") Long logId) {
        PasswordChangeLog log = passwordChangeLogService.getById(logId);
        if (log != null) {
            return Result.success(log);
        }
        return Result.error("日志不存在");
    }

    /**
     * 分页查询日志
     */
    @GetMapping("/page")
    public Result<PageResult<PasswordChangeLog>> getPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        
        Map<String, Object> params = new HashMap<>();
        if (keyword != null && !keyword.isEmpty()) {
            params.put("keyword", keyword);
        }
        
        PageResult<PasswordChangeLog> result = passwordChangeLogService.getPage(params, page, size);
        return Result.success(result);
    }

    /**
     * 删除日志
     */
    @DeleteMapping("/{logId}")
    public Result<String> delete(@PathVariable("logId") Long logId) {
        boolean success = passwordChangeLogService.delete(logId);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}

