package com.hrp.reimb.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.CtrlPayout;
import com.hrp.reimb.service.CtrlPayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reimb/payout")
@CrossOrigin
public class CtrlPayoutController {

    @Autowired
    private CtrlPayoutService ctrlPayoutService;

    /**
     * 我的申请列表
     */
    @GetMapping("/my/{empId}")
    public Result<List<CtrlPayout>> getMyPayouts(@PathVariable Long empId) {
        List<CtrlPayout> payouts = ctrlPayoutService.getMyPayouts(empId);
        return Result.success(payouts);
    }

    /**
     * 根据状态查询申请列表（用于审批）
     */
    @GetMapping("/status/{status}")
    public Result<List<CtrlPayout>> getPayoutsByStatus(@PathVariable String status) {
        List<CtrlPayout> payouts = ctrlPayoutService.getPayoutsByStatus(status);
        return Result.success(payouts);
    }

    /**
     * 根据ID获取申请详情
     */
    @GetMapping("/{id}")
    public Result<CtrlPayout> getById(@PathVariable Long id) {
        CtrlPayout payout = ctrlPayoutService.getById(id);
        return Result.success(payout);
    }

    /**
     * 新增申请
     */
    @PostMapping
    public Result<Void> save(@RequestBody CtrlPayout ctrlPayout) {
        boolean success = ctrlPayoutService.save(ctrlPayout);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 更新申请
     */
    @PutMapping
    public Result<Void> update(@RequestBody CtrlPayout ctrlPayout) {
        boolean success = ctrlPayoutService.update(ctrlPayout);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除申请
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = ctrlPayoutService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 提交申请
     */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        boolean success = ctrlPayoutService.submit(id);
        return success ? Result.success() : Result.error("提交失败");
    }
}

