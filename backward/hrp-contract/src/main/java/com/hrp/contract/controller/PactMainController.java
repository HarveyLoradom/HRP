package com.hrp.contract.controller;

import com.hrp.common.entity.PactMain;
import com.hrp.common.entity.Result;
import com.hrp.contract.service.PactMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract")
@CrossOrigin
public class PactMainController {

    @Autowired
    private PactMainService pactMainService;

    @GetMapping("/list")
    public Result<List<PactMain>> getAll() {
        List<PactMain> contracts = pactMainService.getAll();
        return Result.success(contracts);
    }

    @GetMapping("/status/{status}")
    public Result<List<PactMain>> getByStatus(@PathVariable String status) {
        List<PactMain> contracts = pactMainService.getByStatus(status);
        return Result.success(contracts);
    }

    @GetMapping("/{id}")
    public Result<PactMain> getById(@PathVariable Long id) {
        PactMain contract = pactMainService.getById(id);
        return Result.success(contract);
    }

    @PostMapping
    public Result<Void> save(@RequestBody PactMain pactMain) {
        boolean success = pactMainService.save(pactMain);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody PactMain pactMain) {
        boolean success = pactMainService.update(pactMain);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = pactMainService.delete(id);
        return success ? Result.success() : Result.error("删除失败，只有草稿状态的合同才能删除");
    }

    /**
     * 提交合同
     */
    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id) {
        boolean success = pactMainService.submit(id);
        if (success) {
            String nextApprover = pactMainService.getNextApprover(id);
            return Result.success("提交成功，下一个审批人：" + nextApprover);
        }
        return Result.error("提交失败");
    }

    /**
     * 撤回合同
     */
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        boolean success = pactMainService.withdraw(id);
        return success ? Result.success() : Result.error("撤回失败");
    }

    /**
     * 获取我的审批列表
     */
    @GetMapping("/my-approval/{userId}")
    public Result<List<PactMain>> getMyApprovalContracts(@PathVariable String userId) {
        List<PactMain> contracts = pactMainService.getMyApprovalContracts(userId);
        return Result.success(contracts);
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestParam String userId, @RequestParam(required = false) String opinion) {
        boolean success = pactMainService.approve(id, userId, opinion);
        return success ? Result.success() : Result.error("审批失败");
    }

    /**
     * 审批驳回
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String userId, @RequestParam(required = false) String opinion) {
        boolean success = pactMainService.reject(id, userId, opinion);
        return success ? Result.success() : Result.error("驳回失败");
    }

    /**
     * 归档合同
     */
    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        boolean success = pactMainService.archive(id);
        return success ? Result.success() : Result.error("归档失败");
    }

    /**
     * 获取下一个审批人
     */
    @GetMapping("/{id}/next-approver")
    public Result<String> getNextApprover(@PathVariable Long id) {
        String nextApprover = pactMainService.getNextApprover(id);
        return Result.success(nextApprover);
    }
}

