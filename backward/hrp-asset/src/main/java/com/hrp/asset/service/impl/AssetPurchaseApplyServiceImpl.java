package com.hrp.asset.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.asset.feign.AuthServiceClient;
import com.hrp.asset.mapper.AssetPurchaseApplyDetailMapper;
import com.hrp.asset.mapper.AssetPurchaseApplyMainMapper;
import com.hrp.asset.service.AssetPurchaseApplyService;
import com.hrp.common.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetPurchaseApplyServiceImpl implements AssetPurchaseApplyService {

    @Autowired
    private AssetPurchaseApplyMainMapper applyMainMapper;
    
    @Autowired
    private AssetPurchaseApplyDetailMapper applyDetailMapper;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;

    @Override
    public AssetPurchaseApplyMain getById(Long id) {
        return applyMainMapper.selectById(id);
    }

    @Override
    public AssetPurchaseApplyMain getByApplyNo(String applyNo) {
        return applyMainMapper.selectByApplyNo(applyNo);
    }

    @Override
    public List<AssetPurchaseApplyMain> getByEmpId(Long empId) {
        return applyMainMapper.selectByEmpId(empId);
    }

    @Override
    public List<AssetPurchaseApplyMain> getByStatus(String status) {
        return applyMainMapper.selectByStatus(status);
    }

    @Override
    public List<AssetPurchaseApplyMain> getMyApprovalApplies(String userId) {
        return applyMainMapper.selectByApprover(userId);
    }

    @Override
    public com.hrp.common.entity.PageResult<AssetPurchaseApplyMain> getPage(Long page, Long size, String applyNo, String applyEmpName, String status, String startDate, String endDate) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetPurchaseApplyMain> list = applyMainMapper.selectByConditions(applyNo, applyEmpName, status, startDate, endDate);
        PageInfo<AssetPurchaseApplyMain> pageInfo = new PageInfo<>(list);
        return new com.hrp.common.entity.PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public com.hrp.common.entity.PageResult<AssetPurchaseApplyMain> getPageByApprover(Long page, Long size, String currentUserId, String applyNo, String applyEmpName, String status, String startDate, String endDate) {
        // 根据状态参数决定查询逻辑
        java.util.Map<String, com.hrp.common.entity.ProcessTask> taskMap = new java.util.HashMap<>(); // taskKey -> 任务映射
        java.util.Set<String> taskKeys = new java.util.HashSet<>();
        
        if (authServiceClient != null && currentUserId != null && !currentUserId.trim().isEmpty()) {
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> tasksResult = null;
                
                // 如果状态是PENDING，查询待审批任务；否则查询已完成的任务（用于查看已审批记录）
                if ("PENDING".equals(status)) {
                    tasksResult = authServiceClient.getTasksByAssignee(currentUserId);
                } else {
                    // 查询已完成的任务（COMPLETED状态），用于查看已审批记录
                    tasksResult = authServiceClient.getTasksByAssigneeAndStatus(currentUserId, "COMPLETED");
                }
                
                if (tasksResult != null && tasksResult.getCode() == 200 && tasksResult.getData() != null) {
                    for (com.hrp.common.entity.ProcessTask task : tasksResult.getData()) {
                        if (task.getTaskKey() != null && !task.getTaskKey().trim().isEmpty()) {
                            taskKeys.add(task.getTaskKey());
                            // 保存任务信息，用于后续填充审批人信息
                            // 对于待审批任务，使用print_order最小的（当前节点）
                            // 对于已完成任务，保存每个任务
                            if ("PENDING".equals(status)) {
                                com.hrp.common.entity.ProcessTask existingTask = taskMap.get(task.getTaskKey());
                                if (existingTask == null || 
                                    (task.getPrintOrder() != null && existingTask.getPrintOrder() != null && 
                                     task.getPrintOrder() < existingTask.getPrintOrder()) ||
                                    (task.getPrintOrder() != null && existingTask.getPrintOrder() == null)) {
                                    taskMap.put(task.getTaskKey(), task);
                                }
                            } else {
                                // 对于已完成任务，保存每个任务（可能有多个任务对应同一个taskKey）
                                taskMap.put(task.getTaskKey(), task);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("查询审批任务失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果没有任务，返回空列表
        if (taskKeys.isEmpty()) {
            return new com.hrp.common.entity.PageResult<>(new java.util.ArrayList<>(), 0L, size, page);
        }
        
        // 根据taskKey（applyNo）列表查询采购申请，并应用查询条件
        // 注意：如果status不是PENDING，需要查询所有状态的记录，然后根据taskKeys过滤
        String queryStatus = "PENDING".equals(status) ? status : null; // 非PENDING状态时不限制status
        List<AssetPurchaseApplyMain> allApplies = applyMainMapper.selectByConditions(applyNo, applyEmpName, queryStatus, startDate, endDate);
        
        // 过滤出当前用户需要审批的申请单（taskKey在taskKeys中的），并填充当前审批人信息
        List<AssetPurchaseApplyMain> filteredApplies = new java.util.ArrayList<>();
        for (AssetPurchaseApplyMain apply : allApplies) {
            if (apply.getApplyNo() != null && taskKeys.contains(apply.getApplyNo())) {
                // 如果查询的是非PENDING状态，还需要匹配申请单的实际状态
                if (status != null && !"PENDING".equals(status)) {
                    if (!status.equals(apply.getStatus())) {
                        continue; // 状态不匹配，跳过
                    }
                }
                // 从任务中获取当前审批人信息（如果需要）
                com.hrp.common.entity.ProcessTask task = taskMap.get(apply.getApplyNo());
                if (task != null && task.getAssigneeUserName() != null) {
                    // 可以在这里设置当前审批人信息（如果实体类有对应字段）
                }
                filteredApplies.add(apply);
            }
        }
        
        // 手动分页
        long total = filteredApplies.size();
        long start = (page - 1) * size;
        long end = Math.min(start + size, total);
        List<AssetPurchaseApplyMain> pagedList = start < total ? filteredApplies.subList((int)start, (int)end) : new java.util.ArrayList<>();
        
        return new com.hrp.common.entity.PageResult<>(pagedList, total, size, page);
    }

    @Override
    @Transactional
    public AssetPurchaseApplyMain save(AssetPurchaseApplyMain apply, List<AssetPurchaseApplyDetail> details) {
        // 生成申请单号：ASSET年月日0001
        if (apply.getApplyNo() == null || apply.getApplyNo().isEmpty()) {
            apply.setApplyNo(generateApplyNo());
        }
        
        // 如果没有mainAttachId，生成一个时间戳作为主附件ID
        if (apply.getMainAttachId() == null || apply.getMainAttachId().isEmpty()) {
            apply.setMainAttachId(String.valueOf(System.currentTimeMillis()));
        }
        
        // 设置默认状态
        if (apply.getStatus() == null || apply.getStatus().isEmpty()) {
            apply.setStatus("DRAFT"); // DRAFT状态，对应sys_code的APPLY_STATUS
        }
        
        // 设置申请时间
        if (apply.getApplyTime() == null) {
            apply.setApplyTime(LocalDateTime.now());
        }
        
        // 保存主表
        int result = applyMainMapper.insert(apply);
        if (result > 0 && apply.getId() != null) {
            // 保存明细表
            if (details != null && !details.isEmpty()) {
                for (AssetPurchaseApplyDetail detail : details) {
                    detail.setApplyId(apply.getId());
                }
                applyDetailMapper.insertBatch(details);
            }
            return apply;
        }
        return null;
    }

    @Override
    @Transactional
    public AssetPurchaseApplyMain update(AssetPurchaseApplyMain apply, List<AssetPurchaseApplyDetail> details) {
        // 更新主表
        int result = applyMainMapper.updateById(apply);
        if (result > 0 && apply.getId() != null) {
            // 处理明细表：支持更新现有记录和新增记录，而不是全部删除后插入
            if (details != null && !details.isEmpty()) {
                // 获取现有明细记录
                List<AssetPurchaseApplyDetail> existingDetails = applyDetailMapper.selectByApplyId(apply.getId());
                java.util.Set<Long> existingIds = new java.util.HashSet<>();
                if (existingDetails != null) {
                    for (AssetPurchaseApplyDetail existing : existingDetails) {
                        if (existing.getId() != null) {
                            existingIds.add(existing.getId());
                        }
                    }
                }
                
                // 分离需要更新和新增的记录
                List<AssetPurchaseApplyDetail> toUpdate = new java.util.ArrayList<>();
                List<AssetPurchaseApplyDetail> toInsert = new java.util.ArrayList<>();
                java.util.Set<Long> newIds = new java.util.HashSet<>();
                
                for (AssetPurchaseApplyDetail detail : details) {
                    detail.setApplyId(apply.getId()); // 确保applyId已设置
                    
                    // 如果明细有id且该id存在于现有记录中，则更新
                    if (detail.getId() != null && existingIds.contains(detail.getId())) {
                        toUpdate.add(detail);
                        newIds.add(detail.getId());
                    } else {
                        // 否则，新增记录（清除id以确保是新增）
                        detail.setId(null);
                        toInsert.add(detail);
                    }
                }
                
                // 更新现有记录
                for (AssetPurchaseApplyDetail detail : toUpdate) {
                    detail.setUpdateTime(LocalDateTime.now()); // 设置更新时间
                    applyDetailMapper.updateById(detail);
                }
                
                // 新增记录
                if (!toInsert.isEmpty()) {
                    for (AssetPurchaseApplyDetail detail : toInsert) {
                        detail.setCreateTime(LocalDateTime.now()); // 设置创建时间
                        detail.setUpdateTime(LocalDateTime.now()); // 设置更新时间
                    }
                    applyDetailMapper.insertBatch(toInsert);
                }
                
                // 删除不在新明细列表中的旧记录
                for (Long existingId : existingIds) {
                    if (!newIds.contains(existingId)) {
                        applyDetailMapper.deleteById(existingId);
                    }
                }
            } else {
                // 如果新明细列表为空，删除所有旧明细
                applyDetailMapper.deleteByApplyId(apply.getId());
            }
            return apply;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        AssetPurchaseApplyMain apply = applyMainMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        
        // 只有草稿、撤回或拒绝状态才能删除
        // status: DRAFT, WITHDRAWN, REJECTED (根据sys_code的APPLY_STATUS)
        if (apply.getStatus() == null || (!"DRAFT".equals(apply.getStatus()) && !"WITHDRAWN".equals(apply.getStatus()) && !"REJECTED".equals(apply.getStatus()))) {
            return false;
        }
        
        // 删除附件记录和文件（通过Feign调用auth服务）
        // 优先使用mainAttachId作为businessId删除附件（因为附件是使用mainAttachId存储的）
        // 如果没有mainAttachId，再尝试使用applyNo（兼容旧数据）
        if (authServiceClient != null) {
            try {
                String businessId = null;
                if (apply.getMainAttachId() != null && !apply.getMainAttachId().isEmpty()) {
                    // 优先使用mainAttachId（附件实际使用的businessId）
                    businessId = apply.getMainAttachId();
                    System.out.println("删除采购申请的附件，使用mainAttachId=" + businessId);
                } else if (apply.getApplyNo() != null && !apply.getApplyNo().isEmpty()) {
                    // 如果没有mainAttachId，使用applyNo（兼容旧数据）
                    businessId = apply.getApplyNo();
                    System.out.println("删除采购申请的附件，使用applyNo=" + businessId);
                }
                
                if (businessId != null && !businessId.isEmpty()) {
                    // 使用deleteByBusinessId，只根据businessId删除，不依赖businessType
                    // 因为附件保存时可能使用不同的businessType（如ASSET_TYPE），但businessId是唯一的
                    Result<Void> deleteResult = authServiceClient.deleteAttachmentsByBusinessId(businessId);
                    if (deleteResult != null && deleteResult.getCode() == 200) {
                        System.out.println("附件删除成功，businessId=" + businessId);
                    } else {
                        System.err.println("附件删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                    }
                } else {
                    System.out.println("采购申请没有附件标识（mainAttachId和applyNo都为空），跳过附件删除");
                }
            } catch (Exception e) {
                System.err.println("删除附件失败: " + e.getMessage());
                e.printStackTrace();
                // 附件删除失败，不影响申请删除，只记录错误
            }
        }
        
        // 删除流程任务记录（审批记录）
        // 即使撤回时已经删除过，这里也再次删除，确保数据一致性（防止撤回时删除失败的情况）
        if (apply.getApplyNo() != null && !apply.getApplyNo().isEmpty() && authServiceClient != null) {
            try {
                System.out.println("删除采购申请的流程任务记录（审批记录），applyNo=" + apply.getApplyNo());
                Result<String> deleteResult = authServiceClient.deleteTasksByTaskKey(apply.getApplyNo());
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    System.out.println("流程任务记录删除成功，applyNo=" + apply.getApplyNo());
                } else {
                    System.err.println("流程任务记录删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
                // 流程任务记录删除失败，不影响申请删除，只记录错误
            }
        }
        
        // 删除明细
        applyDetailMapper.deleteByApplyId(id);
        
        // 删除主表
        return applyMainMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean submit(Long applyId) {
        AssetPurchaseApplyMain apply = applyMainMapper.selectById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 只有草稿、撤回或拒绝状态才能提交
        if (apply.getStatus() == null || (!"DRAFT".equals(apply.getStatus()) && !"WITHDRAWN".equals(apply.getStatus()) && !"REJECTED".equals(apply.getStatus()))) {
            return false;
        }
        
        // 如果没有单号，生成一个
        if (apply.getApplyNo() == null || apply.getApplyNo().isEmpty()) {
            apply.setApplyNo(generateApplyNo());
            applyMainMapper.updateById(apply);
        }
        
        System.out.println("=== 开始提交采购申请 ===");
        System.out.println("申请ID: " + apply.getId());
        System.out.println("申请单号: " + apply.getApplyNo());
        System.out.println("模板配置ID: " + apply.getTemplateConfigId());
        
        // 根据模板配置ID获取流程定义
        Long processDefinitionId = null;
        if (authServiceClient != null && apply.getTemplateConfigId() != null) {
            try {
                Result<TemplateConfig> configResult = authServiceClient.getTemplateConfigById(apply.getTemplateConfigId());
                if (configResult != null && configResult.getCode() == 200 && configResult.getData() != null) {
                    TemplateConfig templateConfig = configResult.getData();
                    processDefinitionId = templateConfig.getProcessDefinitionId();
                    System.out.println("流程定义ID: " + processDefinitionId);
                } else {
                    System.err.println("获取模板配置失败: " + (configResult != null ? configResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("获取模板配置异常: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (apply.getTemplateConfigId() == null) {
                System.err.println("警告: templateConfigId为空，无法获取流程定义");
            }
            if (authServiceClient == null) {
                System.err.println("警告: AuthServiceClient未注入，无法获取流程定义");
            }
        }
        
        // 更新状态为待审批
        apply.setStatus("PENDING"); // PENDING状态，对应sys_code的APPLY_STATUS
        boolean updateSuccess = applyMainMapper.updateById(apply) > 0;
        
        if (!updateSuccess) {
            System.err.println("提交失败: 更新申请状态失败");
            return false;
        }
        
        // 如果没有流程定义ID，仍然返回true（状态已更新），但不启动流程
        if (processDefinitionId == null) {
            System.err.println("警告: processDefinitionId为空，无法启动流程");
            return updateSuccess;
        }
        
        if (apply.getApplyNo() == null || apply.getApplyNo().isEmpty()) {
            System.err.println("警告: applyNo为空，无法启动流程");
            return updateSuccess;
        }
        
        // 生成流程任务记录
        if (authServiceClient != null) {
            try {
                // 检查是否有退回的任务
                String startFromNodeName = null;
                boolean hasReturnToCurrent = false;
                
                try {
                    Result<java.util.List<com.hrp.common.entity.ProcessTask>> existingTasksResult = authServiceClient.getTasksByTaskKey(apply.getApplyNo());
                    if (existingTasksResult != null && existingTasksResult.getCode() == 200 && existingTasksResult.getData() != null) {
                        for (com.hrp.common.entity.ProcessTask task : existingTasksResult.getData()) {
                            if ("RETURNED".equals(task.getTaskStatus()) && "RETURN_TO_CURRENT".equals(task.getReturnType())) {
                                for (com.hrp.common.entity.ProcessTask t : existingTasksResult.getData()) {
                                    if ("提交".equals(t.getTaskName()) || "submit".equalsIgnoreCase(t.getTaskName())) {
                                        if (t.getNextTaskId() != null) {
                                            for (com.hrp.common.entity.ProcessTask targetTask : existingTasksResult.getData()) {
                                                if (t.getNextTaskId().equals(targetTask.getTaskId())) {
                                                    startFromNodeName = targetTask.getTaskName();
                                                    hasReturnToCurrent = true;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("检查退回任务时出现异常: " + e.getMessage());
                }
                
                // 处理旧的任务记录
                System.out.println("准备处理旧的流程任务记录（如果存在），applyNo=" + apply.getApplyNo() + ", hasReturnToCurrent=" + hasReturnToCurrent);
                try {
                    if (hasReturnToCurrent) {
                        // 恢复退回的任务为待审批状态，而不是删除
                        Result<Boolean> restoreResult = authServiceClient.restoreReturnedTasks(apply.getApplyNo());
                        if (restoreResult != null && restoreResult.getCode() == 200) {
                            System.out.println("成功恢复退回任务为待审批状态: applyNo=" + apply.getApplyNo());
                        } else {
                            System.err.println("恢复退回任务失败: " + (restoreResult != null ? restoreResult.getMessage() : "未知错误"));
                        }
                    } else {
                        // 删除所有任务记录
                        authServiceClient.deleteTasksByTaskKey(apply.getApplyNo());
                        System.out.println("旧的流程任务记录删除完成（如果有）");
                    }
                } catch (Exception e) {
                    // 如果处理失败（可能是因为没有旧记录），不影响后续流程
                    System.out.println("处理旧任务记录时出现异常（可能没有旧记录）: " + e.getMessage());
                }
                
                // 生成流程任务记录
                System.out.println("准备生成流程任务记录，processDefinitionId=" + processDefinitionId + ", applyNo=" + apply.getApplyNo() + ", startFromNodeName=" + startFromNodeName);
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> result = authServiceClient.generateTasks(processDefinitionId, apply.getApplyNo(), startFromNodeName);
                if (result != null && result.getCode() == 200) {
                    System.out.println("流程任务记录生成成功，applyNo=" + apply.getApplyNo());
                    if (result.getData() != null) {
                        System.out.println("共生成 " + result.getData().size() + " 条任务记录");
                    }
                } else {
                    System.err.println("流程任务记录生成失败: " + (result != null ? result.getMessage() : "未知错误"));
                    System.err.println("返回码: " + (result != null ? result.getCode() : "null"));
                }
            } catch (Exception e) {
                System.err.println("生成流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return updateSuccess;
    }

    @Override
    @Transactional
    public boolean withdraw(Long applyId) {
        AssetPurchaseApplyMain apply = applyMainMapper.selectById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 只有草稿或待审批状态才能撤回
        if (apply.getStatus() == null || (!"DRAFT".equals(apply.getStatus()) && !"PENDING".equals(apply.getStatus()))) {
            return false;
        }
        
        // 删除流程任务记录
        if (apply.getApplyNo() != null && !apply.getApplyNo().isEmpty() && authServiceClient != null) {
            try {
                Result<String> deleteResult = authServiceClient.deleteTasksByTaskKey(apply.getApplyNo());
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    System.out.println("流程任务记录删除成功");
                }
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
            }
        }
        
        // 更新状态为已撤回
        apply.setStatus("WITHDRAWN"); // WITHDRAWN状态，对应sys_code的APPLY_STATUS
        return applyMainMapper.updateById(apply) > 0;
    }

    @Override
    @Transactional
    public boolean approve(Long applyId, String userId, String opinion, String approverSignature) {
        AssetPurchaseApplyMain apply = applyMainMapper.selectById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 完成任务
        if (authServiceClient != null && apply.getApplyNo() != null) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("taskKey", apply.getApplyNo());
                request.put("taskId", null);
                request.put("comment", opinion);
                if (approverSignature != null && !approverSignature.trim().isEmpty()) {
                    request.put("approverSignature", approverSignature);
                }
                
                Result<Boolean> completeResult = authServiceClient.completeTask(request);
                if (completeResult != null && completeResult.getCode() == 200 && Boolean.TRUE.equals(completeResult.getData())) {
                    System.out.println("任务完成成功");
                } else {
                    System.err.println("任务完成失败: " + (completeResult != null ? completeResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("完成任务异常: " + e.getMessage());
            }
        }
        
        // 检查是否所有任务都已完成
        boolean allTasksCompleted = true;
        if (authServiceClient != null && apply.getApplyNo() != null) {
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> tasksResult = authServiceClient.getTasksByTaskKey(apply.getApplyNo());
                if (tasksResult != null && tasksResult.getCode() == 200 && tasksResult.getData() != null) {
                    long pendingCount = tasksResult.getData().stream()
                        .filter(t -> "PENDING".equals(t.getTaskStatus()))
                        .count();
                    allTasksCompleted = (pendingCount == 0);
                }
            } catch (Exception e) {
                System.err.println("检查任务状态异常: " + e.getMessage());
            }
        }
        
        // 如果所有任务都已完成，更新状态为已审批
        if (allTasksCompleted) {
            apply.setStatus("APPROVED"); // APPROVED状态，对应sys_code的APPLY_STATUS
            return applyMainMapper.updateById(apply) > 0;
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean reject(Long applyId, String userId, String opinion) {
        AssetPurchaseApplyMain apply = applyMainMapper.selectById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 获取申请人的用户信息
        String applicantUserId = null;
        if (authServiceClient != null && apply.getApplyEmpId() != null) {
            try {
                Result<User> userResult = authServiceClient.getUserById(String.valueOf(apply.getApplyEmpId()));
                if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                    applicantUserId = userResult.getData().getId();
                }
            } catch (Exception e) {
                System.err.println("获取申请人信息失败: " + e.getMessage());
            }
        }
        
        // 终止流程任务
        if (authServiceClient != null && apply.getApplyNo() != null) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("taskKey", apply.getApplyNo());
                request.put("comment", opinion);
                if (applicantUserId != null) {
                    request.put("returnToUserId", applicantUserId);
                }
                
                Result<Boolean> terminateResult = authServiceClient.terminateTask(request);
                if (terminateResult != null && terminateResult.getCode() == 200 && Boolean.TRUE.equals(terminateResult.getData())) {
                    System.out.println("任务终止成功");
                } else {
                    System.err.println("任务终止失败: " + (terminateResult != null ? terminateResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("终止任务异常: " + e.getMessage());
            }
        }
        
        // 更新状态为已拒绝
        apply.setStatus("REJECTED"); // REJECTED状态，对应sys_code的APPLY_STATUS
        return applyMainMapper.updateById(apply) > 0;
    }

    @Override
    @Transactional
    public boolean returnApply(Long applyId, String returnType, String opinion) {
        AssetPurchaseApplyMain apply = applyMainMapper.selectById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 获取申请人的用户信息
        String applicantUserId = null;
        if (authServiceClient != null && apply.getApplyEmpId() != null) {
            try {
                Result<User> userResult = authServiceClient.getUserById(String.valueOf(apply.getApplyEmpId()));
                if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                    applicantUserId = userResult.getData().getId();
                }
            } catch (Exception e) {
                System.err.println("获取申请人信息失败: " + e.getMessage());
            }
        }
        
        // 退回任务
        if (authServiceClient != null && apply.getApplyNo() != null) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("taskKey", apply.getApplyNo());
                request.put("returnType", returnType != null ? returnType : "RETURN_TO_CURRENT");
                request.put("comment", opinion);
                if (applicantUserId != null) {
                    request.put("returnToUserId", applicantUserId);
                }
                
                Result<Boolean> returnResult = authServiceClient.returnTask(request);
                if (returnResult != null && returnResult.getCode() == 200 && Boolean.TRUE.equals(returnResult.getData())) {
                    System.out.println("任务退回成功");
                } else {
                    System.err.println("任务退回失败: " + (returnResult != null ? returnResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("退回任务异常: " + e.getMessage());
            }
        }
        
        // 将状态改为REJECTED（已拒绝），退回后的状态应该是已拒绝
        // 注意：虽然ProcessTaskServiceImpl.returnTask也会更新状态为REJECTED，但这里也设置一次确保状态正确
        apply.setStatus("REJECTED"); // REJECTED状态，对应sys_code的APPLY_STATUS
        apply.setUpdateTime(LocalDateTime.now());
        return applyMainMapper.updateById(apply) > 0;
    }

    @Override
    public List<AssetPurchaseApplyDetail> getDetailsByApplyId(Long applyId) {
        return applyDetailMapper.selectByApplyId(applyId);
    }

    /**
     * 生成申请单号：ASSET年月日0001
     * 例如：ASSET202601080001, ASSET202601080002
     */
    private String generateApplyNo() {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String prefix = "ASSET" + year + month + day;
        
        // 查询当前日期的最大申请单号
        String maxApplyNo = applyMainMapper.selectMaxApplyNoByPrefix(prefix);
        
        int sequence = 1;
        if (maxApplyNo != null && maxApplyNo.startsWith(prefix)) {
            // 提取序号部分（最后4位）
            try {
                String seqStr = maxApplyNo.substring(prefix.length());
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 格式化为4位序号
        return prefix + String.format("%04d", sequence);
    }
}

