package com.hrp.hr.service.impl;

import com.hrp.common.entity.HrApply;
import com.hrp.common.entity.HrAttRecord;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.TemplateConfig;
import com.hrp.common.entity.User;
import com.hrp.common.exception.BusinessException;
import com.hrp.hr.feign.AuthServiceClient;
import com.hrp.hr.mapper.HrApplyMapper;
import com.hrp.hr.mapper.HrAttRecordMapper;
import com.hrp.hr.service.HrApplyService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HrApplyServiceImpl implements HrApplyService {
    
    /**
     * 计算天数：工时 >= 9小时为1，>= 4小时为0.5，否则为0
     */
    private String calculateDay(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return "0";
        }
        // 1. 先获取两个时间之间的总分钟数（保证精度，无丢失）
        long totalMinutes = Duration.between(startTime, endTime).toMinutes();
        // 2. 转换为double类型的小时数（除以60.0，保留小数部分，避免精度丢失）
        double hours = totalMinutes / 60.0;
        if (hours >= 9) {
            return "1";
        } else if (hours >= 4.5) {
            return "0.5";
        } else {
            return "0";
        }
    }

    @Autowired
    private HrApplyMapper hrApplyMapper;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;
    
    @Autowired
    private HrAttRecordMapper hrAttRecordMapper;

    @Override
    public HrApply getById(Long applyId) {
        return hrApplyMapper.selectById(applyId);
    }

    @Override
    public HrApply getByApplyNo(String applyNo) {
        return hrApplyMapper.selectByApplyNo(applyNo);
    }

    @Override
    public List<HrApply> getMyApplies(Long empId) {
        return hrApplyMapper.selectByEmpId(empId);
    }

    @Override
    public PageResult<HrApply> getMyAppliesPage(Long empId, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<HrApply> list = hrApplyMapper.selectByEmpId(empId);
        PageInfo<HrApply> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<HrApply> getAppliesByStatus(String status) {
        return hrApplyMapper.selectByStatus(status);
    }

    @Override
    public PageResult<HrApply> getAppliesByStatusPage(String status, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<HrApply> list = hrApplyMapper.selectByStatus(status);
        PageInfo<HrApply> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<HrApply> getMyApprovalApplies(String userId) {
        return hrApplyMapper.selectByApprover(userId);
    }

    @Override
    public PageResult<HrApply> getMyApprovalAppliesPage(String userId, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<HrApply> list = hrApplyMapper.selectByApprover(userId);
        PageInfo<HrApply> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<HrApply> getAllApplies() {
        return hrApplyMapper.selectAll();
    }

    @Override
    public PageResult<HrApply> getAllAppliesPage(Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<HrApply> list = hrApplyMapper.selectAll();
        PageInfo<HrApply> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<HrApply> getPage(Long page, Long size, String applyNo, Long empId, 
                                       String hrApplyType, String status, String startDate, String endDate) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<HrApply> list = hrApplyMapper.selectByConditions(applyNo, empId, null, hrApplyType, status, startDate, endDate);
        PageInfo<HrApply> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<HrApply> getPageByApprover(Long page, Long size, String currentUserId,
                                                  String applyNo, String empName, String hrApplyType, 
                                                  String status, String startDate, String endDate) {
        // 根据状态参数决定查询逻辑
        java.util.Map<String, com.hrp.common.entity.ProcessTask> taskMap = new java.util.HashMap<>(); // taskKey -> 任务映射
        java.util.Set<String> taskKeys = new java.util.HashSet<>();
        
        if (authServiceClient != null && currentUserId != null && !currentUserId.trim().isEmpty()) {
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> tasksResult = null;
                
                // 如果状态是PENDING或null（默认查询待审批），查询待审批任务；否则查询已完成的任务（用于查看已审批记录）
                if ("PENDING".equals(status) || status == null || status.isEmpty()) {
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
                            if ("PENDING".equals(status) || status == null || status.isEmpty()) {
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
                // 查询审批任务失败，记录错误但不影响返回空列表
                System.err.println("查询审批任务失败: " + e.getMessage());
            }
        }
        
        // 如果没有任务，返回空列表
        if (taskKeys.isEmpty()) {
            return new PageResult<>(new java.util.ArrayList<>(), 0L, size, page);
        }
        
        // 根据taskKey（applyNo）列表查询业务申请，并应用查询条件
        // 注意：如果status不是PENDING或null，需要查询所有状态的记录，然后根据taskKeys过滤
        String queryStatus = ("PENDING".equals(status) || status == null || status.isEmpty()) ? "PENDING" : null; // 非PENDING状态时不限制status
        List<HrApply> allApplies = hrApplyMapper.selectByConditions(applyNo, null, empName, hrApplyType, queryStatus, startDate, endDate);
        
        // 过滤出当前用户需要审批的申请单（taskKey在taskKeys中的），并填充当前审批人信息
        List<HrApply> filteredApplies = new java.util.ArrayList<>();
        for (HrApply apply : allApplies) {
            if (apply.getApplyNo() != null && taskKeys.contains(apply.getApplyNo())) {
                // 如果查询的是非PENDING状态，还需要匹配申请单的实际状态
                if (status != null && !status.isEmpty() && !"PENDING".equals(status)) {
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
        List<HrApply> pagedList = start < total ? filteredApplies.subList((int)start, (int)end) : new java.util.ArrayList<>();
        
        return new PageResult<>(pagedList, total, size, page);
    }

    @Override
    @Transactional
    public boolean save(HrApply hrApply) {
        // 设置默认值
        if (hrApply.getIsNurse() == null) {
            hrApply.setIsNurse(0);
        }
        // 如果是补卡申请且子类型为空，设置默认值
        if ("SUPPLY".equals(hrApply.getHrApplyType()) && 
            (hrApply.getHrApplySubType() == null || hrApply.getHrApplySubType().isEmpty())) {
            hrApply.setHrApplySubType("SUPPLY");
        }
        // 保存时填充员工和部门信息
        if (hrApply.getEmpId() != null && authServiceClient != null) {
            try {
                // 通过empId查询员工信息
                User user = null;
                // 尝试通过empCode查询
                if (hrApply.getEmpCode() != null && !hrApply.getEmpCode().isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(hrApply.getEmpCode());
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        user = userResult.getData();
                    }
                }
                // 如果获取不到，尝试通过empId查询（empId可能是userId）
                if (user == null) {
                    Result<User> userResult = authServiceClient.getUserById(String.valueOf(hrApply.getEmpId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        user = userResult.getData();
                    }
                }
                
                if (user != null) {
                    if (hrApply.getEmpCode() == null || hrApply.getEmpCode().isEmpty()) {
                        hrApply.setEmpCode(user.getAccount());
                    }
                    if (hrApply.getEmpName() == null || hrApply.getEmpName().isEmpty()) {
                        hrApply.setEmpName(user.getName());
                    }
                    if (hrApply.getEmpPhone() == null || hrApply.getEmpPhone().isEmpty()) {
                        hrApply.setEmpPhone(user.getPhone());
                    }
                    if (hrApply.getDeptId() == null) {
                        hrApply.setDeptId(user.getDeptId());
                    }
                    if (hrApply.getDeptName() == null || hrApply.getDeptName().isEmpty()) {
                        hrApply.setDeptName(user.getDeptName());
                    }
                }
            } catch (Exception e) {
                System.err.println("获取员工信息失败: " + e.getMessage());
            }
        }
        
        if (hrApply.getApplyNo() == null || hrApply.getApplyNo().isEmpty()) {
            hrApply.setApplyNo(generateApplyNo(hrApply.getHrApplyType()));
        }
        
        // 设置状态为草稿（如果未设置）
        if (hrApply.getStatus() == null || hrApply.getStatus().isEmpty()) {
            hrApply.setStatus("DRAFT");
        }
        
        int result = hrApplyMapper.insert(hrApply);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean update(HrApply hrApply) {
        if (hrApply.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        
        // 更新时也填充员工和部门信息（如果缺失）
        if ((hrApply.getEmpCode() == null || hrApply.getEmpName() == null || hrApply.getDeptName() == null) 
            && hrApply.getEmpId() != null && authServiceClient != null) {
            try {
                User user = null;
                if (hrApply.getEmpCode() != null && !hrApply.getEmpCode().isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(hrApply.getEmpCode());
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        user = userResult.getData();
                    }
                }
                if (user == null) {
                    Result<User> userResult = authServiceClient.getUserById(String.valueOf(hrApply.getEmpId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        user = userResult.getData();
                    }
                }
                
                if (user != null) {
                    if (hrApply.getEmpCode() == null || hrApply.getEmpCode().isEmpty()) {
                        hrApply.setEmpCode(user.getAccount());
                    }
                    if (hrApply.getEmpName() == null || hrApply.getEmpName().isEmpty()) {
                        hrApply.setEmpName(user.getName());
                    }
                    if (hrApply.getEmpPhone() == null || hrApply.getEmpPhone().isEmpty()) {
                        hrApply.setEmpPhone(user.getPhone());
                    }
                    if (hrApply.getDeptId() == null) {
                        hrApply.setDeptId(user.getDeptId());
                    }
                    if (hrApply.getDeptName() == null || hrApply.getDeptName().isEmpty()) {
                        hrApply.setDeptName(user.getDeptName());
                    }
                }
            } catch (Exception e) {
                System.err.println("获取员工信息失败: " + e.getMessage());
            }
        }
        
        hrApplyMapper.updateById(hrApply);
        return true;
    }

    @Override
    @Transactional
    public boolean delete(Long applyId) {
        HrApply hrApply = hrApplyMapper.selectById(applyId);
        if (hrApply == null) {
            return false;
        }
        
        // 只有草稿、撤回或拒绝状态才能删除
        if (!"DRAFT".equals(hrApply.getStatus()) && !"WITHDRAWN".equals(hrApply.getStatus()) && !"REJECTED".equals(hrApply.getStatus())) {
            return false;
        }
        
        // 删除流程任务记录
        if (hrApply.getApplyNo() != null && !hrApply.getApplyNo().isEmpty() && authServiceClient != null) {
            try {
                authServiceClient.deleteTasksByTaskKey(hrApply.getApplyNo());
            } catch (Exception e) {
                // 删除流程任务记录失败不影响撤回操作，只记录错误
                System.err.println("删除流程任务记录异常: " + e.getMessage());
            }
        }
        
        // 删除附件
        if (hrApply.getMainAttachId() != null && !hrApply.getMainAttachId().isEmpty() && authServiceClient != null) {
            try {
                String businessId = hrApply.getMainAttachId();
                String businessType = "HR_APPLY";
                authServiceClient.deleteAttachmentsByBusiness(businessType, businessId);
            } catch (Exception e) {
                // 附件删除失败不影响删除操作，只记录错误
                System.err.println("删除附件异常: " + e.getMessage());
            }
        }
        
        return hrApplyMapper.deleteById(applyId) > 0;
    }

    @Override
    public String generateApplyNo(String hrApplyType) {
        // 根据申请类型生成不同的前缀
        String prefix = "HR";
        
        
        // 格式：前缀 + 年月日 + 4位序号，例如：QJ202601010001
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        prefix = prefix + dateStr;
        
        String maxNo = hrApplyMapper.selectMaxApplyNoByPrefix(prefix);
        int seq = 1;
        if (maxNo != null && maxNo.length() > prefix.length()) {
            String seqStr = maxNo.substring(prefix.length());
            try {
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        
        return prefix + String.format("%04d", seq);
    }

    @Override
    @Transactional
    public boolean submit(Long applyId) {
        HrApply hrApply = hrApplyMapper.selectById(applyId);
        if (hrApply == null) {
            return false;
        }
        if (!"DRAFT".equals(hrApply.getStatus()) && !"WITHDRAWN".equals(hrApply.getStatus()) && !"REJECTED".equals(hrApply.getStatus())) {
            return false; // 只有草稿、撤回或拒绝状态才能提交
        }
        
        // 如果没有单号，生成一个
        if (hrApply.getApplyNo() == null || hrApply.getApplyNo().isEmpty()) {
            hrApply.setApplyNo(generateApplyNo(hrApply.getHrApplyType()));
            hrApplyMapper.updateById(hrApply);
        }
        
        // 根据模板配置ID或申请类型从模板设置中获取流程定义
        Long processDefinitionId = null;
        if (authServiceClient != null) {
            try {
                TemplateConfig templateConfig = null;
                
                // 优先使用模板配置ID
                if (hrApply.getTemplateConfigId() != null) {
                    Result<TemplateConfig> configResult = authServiceClient.getTemplateConfigById(hrApply.getTemplateConfigId());
                    if (configResult != null && configResult.getCode() == 200 && configResult.getData() != null) {
                        templateConfig = configResult.getData();
                    }
                }
                
                // 如果没有模板配置ID或获取失败，则根据申请类型获取（使用HR_APPLY_TYPE作为businessType）
                if (templateConfig == null && hrApply.getHrApplyType() != null && !hrApply.getHrApplyType().isEmpty()) {
                    Result<TemplateConfig> configResult = authServiceClient.getTemplateConfigByBusinessType("HR_APPLY_TYPE", hrApply.getHrApplyType());
                    if (configResult != null && configResult.getCode() == 200 && configResult.getData() != null) {
                        templateConfig = configResult.getData();
                        if (templateConfig.getConfigId() != null) {
                            hrApply.setTemplateConfigId(templateConfig.getConfigId());
                        }
                    }
                }
                
                if (templateConfig != null) {
                    processDefinitionId = templateConfig.getProcessDefinitionId();
                    hrApply.setProcessDefinitionId(processDefinitionId);
                }
            } catch (Exception e) {
                System.err.println("获取模板配置失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        hrApply.setStatus("PENDING");
        hrApply.setUpdateTime(LocalDateTime.now());
        boolean updateSuccess = hrApplyMapper.updateById(hrApply) > 0;
        
        if (!updateSuccess) {
            return false;
        }
        
        if (processDefinitionId == null || hrApply.getApplyNo() == null || hrApply.getApplyNo().isEmpty() || authServiceClient == null) {
            if (processDefinitionId == null) {
                System.err.println("警告: processDefinitionId为空，无法启动流程");
            }
            return updateSuccess;
        }
        
        // 生成流程任务记录
        try {
            // 检查是否有退回的任务
            String startFromNodeName = null;
            boolean hasReturnToCurrent = false;
            
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> existingTasksResult = authServiceClient.getTasksByTaskKey(hrApply.getApplyNo());
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
            
            // 处理旧的流程任务记录
            try {
                if (hasReturnToCurrent) {
                    authServiceClient.restoreReturnedTasks(hrApply.getApplyNo());
                } else {
                    authServiceClient.deleteTasksByTaskKey(hrApply.getApplyNo());
                }
            } catch (Exception e) {
                // 处理旧任务记录时出现异常，记录错误但不影响提交
                System.err.println("处理旧任务记录时出现异常: " + e.getMessage());
            }
            
            // 生成流程任务记录
            Result<java.util.List<com.hrp.common.entity.ProcessTask>> result = authServiceClient.generateTasks(
                    processDefinitionId, hrApply.getApplyNo(), startFromNodeName);
            if (result == null || result.getCode() != 200) {
                System.err.println("流程任务记录生成失败: " + (result != null ? result.getMessage() : "未知错误"));
            }
        } catch (Exception e) {
            System.err.println("生成流程任务记录异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return updateSuccess;
    }

    @Override
    @Transactional
    public boolean withdraw(Long applyId) {
        HrApply hrApply = hrApplyMapper.selectById(applyId);
        if (hrApply == null) {
            return false;
        }
        if (!"DRAFT".equals(hrApply.getStatus()) && !"PENDING".equals(hrApply.getStatus())) {
            return false; // 只有草稿或待审批状态才能撤回
        }
        
        // 删除流程任务记录
        if (hrApply.getApplyNo() != null && !hrApply.getApplyNo().isEmpty() && authServiceClient != null) {
            try {
                authServiceClient.deleteTasksByTaskKey(hrApply.getApplyNo());
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        hrApply.setStatus("WITHDRAWN");
        hrApply.setUpdateTime(LocalDateTime.now());
        return hrApplyMapper.updateById(hrApply) > 0;
    }

    @Override
    @Transactional
    public boolean approve(Long applyId, String userId, String opinion, String approverSignature) {
        HrApply hrApply = hrApplyMapper.selectById(applyId);
        if (hrApply == null) {
            return false;
        }
        
        // 完成任务（更新流程任务状态）
        if (authServiceClient != null && hrApply.getApplyNo() != null) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("taskKey", hrApply.getApplyNo());
                request.put("taskId", null); // 自动查找当前待处理任务
                request.put("comment", opinion);
                if (approverSignature != null && !approverSignature.trim().isEmpty()) {
                    request.put("approverSignature", approverSignature);
                }
                
                Result<Boolean> completeResult = authServiceClient.completeTask(request);
                if (completeResult == null || completeResult.getCode() != 200 || !Boolean.TRUE.equals(completeResult.getData())) {
                    System.err.println("任务完成失败: " + (completeResult != null ? completeResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("完成任务异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 检查是否所有任务都已完成
        boolean allTasksCompleted = true;
        if (authServiceClient != null && hrApply.getApplyNo() != null) {
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> tasksResult = authServiceClient.getTasksByTaskKey(hrApply.getApplyNo());
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
        
        // 只有所有任务都完成时，才将状态改为APPROVED
        if (allTasksCompleted) {
            hrApply.setStatus("APPROVED");
            hrApply.setUpdateTime(LocalDateTime.now());
            boolean updateSuccess = hrApplyMapper.updateById(hrApply) > 0;
            
            // 审批通过后，处理考勤记录（插入或更新）
            if (updateSuccess) {
                try {
                    // 如果是补卡申请（SUPPLY），且 supplementId 不为空，则更新考勤记录；否则插入新记录
                    if ("SUPPLY".equals(hrApply.getHrApplyType()) && hrApply.getSupplementId() != null) {
                        // 更新已有的考勤记录
                        HrAttRecord existingRecord = hrAttRecordMapper.selectById(hrApply.getSupplementId());
                        if (existingRecord != null) {
                            // 补卡申请可能只填写了开始时间或结束时间，需要从考勤记录中获取另一个时间点
                            LocalDateTime startTime = hrApply.getStartTime() != null ? hrApply.getStartTime() : existingRecord.getAttStartTime();
                            LocalDateTime endTime = hrApply.getEndTime() != null ? hrApply.getEndTime() : existingRecord.getAttEndTime();
                            
                            // 更新开始时间和结束时间（如果申请中有提供）
                            if (hrApply.getStartTime() != null) {
                                existingRecord.setAttStartTime(hrApply.getStartTime());
                            }
                            if (hrApply.getEndTime() != null) {
                                existingRecord.setAttEndTime(hrApply.getEndTime());
                            }
                            
                            // 计算天数：使用实际的时间点（从申请或记录中获取）
                            String dayValue = calculateDay(startTime, endTime);
                            existingRecord.setDay(dayValue);
                            existingRecord.setAttStatus("NORMAL");
                            existingRecord.setAttType(hrApply.getHrApplyType());
                            existingRecord.setIsSupplement(1);
                            hrAttRecordMapper.updateById(existingRecord);
                        } else {
                            throw new BusinessException("补卡记录不存在，recordId: " + hrApply.getSupplementId());
                        }
                    } else {
                        // 插入新的考勤记录
                        HrAttRecord attRecord = new HrAttRecord();
                        attRecord.setEmpId(hrApply.getEmpId());
                        attRecord.setAttStartTime(hrApply.getStartTime());
                        attRecord.setAttEndTime(hrApply.getEndTime());
                        // att_date 取 att_start_time 的年月日
                        if (hrApply.getStartTime() != null) {
                            attRecord.setAttDate(hrApply.getStartTime().toLocalDate());
                        }
                        attRecord.setAttType(hrApply.getHrApplyType());
                        attRecord.setAttSubType(hrApply.getHrApplySubType());
                        attRecord.setApplyId(hrApply.getApplyId());
                        // 计算天数
                        if(hrApply.getApplyDay()==null) {
                            attRecord.setDay(calculateDay(hrApply.getStartTime(), hrApply.getEndTime()));
                        }
                        else {
                            attRecord.setDay(hrApply.getApplyDay());
                        }
                        // 如果 hr_apply_type 是 'SUPPLY'（补卡），则 is_supplement 为 1，否则为 0
                        if ("SUPPLY".equals(hrApply.getHrApplyType())) {
                            attRecord.setIsSupplement(1);
                        } else {
                            attRecord.setIsSupplement(0);
                        }
                        attRecord.setAttStatus("NORMAL"); // 默认状态
                        hrAttRecordMapper.insert(attRecord);
                    }
                } catch (Exception e) {
                    System.err.println("处理考勤记录失败: " + e.getMessage());
                    e.printStackTrace();
                    // 不抛出异常，避免影响审批流程
                }
            }
            
            return updateSuccess;
        } else {
            hrApply.setStatus("PENDING");
            hrApply.setUpdateTime(LocalDateTime.now());
            return hrApplyMapper.updateById(hrApply) > 0;
        }
    }

    @Override
    @Transactional
    public boolean reject(Long applyId, String userId, String opinion) {
        HrApply hrApply = hrApplyMapper.selectById(applyId);
        if (hrApply == null) {
            return false;
        }
        
        // 获取申请人的用户信息
        String applicantUserId = null;
        String applicantUserName = hrApply.getEmpName();
        String applicantEmpCode = hrApply.getEmpCode();
        
        if (authServiceClient != null) {
            try {
                if (applicantEmpCode != null && !applicantEmpCode.isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(applicantEmpCode);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
                if (applicantUserId == null && hrApply.getEmpId() != null) {
                    Result<User> userResult = authServiceClient.getUserById(String.valueOf(hrApply.getEmpId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取申请人用户信息失败: " + e.getMessage());
                if (applicantUserId == null) {
                    return false;
                }
            }
        }
        
        // 退回任务（拒绝时使用RETURN_TO_START）
        if (authServiceClient != null && hrApply.getApplyNo() != null && applicantUserId != null) {
            try {
                Map<String, Object> returnRequest = new HashMap<>();
                returnRequest.put("taskKey", hrApply.getApplyNo());
                returnRequest.put("taskId", null);
                returnRequest.put("returnType", "RETURN_TO_START");
                returnRequest.put("applicantUserId", applicantUserId);
                returnRequest.put("applicantUserName", applicantUserName);
                returnRequest.put("applicantEmpCode", applicantEmpCode);
                returnRequest.put("comment", opinion != null ? opinion : "驳回");
                
                Result<Boolean> returnResult = authServiceClient.returnTask(returnRequest);
                if (returnResult == null || returnResult.getCode() != 200 || !Boolean.TRUE.equals(returnResult.getData())) {
                    System.err.println("退回任务失败: " + (returnResult != null ? returnResult.getMessage() : "未知错误"));
                    return false;
                }
            } catch (Exception e) {
                System.err.println("退回处理异常: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        hrApply.setStatus("REJECTED");
        hrApply.setUpdateTime(LocalDateTime.now());
        return hrApplyMapper.updateById(hrApply) > 0;
    }

    @Override
    @Transactional
    public boolean returnApply(Long applyId, String returnType, String opinion) {
        HrApply hrApply = hrApplyMapper.selectById(applyId);
        if (hrApply == null) {
            return false;
        }
        
        // 获取申请人的用户信息
        String applicantUserId = null;
        String applicantUserName = hrApply.getEmpName();
        String applicantEmpCode = hrApply.getEmpCode();
        
        if (authServiceClient != null) {
            try {
                if (applicantEmpCode != null && !applicantEmpCode.isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(applicantEmpCode);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
                if (applicantUserId == null && hrApply.getEmpId() != null) {
                    Result<User> userResult = authServiceClient.getUserById(String.valueOf(hrApply.getEmpId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取申请人用户信息失败: " + e.getMessage());
                if (applicantUserId == null) {
                    return false;
                }
            }
        }
        
        // 退回任务
        if (authServiceClient != null && hrApply.getApplyNo() != null && applicantUserId != null) {
            try {
                Map<String, Object> returnRequest = new HashMap<>();
                returnRequest.put("taskKey", hrApply.getApplyNo());
                returnRequest.put("taskId", null);
                returnRequest.put("returnType", returnType != null ? returnType : "RETURN_TO_START");
                returnRequest.put("applicantUserId", applicantUserId);
                returnRequest.put("applicantUserName", applicantUserName);
                returnRequest.put("applicantEmpCode", applicantEmpCode);
                returnRequest.put("comment", opinion != null ? opinion : "退回");
                
                Result<Boolean> returnResult = authServiceClient.returnTask(returnRequest);
                if (returnResult == null || returnResult.getCode() != 200 || !Boolean.TRUE.equals(returnResult.getData())) {
                    System.err.println("退回任务失败: " + (returnResult != null ? returnResult.getMessage() : "未知错误"));
                    return false;
                }
            } catch (Exception e) {
                System.err.println("退回处理异常: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        hrApply.setStatus("REJECTED");
        hrApply.setUpdateTime(LocalDateTime.now());
        return hrApplyMapper.updateById(hrApply) > 0;
    }
}

