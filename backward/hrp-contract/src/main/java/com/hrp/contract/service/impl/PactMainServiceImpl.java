package com.hrp.contract.service.impl;

import com.hrp.contract.feign.AuthServiceClient;
import com.hrp.contract.mapper.PactMainMapper;
import com.hrp.contract.service.PactMainService;
import com.hrp.common.entity.PactMain;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.TemplateConfig;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PactMainServiceImpl implements PactMainService {

    @Autowired
    private PactMainMapper pactMainMapper;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;

    @Override
    public List<PactMain> getAll() {
        return pactMainMapper.selectAll();
    }

    @Override
    public PageResult<PactMain> getAllPage(Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<PactMain> list = pactMainMapper.selectAll();
        PageInfo<PactMain> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<PactMain> getByStatus(String status) {
        return pactMainMapper.selectByStatus(status);
    }

    @Override
    public PageResult<PactMain> getByStatusPage(String status, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<PactMain> list = pactMainMapper.selectByStatus(status);
        PageInfo<PactMain> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PactMain getById(Long id) {
        return pactMainMapper.selectById(id);
    }

    @Override
    public PactMain getByContractNo(String contractNo) {
        return pactMainMapper.selectByContractNo(contractNo);
    }

    @Override
    @Transactional
    public PactMain save(PactMain pactMain) {
        if (pactMain.getContractNo() == null || pactMain.getContractNo().isEmpty()) {
            pactMain.setContractNo(generateContractNo());
        }
        if (pactMain.getStatus() == null || pactMain.getStatus().isEmpty()) {
            pactMain.setStatus("DRAFT");
        }
        int result = pactMainMapper.insert(pactMain);
        if (result > 0) {
            // insert方法执行后，pactId会被自动设置到pactMain对象中（通过useGeneratedKeys）
            return pactMain;
        }
        return null;
    }
    
    /**
     * 生成合同编号：PACT年月日0001
     * 例如：PACT202501050001, PACT202501050002
     */
    private String generateContractNo() {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String prefix = "PACT" + year + month + day;
        
        // 查询当前日期的最大合同编号
        String maxContractNo = pactMainMapper.selectMaxContractNoByPrefix(prefix);
        
        int sequence = 1;
        if (maxContractNo != null && maxContractNo.startsWith(prefix)) {
            // 提取序号部分（最后4位）
            try {
                String seqStr = maxContractNo.substring(prefix.length());
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 格式化为4位序号
        return prefix + String.format("%04d", sequence);
    }

    @Override
    @Transactional
    public boolean update(PactMain pactMain) {
        return pactMainMapper.updateById(pactMain) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        PactMain contract = pactMainMapper.selectById(id);
        if (contract == null) {
            return false;
        }
        // 草稿、已撤回、已拒绝状态都可以删除（物理删除）
        String status = contract.getStatus();
        if (!"DRAFT".equals(status) && !"WITHDRAWN".equals(status) && !"REJECTED".equals(status)) {
            return false;
        }
        
        // 删除附件记录和文件（通过Feign调用auth服务）
        // 优先使用mainAttachId作为businessId删除附件（因为附件是使用mainAttachId存储的）
        // 如果没有mainAttachId，再尝试使用contractNo（兼容旧数据）
        if (authServiceClient != null) {
            try {
                String businessId = null;
                if (contract.getMainAttachId() != null && !contract.getMainAttachId().isEmpty()) {
                    // 优先使用mainAttachId（附件实际使用的businessId）
                    businessId = contract.getMainAttachId();
                    System.out.println("删除合同的附件，使用mainAttachId=" + businessId);
                } else if (contract.getContractNo() != null && !contract.getContractNo().isEmpty()) {
                    // 如果没有mainAttachId，使用contractNo（兼容旧数据）
                    businessId = contract.getContractNo();
                    System.out.println("删除合同的附件，使用contractNo=" + businessId);
                }
                
                if (businessId != null && !businessId.isEmpty()) {
                    Result<Void> deleteResult = authServiceClient.deleteAttachmentsByBusiness("CONTRACT", businessId);
                    if (deleteResult != null && deleteResult.getCode() == 200) {
                        System.out.println("附件删除成功，businessId=" + businessId);
                    } else {
                        System.err.println("附件删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                    }
                }
            } catch (Exception e) {
                // 附件删除失败，不影响合同删除，只记录日志
                System.err.println("删除合同附件时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 删除流程任务记录（审批记录）
        // 即使撤回时已经删除过，这里也再次删除，确保数据一致性（防止撤回时删除失败的情况）
        if (contract.getContractNo() != null && !contract.getContractNo().isEmpty() && authServiceClient != null) {
            try {
                System.out.println("删除合同的流程任务记录（审批记录），contractNo=" + contract.getContractNo());
                Result<String> deleteResult = authServiceClient.deleteTasksByTaskKey(contract.getContractNo());
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    System.out.println("流程任务记录删除成功，contractNo=" + contract.getContractNo());
                } else {
                    System.err.println("流程任务记录删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
                // 不影响删除操作，只记录错误
            }
        }
        
        // 删除合同记录
        return pactMainMapper.deleteById(id) > 0;
    }

    @Override
    public List<PactMain> getMyApprovalContracts(String userId) {
        return pactMainMapper.selectByApprover(userId);
    }

    @Override
    public com.hrp.common.entity.PageResult<PactMain> getPage(Long page, Long size, String contractNo, String contractName, String contractType, String status, String startDate, String endDate, Long empId) {
        com.github.pagehelper.PageHelper.startPage(page.intValue(), size.intValue());
        List<PactMain> list = pactMainMapper.selectByConditions(empId, contractNo, contractName, contractType, status, startDate, endDate);
        com.github.pagehelper.PageInfo<PactMain> pageInfo = new com.github.pagehelper.PageInfo<>(list);
        return new com.hrp.common.entity.PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public com.hrp.common.entity.PageResult<PactMain> getPageByApprover(Long page, Long size, String currentUserId, String contractNo, String contractName, String contractType, String status, String startDate, String endDate) {
        // 根据状态参数决定查询逻辑
        java.util.Map<String, com.hrp.common.entity.ProcessTask> taskMap = new java.util.HashMap<>();
        java.util.Set<String> taskKeys = new java.util.HashSet<>();
        
        if (authServiceClient != null && currentUserId != null && !currentUserId.trim().isEmpty()) {
            try {
                com.hrp.common.entity.Result<java.util.List<com.hrp.common.entity.ProcessTask>> tasksResult = null;
                
                // 如果状态是PENDING，查询待审批任务；否则查询已完成的任务
                if ("PENDING".equals(status)) {
                    tasksResult = authServiceClient.getTasksByAssignee(currentUserId);
                } else {
                    tasksResult = authServiceClient.getTasksByAssigneeAndStatus(currentUserId, "COMPLETED");
                }
                
                if (tasksResult != null && tasksResult.getCode() == 200 && tasksResult.getData() != null) {
                    for (com.hrp.common.entity.ProcessTask task : tasksResult.getData()) {
                        if (task.getTaskKey() != null && !task.getTaskKey().trim().isEmpty()) {
                            // 如果businessType为null，尝试从taskKey推断（合同编号通常以PACT开头）
                            String businessType = task.getBusinessType();
                            if (businessType == null || businessType.trim().isEmpty()) {
                                // 尝试从taskKey推断业务类型
                                String taskKey = task.getTaskKey();
                                if (taskKey != null && taskKey.startsWith("PACT")) {
                                    businessType = "CONTRACT";
                                }
                            }
                            
                            // 只处理合同类型的任务
                            if ("CONTRACT".equals(businessType)) {
                                taskKeys.add(task.getTaskKey());
                                if ("PENDING".equals(status)) {
                                    com.hrp.common.entity.ProcessTask existingTask = taskMap.get(task.getTaskKey());
                                    if (existingTask == null || 
                                        (task.getPrintOrder() != null && existingTask.getPrintOrder() != null && 
                                         task.getPrintOrder() < existingTask.getPrintOrder()) ||
                                        (task.getPrintOrder() != null && existingTask.getPrintOrder() == null)) {
                                        taskMap.put(task.getTaskKey(), task);
                                    }
                                } else {
                                    taskMap.put(task.getTaskKey(), task);
                                }
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
        
        // 根据taskKey（contractNo）列表查询合同，并应用查询条件
        String queryStatus = "PENDING".equals(status) ? status : null;
        // 审批列表不需要按empId过滤，传入null
        List<PactMain> allContracts = pactMainMapper.selectByConditions(null, contractNo, contractName, contractType, queryStatus, startDate, endDate);
        
        // 过滤出当前用户需要审批的合同
        List<PactMain> filteredContracts = new java.util.ArrayList<>();
        for (PactMain contract : allContracts) {
            if (contract.getContractNo() != null && taskKeys.contains(contract.getContractNo())) {
                if (status != null && !"PENDING".equals(status)) {
                    if (!status.equals(contract.getStatus())) {
                        continue;
                    }
                }
                com.hrp.common.entity.ProcessTask task = taskMap.get(contract.getContractNo());
                if (task != null && task.getAssigneeUserName() != null) {
                    // 可以在这里设置当前审批人信息（如果需要）
                }
                filteredContracts.add(contract);
            }
        }
        
        // 手动分页
        int start = (page.intValue() - 1) * size.intValue();
        int end = Math.min(start + size.intValue(), filteredContracts.size());
        List<PactMain> pageList = start < filteredContracts.size() ? filteredContracts.subList(start, end) : new java.util.ArrayList<>();
        
        return new com.hrp.common.entity.PageResult<>(pageList, (long) filteredContracts.size(), size, page);
    }

    @Override
    @Transactional
    public boolean submit(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        // 允许草稿、已撤回、已拒绝状态的合同提交
        String status = contract.getStatus();
        if (!"DRAFT".equals(status) && !"WITHDRAWN".equals(status) && !"REJECTED".equals(status)) {
            System.err.println("提交失败: 只有草稿、已撤回或已拒绝状态的合同才能提交，当前状态: " + status);
            return false;
        }
        
        contract.setStatus("PENDING");
        contract.setUpdateTime(LocalDateTime.now());
        boolean updateSuccess = pactMainMapper.updateById(contract) > 0;
        
        if (!updateSuccess) {
            System.err.println("提交失败: 更新合同状态失败");
            return false;
        }
        
        // 获取流程定义ID
        Long processDefinitionId = null;
        if (contract.getTemplateConfigId() != null && authServiceClient != null) {
            try {
                System.out.println("=== 获取流程定义ID ===");
                System.out.println("templateConfigId: " + contract.getTemplateConfigId());
                Result<TemplateConfig> templateConfigResult = authServiceClient.getTemplateConfigById(contract.getTemplateConfigId());
                System.out.println("模板配置查询结果 - Code: " + (templateConfigResult != null ? templateConfigResult.getCode() : "null"));
                if (templateConfigResult != null && templateConfigResult.getCode() == 200 && templateConfigResult.getData() != null) {
                    TemplateConfig templateConfig = templateConfigResult.getData();
                    processDefinitionId = templateConfig.getProcessDefinitionId();
                    System.out.println("processDefinitionId: " + processDefinitionId);
                    System.out.println("流程定义名称: " + templateConfig.getProcessDefinitionName());
                } else {
                    System.err.println("获取模板配置失败 - Result: " + templateConfigResult);
                    if (templateConfigResult != null) {
                        System.err.println("错误信息: " + templateConfigResult.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("获取模板配置异常: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (contract.getTemplateConfigId() == null) {
                System.err.println("警告: templateConfigId为空");
            }
            if (authServiceClient == null) {
                System.err.println("警告: authServiceClient未注入");
            }
        }
        
        if (processDefinitionId == null) {
            System.err.println("警告: processDefinitionId为空，无法启动流程");
            System.err.println("templateConfigId: " + contract.getTemplateConfigId());
            return updateSuccess; // 即使没有流程定义，也返回true表示状态更新成功
        }
        
        if (contract.getContractNo() == null || contract.getContractNo().isEmpty()) {
            System.err.println("警告: contractNo为空，无法启动流程");
            return updateSuccess;
        }
        
        // 生成流程任务记录（新的基于数据库表的方式）
        if (authServiceClient == null) {
            System.err.println("警告: AuthServiceClient未注入，无法生成流程任务记录");
            return updateSuccess;
        }
        
        try {
            // 检查是否有退回的任务
            String startFromNodeName = null;
            boolean hasReturnToCurrent = false;
            
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> existingTasksResult = authServiceClient.getTasksByTaskKey(contract.getContractNo());
                if (existingTasksResult != null && existingTasksResult.getCode() == 200 && existingTasksResult.getData() != null) {
                    // 查找是否有RETURN_TO_CURRENT类型的退回任务
                    for (com.hrp.common.entity.ProcessTask task : existingTasksResult.getData()) {
                        if ("RETURNED".equals(task.getTaskStatus()) && "RETURN_TO_CURRENT".equals(task.getReturnType())) {
                            // 找到提交节点，获取其next_task_id指向的节点名称
                            for (com.hrp.common.entity.ProcessTask t : existingTasksResult.getData()) {
                                if ("提交".equals(t.getTaskName()) || "submit".equalsIgnoreCase(t.getTaskName())) {
                                    if (t.getNextTaskId() != null) {
                                        // 查找next_task_id指向的节点
                                        for (com.hrp.common.entity.ProcessTask targetTask : existingTasksResult.getData()) {
                                            if (t.getNextTaskId().equals(targetTask.getTaskId())) {
                                                startFromNodeName = targetTask.getTaskName();
                                                hasReturnToCurrent = true;
                                                System.out.println("检测到退回后重新提交到本节点，节点名称: " + startFromNodeName);
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
                e.printStackTrace();
                // 不影响后续流程，继续执行
            }
            
            // 如果是退回重新提交到本节点（RETURN_TO_CURRENT），恢复退回的任务为待审批状态，保留其他已完成的任务
            // 如果是退回重新走流程（RETURN_TO_START）或正常提交，删除所有旧的任务记录
            System.out.println("准备处理旧的流程任务记录（如果存在），contractNo=" + contract.getContractNo() + ", hasReturnToCurrent=" + hasReturnToCurrent);
            try {
                if (hasReturnToCurrent) {
                    // 恢复退回的任务为待审批状态，而不是删除
                    // 这样保留退回历史记录，同时让任务重新进入审批流程
                    Result<Boolean> restoreResult = authServiceClient.restoreReturnedTasks(contract.getContractNo());
                    if (restoreResult != null && restoreResult.getCode() == 200) {
                        System.out.println("成功恢复退回任务为待审批状态: contractNo=" + contract.getContractNo());
                    } else {
                        System.err.println("恢复退回任务失败: " + (restoreResult != null ? restoreResult.getMessage() : "未知错误"));
                    }
                } else {
                    // 删除所有任务记录
                    authServiceClient.deleteTasksByTaskKey(contract.getContractNo());
                    System.out.println("旧的流程任务记录删除完成（如果有）");
                }
            } catch (Exception e) {
                // 如果处理失败（可能是因为没有旧记录），不影响后续流程
                System.out.println("处理旧任务记录时出现异常（可能没有旧记录）: " + e.getMessage());
            }
            
            System.out.println("准备生成流程任务记录，processDefinitionId=" + processDefinitionId + ", contractNo=" + contract.getContractNo() + ", startFromNodeName=" + startFromNodeName);
            Result<java.util.List<com.hrp.common.entity.ProcessTask>> result = authServiceClient.generateTasks(processDefinitionId, contract.getContractNo(), startFromNodeName);
            if (result != null && result.getCode() == 200) {
                System.out.println("流程任务记录生成成功，contractNo=" + contract.getContractNo());
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
            // 不影响提交操作，只记录错误
        }
        
        return updateSuccess;
    }

    @Override
    @Transactional
    public boolean withdraw(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        // 只有草稿或待审批状态才能撤回
        if (!"DRAFT".equals(contract.getStatus()) && !"PENDING".equals(contract.getStatus())) {
            return false;
        }
        
        // 删除流程任务记录
        if (contract.getContractNo() != null && !contract.getContractNo().isEmpty() && authServiceClient != null) {
            try {
                System.out.println("撤回合同，准备删除流程任务记录，contractNo=" + contract.getContractNo());
                Result<String> deleteResult = authServiceClient.deleteTasksByTaskKey(contract.getContractNo());
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    System.out.println("流程任务记录删除成功，contractNo=" + contract.getContractNo());
                } else {
                    System.err.println("流程任务记录删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
                // 不影响撤回操作，只记录错误
        }
        }
        
        contract.setStatus("WITHDRAWN");
        contract.setUpdateTime(LocalDateTime.now());
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean approve(Long contractId, String userId, String opinion, String signature) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        
        // 调用流程任务服务完成当前任务
        if (authServiceClient != null && contract.getContractNo() != null && !contract.getContractNo().trim().isEmpty()) {
            try {
                java.util.Map<String, Object> request = new java.util.HashMap<>();
                request.put("taskKey", contract.getContractNo());
                request.put("comment", opinion);
                if (signature != null && !signature.trim().isEmpty()) {
                    request.put("approverSignature", signature);
                }
                
                System.out.println("准备完成任务: taskKey=" + contract.getContractNo() + ", opinion=" + opinion);
                Result<Boolean> completeResult = authServiceClient.completeTask(request);
                if (completeResult != null && completeResult.getCode() == 200 && Boolean.TRUE.equals(completeResult.getData())) {
                    System.out.println("任务完成成功");
                } else {
                    System.err.println("任务完成失败: " + (completeResult != null ? completeResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("完成任务异常: " + e.getMessage());
                e.printStackTrace();
                // 如果流程任务处理失败，仍然更新合同状态，避免审批无法继续
            }
        }
        
        // 检查是否所有任务都已完成
        boolean allTasksCompleted = true;
        if (authServiceClient != null && contract.getContractNo() != null && !contract.getContractNo().trim().isEmpty()) {
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> tasksResult = authServiceClient.getTasksByTaskKey(contract.getContractNo());
                if (tasksResult != null && tasksResult.getCode() == 200 && tasksResult.getData() != null) {
                    long pendingCount = tasksResult.getData().stream()
                        .filter(t -> "PENDING".equals(t.getTaskStatus()))
                        .count();
                    allTasksCompleted = (pendingCount == 0);
                    System.out.println("待处理任务数: " + pendingCount + ", 所有任务是否完成: " + allTasksCompleted);
                }
            } catch (Exception e) {
                System.err.println("检查任务状态异常: " + e.getMessage());
                e.printStackTrace();
                // 如果检查失败，默认认为未完成，保持PENDING状态
                allTasksCompleted = false;
            }
        }
        
        // 只有所有任务都完成时，才将状态改为APPROVED；否则保持PENDING状态
        if (allTasksCompleted) {
        contract.setStatus("APPROVED");
        } else {
            contract.setStatus("PENDING");
        }
        contract.setUpdateTime(LocalDateTime.now());
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean reject(Long contractId, String userId, String opinion) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }

        contract.setStatus("REJECTED");

        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean returnContract(Long contractId, String returnType, String opinion) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        
        // 获取申请人的用户信息（sys_user.id）
        String applicantUserId = null;
        String applicantUserName = contract.getEmpName();
        String applicantEmpCode = contract.getEmpCode();
        
        if (authServiceClient != null) {
            try {
                // 优先使用empCode（工号）获取用户信息
                if (applicantEmpCode != null && !applicantEmpCode.isEmpty()) {
                    Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserByAccount(applicantEmpCode);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        com.hrp.common.entity.User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
                // 如果通过empCode获取不到，尝试使用empId（用户ID）
                if (applicantUserId == null && contract.getEmpId() != null) {
                    Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserById(String.valueOf(contract.getEmpId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        com.hrp.common.entity.User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取申请人用户信息失败: " + e.getMessage());
                e.printStackTrace();
                // 如果获取不到用户信息，无法退回
                if (applicantUserId == null) {
                    System.err.println("无法获取申请人的用户ID，退回失败");
                    return false;
                }
            }
        }
        
        // 如果获取不到用户信息，无法退回（returnTask需要applicantUserId）
        if (applicantUserId == null) {
            System.err.println("无法获取申请人的用户ID，退回失败");
            return false;
        }
        
        // 根据returnType决定处理方式
        if (authServiceClient != null && contract.getContractNo() != null) {
            try {
                java.util.Map<String, Object> returnRequest = new java.util.HashMap<>();
                returnRequest.put("taskKey", contract.getContractNo());
                returnRequest.put("taskId", null); // 自动查找当前待处理任务
                returnRequest.put("returnType", returnType);
                returnRequest.put("applicantUserId", applicantUserId);
                returnRequest.put("applicantUserName", applicantUserName != null ? applicantUserName : "");
                returnRequest.put("applicantEmpCode", applicantEmpCode != null ? applicantEmpCode : "");
                returnRequest.put("comment", opinion != null ? opinion : "退回");
                
                Result<Boolean> returnResult = authServiceClient.returnTask(returnRequest);
                if (returnResult == null || returnResult.getCode() != 200 || !Boolean.TRUE.equals(returnResult.getData())) {
                    System.err.println("退回任务失败: " + (returnResult != null ? returnResult.getMessage() : "未知错误"));
                    return false;
                }
                
                System.out.println("退回任务成功: contractNo=" + contract.getContractNo() + ", returnType=" + returnType);
            } catch (Exception e) {
                System.err.println("退回处理异常: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        // 将状态改为REJECTED（已拒绝），退回后的状态应该是已拒绝
        contract.setStatus("REJECTED");
        contract.setUpdateTime(LocalDateTime.now());
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean archive(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        contract.setStatus("ARCHIVED");
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    public String getNextApprover(Long contractId) {
        // TODO: 从流程任务中获取下一个审批人
        return "待实现";
    }

    @Override
    @Transactional
    public boolean submitByContractNo(String contractNo) {
        PactMain contract = pactMainMapper.selectByContractNo(contractNo);
        if (contract == null) {
            return false;
        }
        // 使用已有的submit方法，传入pactId
        return submit(contract.getPactId());
    }

    @Override
    public PageResult<PactMain> getApprovedContractsPage(Long page, Long size, String contractNo, String contractName, String contractType, String executionStatus) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<PactMain> list = pactMainMapper.selectApprovedContractsByConditions(contractNo, contractName, contractType, executionStatus);
        PageInfo<PactMain> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional
    public boolean invalidate(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        contract.setExecutionStatus("INVALID");
        contract.setIsManualModify(1); // 手动修改
        contract.setUpdateTime(LocalDateTime.now());
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean archiveManual(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        
        // 校验：只有已履约状态的合同才能归档
        String executionStatus = contract.getExecutionStatus();
        if (executionStatus == null || !executionStatus.equals("COMPLETED")) {
            throw new RuntimeException("只有已履约状态的合同才能归档");
        }
        
        contract.setExecutionStatus("ARCHIVED");
        contract.setIsManualModify(1); // 手动修改
        contract.setUpdateTime(LocalDateTime.now());
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public void updateExecutionStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 更新待履约和履约中的合同
        // 当前时间 >= 开始时间，且执行状态为待履约或履约中的，改为履约中
        // 注意：只更新非人工修改的合同
        pactMainMapper.updateExecutionStatusToExecuting(now);
        
        // 当前时间 >= 结束时间，且执行状态为履约中的，改为已履约
        // 注意：只更新非人工修改的合同
        pactMainMapper.updateExecutionStatusToCompleted(now);
    }
}

