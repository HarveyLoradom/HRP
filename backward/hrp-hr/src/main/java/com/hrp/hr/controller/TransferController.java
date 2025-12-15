package com.hrp.hr.controller;

import com.hrp.common.entity.Result;
import com.hrp.hr.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/transfer")
@CrossOrigin
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping("/my-list")
    public Result<List<?>> getMyList(@RequestParam(required = false) Long empId) {
        return Result.success(transferService.getMyList(empId));
    }

    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(transferService.getDetail(id));
    }

    @GetMapping("/records/{id}")
    public Result<List<?>> getRecords(@PathVariable Long id) {
        return Result.success(transferService.getRecords(id));
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("transferId").toString());
        String userId = params.get("userId").toString();
        String opinion = params.get("opinion") != null ? params.get("opinion").toString() : "";
        boolean success = transferService.approve(id, userId, opinion);
        return success ? Result.success() : Result.error("审批失败");
    }

    @PostMapping("/reject")
    public Result<Void> reject(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("transferId").toString());
        String userId = params.get("userId").toString();
        String opinion = params.get("opinion") != null ? params.get("opinion").toString() : "";
        boolean success = transferService.reject(id, userId, opinion);
        return success ? Result.success() : Result.error("驳回失败");
    }
}

