package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.AssetApprovalDetailMapper;
import com.hrp.asset.mapper.AssetApprovalMapper;
import com.hrp.asset.mapper.AssetApprovalRecordMapper;
import com.hrp.asset.service.AssetApprovalService;
import com.hrp.common.entity.AssetApproval;
import com.hrp.common.entity.AssetApprovalDTO;
import com.hrp.common.entity.AssetApprovalDetail;
import com.hrp.common.entity.AssetApprovalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AssetApprovalServiceImpl implements AssetApprovalService {

    @Autowired
    private AssetApprovalMapper assetApprovalMapper;

    @Autowired
    private AssetApprovalDetailMapper assetApprovalDetailMapper;

    @Autowired
    private AssetApprovalRecordMapper assetApprovalRecordMapper;

    @Override
    public List<AssetApproval> getAll() {
        return assetApprovalMapper.selectAll();
    }

    @Override
    public List<AssetApproval> getByType(String approvalType) {
        return assetApprovalMapper.selectByType(approvalType);
    }

    @Override
    public List<AssetApproval> getByApplicant(Long applicantId) {
        return assetApprovalMapper.selectByApplicant(applicantId);
    }

    @Override
    public List<AssetApproval> getByApprover(String approverId) {
        return assetApprovalMapper.selectByApprover(approverId);
    }

    @Override
    public List<AssetApproval> getByStatus(String status) {
        return assetApprovalMapper.selectByStatus(status);
    }

    @Override
    public List<AssetApproval> getMyList(String userId, Long empId, String approvalType) {
        // 获取自己发起的和待自己审批的数据
        List<AssetApproval> myCreated = empId != null ? assetApprovalMapper.selectByApplicant(empId) : Collections.emptyList();
        List<AssetApproval> myApproval = assetApprovalMapper.selectByApprover(userId);
        
        // 合并去重
        Set<Long> idSet = new HashSet<>();
        List<AssetApproval> result = new ArrayList<>();
        
        // 添加自己发起的
        if (myCreated != null) {
            for (AssetApproval item : myCreated) {
                if ((approvalType == null || approvalType.equals(item.getApprovalType())) 
                    && !idSet.contains(item.getApprovalId())) {
                    idSet.add(item.getApprovalId());
                    result.add(item);
                }
            }
        }
        
        // 添加待审批的
        if (myApproval != null) {
            for (AssetApproval item : myApproval) {
                if ((approvalType == null || approvalType.equals(item.getApprovalType())) 
                    && !idSet.contains(item.getApprovalId())) {
                    idSet.add(item.getApprovalId());
                    result.add(item);
                }
            }
        }
        
        // 按创建时间倒序排序
        result.sort((a, b) -> {
            if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        
        return result;
    }

    @Override
    public AssetApproval getById(Long id) {
        return assetApprovalMapper.selectById(id);
    }

    @Override
    public AssetApprovalDTO getDetail(Long id) {
        AssetApproval approval = assetApprovalMapper.selectById(id);
        if (approval == null) {
            return null;
        }
        AssetApprovalDTO dto = new AssetApprovalDTO();
        dto.setApproval(approval);
        dto.setDetails(assetApprovalDetailMapper.selectByApprovalId(id));
        dto.setRecords(assetApprovalRecordMapper.selectByApprovalId(id));
        return dto;
    }

    @Override
    @Transactional
    public boolean save(AssetApprovalDTO dto) {
        AssetApproval approval = dto.getApproval();
        if (approval == null) {
            return false;
        }
        
        // 生成审批单号
        if (approval.getApprovalNo() == null || approval.getApprovalNo().isEmpty()) {
            String prefix = getApprovalTypePrefix(approval.getApprovalType());
            approval.setApprovalNo(prefix + System.currentTimeMillis());
        }
        
        if (approval.getStatus() == null || approval.getStatus().isEmpty()) {
            approval.setStatus("DRAFT");
        }
        
        int result = assetApprovalMapper.insert(approval);
        if (result > 0 && dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (AssetApprovalDetail detail : dto.getDetails()) {
                detail.setApprovalId(approval.getApprovalId());
                assetApprovalDetailMapper.insert(detail);
            }
        }
        
        return result > 0;
    }

    @Override
    @Transactional
    public boolean update(AssetApprovalDTO dto) {
        AssetApproval approval = dto.getApproval();
        if (approval == null || approval.getApprovalId() == null) {
            return false;
        }
        
        // 只能修改草稿状态的审批单
        AssetApproval existing = assetApprovalMapper.selectById(approval.getApprovalId());
        if (existing == null || !"DRAFT".equals(existing.getStatus())) {
            return false;
        }
        
        int result = assetApprovalMapper.updateById(approval);
        if (result > 0) {
            // 删除原有明细，重新插入
            assetApprovalDetailMapper.deleteByApprovalId(approval.getApprovalId());
            if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
                for (AssetApprovalDetail detail : dto.getDetails()) {
                    detail.setApprovalId(approval.getApprovalId());
                    assetApprovalDetailMapper.insert(detail);
                }
            }
        }
        
        return result > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        AssetApproval approval = assetApprovalMapper.selectById(id);
        if (approval == null || !"DRAFT".equals(approval.getStatus())) {
            return false; // 只能删除草稿状态的审批单
        }
        assetApprovalDetailMapper.deleteByApprovalId(id);
        return assetApprovalMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean submit(Long approvalId) {
        AssetApproval approval = assetApprovalMapper.selectById(approvalId);
        if (approval == null || !"DRAFT".equals(approval.getStatus())) {
            return false;
        }
        approval.setStatus("PENDING");
        // TODO: 启动流程实例
        return assetApprovalMapper.updateById(approval) > 0;
    }

    @Override
    @Transactional
    public boolean approve(Long approvalId, String userId, String opinion) {
        AssetApproval approval = assetApprovalMapper.selectById(approvalId);
        if (approval == null) {
            return false;
        }
        
        // 保存审批记录
        AssetApprovalRecord record = new AssetApprovalRecord();
        record.setApprovalId(approvalId);
        record.setApproverId(userId);
        record.setApprovalType("APPROVE");
        record.setApprovalOpinion(opinion);
        record.setApprovalTime(LocalDateTime.now());
        assetApprovalRecordMapper.insert(record);
        
        // TODO: 处理审批逻辑，更新流程任务，判断是否流程结束
        // 如果流程结束，更新状态为APPROVED
        approval.setStatus("APPROVED");
        return assetApprovalMapper.updateById(approval) > 0;
    }

    @Override
    @Transactional
    public boolean reject(Long approvalId, String userId, String opinion) {
        AssetApproval approval = assetApprovalMapper.selectById(approvalId);
        if (approval == null) {
            return false;
        }
        
        // 保存审批记录
        AssetApprovalRecord record = new AssetApprovalRecord();
        record.setApprovalId(approvalId);
        record.setApproverId(userId);
        record.setApprovalType("REJECT");
        record.setApprovalOpinion(opinion);
        record.setApprovalTime(LocalDateTime.now());
        assetApprovalRecordMapper.insert(record);
        
        // TODO: 处理驳回逻辑，结束流程
        approval.setStatus("REJECTED");
        return assetApprovalMapper.updateById(approval) > 0;
    }

    private String getApprovalTypePrefix(String approvalType) {
        if ("TRANSFER".equals(approvalType)) {
            return "ZCDB";
        } else if ("DISPOSAL".equals(approvalType)) {
            return "ZCCZ";
        } else if ("INVENTORY_DIFF".equals(approvalType)) {
            return "ZCPD";
        } else if ("CHANGE".equals(approvalType)) {
            return "ZCBD";
        }
        return "ZCSP";
    }
}
