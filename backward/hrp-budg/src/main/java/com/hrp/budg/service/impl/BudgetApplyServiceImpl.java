package com.hrp.budg.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.budg.feign.AuthServiceClient;
import com.hrp.budg.mapper.BudgetApplyMapper;
import com.hrp.budg.mapper.BudgetDetailRecordMapper;
import com.hrp.budg.mapper.BudgetMapper;
import com.hrp.budg.service.BudgetApplyService;
import com.hrp.common.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预算申请服务实现
 */
@Service
public class BudgetApplyServiceImpl implements BudgetApplyService {

    @Autowired
    private BudgetApplyMapper budgetApplyMapper;
    
    @Autowired
    private BudgetMapper budgetMapper;
    
    @Autowired
    private BudgetDetailRecordMapper budgetDetailRecordMapper;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;

    @Override
    public BudgetApply getById(Long id) {
        return budgetApplyMapper.selectById(id);
    }

    @Override
    public BudgetApply getByNo(String applyNo) {
        return budgetApplyMapper.selectByNo(applyNo);
    }

    @Override
    public PageResult<BudgetApply> getPage(Long page, Long size, String applyNo, Long itemId, String applicantName, String applicantCode, String status, String startDate, String endDate) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<BudgetApply> list = budgetApplyMapper.selectByConditions(applyNo, itemId, applicantName, applicantCode, status, startDate, endDate);
        PageInfo<BudgetApply> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<BudgetApply> getPageByApprover(Long page, Long size, String currentUserId,
                                                      String applyNo, Long itemId, String applicantName,
                                                      String status, String startDate, String endDate) {
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
            return new PageResult<>(new java.util.ArrayList<>(), 0L, size, page);
        }
        
        // 如果状态是PENDING，查询待审批状态的申请单；否则不限制状态，后续根据taskKeys和实际状态过滤
        String filterStatus = "PENDING".equals(status) ? status : null;
        
        // 根据taskKey（applyNo）列表查询预算申请，并应用查询条件（审批列表不需要按申请人代码过滤，传入null）
        List<BudgetApply> allApplies = budgetApplyMapper.selectByConditions(applyNo, itemId, applicantName, null, filterStatus, startDate, endDate);
        
        // 过滤出当前用户需要审批的申请单（taskKey在taskKeys中的），并填充当前审批人信息
        List<BudgetApply> filteredApplies = new java.util.ArrayList<>();
        for (BudgetApply apply : allApplies) {
            if (apply.getApplyNo() != null && taskKeys.contains(apply.getApplyNo())) {
                // 如果查询的是非PENDING状态，还需要匹配申请单的实际状态
                if (status != null && !"PENDING".equals(status)) {
                    if (!status.equals(apply.getStatus())) {
                        continue; // 状态不匹配，跳过
                    }
                }
                // 从任务中获取当前审批人信息
                com.hrp.common.entity.ProcessTask task = taskMap.get(apply.getApplyNo());
                if (task != null && task.getAssigneeUserName() != null) {
                    // 设置当前审批人（taskMap中存储的已经是print_order最小的任务，即当前节点）
                    apply.setCurrentApprover(task.getAssigneeUserName());
                }
                filteredApplies.add(apply);
            }
        }
        
        // 手动分页
        long total = filteredApplies.size();
        long start = (page - 1) * size;
        long end = Math.min(start + size, total);
        List<BudgetApply> pagedList = start < total ? filteredApplies.subList((int)start, (int)end) : new java.util.ArrayList<>();
        
        return new PageResult<>(pagedList, total, size, page);
    }
    
    /**
     * 判断当前用户是否是申请单的审批人
     */
    private boolean isCurrentUserApprover(BudgetApply apply, String currentUserAccount) {
        if (currentUserAccount == null || currentUserAccount.trim().isEmpty()) {
            return false;
        }
        
        // 如果流程定义ID为空或申请单号为空，无法判断
        if (apply.getProcessDefinitionId() == null || apply.getApplyNo() == null) {
            return false;
        }
        
        try {
            // 获取当前用户的详细信息（包括姓名）
            String currentUserName = null;
            if (authServiceClient != null) {
                Result<User> userResult = authServiceClient.getUserByAccount(currentUserAccount);
                if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                    User user = userResult.getData();
                    currentUserName = user.getName(); // 用户姓名
                }
            }
            
            // 使用Feign客户端调用auth服务获取流程节点信息（带业务数据）
            if (authServiceClient != null) {
                Result<List<ProcessNodeInfo>> response = authServiceClient.getProcessNodesWithBusiness(
                        apply.getProcessDefinitionId(), apply.getApplyNo());
                
                if (response != null && response.getCode() == 200 && response.getData() != null) {
                    List<ProcessNodeInfo> nodes = response.getData();
                    
                    // 检查当前用户是否是任一节点的审批人
                    for (ProcessNodeInfo node : nodes) {
                        String assigneeName = node.getAssigneeName();
                        String assigneeCode = node.getAssigneeCode();
                        String assigneeId = node.getAssigneeId();
                        
                        // 检查账号、工号、ID或姓名是否匹配
                        if (currentUserAccount.equals(assigneeName) || 
                            currentUserAccount.equals(assigneeCode) || 
                            currentUserAccount.equals(assigneeId) ||
                            (currentUserName != null && currentUserName.equals(assigneeName))) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("判断审批人失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    @Override
    public List<BudgetApply> getByItemId(Long itemId) {
        return budgetApplyMapper.selectByItemId(itemId);
    }

    @Override
    @Transactional
    public BudgetApply save(BudgetApply budgetApply) {
        // 验证：同一项目+同一主体+同一年度，只能申请一次
        if (budgetApply.getItemId() != null && budgetApply.getSubjectId() != null && budgetApply.getBudgetYear() != null) {
            BudgetApply existingApply = budgetApplyMapper.selectByItemSubjectYear(
                budgetApply.getItemId(), 
                budgetApply.getSubjectId(), 
                budgetApply.getBudgetYear(),
                null // 新增时不需要排除任何ID
            );
            if (existingApply != null) {
                throw new RuntimeException("该预算项目、预算主体在" + budgetApply.getBudgetYear() + "年度已存在申请，不能重复申请");
            }
        }
        
        // 生成申请单号：BUDG+年月日+0001，例如：BUDG202512230001
        if (budgetApply.getApplyNo() == null || budgetApply.getApplyNo().isEmpty()) {
            budgetApply.setApplyNo(generateApplyNo());
        }
        // 如果没有mainAttachId，生成一个时间戳作为主附件ID
        if (budgetApply.getMainAttachId() == null || budgetApply.getMainAttachId().isEmpty()) {
            budgetApply.setMainAttachId(String.valueOf(System.currentTimeMillis()));
        }
        if (budgetApply.getStatus() == null) {
            budgetApply.setStatus("DRAFT");
        }
        // 设置申请时间，默认为当前时间
        if (budgetApply.getApplyDate() == null) {
            budgetApply.setApplyDate(LocalDateTime.now());
        }
        
        // 如果deptId或deptCode为空，尝试根据申请人信息获取部门信息
        if ((budgetApply.getDeptId() == null || budgetApply.getDeptCode() == null || budgetApply.getDeptCode().isEmpty()) 
            && authServiceClient != null) {
            try {
                User user = null;
                // 优先使用applicantCode（工号）获取用户信息
                if (budgetApply.getApplicantCode() != null && !budgetApply.getApplicantCode().isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(budgetApply.getApplicantCode());
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        user = userResult.getData();
                    }
                }
                // 如果通过applicantCode获取不到，尝试使用applicantId（用户ID）
                if (user == null && budgetApply.getApplicantId() != null) {
                    Result<User> userResult = authServiceClient.getUserById(String.valueOf(budgetApply.getApplicantId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        user = userResult.getData();
                    }
                }
                
                // 如果获取到用户信息，设置部门信息
                if (user != null && user.getDeptCode() != null && !user.getDeptCode().isEmpty()) {
                    budgetApply.setDeptCode(user.getDeptCode());
                    budgetApply.setDeptName(user.getDeptName());
                    // 如果User中有deptId，直接使用；否则可以通过deptCode在后续查询时获取
                    if (user.getDeptId() != null) {
                        budgetApply.setDeptId(user.getDeptId());
                    }
                }
            } catch (Exception e) {
                System.err.println("获取申请人部门信息失败: " + e.getMessage());
                e.printStackTrace();
                // 不影响保存流程，继续执行
            }
        }
        
        budgetApply.setCreateTime(LocalDateTime.now());
        budgetApply.setUpdateTime(LocalDateTime.now());
        boolean success = budgetApplyMapper.insert(budgetApply) > 0;
        if (success) {
            return budgetApply; // 返回包含生成的applyNo的对象
        }
        return null;
    }

    /**
     * 生成申请单号：BUDG+年月日+0001
     * 例如：BUDG202512230001, BUDG202512230002
     */
    private String generateApplyNo() {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String prefix = "BUDG" + year + month + day;
        
        // 查询当前日期的最大申请单号
        String maxApplyNo = budgetApplyMapper.selectMaxApplyNoByPrefix(prefix);
        
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

    @Override
    @Transactional
    public BudgetApply update(BudgetApply budgetApply) {
        // 验证：同一项目+同一主体+同一年度，只能申请一次（排除当前申请）
        if (budgetApply.getItemId() != null && budgetApply.getSubjectId() != null && budgetApply.getBudgetYear() != null) {
            BudgetApply existingApply = budgetApplyMapper.selectByItemSubjectYear(
                budgetApply.getItemId(), 
                budgetApply.getSubjectId(), 
                budgetApply.getBudgetYear(),
                budgetApply.getApplyId() // 更新时排除当前申请ID
            );
            if (existingApply != null) {
                throw new RuntimeException("该预算项目、预算主体在" + budgetApply.getBudgetYear() + "年度已存在申请，不能重复申请");
            }
        }
        
        budgetApply.setUpdateTime(LocalDateTime.now());
        boolean success = budgetApplyMapper.updateById(budgetApply) > 0;
        if (success) {
            // 返回更新后的对象（包含applyNo等信息）
            return budgetApplyMapper.selectById(budgetApply.getApplyId());
        }
        return null;
    }

    @Override
    @Transactional
    public boolean submit(Long id) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            System.err.println("提交失败: 预算申请不存在, id=" + id);
            return false;
        }
        
        // 验证：同一项目+同一主体+同一年度，只能申请一次（排除当前申请）
        if (apply.getItemId() != null && apply.getSubjectId() != null && apply.getBudgetYear() != null) {
            BudgetApply existingApply = budgetApplyMapper.selectByItemSubjectYear(
                apply.getItemId(), 
                apply.getSubjectId(), 
                apply.getBudgetYear(),
                apply.getApplyId() // 提交时排除当前申请ID
            );
            if (existingApply != null) {
                throw new RuntimeException("该预算项目、预算主体在" + apply.getBudgetYear() + "年度已存在申请，不能重复申请");
            }
        }
        
        System.out.println("=== 开始提交预算申请 ===");
        System.out.println("申请ID: " + apply.getApplyId());
        System.out.println("申请单号: " + apply.getApplyNo());
        System.out.println("流程定义ID: " + apply.getProcessDefinitionId());
        System.out.println("模板配置ID: " + apply.getTemplateConfigId());
        System.out.println("部门ID: " + apply.getDeptId());
        System.out.println("部门编码: " + apply.getDeptCode());
        
        apply.setStatus("PENDING");
        apply.setUpdateTime(LocalDateTime.now());
        boolean updateSuccess = budgetApplyMapper.updateById(apply) > 0;
        
        if (!updateSuccess) {
            System.err.println("提交失败: 更新申请状态失败");
            return false;
        }
        
        Long processDefinitionId = apply.getProcessDefinitionId();
        if (processDefinitionId == null) {
            System.err.println("警告: processDefinitionId为空，无法启动流程");
            System.err.println("templateConfigId: " + apply.getTemplateConfigId());
            return updateSuccess; // 即使没有流程定义，也返回true表示状态更新成功
        }
        
        if (apply.getApplyNo() == null || apply.getApplyNo().isEmpty()) {
            System.err.println("警告: applyNo为空，无法启动流程");
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
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> existingTasksResult = authServiceClient.getTasksByTaskKey(apply.getApplyNo());
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
            System.out.println("准备处理旧的流程任务记录（如果存在），applyNo=" + apply.getApplyNo() + ", hasReturnToCurrent=" + hasReturnToCurrent);
            try {
                if (hasReturnToCurrent) {
                    // 恢复退回的任务为待审批状态，而不是删除
                    // 这样保留退回历史记录，同时让任务重新进入审批流程
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
            // 不影响提交操作，只记录错误
        }
        
        return updateSuccess;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id, String opinion, String approverSignature) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        
        // 完成任务（更新流程任务状态）
        if (authServiceClient != null && apply.getApplyNo() != null) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("taskKey", apply.getApplyNo());
                request.put("taskId", null); // 自动查找当前待处理任务
                request.put("comment", opinion);
                if (approverSignature != null && !approverSignature.trim().isEmpty()) {
                    request.put("approverSignature", approverSignature);
                }
                
                System.out.println("准备完成任务: taskKey=" + apply.getApplyNo() + ", opinion=" + opinion);
                Result<Boolean> completeResult = authServiceClient.completeTask(request);
                if (completeResult != null && completeResult.getCode() == 200 && Boolean.TRUE.equals(completeResult.getData())) {
                    System.out.println("任务完成成功");
                } else {
                    System.err.println("任务完成失败: " + (completeResult != null ? completeResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("完成任务异常: " + e.getMessage());
                e.printStackTrace();
                // 不影响审批操作，只记录错误
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
                    System.out.println("待处理任务数: " + pendingCount + ", 所有任务是否完成: " + allTasksCompleted);
                }
            } catch (Exception e) {
                System.err.println("检查任务状态异常: " + e.getMessage());
                e.printStackTrace();
                // 默认认为已完成，继续审批流程
            }
        }
        
        // 只有所有任务都完成时，才将申请状态改为APPROVED
        if (allTasksCompleted) {
            apply.setStatus("APPROVED");
        } else {
            // 如果还有待处理任务，保持PENDING状态
            apply.setStatus("PENDING");
        }
        apply.setUpdateTime(LocalDateTime.now());
        boolean success = budgetApplyMapper.updateById(apply) > 0;
        
        if (success && allTasksCompleted) {
            // 审批通过后，更新预算总额
            // 查找对应的预算记录
            List<Budget> budgets = budgetMapper.selectBySubjectAndItem(apply.getSubjectId(), apply.getItemId());
            if (budgets != null && !budgets.isEmpty()) {
                // 使用第一个匹配的预算（通常一个主体和一个项目对应一个预算）
                Budget budget = budgets.get(0);
                
                // 更新预算总额为申请的金额（如果预算总额为0或null，则设置为申请金额）
                if (budget.getBudgetAmount() == null || budget.getBudgetAmount().compareTo(new java.math.BigDecimal("0")) == 0) {
                    budget.setBudgetAmount(apply.getApplyAmount());
                    budget.setUpdateTime(LocalDateTime.now());
                    int updateResult = budgetMapper.updateById(budget);
                    if (updateResult <= 0) {
                        throw new RuntimeException("更新预算总额失败");
                    }
                    System.out.println("更新预算总额: budgetId=" + budget.getBudgetId() + ", budgetAmount=" + apply.getApplyAmount());
                }
            } else {
                // 如果没有找到预算记录，创建新的预算记录
                Budget newBudget = new Budget();
                newBudget.setBudgetYear(apply.getBudgetYear());
                newBudget.setBudgetPeriod("YEAR");
                newBudget.setSubjectId(apply.getSubjectId());
                newBudget.setSubjectCode(apply.getSubjectCode());
                newBudget.setItemId(apply.getItemId());
                newBudget.setItemCode(apply.getItemCode());
                newBudget.setBudgetAmount(apply.getApplyAmount());
                newBudget.setCreateUser(apply.getCreateUser());
                

                newBudget.setBudgetNo(apply.getApplyNo());
                newBudget.setBudgetName(apply.getItemName());
                
                int insertResult = budgetMapper.insert(newBudget);
                if (insertResult <= 0) {
                    throw new RuntimeException("创建预算记录失败");
                }
                
                // 检查是否成功获取了生成的 budget_id
                if (newBudget.getBudgetId() == null) {
                    throw new RuntimeException("创建预算记录失败：未获取到预算ID");
                }
                

            }
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean reject(Long id, String opinion) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        
        // 如果之前已经审批通过并创建了明细记录，需要作废
        if ("APPROVED".equals(apply.getStatus())) {
            budgetDetailRecordMapper.cancelByBusinessNo(apply.getApplyNo());
        }
        
        apply.setStatus("REJECTED");
        apply.setUpdateTime(LocalDateTime.now());
        return budgetApplyMapper.updateById(apply) > 0;
    }
    
    @Override
    @Transactional
    public boolean returnApply(Long id, String returnType, String opinion) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        
        // 如果之前已经审批通过并创建了明细记录，需要作废
        if ("APPROVED".equals(apply.getStatus())) {
            budgetDetailRecordMapper.cancelByBusinessNo(apply.getApplyNo());
        }
        
        // 获取申请人的用户信息（sys_user.id）
        String applicantUserId = null;
        String applicantUserName = apply.getApplicantName();
        String applicantEmpCode = apply.getApplicantCode();
        
        if (authServiceClient != null) {
            try {
                // 优先使用applicantCode（工号）获取用户信息
                if (applicantEmpCode != null && !applicantEmpCode.isEmpty()) {
                    Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserByAccount(applicantEmpCode);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        com.hrp.common.entity.User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
                // 如果通过applicantCode获取不到，尝试使用applicantId（用户ID）
                if (applicantUserId == null && apply.getApplicantId() != null) {
                    Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserById(String.valueOf(apply.getApplicantId()));
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
        
        // 根据returnType决定处理方式
        if (authServiceClient != null && apply.getApplyNo() != null && applicantUserId != null) {
            try {
                Map<String, Object> returnRequest = new HashMap<>();
                returnRequest.put("taskKey", apply.getApplyNo());
                returnRequest.put("taskId", null); // 自动查找当前待处理任务
                returnRequest.put("returnType", returnType);
                returnRequest.put("applicantUserId", applicantUserId);
                returnRequest.put("applicantUserName", applicantUserName);
                returnRequest.put("applicantEmpCode", applicantEmpCode);
                returnRequest.put("comment", opinion != null ? opinion : "退回");
                
                Result<Boolean> returnResult = authServiceClient.returnTask(returnRequest);
                if (returnResult == null || returnResult.getCode() != 200 || !Boolean.TRUE.equals(returnResult.getData())) {
                    System.err.println("退回任务失败: " + (returnResult != null ? returnResult.getMessage() : "未知错误"));
                    return false;
                }
                
                System.out.println("退回任务成功: applyNo=" + apply.getApplyNo() + ", returnType=" + returnType);
            } catch (Exception e) {
                System.err.println("退回处理异常: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        // 将状态改为REJECTED（已拒绝），退回后的状态应该是已拒绝
        // 注意：虽然ProcessTaskServiceImpl.returnTask也会更新状态为REJECTED，但这里也设置一次确保状态正确
        apply.setStatus("REJECTED");
        apply.setUpdateTime(LocalDateTime.now());
        return budgetApplyMapper.updateById(apply) > 0;
    }

    @Override
    @Transactional
    public boolean withdraw(Long id) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        
        // 只有草稿或待审批状态的申请才能撤回
        if (!"DRAFT".equals(apply.getStatus()) && !"PENDING".equals(apply.getStatus())) {
            throw new RuntimeException("只有草稿或待审批状态的申请才能撤回");
        }
        
        // 如果已经创建了明细记录，需要作废
        if ("APPROVED".equals(apply.getStatus())) {
            budgetDetailRecordMapper.cancelByBusinessNo(apply.getApplyNo());
        }
        
        // 删除流程任务记录
        if (apply.getApplyNo() != null && !apply.getApplyNo().isEmpty() && authServiceClient != null) {
            try {
                System.out.println("撤回预算申请，准备删除流程任务记录，applyNo=" + apply.getApplyNo());
                Result<String> deleteResult = authServiceClient.deleteTasksByTaskKey(apply.getApplyNo());
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    System.out.println("流程任务记录删除成功，applyNo=" + apply.getApplyNo());
                } else {
                    System.err.println("流程任务记录删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
                // 不影响撤回操作，只记录错误
            }
        }
        
        apply.setStatus("WITHDRAWN");
        apply.setUpdateTime(LocalDateTime.now());
        return budgetApplyMapper.updateById(apply) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        
        // 只有草稿状态、撤回状态或拒绝状态的申请才能删除
        if (!"DRAFT".equals(apply.getStatus()) && !"WITHDRAWN".equals(apply.getStatus()) && !"REJECTED".equals(apply.getStatus())) {
            throw new RuntimeException("只有草稿状态、撤回状态或拒绝状态的申请才能删除");
        }
        
        // 删除关联的明细记录（如果有）
        budgetDetailRecordMapper.cancelByBusinessNo(apply.getApplyNo());
        
        // 删除附件记录和文件（通过Feign调用auth服务）
        // 优先使用mainAttachId作为businessId删除附件（因为附件是使用mainAttachId存储的）
        // 如果没有mainAttachId，再尝试使用applyNo（兼容旧数据）
        if (authServiceClient != null) {
            try {
                String businessId = null;
                if (apply.getMainAttachId() != null && !apply.getMainAttachId().isEmpty()) {
                    // 优先使用mainAttachId（附件实际使用的businessId）
                    businessId = apply.getMainAttachId();
                    System.out.println("删除预算申请的附件，使用mainAttachId=" + businessId);
                } else if (apply.getApplyNo() != null && !apply.getApplyNo().isEmpty()) {
                    // 如果没有mainAttachId，使用applyNo（兼容旧数据）
                    businessId = apply.getApplyNo();
                    System.out.println("删除预算申请的附件，使用applyNo=" + businessId);
                }
                
                if (businessId != null && !businessId.isEmpty()) {
                    Result<Void> deleteResult = authServiceClient.deleteAttachmentsByBusiness("BUDGET_APPLY", businessId);
                    if (deleteResult != null && deleteResult.getCode() == 200) {
                        System.out.println("附件删除成功，businessId=" + businessId);
                    } else {
                        System.err.println("附件删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                    }
                } else {
                    System.out.println("预算申请没有附件标识（mainAttachId和applyNo都为空），跳过附件删除");
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
                System.out.println("删除预算申请的流程任务记录（审批记录），applyNo=" + apply.getApplyNo());
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
        
        // 删除申请
        return budgetApplyMapper.deleteById(id) > 0;
    }
}

