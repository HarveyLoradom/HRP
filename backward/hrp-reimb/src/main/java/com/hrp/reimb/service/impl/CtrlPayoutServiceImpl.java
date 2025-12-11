package com.hrp.reimb.service.impl;

import com.hrp.reimb.mapper.CtrlPayoutMapper;
import com.hrp.reimb.service.CtrlPayoutService;
import com.hrp.common.entity.CtrlPayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CtrlPayoutServiceImpl implements CtrlPayoutService {

    @Autowired
    private CtrlPayoutMapper ctrlPayoutMapper;

    @Override
    public List<CtrlPayout> getMyPayouts(Long empId) {
        return ctrlPayoutMapper.selectByEmpId(empId);
    }

    @Override
    public List<CtrlPayout> getPayoutsByStatus(String status) {
        return ctrlPayoutMapper.selectByStatus(status);
    }

    @Override
    public CtrlPayout getById(Long id) {
        return ctrlPayoutMapper.selectById(id);
    }

    @Override
    public CtrlPayout getByBillcode(String billcode) {
        return ctrlPayoutMapper.selectByBillcode(billcode);
    }

    @Override
    @Transactional
    public boolean save(CtrlPayout ctrlPayout) {
        if (ctrlPayout.getPayoutBillcode() == null || ctrlPayout.getPayoutBillcode().isEmpty()) {
            String prefix = "APPLY".equals(ctrlPayout.getBillType()) ? "APPLY" : "PAYOUT";
            ctrlPayout.setPayoutBillcode(prefix + System.currentTimeMillis());
        }
        if (ctrlPayout.getBillType() == null || ctrlPayout.getBillType().isEmpty()) {
            ctrlPayout.setBillType("APPLY"); // 默认为申请单
        }
        if (ctrlPayout.getApplyDate() == null) {
            ctrlPayout.setApplyDate(LocalDateTime.now());
        }
        if (ctrlPayout.getStatus() == null || ctrlPayout.getStatus().isEmpty()) {
            ctrlPayout.setStatus("DRAFT");
        }
        return ctrlPayoutMapper.insert(ctrlPayout) > 0;
    }

    @Override
    @Transactional
    public boolean update(CtrlPayout ctrlPayout) {
        return ctrlPayoutMapper.updateById(ctrlPayout) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(id);
        if (payout == null) {
            return false;
        }
        // 只有草稿状态才能删除
        if (!"DRAFT".equals(payout.getStatus())) {
            return false;
        }
        return ctrlPayoutMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean submit(Long payoutId) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        if (!"DRAFT".equals(payout.getStatus())) {
            return false; // 只有草稿状态才能提交
        }
        payout.setStatus("PENDING");
        // TODO: 启动流程实例
        return ctrlPayoutMapper.updateById(payout) > 0;
    }

    @Override
    @Transactional
    public boolean withdraw(Long payoutId) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        if (!"PENDING".equals(payout.getStatus())) {
            return false; // 只有待审批状态才能撤回
        }
        payout.setStatus("WITHDRAWN");
        // TODO: 取消流程实例
        return ctrlPayoutMapper.updateById(payout) > 0;
    }

    @Override
    public List<CtrlPayout> getMyApprovalPayouts(String userId) {
        return ctrlPayoutMapper.selectByApprover(userId);
    }

    @Override
    public List<CtrlPayout> getAllPayouts() {
        return ctrlPayoutMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean approve(Long payoutId, String userId, String opinion) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        // TODO: 处理审批逻辑，更新流程任务，判断是否流程结束
        // 如果流程结束，更新状态为APPROVED
        payout.setStatus("APPROVED");
        payout.setRemark(opinion);
        boolean success = ctrlPayoutMapper.updateById(payout) > 0;
        
        // 保存审批记录
        if (success) {
            // TODO: 从流程任务中获取审批人信息
            // CtrlPayoutApproval approval = new CtrlPayoutApproval();
            // approval.setPayoutId(payoutId);
            // approval.setApproverId(...);
            // approval.setApprovalType("APPROVE");
            // approval.setApprovalOpinion(opinion);
            // approval.setApprovalTime(LocalDateTime.now());
            // ctrlPayoutApprovalMapper.insert(approval);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean reject(Long payoutId, String userId, String opinion) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        // TODO: 处理驳回逻辑，结束流程
        payout.setStatus("REJECTED");
        payout.setRemark(opinion);
        boolean success = ctrlPayoutMapper.updateById(payout) > 0;
        
        // 保存审批记录
        if (success) {
            // TODO: 从流程任务中获取审批人信息
            // CtrlPayoutApproval approval = new CtrlPayoutApproval();
            // approval.setPayoutId(payoutId);
            // approval.setApproverId(...);
            // approval.setApprovalType("REJECT");
            // approval.setApprovalOpinion(opinion);
            // approval.setApprovalTime(LocalDateTime.now());
            // ctrlPayoutApprovalMapper.insert(approval);
        }
        
        return success;
    }

    @Override
    public String getNextApprover(Long payoutId) {
        // TODO: 从流程任务中获取下一个审批人
        return "待实现";
    }
}

