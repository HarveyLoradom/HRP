package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.ProcurementRequirementApprovalMapper;
import com.hrp.asset.mapper.ProcurementRequirementDetailMapper;
import com.hrp.asset.mapper.ProcurementRequirementMapper;
import com.hrp.asset.service.ProcurementRequirementService;
import com.hrp.common.entity.ProcurementRequirement;
import com.hrp.common.entity.ProcurementRequirementApproval;
import com.hrp.common.entity.ProcurementRequirementDetail;
import com.hrp.common.entity.ProcurementRequirementDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProcurementRequirementServiceImpl implements ProcurementRequirementService {

    @Autowired
    private ProcurementRequirementMapper procurementRequirementMapper;

    @Autowired
    private ProcurementRequirementDetailMapper procurementRequirementDetailMapper;

    @Autowired
    private ProcurementRequirementApprovalMapper procurementRequirementApprovalMapper;

    @Override
    public List<ProcurementRequirement> getAll() {
        return procurementRequirementMapper.selectAll();
    }

    @Override
    public List<ProcurementRequirement> getByStatus(String status) {
        return procurementRequirementMapper.selectByStatus(status);
    }

    @Override
    public List<ProcurementRequirement> getByApplicant(Long applicantId) {
        return procurementRequirementMapper.selectByApplicant(applicantId);
    }

    @Override
    public List<ProcurementRequirement> getByApprover(String approverId) {
        return procurementRequirementMapper.selectByApprover(approverId);
    }

    @Override
    public List<ProcurementRequirement> getMyList(String userId, Long empId) {
        // 获取自己发起的和待自己审批的数据
        List<ProcurementRequirement> myCreated = empId != null ? procurementRequirementMapper.selectByApplicant(empId) : Collections.emptyList();
        List<ProcurementRequirement> myApproval = procurementRequirementMapper.selectByApprover(userId);
        
        // 合并去重
        Set<Long> idSet = new HashSet<>();
        List<ProcurementRequirement> result = new ArrayList<>();
        
        // 添加自己发起的
        if (myCreated != null) {
            for (ProcurementRequirement item : myCreated) {
                if (!idSet.contains(item.getRequirementId())) {
                    idSet.add(item.getRequirementId());
                    result.add(item);
                }
            }
        }
        
        // 添加待审批的
        if (myApproval != null) {
            for (ProcurementRequirement item : myApproval) {
                if (!idSet.contains(item.getRequirementId())) {
                    idSet.add(item.getRequirementId());
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
    public com.hrp.common.entity.PageResult<ProcurementRequirement> getMyListPage(String userId, Long empId, Long page, Long size) {
        // 先获取所有数据（合并后的）
        List<ProcurementRequirement> allData = getMyList(userId, empId);
        Long total = (long) allData.size();
        
        // 内存分页
        Long offset = (page - 1) * size;
        Long end = Math.min(offset + size, total);
        List<ProcurementRequirement> records = new ArrayList<>();
        for (Long i = offset; i < end; i++) {
            records.add(allData.get(i.intValue()));
        }
        
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
    }

    @Override
    public com.hrp.common.entity.PageResult<ProcurementRequirement> getAllPage(Long page, Long size) {
        List<ProcurementRequirement> allData = procurementRequirementMapper.selectAll();
        Long total = (long) allData.size();
        
        // 内存分页
        Long offset = (page - 1) * size;
        Long end = Math.min(offset + size, total);
        List<ProcurementRequirement> records = new ArrayList<>();
        for (Long i = offset; i < end; i++) {
            records.add(allData.get(i.intValue()));
        }
        
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
    }

    @Override
    public ProcurementRequirement getById(Long id) {
        return procurementRequirementMapper.selectById(id);
    }

    @Override
    public ProcurementRequirementDTO getDetail(Long id) {
        ProcurementRequirement requirement = procurementRequirementMapper.selectById(id);
        if (requirement == null) {
            return null;
        }

        ProcurementRequirementDTO dto = new ProcurementRequirementDTO();
        dto.setRequirement(requirement);
        dto.setDetails(procurementRequirementDetailMapper.selectByRequirementId(id));
        dto.setApprovals(procurementRequirementApprovalMapper.selectByRequirementId(id));
        return dto;
    }

    @Override
    @Transactional
    public boolean save(ProcurementRequirementDTO dto) {
        ProcurementRequirement requirement = dto.getRequirement();
        
        if (requirement.getRequirementId() == null) {
            // 新增
            if (requirement.getRequirementNo() == null || requirement.getRequirementNo().isEmpty()) {
                requirement.setRequirementNo("PROC-" + System.currentTimeMillis());
            }
            if (requirement.getStatus() == null || requirement.getStatus().isEmpty()) {
                requirement.setStatus("DRAFT");
            }
            
            int result = procurementRequirementMapper.insert(requirement);
            if (result > 0 && dto.getDetails() != null) {
                for (ProcurementRequirementDetail detail : dto.getDetails()) {
                    detail.setRequirementId(requirement.getRequirementId());
                    procurementRequirementDetailMapper.insert(detail);
                }
            }
            return result > 0;
        } else {
            // 更新
            procurementRequirementMapper.updateById(requirement);
            
            // 删除旧明细
            procurementRequirementDetailMapper.deleteByRequirementId(requirement.getRequirementId());
            
            // 插入新明细
            if (dto.getDetails() != null) {
                for (ProcurementRequirementDetail detail : dto.getDetails()) {
                    detail.setRequirementId(requirement.getRequirementId());
                    procurementRequirementDetailMapper.insert(detail);
                }
            }
            return true;
        }
    }

    @Override
    @Transactional
    public boolean submit(Long requirementId) {
        ProcurementRequirement requirement = procurementRequirementMapper.selectById(requirementId);
        if (requirement == null) {
            return false;
        }
        if (!"DRAFT".equals(requirement.getStatus())) {
            return false;
        }
        requirement.setStatus("PENDING");
        // TODO: 启动流程实例
        return procurementRequirementMapper.updateById(requirement) > 0;
    }

    @Override
    @Transactional
    public boolean withdraw(Long requirementId) {
        ProcurementRequirement requirement = procurementRequirementMapper.selectById(requirementId);
        if (requirement == null) {
            return false;
        }
        if (!"PENDING".equals(requirement.getStatus())) {
            return false;
        }
        requirement.setStatus("DRAFT");
        // TODO: 取消流程实例
        return procurementRequirementMapper.updateById(requirement) > 0;
    }

    @Override
    @Transactional
    public boolean approve(Long requirementId, String userId, String opinion) {
        ProcurementRequirement requirement = procurementRequirementMapper.selectById(requirementId);
        if (requirement == null) {
            return false;
        }
        requirement.setStatus("APPROVED");
        
        boolean success = procurementRequirementMapper.updateById(requirement) > 0;
        
        if (success) {
            ProcurementRequirementApproval approval = new ProcurementRequirementApproval();
            approval.setRequirementId(requirementId);
            approval.setApproverId(userId);
            approval.setApprovalType("APPROVE");
            approval.setApprovalOpinion(opinion);
            approval.setApprovalTime(LocalDateTime.now());
            procurementRequirementApprovalMapper.insert(approval);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean reject(Long requirementId, String userId, String opinion) {
        ProcurementRequirement requirement = procurementRequirementMapper.selectById(requirementId);
        if (requirement == null) {
            return false;
        }
        requirement.setStatus("REJECTED");
        
        boolean success = procurementRequirementMapper.updateById(requirement) > 0;
        
        if (success) {
            ProcurementRequirementApproval approval = new ProcurementRequirementApproval();
            approval.setRequirementId(requirementId);
            approval.setApproverId(userId);
            approval.setApprovalType("REJECT");
            approval.setApprovalOpinion(opinion);
            approval.setApprovalTime(LocalDateTime.now());
            procurementRequirementApprovalMapper.insert(approval);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        procurementRequirementDetailMapper.deleteByRequirementId(id);
        return procurementRequirementMapper.deleteById(id) > 0;
    }
}

