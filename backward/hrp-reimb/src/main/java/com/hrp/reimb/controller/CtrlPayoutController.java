package com.hrp.reimb.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.CtrlPayout;
import com.hrp.common.entity.CtrlPayoutDTO;
import com.hrp.common.entity.CtrlPayoutDetail;
import com.hrp.common.entity.CtrlPayoutInvoice;
import com.hrp.common.entity.CtrlPayoutPayment;
import com.hrp.common.entity.CtrlPayoutApproval;
import com.hrp.reimb.service.CtrlPayoutService;
import com.hrp.reimb.service.CtrlPayoutDetailService;
import com.hrp.reimb.service.CtrlPayoutInvoiceService;
import com.hrp.reimb.service.CtrlPayoutPaymentService;
import com.hrp.reimb.service.CtrlPayoutApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reimb/payout")
@CrossOrigin
public class CtrlPayoutController {

    @Autowired
    private CtrlPayoutService ctrlPayoutService;

    @Autowired
    private CtrlPayoutDetailService ctrlPayoutDetailService;

    @Autowired
    private CtrlPayoutInvoiceService ctrlPayoutInvoiceService;

    @Autowired
    private CtrlPayoutPaymentService ctrlPayoutPaymentService;

    @Autowired
    private CtrlPayoutApprovalService ctrlPayoutApprovalService;

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
     * 根据ID获取申请详情（仅主表）
     */
    @GetMapping("/{id}")
    public Result<CtrlPayout> getById(@PathVariable Long id) {
        CtrlPayout payout = ctrlPayoutService.getById(id);
        return Result.success(payout);
    }

    /**
     * 根据ID获取完整信息（包括明细、发票、支付清单、审批记录）
     */
    @GetMapping("/{id}/detail")
    public Result<CtrlPayoutDTO> getDetailById(@PathVariable Long id) {
        CtrlPayout payout = ctrlPayoutService.getById(id);
        if (payout == null) {
            return Result.error("单据不存在");
        }
        
        CtrlPayoutDTO dto = new CtrlPayoutDTO();
        dto.setPayout(payout);
        
        // 如果是报账单，获取明细、发票、支付清单
        if ("PAYOUT".equals(payout.getBillType())) {
            dto.setDetails(ctrlPayoutDetailService.getByPayoutId(id));
            dto.setInvoices(ctrlPayoutInvoiceService.getByPayoutId(id));
            dto.setPayments(ctrlPayoutPaymentService.getByPayoutId(id));
        }
        
        // 获取审批记录
        dto.setApprovals(ctrlPayoutApprovalService.getByPayoutId(id));
        
        return Result.success(dto);
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
    public Result<String> submit(@PathVariable Long id) {
        boolean success = ctrlPayoutService.submit(id);
        if (success) {
            String nextApprover = ctrlPayoutService.getNextApprover(id);
            return Result.success("提交成功，下一个审批人：" + nextApprover);
        }
        return Result.error("提交失败");
    }

    /**
     * 撤回申请
     */
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        boolean success = ctrlPayoutService.withdraw(id);
        return success ? Result.success() : Result.error("撤回失败");
    }

    /**
     * 获取我的审批列表
     */
    @GetMapping("/my-approval/{userId}")
    public Result<List<CtrlPayout>> getMyApprovalPayouts(@PathVariable String userId) {
        List<CtrlPayout> payouts = ctrlPayoutService.getMyApprovalPayouts(userId);
        return Result.success(payouts);
    }

    /**
     * 获取所有申请（管理员查询）
     */
    @GetMapping("/all")
    public Result<List<CtrlPayout>> getAllPayouts() {
        List<CtrlPayout> payouts = ctrlPayoutService.getAllPayouts();
        return Result.success(payouts);
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestParam String userId, @RequestParam(required = false) String opinion) {
        boolean success = ctrlPayoutService.approve(id, userId, opinion);
        return success ? Result.success() : Result.error("审批失败");
    }

    /**
     * 审批驳回
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String userId, @RequestParam(required = false) String opinion) {
        boolean success = ctrlPayoutService.reject(id, userId, opinion);
        return success ? Result.success() : Result.error("驳回失败");
    }

    /**
     * 获取下一个审批人
     */
    @GetMapping("/{id}/next-approver")
    public Result<String> getNextApprover(@PathVariable Long id) {
        String nextApprover = ctrlPayoutService.getNextApprover(id);
        return Result.success(nextApprover);
    }

    /**
     * 获取审批记录
     */
    @GetMapping("/{id}/approvals")
    public Result<List<CtrlPayoutApproval>> getApprovals(@PathVariable Long id) {
        List<CtrlPayoutApproval> approvals = ctrlPayoutApprovalService.getByPayoutId(id);
        return Result.success(approvals);
    }

    /**
     * 保存报账单完整信息（包括明细、发票、支付清单）
     */
    @PostMapping("/payout/save")
    public Result<Void> savePayout(@RequestBody CtrlPayoutDTO dto) {
        CtrlPayout payout = dto.getPayout();
        if (payout == null) {
            return Result.error("主表信息不能为空");
        }
        
        // 设置单据类型为报账单
        payout.setBillType("PAYOUT");
        
        // 保存主表
        boolean success = ctrlPayoutService.save(payout);
        if (!success) {
            return Result.error("保存主表失败");
        }
        
        // 保存明细
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (CtrlPayoutDetail detail : dto.getDetails()) {
                detail.setPayoutId(payout.getPayoutId());
            }
            if (!ctrlPayoutDetailService.saveBatch(dto.getDetails())) {
                return Result.error("保存明细失败");
            }
        }
        
        // 保存发票
        if (dto.getInvoices() != null && !dto.getInvoices().isEmpty()) {
            for (CtrlPayoutInvoice invoice : dto.getInvoices()) {
                invoice.setPayoutId(payout.getPayoutId());
            }
            if (!ctrlPayoutInvoiceService.saveBatch(dto.getInvoices())) {
                return Result.error("保存发票失败");
            }
        }
        
        // 保存支付清单
        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            for (CtrlPayoutPayment payment : dto.getPayments()) {
                payment.setPayoutId(payout.getPayoutId());
            }
            if (!ctrlPayoutPaymentService.saveBatch(dto.getPayments())) {
                return Result.error("保存支付清单失败");
            }
        }
        
        return Result.success();
    }

    /**
     * 更新报账单完整信息
     */
    @PutMapping("/payout/update")
    public Result<Void> updatePayout(@RequestBody CtrlPayoutDTO dto) {
        CtrlPayout payout = dto.getPayout();
        if (payout == null || payout.getPayoutId() == null) {
            return Result.error("主表信息不能为空");
        }
        
        // 更新主表
        boolean success = ctrlPayoutService.update(payout);
        if (!success) {
            return Result.error("更新主表失败");
        }
        
        // 删除旧的明细、发票、支付清单
        ctrlPayoutDetailService.deleteByPayoutId(payout.getPayoutId());
        ctrlPayoutInvoiceService.deleteByPayoutId(payout.getPayoutId());
        ctrlPayoutPaymentService.deleteByPayoutId(payout.getPayoutId());
        
        // 保存新的明细、发票、支付清单
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (CtrlPayoutDetail detail : dto.getDetails()) {
                detail.setPayoutId(payout.getPayoutId());
            }
            ctrlPayoutDetailService.saveBatch(dto.getDetails());
        }
        
        if (dto.getInvoices() != null && !dto.getInvoices().isEmpty()) {
            for (CtrlPayoutInvoice invoice : dto.getInvoices()) {
                invoice.setPayoutId(payout.getPayoutId());
            }
            ctrlPayoutInvoiceService.saveBatch(dto.getInvoices());
        }
        
        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            for (CtrlPayoutPayment payment : dto.getPayments()) {
                payment.setPayoutId(payout.getPayoutId());
            }
            ctrlPayoutPaymentService.saveBatch(dto.getPayments());
        }
        
        return Result.success();
    }
}

