package com.hrp.hr.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.hr.service.BusinessApplyService;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/business-apply")
@CrossOrigin
public class BusinessApplyController {

    @Autowired
    private BusinessApplyService businessApplyService;

    @GetMapping("/my-list")
    public Result<List<?>> getMyList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long empId) {
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            userId = JwtUtil.getUserId(token.substring(7));
        }
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        // 简化处理，直接返回用户自己的数据
        return Result.success(businessApplyService.getMyList(userId, empId));
    }

    @GetMapping("/list")
    public Result<List<?>> getAll() {
        return Result.success(businessApplyService.getAll());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(businessApplyService.getById(id));
    }

    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(businessApplyService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Map<String, Object> data) {
        boolean success = businessApplyService.save(data);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Map<String, Object> data) {
        boolean success = businessApplyService.update(data);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = businessApplyService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        boolean success = businessApplyService.submit(id);
        if (!success) {
            throw new BusinessException("提交失败");
        }
        return Result.success();
    }

    @PostMapping("/withdraw/{id}")
    public Result<Void> withdraw(@PathVariable Long id) {
        boolean success = businessApplyService.withdraw(id);
        if (!success) {
            throw new BusinessException("撤回失败");
        }
        return Result.success();
    }

    @GetMapping("/records/{id}")
    public Result<List<?>> getRecords(@PathVariable Long id) {
        return Result.success(businessApplyService.getRecords(id));
    }
}

