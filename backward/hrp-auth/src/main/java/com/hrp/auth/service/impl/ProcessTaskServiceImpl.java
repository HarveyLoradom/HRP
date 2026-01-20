package com.hrp.auth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.auth.feign.AssetServiceClient;
import com.hrp.auth.feign.BudgServiceClient;
import com.hrp.auth.feign.ContractServiceClient;
import com.hrp.auth.feign.HrServiceClient;
import com.hrp.auth.mapper.ProcessTaskMapper;
import com.hrp.auth.service.*;
import com.hrp.common.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流程任务服务实现类
 */
@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

    @Autowired
    private ProcessTaskMapper processTaskMapper;


    @Autowired(required = false)
    private PositionService positionService;

    @Autowired(required = false)
    private DeptService deptService;

    @Autowired(required = false)
    private UserEmployeeService userEmployeeService;


    @Autowired(required = false)
    private ProcessDefinitionService processDefinitionService;

    @Autowired(required = false)
    private BudgServiceClient budgServiceClient;
    
    @Autowired(required = false)
    private com.hrp.auth.feign.ReimbServiceClient reimbServiceClient;
    
    @Autowired(required = false)
    private ContractServiceClient contractServiceClient;
    
    @Autowired(required = false)
    private AssetServiceClient assetServiceClient;
    
    @Autowired(required = false)
    private HrServiceClient hrServiceClient;
    
    @Autowired(required = false)
    private TemplateConfigService templateConfigService;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProcessTask getById(String taskId) {
        return processTaskMapper.selectById(taskId);
    }

    @Override
    public List<ProcessTask> getByInstanceId(Long processInstanceId) {
        return processTaskMapper.selectByInstanceId(processInstanceId);
    }

    @Override
    public List<ProcessTask> getByAssignee(String assigneeUserId) {
        return processTaskMapper.selectByAssignee(assigneeUserId);
    }

    @Override
    public PageResult<ProcessTask> getByAssigneePage(String assigneeUserId, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<ProcessTask> list = processTaskMapper.selectByAssignee(assigneeUserId);
        PageInfo<ProcessTask> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<ProcessTask> getByAssigneeAndStatus(String assigneeUserId, String taskStatus) {
        return processTaskMapper.selectByAssigneeAndStatus(assigneeUserId, taskStatus);
    }
    
    @Override
    public PageResult<ProcessTask> getByAssigneeAndStatusPage(String assigneeUserId, String taskStatus, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<ProcessTask> list = processTaskMapper.selectByAssigneeAndStatus(assigneeUserId, taskStatus);
        PageInfo<ProcessTask> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }
    
    @Override
    public List<ProcessTask> getAllCurrentTasks() {
        List<ProcessTask> tasks = processTaskMapper.selectAllCurrentTasks();
        // 填充业务类型和流程定义名称（从模板设置表获取business_type）
        for (ProcessTask task : tasks) {
            if (task.getBusinessType() == null || task.getBusinessType().isEmpty() ||
                task.getProcessDefinitionName() == null || task.getProcessDefinitionName().isEmpty()) {
                // 从业务表获取templateConfigId，然后从模板设置表获取business_type
                BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(task.getTaskKey());
                if (businessTypeInfo != null) {
                    // business_type 从模板设置表获取
                    if (task.getBusinessType() == null || task.getBusinessType().isEmpty()) {
                        task.setBusinessType(businessTypeInfo.getBusinessType());
                    }
                    // 设置流程定义名称
                    if (task.getProcessDefinitionName() == null || task.getProcessDefinitionName().isEmpty()) {
                        task.setProcessDefinitionName(businessTypeInfo.getProcessDefinitionName());
                    }
                }
            }
        }
        return tasks;
    }
    
    @Override
    public PageResult<ProcessTask> getAllCurrentTasksPage(Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<ProcessTask> tasks = processTaskMapper.selectAllCurrentTasks();
        // 填充业务类型和流程定义名称（从模板设置表获取business_type）
        for (ProcessTask task : tasks) {
            if (task.getBusinessType() == null || task.getBusinessType().isEmpty() ||
                task.getProcessDefinitionName() == null || task.getProcessDefinitionName().isEmpty()) {
                // 从业务表获取templateConfigId，然后从模板设置表获取business_type
                BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(task.getTaskKey());
                if (businessTypeInfo != null) {
                    // business_type 从模板设置表获取
                    if (task.getBusinessType() == null || task.getBusinessType().isEmpty()) {
                        task.setBusinessType(businessTypeInfo.getBusinessType());
                    }
                    // 设置流程定义名称
                    if (task.getProcessDefinitionName() == null || task.getProcessDefinitionName().isEmpty()) {
                        task.setProcessDefinitionName(businessTypeInfo.getProcessDefinitionName());
                    }
                }
            }
        }
        PageInfo<ProcessTask> pageInfo = new PageInfo<>(tasks);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }
    
    /**
     * 业务类型信息内部类
     */
    private static class BusinessTypeInfo {
        private String businessType; // 从template_config表的business_type字段获取
        private String processDefinitionName;
        
        public BusinessTypeInfo(String businessType, String processDefinitionName) {
            this.businessType = businessType;
            this.processDefinitionName = processDefinitionName;
        }
        
        public String getBusinessType() {
            return businessType;
        }
        
        public String getProcessDefinitionName() {
            return processDefinitionName;
        }
    }
    
    /**
     * 从业务表获取templateConfigId，然后从模板设置表获取business_type
     * @param businessKey 业务主键（如：BUDG202512310001、SQD202601010001）
     * @return 业务类型信息，如果找不到返回null
     */
    private BusinessTypeInfo getBusinessTypeByBusinessKey(String businessKey) {
        try {
            Long templateConfigId = null;
            ProcessDefinition definition = null;
            
            if (budgServiceClient != null && businessKey != null) {
                Result<BudgetApply> result = budgServiceClient.getBudgetApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    BudgetApply apply = result.getData();
                    templateConfigId = apply.getTemplateConfigId();
                }
            }

            if (templateConfigId == null && definition == null && reimbServiceClient != null && businessKey != null) {
                Result<CtrlPayout> result = reimbServiceClient.getCtrlPayoutByBillcode(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    CtrlPayout payout = result.getData();
                    templateConfigId = payout.getTemplateConfigId();
                }
            }

            if (templateConfigId == null && definition == null && hrServiceClient != null && businessKey != null) {
                Result<com.hrp.common.entity.HrApply> result = hrServiceClient.getHrApplyByApplyNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    com.hrp.common.entity.HrApply hrApply = result.getData();
                    templateConfigId = hrApply.getTemplateConfigId();
                }
            }

            if (templateConfigId == null && definition == null && contractServiceClient != null && businessKey != null) {
                Result<PactMain> result = contractServiceClient.getContractByContractNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    PactMain contract = result.getData();
                    templateConfigId = contract.getTemplateConfigId();
                }
            }

            if (templateConfigId == null && definition == null && assetServiceClient != null && businessKey != null) {
                Result<AssetPurchaseApplyMain> result = assetServiceClient.getAssetPurchaseApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    AssetPurchaseApplyMain assetPurchaseApplyMain = result.getData();
                    templateConfigId = assetPurchaseApplyMain.getTemplateConfigId();
                }
            }

            // 优先通过templateConfigId查询模板设置表获取business_type
            if (templateConfigId != null && templateConfigService != null) {
                TemplateConfig templateConfig = templateConfigService.getById(templateConfigId);
                if (templateConfig != null) {
                    String businessType = templateConfig.getBusinessType(); // 从template_config表的business_type字段获取
                    String processDefinitionName = templateConfig.getProcessDefinitionName();
                    if (businessType != null && !businessType.isEmpty()) {
                        return new BusinessTypeInfo(businessType, processDefinitionName);
                    }
                }
            }
            
            // 如果没有templateConfigId，使用流程定义表的definition_key作为业务类型（兼容旧数据）
            if (definition != null) {
                String businessType = definition.getDefinitionKey();
                String processDefinitionName = definition.getDefinitionName();
                if (businessType != null && !businessType.isEmpty()) {
                    return new BusinessTypeInfo(businessType, processDefinitionName);
                }
            }
        } catch (Exception e) {
            System.err.println("从模板设置表获取业务类型失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 从业务表获取templateConfigId，然后通过模板设置表获取processDefinitionId，最后查询流程定义表
     * @param businessKey 业务主键（如：BUDG202512310001、SQD202601010001）
     * @return 流程定义对象，如果找不到返回null
     */
    private ProcessDefinition getProcessDefinitionByBusinessKey(String businessKey) {
        try {
            Long templateConfigId = null;
            
            // 尝试从预算申请表获取templateConfigId
            if (budgServiceClient != null && businessKey != null) {
                Result<BudgetApply> result = budgServiceClient.getBudgetApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    BudgetApply apply = result.getData();
                    // 优先使用templateConfigId，如果没有则使用processDefinitionId（兼容旧数据）
                    templateConfigId = apply.getTemplateConfigId();
                    if (templateConfigId == null) {
                        // 如果没有templateConfigId，直接使用processDefinitionId（兼容旧数据）
                        Long processDefinitionId = apply.getProcessDefinitionId();
                        if (processDefinitionId != null && processDefinitionService != null) {
                            return processDefinitionService.getById(processDefinitionId);
                        }
                    }
                }
            }
            
            // 如果预算申请表没有找到，尝试从报账申请表获取templateConfigId
            if (templateConfigId == null && reimbServiceClient != null && businessKey != null) {
                Result<CtrlPayout> result = reimbServiceClient.getCtrlPayoutByBillcode(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    CtrlPayout payout = result.getData();
                    // 优先使用templateConfigId，如果没有则使用processDefinitionId（兼容旧数据）
                    templateConfigId = payout.getTemplateConfigId();
                    if (templateConfigId == null) {
                        // 如果没有templateConfigId，直接使用processDefinitionId（兼容旧数据）
                        Long processDefinitionId = payout.getProcessDefinitionId();
                        if (processDefinitionId != null && processDefinitionService != null) {
                            return processDefinitionService.getById(processDefinitionId);
                        }
                    }
                }
            }
            
            // 如果报账申请表没有找到，尝试从采购申请表获取templateConfigId
            if (templateConfigId == null && assetServiceClient != null && businessKey != null) {
                Result<AssetPurchaseApplyMain> result = assetServiceClient.getAssetPurchaseApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    AssetPurchaseApplyMain apply = result.getData();
                    templateConfigId = apply.getTemplateConfigId();
                }
            }
            
            // 如果采购申请表没有找到，尝试从HR业务申请表获取templateConfigId
            if (templateConfigId == null && hrServiceClient != null && businessKey != null) {
                Result<com.hrp.common.entity.HrApply> result = hrServiceClient.getHrApplyByApplyNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    com.hrp.common.entity.HrApply hrApply = result.getData();
                    templateConfigId = hrApply.getTemplateConfigId();
                    if (templateConfigId == null) {
                        // 如果没有templateConfigId，直接使用processDefinitionId（兼容旧数据）
                        Long processDefinitionId = hrApply.getProcessDefinitionId();
                        if (processDefinitionId != null && processDefinitionService != null) {
                            return processDefinitionService.getById(processDefinitionId);
                        }
                    }
                }
            }
            
            // 如果HR业务申请表没有找到，尝试从合同表获取templateConfigId
            if (templateConfigId == null && contractServiceClient != null && businessKey != null) {
                Result<PactMain> result = contractServiceClient.getContractByContractNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    PactMain contract = result.getData();
                    templateConfigId = contract.getTemplateConfigId();
                    // 合同表没有processDefinitionId字段，只能通过templateConfigId获取
                }
            }
            
            // 通过templateConfigId查询模板设置表，获取processDefinitionId
            if (templateConfigId != null && templateConfigService != null) {
                TemplateConfig templateConfig = templateConfigService.getById(templateConfigId);
                if (templateConfig != null && templateConfig.getProcessDefinitionId() != null) {
                    // 通过processDefinitionId查询流程定义表
                    if (processDefinitionService != null) {
                        ProcessDefinition definition = processDefinitionService.getById(templateConfig.getProcessDefinitionId());
                        return definition;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("从流程定义表获取业务类型失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 通用的业务数据获取方法：根据businessType动态获取业务数据
     * @param businessKey 业务主键
     * @param businessType 业务类型（从template_config表的business_type字段获取，如：BUDGET_TYPE, APPLY_TYPE, HR_APPLY_TYPE, CONTRACT等）
     * @return 业务数据对象（BudgetApply、CtrlPayout、HrApply或PactMain），如果找不到返回null
     */
    private Object getBusinessDataByType(String businessKey, String businessType) {
        if (businessKey == null || businessType == null) {
            return null;
        }
        
        try {
            // 根据businessType动态判断使用哪个服务客户端
            // businessType可能是：BUDGET_TYPE, APPLY_TYPE, PAYOUT_TYPE, HR_APPLY_TYPE, CONTRACT等
            if (businessType.contains("BUDGET") && budgServiceClient != null) {
                Result<BudgetApply> result = budgServiceClient.getBudgetApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return result.getData();
                }
            } else if ((businessType.contains("APPLY") || businessType.contains("PAYOUT")) && !businessType.contains("HR") && reimbServiceClient != null) {
                // 报账申请：APPLY_TYPE或PAYOUT_TYPE，但不包含HR
                Result<CtrlPayout> result = reimbServiceClient.getCtrlPayoutByBillcode(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return result.getData();
                }
            } else if ((businessType.contains("HR") || businessType.contains("HR_APPLY")) && hrServiceClient != null) {
                // HR业务申请：HR_APPLY_TYPE或HR_APPLY
                Result<com.hrp.common.entity.HrApply> result = hrServiceClient.getHrApplyByApplyNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return result.getData();
                }
            } else if ((businessType.contains("ASSET") || businessType.contains("PURCHASE")) && assetServiceClient != null) {
                Result<AssetPurchaseApplyMain> result = assetServiceClient.getAssetPurchaseApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return result.getData();
                }
            } else if (businessType.contains("CONTRACT") && contractServiceClient != null) {
                Result<PactMain> result = contractServiceClient.getContractByContractNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return result.getData();
                }
            }
        } catch (Exception e) {
            System.err.println("获取业务数据失败: businessKey=" + businessKey + ", businessType=" + businessType + ", error=" + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ProcessTask> getByBusinessKey(String businessKey) {
        return processTaskMapper.selectByBusinessKey(businessKey);
    }

    @Override
    public List<ProcessTask> getByStatus(String taskStatus) {
        return processTaskMapper.selectByStatus(taskStatus);
    }

    @Override
    public PageResult<ProcessTask> getByStatusPage(String taskStatus, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<ProcessTask> list = processTaskMapper.selectByStatus(taskStatus);
        PageInfo<ProcessTask> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<ProcessTask> getByBusinessKeyPage(String businessKey, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<ProcessTask> list = processTaskMapper.selectByBusinessKey(businessKey);
        PageInfo<ProcessTask> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional
    public boolean transferTask(String taskId, String newAssigneeUserId, String newAssigneeUserName, String newAssigneeEmpCode) {
        ProcessTask task = processTaskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        // 检查是否有转签权限
        if (task.getAllowTransfer() == null || task.getAllowTransfer() != 1) {
            System.err.println("当前任务不允许转签: taskId=" + taskId);
            return false;
        }
        
        // 检查任务状态（只有待处理的任务才能转签）
        if (!"PENDING".equals(task.getTaskStatus())) {
            System.err.println("只有待处理的任务才能转签: taskId=" + taskId + ", status=" + task.getTaskStatus());
            return false;
        }
        
        // 保存原审批人信息和任务信息
        String originalAssigneeUserId = task.getAssigneeUserId();
        String originalAssigneeUserName = task.getAssigneeUserName();
        String originalAssigneeEmpCode = task.getAssigneeEmpCode();
        String taskKey = task.getTaskKey();
        String taskName = task.getTaskName();
        
        // 查找所有相同的 taskKey、taskName 且 assigneeUserId 等于原审批人的待处理任务记录
        // 这样可以确保同一节点的所有原审批人的任务记录都被更新（包括会签场景）
        // 注意：只更新相同的 taskName，因为不同节点的任务不应该一起转签
        List<ProcessTask> allTasks = processTaskMapper.selectByTaskKey(taskKey);
        if (allTasks == null || allTasks.isEmpty()) {
            return false;
        }
        
        // 筛选出需要转签的任务：相同的 taskName 且 assigneeUserId 等于原审批人且状态为 PENDING
        List<ProcessTask> tasksToUpdate = new java.util.ArrayList<>();
        for (ProcessTask t : allTasks) {
            if (taskName.equals(t.getTaskName()) && 
                originalAssigneeUserId.equals(t.getAssigneeUserId()) && 
                "PENDING".equals(t.getTaskStatus())) {
                tasksToUpdate.add(t);
            }
        }
        
        if (tasksToUpdate.isEmpty()) {
            return false;
        }
        
        // 转签备注信息
        String transferComment = String.format("由 %s（%s）转签给：%s（%s）", 
            originalAssigneeUserName, originalAssigneeEmpCode, newAssigneeUserName, newAssigneeEmpCode);
        
        // 同一节点（相同taskName）的所有任务是一个整体，需要共享同一个approver_list
        // 需要更新同一节点所有任务的approver_list，而不仅仅是当前转签的任务
        // 查找同一节点的所有任务（相同taskName和taskKey）
        List<ProcessTask> sameNodeTasks = new java.util.ArrayList<>();
        for (ProcessTask t : allTasks) {
            if (taskName.equals(t.getTaskName()) && taskKey.equals(t.getTaskKey())) {
                sameNodeTasks.add(t);
            }
        }
        
        // 计算更新后的approver_list（将原审批人替换为转签人）
        String updatedApproverList = null;
        if (!sameNodeTasks.isEmpty()) {
            // 使用第一个任务的approver_list作为基准
            String baseApproverList = sameNodeTasks.get(0).getApproverList();
            if (baseApproverList != null && !baseApproverList.trim().isEmpty()) {
                // 如果包含原审批人，替换为新审批人
                if (baseApproverList.contains(originalAssigneeUserName)) {
                    updatedApproverList = baseApproverList.replace(originalAssigneeUserName, newAssigneeUserName);
                } else {
                    // 如果不包含，保持原样（可能approver_list已经更新过）
                    updatedApproverList = baseApproverList;
                }
            } else {
                // 如果为空，设置为新审批人
                updatedApproverList = newAssigneeUserName;
            }
        }
        
        // 批量更新所有需要转签的任务记录（同一节点同一审批人的所有任务记录）
        boolean allSuccess = true;
        for (ProcessTask taskToUpdate : tasksToUpdate) {
            // 更新审批人相关信息为转签人的信息
            taskToUpdate.setAssigneeUserId(newAssigneeUserId);
            taskToUpdate.setAssigneeUserName(newAssigneeUserName);
            taskToUpdate.setAssigneeEmpCode(newAssigneeEmpCode);
            
            // 更新 approver_list
            taskToUpdate.setApproverList(updatedApproverList);
            
            // 在comment中记录转签信息
            if (taskToUpdate.getComment() != null && !taskToUpdate.getComment().trim().isEmpty()) {
                taskToUpdate.setComment(taskToUpdate.getComment() + "\n" + transferComment);
            } else {
                taskToUpdate.setComment(transferComment);
            }
            
            taskToUpdate.setUpdateTime(LocalDateTime.now());
            
            // 更新任务记录
            if (processTaskMapper.updateById(taskToUpdate) <= 0) {
                allSuccess = false;
            }
        }
        
        // 更新同一节点其他任务的approver_list（同一节点的所有任务共享approver_list）
        if (updatedApproverList != null && !sameNodeTasks.isEmpty()) {
            for (ProcessTask sameNodeTask : sameNodeTasks) {
                // 跳过已经更新的任务
                boolean alreadyUpdated = false;
                for (ProcessTask updatedTask : tasksToUpdate) {
                    if (sameNodeTask.getTaskId().equals(updatedTask.getTaskId())) {
                        alreadyUpdated = true;
                        break;
                    }
                }
                if (!alreadyUpdated) {
                    // 更新同一节点其他任务的approver_list
                    sameNodeTask.setApproverList(updatedApproverList);
                    sameNodeTask.setUpdateTime(LocalDateTime.now());
                    processTaskMapper.updateById(sameNodeTask);
                    System.out.println("更新同一节点其他任务的approver_list: taskId=" + sameNodeTask.getTaskId() + 
                        ", taskName=" + sameNodeTask.getTaskName() + ", approverList=" + updatedApproverList);
                }
            }
        }
        
        return allSuccess;
    }

    @Override
    public NextNodeInfo getNextNodeInfo(Long processInstanceId) {
        if (processInstanceId == null) {
            return null;
        }

        // 查找下一个待处理的任务
        ProcessTask nextTask = processTaskMapper.selectNextPendingTask(processInstanceId);
        if (nextTask == null) {
            return null;
        }

        NextNodeInfo nextNodeInfo = new NextNodeInfo();
        nextNodeInfo.setTaskName(nextTask.getTaskName());

        // 判断审批人类型
        String assigneeType = nextTask.getAssigneeType();
        if (assigneeType == null || assigneeType.isEmpty()) {
            // 如果没有设置assigneeType，通过任务名称判断
            String taskName = nextTask.getTaskName() != null ? nextTask.getTaskName() : "";
            if (taskName.contains("归口") || taskName.contains("归口审批")) {
                assigneeType = "manage_dept";
            } else if (taskName.contains("部门") || taskName.contains("部门负责人")) {
                assigneeType = "dept";
            } else {
                assigneeType = "user"; // 默认用户
            }
        }
        nextNodeInfo.setAssigneeType(assigneeType);

        // 根据审批人类型获取审批人信息
        String businessKey = nextTask.getTaskKey();
        String businessType = nextTask.getBusinessType();
        if (businessType == null || businessType.isEmpty()) {
            // 从模板设置表获取business_type
            BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(businessKey);
            if (businessTypeInfo != null && businessTypeInfo.getBusinessType() != null) {
                businessType = businessTypeInfo.getBusinessType();
            } else {
                businessType = inferBusinessType(businessKey);
            }
        }
        
        if ("manage_dept".equals(assigneeType)) {
            // 归口审批人：从业务表获取归口负责人信息
            nextNodeInfo.setApprovalPositionName("归口审批人");
            fillManageDeptApprover(nextNodeInfo, businessKey, businessType);
        } else if ("dept".equals(assigneeType)) {
            // 部门负责人：从业务表获取部门信息，然后查询部门负责人
            nextNodeInfo.setApprovalPositionName("部门负责人");
            fillDeptApprover(nextNodeInfo, nextTask, businessKey, businessType);
        } else if ("responsible".equals(assigneeType)) {
            // 请选择负责人：根据发起人是否护士，自动分配给发起人部门负责人或部门护士长
            fillResponsibleApprover(nextNodeInfo, nextTask, businessKey, businessType);
        } else if ("position".equals(assigneeType)) {
            // 岗位：从任务配置中获取岗位代码，然后查询该岗位下的所有员工
            nextNodeInfo.setApprovalPositionName(nextTask.getTaskName());
            fillPositionApprover(nextNodeInfo, nextTask);
        } else {
            // 指定用户：直接使用任务中的审批人信息
            if (nextTask.getAssigneeUserName() != null) {
                nextNodeInfo.setApproverName(nextTask.getAssigneeUserName());
                nextNodeInfo.setApproverCode(nextTask.getAssigneeEmpCode());
                nextNodeInfo.setApprovalPositionName(nextTask.getTaskName());
            }
        }

        return nextNodeInfo;
    }

    /**
     * 从业务表中获取预算主体ID（不再从流程变量获取）
     */
    private Long getSubjectIdFromBusinessTable(String businessKey, String businessType) {
        try {
            // 使用通用方法获取业务数据
            Object businessData = getBusinessDataByType(businessKey, businessType);
            if (businessData instanceof BudgetApply) {
                BudgetApply apply = (BudgetApply) businessData;
                return apply.getSubjectId();
            }
        } catch (Exception e) {
            System.err.println("从业务表获取subjectId失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取归口审批人信息（从业务表获取）
     */
    private void fillManageDeptApprover(NextNodeInfo nextNodeInfo, String businessKey, String businessType) {
        try {
            // 使用通用方法获取业务数据
            Object businessData = getBusinessDataByType(businessKey, businessType);
            if (businessData instanceof BudgetApply && budgServiceClient != null) {
                BudgetApply apply = (BudgetApply) businessData;
                if (apply.getSubjectId() != null) {
                    try {
                        Result<BudgetSubject> result = budgServiceClient.getBudgetSubjectById(apply.getSubjectId());
                        if (result != null && result.getCode() == 200 && result.getData() != null) {
                            BudgetSubject subject = result.getData();
                            nextNodeInfo.setApproverName(subject.getManageEmpName());
                            nextNodeInfo.setApproverCode(subject.getManageEmpCode());
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("通过Feign调用获取归口审批人信息失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // 如果所有方式都失败，设置占位信息
            if (nextNodeInfo.getApproverName() == null || nextNodeInfo.getApproverName().isEmpty()) {
                nextNodeInfo.setApproverName("待分配");
                nextNodeInfo.setApproverCode("");
            }
            
        } catch (Exception e) {
            System.err.println("获取归口审批人信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取负责人信息（根据流程配置的responsibleType，选择部门负责人或护士长）
     * responsibleType从task.getDeptCode()中获取（前端在配置时将responsibleType存储到deptCode字段中）
     */
    private void fillResponsibleApprover(NextNodeInfo nextNodeInfo, ProcessTask task, String businessKey, String businessType) {
        try {
            // 从任务中获取responsibleType配置（用户在流程配置时选择的：DEPT_MANAGER或NURSE_MANAGER）
            // 前端将responsibleType存储在deptCode字段中
            String responsibleType = task.getDeptCode();
            
            // 如果deptCode为空或不是responsibleType值，尝试从流程定义JSON中解析
            if (responsibleType == null || responsibleType.isEmpty() || 
                (!"DEPT_MANAGER".equals(responsibleType) && !"NURSE_MANAGER".equals(responsibleType) && !"VICE_PRESIDENT".equals(responsibleType))) {
                // 从流程定义JSON中解析responsibleType（从业务表获取processDefinitionId）
                // 由于ProcessTask没有processDefinitionId字段，需要从业务表获取
                Long processDefinitionId = null;
                if (businessKey != null && businessType != null) {
                    // 使用通用方法获取业务数据
                    Object businessData = getBusinessDataByType(businessKey, businessType);
                    if (businessData instanceof BudgetApply) {
                        processDefinitionId = ((BudgetApply) businessData).getProcessDefinitionId();
                    } else if (businessData instanceof CtrlPayout) {
                        processDefinitionId = ((CtrlPayout) businessData).getProcessDefinitionId();
                    } else if (businessData instanceof com.hrp.common.entity.HrApply) {
                        com.hrp.common.entity.HrApply hrApply = (com.hrp.common.entity.HrApply) businessData;
                        processDefinitionId = hrApply.getProcessDefinitionId();
                        if (processDefinitionId == null && hrApply.getTemplateConfigId() != null && templateConfigService != null) {
                            TemplateConfig config = templateConfigService.getById(hrApply.getTemplateConfigId());
                            if (config != null) {
                                processDefinitionId = config.getProcessDefinitionId();
                            }
                        }
                    } else if (businessData instanceof AssetPurchaseApplyMain) {
                        // 采购申请没有processDefinitionId字段，需要通过templateConfigId获取
                        AssetPurchaseApplyMain apply = (AssetPurchaseApplyMain) businessData;
                        if (apply.getTemplateConfigId() != null && templateConfigService != null) {
                            TemplateConfig config = templateConfigService.getById(apply.getTemplateConfigId());
                            if (config != null) {
                                processDefinitionId = config.getProcessDefinitionId();
                            }
                        }
                    }
                }
                
                if (processDefinitionService != null && processDefinitionId != null) {
                    try {
                        ProcessDefinition definition = processDefinitionService.getById(processDefinitionId);
                        if (definition != null && definition.getProcessJson() != null && !definition.getProcessJson().isEmpty()) {
                            // 解析processJson，查找对应节点的responsibleType
                            JsonNode jsonNode = objectMapper.readTree(definition.getProcessJson());
                            if (jsonNode.has("nodes") && jsonNode.get("nodes").isArray()) {
                                for (JsonNode node : jsonNode.get("nodes")) {
                                    // 根据taskKey或taskName匹配节点
                                    if ((task.getTaskKey() != null && node.has("id") && task.getTaskKey().equals(node.get("id").asText())) ||
                                        (task.getTaskName() != null && node.has("name") && task.getTaskName().equals(node.get("name").asText()))) {
                                        // 找到对应的节点，获取responsibleType或deptCode
                                        if (node.has("responsibleType")) {
                                            responsibleType = node.get("responsibleType").asText("DEPT_MANAGER");
                                        } else if (node.has("deptCode") && 
                                                  ("DEPT_MANAGER".equals(node.get("deptCode").asText()) || 
                                                   "NURSE_MANAGER".equals(node.get("deptCode").asText()) ||
                                                   "VICE_PRESIDENT".equals(node.get("deptCode").asText()))) {
                                            responsibleType = node.get("deptCode").asText();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("从流程定义JSON解析responsibleType失败: " + e.getMessage());
                    }
                }
                // 如果仍然无法获取，使用默认值
                if (responsibleType == null || responsibleType.isEmpty() || 
                    (!"DEPT_MANAGER".equals(responsibleType) && !"NURSE_MANAGER".equals(responsibleType) && !"VICE_PRESIDENT".equals(responsibleType))) {
                    responsibleType = "DEPT_MANAGER"; // 默认使用部门负责人
                }
            }
            
            // 从业务表获取发起人部门信息（支持预算申请和报账申请）
            if (businessKey != null && businessType != null) {
                Long deptId = null;
                String deptCode = null;
                
                // 使用通用方法获取业务数据
                Object businessData = getBusinessDataByType(businessKey, businessType);
                if (businessData instanceof BudgetApply) {
                    BudgetApply apply = (BudgetApply) businessData;
                    deptId = apply.getDeptId();
                    deptCode = apply.getDeptCode();
                } else if (businessData instanceof CtrlPayout) {
                    CtrlPayout payout = (CtrlPayout) businessData;
                    deptId = payout.getDeptId();
                    // CtrlPayout没有deptCode字段，需要通过deptId查询
                } else if (businessData instanceof com.hrp.common.entity.HrApply) {
                    com.hrp.common.entity.HrApply hrApply = (com.hrp.common.entity.HrApply) businessData;
                    deptId = hrApply.getDeptId();
                    // HrApply没有deptCode字段，需要通过deptId查询
                }
                
                if (deptId != null || (deptCode != null && !deptCode.isEmpty())) {
                    // 根据部门ID或部门代码查询部门信息
                    if (deptService != null) {
                        Dept dept = null;
                        if (deptId != null) {
                            dept = deptService.getById(deptId);
                        } else if (deptCode != null && !deptCode.isEmpty()) {
                            dept = deptService.getByCode(deptCode);
                        }
                        
                        if (dept != null) {
                            // 根据用户选择的responsibleType选择负责人、护士长或分管院长
                            if ("NURSE_MANAGER".equals(responsibleType)) {
                                // 选择护士长
                                if (dept.getNurseManagerName() != null && !dept.getNurseManagerName().isEmpty()) {
                                    nextNodeInfo.setApproverName(dept.getNurseManagerName());
                                    nextNodeInfo.setApproverCode(dept.getNurseManagerCode());
                                    nextNodeInfo.setApprovalPositionName("部门护士长");
                                    return;
                                }
                            } else if ("VICE_PRESIDENT".equals(responsibleType)) {
                                // 选择分管院长
                                if (dept.getVicePresidentName() != null && !dept.getVicePresidentName().isEmpty()) {
                                    nextNodeInfo.setApproverName(dept.getVicePresidentName());
                                    nextNodeInfo.setApproverCode(dept.getVicePresidentCode());
                                    nextNodeInfo.setApprovalPositionName("部门分管院长");
                                    return;
                                }
                            } else {
                                // 默认选择部门负责人
                                if (dept.getDeptManagerName() != null && !dept.getDeptManagerName().isEmpty()) {
                                    nextNodeInfo.setApproverName(dept.getDeptManagerName());
                                    nextNodeInfo.setApproverCode(dept.getDeptManagerCode());
                                    nextNodeInfo.setApprovalPositionName("部门负责人");
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            
            // 如果无法获取，设置占位信息
            if (nextNodeInfo.getApproverName() == null || nextNodeInfo.getApproverName().isEmpty()) {
                nextNodeInfo.setApproverName("待分配");
                nextNodeInfo.setApproverCode("");
                nextNodeInfo.setApprovalPositionName("负责人");
            }
            
        } catch (Exception e) {
            System.err.println("获取负责人信息失败: " + e.getMessage());
            nextNodeInfo.setApproverName("待分配");
            nextNodeInfo.setApproverCode("");
            nextNodeInfo.setApprovalPositionName("负责人");
        }
    }

    /**
     * 获取部门负责人信息
     * 优先使用任务中存储的deptCode（流程配置时选择的部门），如果没有或是指定占位符，则使用发起人部门
     */
    private void fillDeptApprover(NextNodeInfo nextNodeInfo, ProcessTask task, String businessKey, String businessType) {
        try {
            String selectedDeptCode = null;
            Long selectedDeptId = null;
            
            // 优先检查任务中是否存储了部门编码（流程配置时选择的部门）
            String taskDeptCode = task.getDeptCode();
            if (taskDeptCode != null && !taskDeptCode.isEmpty() && 
                !"INITIATOR_DEPT".equals(taskDeptCode) && 
                !"DEPT_MANAGER".equals(taskDeptCode) && 
                !"NURSE_MANAGER".equals(taskDeptCode) && 
                !"VICE_PRESIDENT".equals(taskDeptCode)) {
                // 任务中存储的是具体部门的编码，使用该部门
                selectedDeptCode = taskDeptCode;
                System.out.println("=== 从任务中获取部门编码 ===");
                System.out.println("任务名称: " + task.getTaskName());
                System.out.println("部门编码: " + selectedDeptCode);
            }
            
            // 如果没有从任务中获取到部门编码，或者任务中存储的是占位符，则从业务表获取发起人部门
            if (selectedDeptCode == null && businessKey != null && businessType != null) {
                System.out.println("=== 从业务表获取发起人部门信息 ===");
                // 使用通用方法获取业务数据
                Object businessData = getBusinessDataByType(businessKey, businessType);
                if (businessData instanceof BudgetApply) {
                    BudgetApply apply = (BudgetApply) businessData;
                    selectedDeptId = apply.getDeptId();
                    selectedDeptCode = apply.getDeptCode();
                } else if (businessData instanceof CtrlPayout) {
                    CtrlPayout payout = (CtrlPayout) businessData;
                    selectedDeptId = payout.getDeptId();
                    // CtrlPayout没有deptCode字段，需要通过deptId查询
                } else if (businessData instanceof com.hrp.common.entity.HrApply) {
                    com.hrp.common.entity.HrApply hrApply = (com.hrp.common.entity.HrApply) businessData;
                    selectedDeptId = hrApply.getDeptId();
                    // HrApply没有deptCode字段，需要通过deptId查询
                } else if (businessData instanceof AssetPurchaseApplyMain) {
                    AssetPurchaseApplyMain apply = (AssetPurchaseApplyMain) businessData;
                    // AssetPurchaseApplyMain的applyDeptId是String类型
                    if (apply.getApplyDeptId() != null && !apply.getApplyDeptId().isEmpty()) {
                        try {
                            selectedDeptId = Long.parseLong(apply.getApplyDeptId());
                        } catch (NumberFormatException e) {
                            // 如果无法解析为Long，尝试作为deptCode使用
                            selectedDeptCode = apply.getApplyDeptId();
                        }
                    }
                    // 如果采购申请有部门编码，优先使用
                    if (apply.getApplyDeptCode() != null && !apply.getApplyDeptCode().isEmpty()) {
                        selectedDeptCode = apply.getApplyDeptCode();
                    }
                }
                System.out.println("发起人部门ID: " + selectedDeptId);
                System.out.println("发起人部门编码: " + selectedDeptCode);
            }
            
            // 根据部门ID或部门代码查询部门信息
            if (selectedDeptId != null || (selectedDeptCode != null && !selectedDeptCode.isEmpty())) {
                if (deptService != null) {
                    Dept dept = null;
                    if (selectedDeptId != null) {
                        dept = deptService.getById(selectedDeptId);
                    } else if (selectedDeptCode != null && !selectedDeptCode.isEmpty()) {
                        dept = deptService.getByCode(selectedDeptCode);
                    }
                    
                    if (dept != null) {
                        // 直接使用部门负责人信息
                        if (dept.getDeptManagerName() != null && !dept.getDeptManagerName().isEmpty()) {
                            nextNodeInfo.setApproverName(dept.getDeptManagerName());
                            nextNodeInfo.setApproverCode(dept.getDeptManagerCode());
                            System.out.println("部门负责人: 部门=" + dept.getDeptName() + 
                                ", 负责人姓名=" + dept.getDeptManagerName() + ", 负责人工号=" + dept.getDeptManagerCode());
                            return;
                        } else {
                            System.err.println("部门 " + dept.getDeptName() + " 没有设置负责人");
                        }
                    } else {
                        if (selectedDeptId != null) {
                            System.err.println("未找到部门ID为 " + selectedDeptId + " 的部门");
                        } else {
                            System.err.println("未找到部门编码为 " + selectedDeptCode + " 的部门");
                        }
                    }
                }
            }
            
            // 如果无法获取，设置占位信息
            if (nextNodeInfo.getApproverName() == null || nextNodeInfo.getApproverName().isEmpty()) {
                nextNodeInfo.setApproverName("待分配");
                nextNodeInfo.setApproverCode("");
            }
            
        } catch (Exception e) {
            System.err.println("获取部门负责人信息失败: " + e.getMessage());
            e.printStackTrace();
            nextNodeInfo.setApproverName("待分配");
            nextNodeInfo.setApproverCode("");
        }
    }

    /**
     * 获取岗位审批人信息
     * 从任务表中直接读取岗位代码（positionCode字段）
     * 然后根据岗位代码查询该岗位下的所有员工
     * 当一个岗位有多个人时，显示第一个人的信息，并在审批岗位名称中提示"（多人）"
     */
    private void fillPositionApprover(NextNodeInfo nextNodeInfo, ProcessTask task) {
        try {
            // 直接从任务表中获取岗位代码
            String positionCode = task.getPositionCode();
            
            // 如果找到了岗位代码，查询该岗位下的所有员工
            if (positionCode != null && !positionCode.isEmpty() && positionService != null) {
                List<com.hrp.common.entity.User> users = positionService.getUsersByPositionCode(positionCode);
                
                if (users != null && !users.isEmpty()) {
                    // 如果岗位有多个人，显示第一个人的信息，并在岗位名称中提示"（多人）"
                    com.hrp.common.entity.User firstUser = users.get(0);
                    nextNodeInfo.setApproverName(firstUser.getName());
                    nextNodeInfo.setApproverCode(firstUser.getAccount());
                    
                    // 如果有多个人，在审批岗位名称中提示
                    if (users.size() > 1) {
                        String positionName = nextNodeInfo.getApprovalPositionName();
                        if (positionName != null && !positionName.contains("（") && !positionName.contains("（")) {
                            nextNodeInfo.setApprovalPositionName(positionName + "（" + users.size() + "人）");
                        }
                    }
                    
                    return;
                }
            }
            
            // 如果无法获取，设置占位信息
            if (nextNodeInfo.getApproverName() == null || nextNodeInfo.getApproverName().isEmpty()) {
                nextNodeInfo.setApproverName("待分配");
                nextNodeInfo.setApproverCode("");
            }
            
        } catch (Exception e) {
            System.err.println("获取岗位审批人信息失败: " + e.getMessage());
            nextNodeInfo.setApproverName("待分配");
            nextNodeInfo.setApproverCode("");
        }
    }

    @Override
    public NextNodeInfo getNextNodeInfoByBusinessKey(String businessKey) {
        if (businessKey == null || businessKey.isEmpty()) {
            return null;
        }

        // 直接从任务表中查找
        List<ProcessTask> tasks = processTaskMapper.selectByBusinessKey(businessKey);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        
        // 找到当前待处理的任务（print_order最小的PENDING任务，排除加签任务）
        // 因为当前正在审批的任务应该是流程中最早的节点
        ProcessTask currentTask = tasks.stream()
                .filter(task -> "PENDING".equals(task.getTaskStatus()))
                .filter(task -> {
                    // 排除加签任务（isAddsignTask = 1），加签任务应该和原节点一起处理
                    // 但如果原节点已完成，加签任务可以视为当前节点
                    if (task.getIsAddsignTask() != null && task.getIsAddsignTask() == 1) {
                        // 检查是否有相同print_order的非加签任务也是PENDING
                        boolean hasNonAddsignPendingAtSameOrder = tasks.stream()
                                .anyMatch(t -> "PENDING".equals(t.getTaskStatus()) 
                                        && (t.getIsAddsignTask() == null || t.getIsAddsignTask() == 0)
                                        && t.getPrintOrder() != null 
                                        && t.getPrintOrder().equals(task.getPrintOrder())
                                        && !t.getTaskId().equals(task.getTaskId()));
                        // 如果同一print_order有非加签的PENDING任务，排除加签任务
                        return !hasNonAddsignPendingAtSameOrder;
                    }
                    return true;
                })
                .sorted((t1, t2) -> {
                    // 按print_order排序，print_order小的在前
                    Integer order1 = t1.getPrintOrder();
                    Integer order2 = t2.getPrintOrder();
                    
                    if (order1 != null && order2 != null) {
                        int orderCompare = order1.compareTo(order2);
                        if (orderCompare != 0) {
                            return orderCompare;
                        }
                        // 如果print_order相同，加签任务优先（在非加签任务之后）
                        Integer isAddsign1 = t1.getIsAddsignTask() != null && t1.getIsAddsignTask() == 1 ? 1 : 0;
                        Integer isAddsign2 = t2.getIsAddsignTask() != null && t2.getIsAddsignTask() == 1 ? 1 : 0;
                        return isAddsign1.compareTo(isAddsign2);
                    } else if (order1 != null) {
                        return -1;
                    } else if (order2 != null) {
                        return 1;
                    }
                    
                    // 如果print_order相同或为null，按createTime排序
                    if (t1.getCreateTime() != null && t2.getCreateTime() != null) {
                        return t1.getCreateTime().compareTo(t2.getCreateTime());
                    }
                    return 0;
                })
                .findFirst()
                .orElse(null);
        
        if (currentTask == null) {
            // 没有待处理的任务
            return null;
        }
        
        // 找到下一个节点：按照next_task_id查找，而不是按照print_order
        // 对于同一节点的多个任务（如财务岗位的3个审批人），它们的next_task_id应该一致
        // 所以只要找到当前任务的next_task_id，就能找到下一个节点
        ProcessTask nextTask = null;
        
        String nextTaskId = currentTask.getNextTaskId();
        if (nextTaskId != null && !nextTaskId.trim().isEmpty()) {
            // 通过next_task_id查找下一个任务
            // 注意：对于会签/或签场景，下一个节点也可能有多个任务，取第一个即可
            nextTask = tasks.stream()
                    .filter(task -> nextTaskId.equals(task.getTaskId()))
                    .filter(task -> "PENDING".equals(task.getTaskStatus()))
                    .findFirst()
                    .orElse(null);
            
            // 如果没有找到PENDING状态的任务，可能下一个节点已经完成或还未创建
            // 这种情况下，尝试查找任意状态的任务，以获取节点信息
            if (nextTask == null) {
                nextTask = tasks.stream()
                        .filter(task -> nextTaskId.equals(task.getTaskId()))
                        .findFirst()
                        .orElse(null);
                
                if (nextTask != null && !"PENDING".equals(nextTask.getTaskStatus())) {
                    // 如果下一个任务不是PENDING状态，说明已经完成，返回null表示没有下一个待处理节点
                    System.out.println("下一节点任务已完成或状态异常: nextTaskId=" + nextTaskId + ", status=" + nextTask.getTaskStatus());
                    return null;
                }
            }
        }
        
        // 如果next_task_id为null，说明当前节点是最后一个节点
        if (nextTaskId == null || nextTaskId.trim().isEmpty()) {
            System.out.println("当前节点是最后一个节点，没有下一节点: taskId=" + currentTask.getTaskId());
            return null;
        }
        
        // 如果通过next_task_id没有找到任务，记录警告
        if (nextTask == null) {
            System.out.println("警告: 通过next_task_id未找到下一个任务: nextTaskId=" + nextTaskId);
        }
        
        if (nextTask != null) {
            NextNodeInfo nextNodeInfo = new NextNodeInfo();
            nextNodeInfo.setTaskName(nextTask.getTaskName());
            
            // 根据审批人类型填充审批人信息
            String assigneeType = nextTask.getAssigneeType();
            if (assigneeType == null || assigneeType.isEmpty()) {
                // 如果没有设置assigneeType，通过任务名称判断
                String taskName = nextTask.getTaskName() != null ? nextTask.getTaskName() : "";
                if (taskName.contains("归口") || taskName.contains("归口审批")) {
                    assigneeType = "manage_dept";
                } else if (taskName.contains("部门") || taskName.contains("部门负责人")) {
                    assigneeType = "dept";
                } else {
                    assigneeType = "user"; // 默认用户
                }
            }
            nextNodeInfo.setAssigneeType(assigneeType);
            
            // 根据审批人类型获取审批人信息
            String businessType = inferBusinessType(businessKey);
            
            if ("manage_dept".equals(assigneeType)) {
                // 归口审批人：从业务表获取归口负责人信息
                nextNodeInfo.setApprovalPositionName("归口审批人");
                fillManageDeptApprover(nextNodeInfo, businessKey, businessType);
            } else if ("dept".equals(assigneeType)) {
                // 部门负责人：从业务表获取部门信息，然后查询部门负责人
                nextNodeInfo.setApprovalPositionName("部门负责人");
                fillDeptApprover(nextNodeInfo, nextTask, businessKey, businessType);
            } else if ("responsible".equals(assigneeType)) {
                // 请选择负责人：根据发起人是否护士，自动分配给发起人部门负责人或部门护士长
                fillResponsibleApprover(nextNodeInfo, nextTask, businessKey, businessType);
            } else if ("position".equals(assigneeType)) {
                // 岗位：从任务配置中获取岗位代码，然后查询该岗位下的所有员工
                nextNodeInfo.setApprovalPositionName(nextTask.getTaskName());
                fillPositionApprover(nextNodeInfo, nextTask);
            } else {
                // 指定用户：直接使用任务中的审批人信息
                if (nextTask.getAssigneeUserName() != null) {
                    nextNodeInfo.setApproverName(nextTask.getAssigneeUserName());
                    nextNodeInfo.setApproverCode(nextTask.getAssigneeEmpCode());
                    nextNodeInfo.setApprovalPositionName(nextTask.getTaskName());
                }
            }
            
            // 优先使用approverList（如果存在），否则使用approverName
            if (nextTask.getApproverList() != null && !nextTask.getApproverList().trim().isEmpty()) {
                nextNodeInfo.setApproverList(nextTask.getApproverList());
                // 如果approverList存在，也更新approverName为第一个审批人（用于兼容）
                String[] approvers = nextTask.getApproverList().split(",");
                if (approvers.length > 0 && (nextNodeInfo.getApproverName() == null || nextNodeInfo.getApproverName().isEmpty())) {
                    nextNodeInfo.setApproverName(approvers[0].trim());
                }
            }
            
            return nextNodeInfo;
        }

        return null;
    }
    
    @Override
    @Transactional
    public List<ProcessTask> generateTasksFromDefinition(Long processDefinitionId, String taskKey, String startFromNodeName) {
        System.out.println("=== 开始生成流程任务记录 ===");
        System.out.println("processDefinitionId: " + processDefinitionId + ", taskKey: " + taskKey + ", startFromNodeName: " + startFromNodeName);
        
        List<ProcessTask> tasks = new java.util.ArrayList<>();
        
        if (processDefinitionId == null || taskKey == null || taskKey.trim().isEmpty()) {
            System.err.println("流程定义ID或任务KEY为空，无法生成任务记录");
            return tasks;
        }
        
        if (processDefinitionService == null) {
            System.err.println("ProcessDefinitionService未注入，无法生成任务记录");
            return tasks;
        }
        
        try {
            // 获取流程节点信息（包含审批人等业务数据）
            List<com.hrp.common.entity.ProcessNodeInfo> nodes = processDefinitionService.getProcessNodesWithBusiness(processDefinitionId, taskKey);
            
            if (nodes == null || nodes.isEmpty()) {
                System.out.println("未找到流程节点，无法生成任务记录");
                return tasks;
            }
            
            System.out.println("获取到 " + nodes.size() + " 个流程节点");
            
            // 检查是否有退回后重新提交到本节点的情况（RETURN_TO_CURRENT）
            // 如果任务已经恢复为PENDING状态（之前是退回的），则不需要重新生成，直接返回
            // 如果有退回状态的任务，且returnType是RETURN_TO_CURRENT，从提交节点的next_task_id指向的节点开始生成
            List<ProcessTask> existingTasks = processTaskMapper.selectByTaskKey(taskKey);
            String returnToCurrentNodeName = null;
            boolean hasRestoredTask = false;
            
            if (existingTasks != null && !existingTasks.isEmpty()) {
                // 检查是否已经有恢复的退回任务（PENDING状态但returnType是RETURN_TO_CURRENT）
                for (ProcessTask task : existingTasks) {
                    if ("PENDING".equals(task.getTaskStatus()) && "RETURN_TO_CURRENT".equals(task.getReturnType())) {
                        hasRestoredTask = true;
                        System.out.println("检测到已恢复的退回任务，任务已恢复为待审批状态，不需要重新生成: taskId=" + task.getTaskId() + ", taskName=" + task.getTaskName());
                        break;
                    }
                }
                
                // 如果任务已经恢复，只需要生成后续任务，不需要重新生成当前任务
                if (!hasRestoredTask) {
                    // 查找是否有退回状态的任务（还未恢复的）
                    for (ProcessTask task : existingTasks) {
                        if ("RETURNED".equals(task.getTaskStatus()) && "RETURN_TO_CURRENT".equals(task.getReturnType())) {
                            // 找到提交节点
                            ProcessTask submitTask = null;
                            for (ProcessTask t : existingTasks) {
                                if ("提交".equals(t.getTaskName()) || "submit".equalsIgnoreCase(t.getTaskName())) {
                                    submitTask = t;
                                    break;
                                }
                            }
                            if (submitTask != null && submitTask.getNextTaskId() != null) {
                                // 提交节点的next_task_id指向退回到的节点，找到该节点
                                for (ProcessTask t : existingTasks) {
                                    if (submitTask.getNextTaskId().equals(t.getTaskId())) {
                                        returnToCurrentNodeName = t.getTaskName();
                                        System.out.println("检测到退回后重新提交到本节点，将从节点 \"" + returnToCurrentNodeName + "\" 开始生成任务");
                                        break;
                                    }
                                }
                            }
                            break; // 只处理第一个退回任务
                        }
                    }
                } else {
                    // 如果任务已经恢复，需要找到恢复的任务，然后生成后续任务
                    for (ProcessTask task : existingTasks) {
                        if ("PENDING".equals(task.getTaskStatus()) && "RETURN_TO_CURRENT".equals(task.getReturnType())) {
                            returnToCurrentNodeName = task.getTaskName();
                            System.out.println("检测到已恢复的退回任务，将从该节点开始生成后续任务: " + returnToCurrentNodeName);
                            break;
                        }
                    }
                }
            }
            
            // 如果指定了从某个节点开始生成（退回后重新提交到本节点，或手动指定），需要过滤掉该节点之前的节点
            String actualStartNodeName = returnToCurrentNodeName != null ? returnToCurrentNodeName : startFromNodeName;
            boolean skipFirstNode = false; // 是否跳过第一个节点（因为已经恢复为PENDING）
            if (actualStartNodeName != null && !actualStartNodeName.trim().isEmpty()) {
                int startIndex = -1;
                for (int i = 0; i < nodes.size(); i++) {
                    com.hrp.common.entity.ProcessNodeInfo node = nodes.get(i);
                    if (actualStartNodeName.equals(node.getName()) && "userTask".equals(node.getType())) {
                        startIndex = i;
                        break;
                    }
                }
                if (startIndex >= 0) {
                    // 如果任务已经恢复，跳过第一个节点（已恢复的节点），从第二个节点开始生成
                    if (hasRestoredTask) {
                        startIndex = startIndex + 1; // 跳过已恢复的节点
                        skipFirstNode = true;
                        System.out.println("任务已恢复，跳过已恢复的节点，从后续节点开始生成");
                    }
                    // 从指定节点开始截取
                    if (startIndex < nodes.size()) {
                        nodes = new java.util.ArrayList<>(nodes.subList(startIndex, nodes.size()));
                        System.out.println("从节点 \"" + actualStartNodeName + "\" 开始生成任务，剩余 " + nodes.size() + " 个节点");
                    } else {
                        // 如果已经恢复的任务是最后一个节点，不需要生成新任务
                        System.out.println("已恢复的任务是最后一个节点，不需要生成新任务");
                        return tasks;
                    }
                } else {
                    System.err.println("警告: 未找到指定节点 \"" + actualStartNodeName + "\"，将从流程开始生成任务");
                }
            }
            
            // 创建任务列表（用于设置nextTaskId）
            java.util.List<ProcessTask> taskList = new java.util.ArrayList<>();
            
            // 遍历节点，创建任务记录
            for (int i = 0; i < nodes.size(); i++) {
                com.hrp.common.entity.ProcessNodeInfo node = nodes.get(i);
                
                // 跳过非userTask类型的节点
                if (!"userTask".equals(node.getType())) {
                    continue;
                }
                
                // 检查该节点是否已经存在任务（避免重复生成）
                // 如果已存在PENDING或COMPLETED状态的任务，跳过生成
                boolean nodeTaskExists = false;
                if (existingTasks != null && !existingTasks.isEmpty()) {
                    for (ProcessTask existingTask : existingTasks) {
                        if (node.getName().equals(existingTask.getTaskName()) && 
                            taskKey.equals(existingTask.getTaskKey())) {
                            // 如果已恢复的任务是当前节点，跳过生成（因为已经恢复了）
                            if (hasRestoredTask && "PENDING".equals(existingTask.getTaskStatus()) && 
                                "RETURN_TO_CURRENT".equals(existingTask.getReturnType()) &&
                                returnToCurrentNodeName != null && returnToCurrentNodeName.equals(node.getName())) {
                                // 这是已恢复的节点，跳过
                                nodeTaskExists = true;
                                System.out.println("节点 \"" + node.getName() + "\" 已恢复为待审批状态，跳过生成");
                                break;
                            }
                            // 如果后续节点已经存在且是PENDING或COMPLETED状态，也跳过生成（避免重复）
                            if (("PENDING".equals(existingTask.getTaskStatus()) || 
                                 "COMPLETED".equals(existingTask.getTaskStatus())) &&
                                (!hasRestoredTask || returnToCurrentNodeName == null || !returnToCurrentNodeName.equals(node.getName()))) {
                                nodeTaskExists = true;
                                System.out.println("节点 \"" + node.getName() + "\" 已存在任务（状态: " + existingTask.getTaskStatus() + "），跳过生成");
                                break;
                            }
                        }
                    }
                }
                
                if (nodeTaskExists) {
                    continue; // 跳过已存在的节点
                }
                
                // 如果是岗位类型（position），需要为每个用户创建一条记录（或签场景）
                if ("position".equals(node.getAssigneeType()) && node.getPositionCode() != null && 
                    !node.getPositionCode().isEmpty() && positionService != null) {
                    
                    try {
                        // 查询该岗位下的所有用户
                        List<com.hrp.common.entity.User> users = positionService.getUsersByPositionCode(node.getPositionCode());
                        if (users != null && !users.isEmpty()) {
                            System.out.println("节点 " + node.getName() + " 是岗位类型，找到 " + users.size() + " 个用户，将为每个用户创建任务记录");
                            
                            // 确定审批类型：如果是岗位类型，默认为或签（OR_SIGN），除非明确指定为会签（COUNTERSIGN）
                            String approvalType = "OR_SIGN"; // 岗位类型默认或签
                            if (node.getApprovalType() != null && !node.getApprovalType().isEmpty()) {
                                // 如果节点中有明确的审批类型，使用节点的审批类型
                                if ("MULTI".equals(node.getApprovalType()) || "COUNTERSIGN".equals(node.getApprovalType())) {
                                    approvalType = "COUNTERSIGN"; // 会签（全部通过）
                                } else if ("OR".equals(node.getApprovalType()) || "OR_SIGN".equals(node.getApprovalType())) {
                                    approvalType = "OR_SIGN"; // 或签（任一通过）
                                } else {
                                    approvalType = node.getApprovalType(); // 使用节点中的审批类型
                                }
                            }
                            
                            // 为每个用户创建一条任务记录
                            for (com.hrp.common.entity.User user : users) {
                                String taskId = java.util.UUID.randomUUID().toString();
                                ProcessTask task = createTaskFromNode(node, taskId, taskKey);
                                
                                // 设置审批类型
                                task.setApprovalType(approvalType);
                                
                                // 设置具体用户信息
                                task.setAssigneeUserId(user.getId());
                                task.setAssigneeUserName(user.getName());
                                task.setAssigneeEmpCode(user.getAccount()); // 使用account作为工号
                                
                                taskList.add(task);
                            }
                            continue; // 已经处理了，跳过下面的单个任务创建逻辑
                        } else {
                            System.err.println("节点 " + node.getName() + " 是岗位类型，但未找到任何用户，positionCode=" + node.getPositionCode());
                        }
                    } catch (Exception e) {
                        System.err.println("查询岗位用户失败: positionCode=" + node.getPositionCode() + ", 错误=" + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                // 单个审批人或非岗位类型，创建一条任务记录
                String taskId = java.util.UUID.randomUUID().toString();
                ProcessTask task = createTaskFromNode(node, taskId, taskKey);
                
                // 设置审批人信息
                if (node.getAssigneeId() != null && !node.getAssigneeId().isEmpty()) {
                    task.setAssigneeUserId(node.getAssigneeId());
                }
                if (node.getAssigneeName() != null && !node.getAssigneeName().isEmpty()) {
                    // 检查是否是描述性文字，如果是，说明没有获取到真实的审批人姓名，不设置
                    String assigneeName = node.getAssigneeName();
                    if (assigneeName.contains("发起人") || assigneeName.contains("部门") || 
                        assigneeName.contains("审批人") || assigneeName.contains("护士长") || 
                        assigneeName.contains("分管") || assigneeName.contains("岗位:") ||
                        assigneeName.contains("归口")) {
                        // 描述性文字，不设置到任务记录中，保持为空
                        // 这样前端显示时会显示"-"或"待分配"
                    } else {
                        // 真实的审批人姓名，设置到任务记录
                        task.setAssigneeUserName(assigneeName);
                    }
                }
                if (node.getAssigneeCode() != null && !node.getAssigneeCode().isEmpty()) {
                    task.setAssigneeEmpCode(node.getAssigneeCode());
                }
                
                taskList.add(task);
            }
            
            // 设置nextTaskId
            // 按节点名称分组，同一节点的所有任务记录的nextTaskId都指向下一个节点的第一个任务记录
            Map<String, List<ProcessTask>> tasksByNodeName = new java.util.LinkedHashMap<>();
            for (ProcessTask task : taskList) {
                String nodeName = task.getTaskName();
                tasksByNodeName.computeIfAbsent(nodeName, k -> new java.util.ArrayList<>()).add(task);
            }
            
            // 构建节点名称到printOrder的映射，并按printOrder排序
            Map<String, Integer> nodePrintOrderMap = new java.util.HashMap<>();
            for (String nodeName : tasksByNodeName.keySet()) {
                Integer printOrder = null;
                if (nodes != null) {
                    for (com.hrp.common.entity.ProcessNodeInfo node : nodes) {
                        if (node.getName().equals(nodeName) && "userTask".equals(node.getType())) {
                            printOrder = node.getPrintOrder();
                            break;
                        }
                    }
                }
                // 如果节点没有printOrder，尝试从已创建的任务中获取
                if (printOrder == null && !tasksByNodeName.get(nodeName).isEmpty()) {
                    printOrder = tasksByNodeName.get(nodeName).get(0).getPrintOrder();
                }
                // 如果还是没有，使用一个很大的数字作为默认值（排在最后）
                if (printOrder == null) {
                    printOrder = 9999;
                }
                nodePrintOrderMap.put(nodeName, printOrder);
            }
            
            // 按printOrder排序节点名称列表
            List<String> nodeNames = new java.util.ArrayList<>(tasksByNodeName.keySet());
            nodeNames.sort((name1, name2) -> {
                Integer order1 = nodePrintOrderMap.get(name1);
                Integer order2 = nodePrintOrderMap.get(name2);
                if (order1 == null && order2 == null) {
                    return 0;
                }
                if (order1 == null) {
                    return 1; // null排在后面
                }
                if (order2 == null) {
                    return -1; // null排在前面
                }
                return order1.compareTo(order2);
            });
            
            System.out.println("=== 节点顺序（按printOrder排序） ===");
            for (int idx = 0; idx < nodeNames.size(); idx++) {
                String nodeName = nodeNames.get(idx);
                Integer printOrder = nodePrintOrderMap.get(nodeName);
                System.out.println("节点 " + (idx + 1) + ": " + nodeName + " (printOrder: " + printOrder + ")");
            }
            
            // 为每个节点设置nextTaskId、审批人列表（同一节点所有审批人用逗号拼接）和打印顺序
            int printOrderCounter = 1;
            for (int i = 0; i < nodeNames.size(); i++) {
                String currentNodeName = nodeNames.get(i);
                List<ProcessTask> currentTasks = tasksByNodeName.get(currentNodeName);
                
                if (currentTasks != null && !currentTasks.isEmpty()) {
                    // 收集当前节点的所有审批人姓名（去重），过滤掉描述性文字
                    java.util.Set<String> approverNames = new java.util.LinkedHashSet<>();
                    for (ProcessTask task : currentTasks) {
                        if (task.getAssigneeUserName() != null && !task.getAssigneeUserName().isEmpty()) {
                            String name = task.getAssigneeUserName();
                            // 过滤掉描述性文字
                            if (!name.contains("发起人") && !name.contains("部门") && 
                                !name.contains("审批人") && !name.contains("护士长") && 
                                !name.contains("分管") && !name.contains("岗位:") &&
                                !name.contains("归口")) {
                                approverNames.add(name);
                            }
                        }
                    }
                    
                    // 拼接审批人姓名列表（用逗号分隔）
                    String approverListStr = approverNames.isEmpty() ? null : String.join(",", approverNames);
                    
                    // 获取当前节点的 printOrder（从映射中获取）
                    Integer nodePrintOrder = nodePrintOrderMap.get(currentNodeName);
                    if (nodePrintOrder == null || nodePrintOrder == 9999) {
                        // 如果还是没有有效的printOrder，使用计数器
                        nodePrintOrder = printOrderCounter++;
                    }
                    
                    // 为当前节点的所有任务记录设置审批人列表和打印顺序
                    for (ProcessTask task : currentTasks) {
                        task.setApproverList(approverListStr); // 设置审批人列表（同一节点的所有审批人）
                        task.setPrintOrder(nodePrintOrder); // 设置打印顺序
                        
                        // 设置nextTaskId（如果是最后一个节点，nextTaskId为null）
                        if (i < nodeNames.size() - 1) {
                            String nextNodeName = nodeNames.get(i + 1);
                            List<ProcessTask> nextTasks = tasksByNodeName.get(nextNodeName);
                            if (nextTasks != null && !nextTasks.isEmpty()) {
                                task.setNextTaskId(nextTasks.get(0).getTaskId());
                                System.out.println("设置nextTaskId: " + currentNodeName + " -> " + nextNodeName + " (taskId: " + nextTasks.get(0).getTaskId() + ")");
                            }
                        } else {
                            System.out.println("最后一个节点，nextTaskId为null: " + currentNodeName);
                        }
                    }
                }
            }
            
            // 批量插入任务记录
            for (ProcessTask task : taskList) {
                // 如果是"提交"节点，自动完成
                if ("提交".equals(task.getTaskName()) || "submit".equalsIgnoreCase(task.getTaskName())) {
                    task.setTaskStatus("COMPLETED");
                    task.setCompleteTime(LocalDateTime.now());
                    task.setComment("同意");
                }
                
                processTaskMapper.insert(task);
                tasks.add(task);
                System.out.println("生成任务记录: taskId=" + task.getTaskId() + ", taskName=" + task.getTaskName() + 
                    ", nextTaskId=" + task.getNextTaskId() + ", assigneeName=" + task.getAssigneeUserName());
            }
            
            // 如果任务已经恢复，需要将已恢复的任务的nextTaskId指向新生成的第一个任务
            if (hasRestoredTask && !tasks.isEmpty() && returnToCurrentNodeName != null) {
                // 找到已恢复的任务
                for (ProcessTask existingTask : existingTasks) {
                    if ("PENDING".equals(existingTask.getTaskStatus()) && 
                        "RETURN_TO_CURRENT".equals(existingTask.getReturnType()) &&
                        returnToCurrentNodeName.equals(existingTask.getTaskName())) {
                        // 将已恢复任务的nextTaskId指向新生成的第一个任务
                        existingTask.setNextTaskId(tasks.get(0).getTaskId());
                        existingTask.setUpdateTime(LocalDateTime.now());
                        processTaskMapper.updateById(existingTask);
                        System.out.println("已更新恢复任务的nextTaskId指向新生成的第一个任务: taskId=" + existingTask.getTaskId() + ", nextTaskId=" + tasks.get(0).getTaskId());
                        break;
                    }
                }
            }
            
            System.out.println("=== 流程任务记录生成完成，共生成 " + tasks.size() + " 条记录 ===");
            
        } catch (Exception e) {
            System.err.println("生成流程任务记录失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成流程任务记录失败: " + e.getMessage(), e);
        }
        
        return tasks;
    }
    
    /**
     * 从节点信息创建任务对象的辅助方法
     */
    private ProcessTask createTaskFromNode(com.hrp.common.entity.ProcessNodeInfo node, String taskId, String taskKey) {
        ProcessTask task = new ProcessTask();
        task.setTaskId(taskId);
        task.setTaskKey(taskKey); // taskKey就是apply_no
        task.setTaskName(node.getName());
        task.setProcessInstanceId(0L); // 暂时使用0，如果数据库字段允许NULL可以改为null
        task.setTaskStatus("PENDING"); // 初始状态为待处理
        
        // 设置审批人类型
        task.setAssigneeType(node.getAssigneeType());
        
        // 设置权限信息
        task.setAllowAddsign(node.getAllowAddsign() != null ? node.getAllowAddsign() : 0);
        task.setAllowTransfer(node.getAllowTransfer() != null ? node.getAllowTransfer() : 0);
        task.setAllowReject(node.getAllowReject() != null ? node.getAllowReject() : 0);
        
        // 设置其他属性
        task.setNeedPrint(node.getNeedPrint() != null ? node.getNeedPrint() : 0);
        task.setPositionCode(node.getPositionCode());
        task.setDeptCode(node.getDeptCode());
        task.setIsAddsignTask(0); // 初始不是加签任务
        task.setTaskType("APPROVE"); // 任务类型默认为审批（APPROVE），节点类型使用type字段
        
        // 设置审批类型，转换可能的缩写形式
        String approvalType = node.getApprovalType();
        if (approvalType != null && !approvalType.isEmpty()) {
            // 标准化审批类型值
            if ("OR".equals(approvalType)) {
                approvalType = "OR_SIGN"; // OR -> OR_SIGN
            } else if ("MULTI".equals(approvalType)) {
                approvalType = "COUNTERSIGN"; // MULTI -> COUNTERSIGN
            }
            task.setApprovalType(approvalType);
        } else {
            task.setApprovalType("SINGLE"); // 默认单人审批
        }
        
        return task;
    }
    
    @Override
    @Transactional
    public int deleteByTaskKey(String taskKey) {
        if (taskKey == null || taskKey.trim().isEmpty()) {
            System.err.println("deleteByTaskKey: taskKey为空，无法删除");
            return 0;
        }
        System.out.println("=== 开始物理删除流程任务记录 ===");
        System.out.println("taskKey=" + taskKey);
        
        // 先查询一下有多少条记录
        List<ProcessTask> tasks = processTaskMapper.selectByTaskKey(taskKey);
        int count = tasks != null ? tasks.size() : 0;
        System.out.println("查询到 " + count + " 条任务记录待删除");
        
        // 执行物理删除
        int deletedCount = processTaskMapper.deleteByTaskKey(taskKey);
        System.out.println("实际删除了 " + deletedCount + " 条记录");
        System.out.println("=== 物理删除流程任务记录完成 ===");
        
        return deletedCount;
    }
    
    @Override
    @Transactional
    public boolean deleteTaskById(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            System.err.println("deleteTaskById: taskId为空，无法删除");
            return false;
        }
        int deletedCount = processTaskMapper.deleteById(taskId);
        return deletedCount > 0;
    }
    
    @Override
    public List<ProcessTask> getByTaskKey(String taskKey) {
        if (taskKey == null || taskKey.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return processTaskMapper.selectByTaskKey(taskKey);
    }
    
    @Override
    @Transactional
    public boolean completeTask(String taskKey, String taskId, String comment, String approverSignature) {
        System.out.println("=== 开始完成任务 ===");
        System.out.println("taskKey: " + taskKey + ", taskId: " + taskId + ", comment: " + comment);
        
        if (taskKey == null || taskKey.trim().isEmpty()) {
            System.err.println("任务KEY为空，无法完成任务");
            return false;
        }
        
        ProcessTask currentTask = null;
        
        // 如果提供了taskId，直接使用；否则查找当前待处理的任务
        if (taskId != null && !taskId.trim().isEmpty()) {
            currentTask = processTaskMapper.selectById(taskId);
            if (currentTask == null || !taskKey.equals(currentTask.getTaskKey())) {
                System.err.println("任务不存在或taskKey不匹配: taskId=" + taskId + ", taskKey=" + taskKey);
                return false;
            }
        } else {
            // 查找当前待处理的任务（按print_order排序，取最小的）
            List<ProcessTask> tasks = processTaskMapper.selectByTaskKey(taskKey);
            if (tasks == null || tasks.isEmpty()) {
                System.err.println("未找到任务记录: taskKey=" + taskKey);
                return false;
            }
            
            // 筛选出待处理的任务
            List<ProcessTask> pendingTasks = tasks.stream()
                .filter(t -> "PENDING".equals(t.getTaskStatus()))
                .collect(java.util.stream.Collectors.toList());
            
            if (pendingTasks.isEmpty()) {
                System.err.println("未找到待处理的任务: taskKey=" + taskKey);
                return false;
            }
            
            // 按print_order排序，print_order小的优先
            // 如果print_order相同或为null，按createTime排序
            pendingTasks.sort((t1, t2) -> {
                Integer order1 = t1.getPrintOrder();
                Integer order2 = t2.getPrintOrder();
                
                // 如果都有print_order，按print_order排序
                if (order1 != null && order2 != null) {
                    int orderCompare = order1.compareTo(order2);
                    if (orderCompare != 0) {
                        return orderCompare;
                    }
                } else if (order1 != null) {
                    // order1不为null，order2为null，order1优先
                    return -1;
                } else if (order2 != null) {
                    // order2不为null，order1为null，order2优先
                    return 1;
                }
                // 如果都为null或print_order相同，按createTime排序
                if (t1.getCreateTime() != null && t2.getCreateTime() != null) {
                    return t1.getCreateTime().compareTo(t2.getCreateTime());
                }
                return 0;
            });
            
            // 取print_order最小的任务（即当前应该处理的任务）
            currentTask = pendingTasks.get(0);
            
            System.out.println("找到当前待处理任务: taskId=" + currentTask.getTaskId() + 
                ", taskName=" + currentTask.getTaskName() + 
                ", printOrder=" + currentTask.getPrintOrder());
        }
        
        // 检查任务状态
        if (!"PENDING".equals(currentTask.getTaskStatus())) {
            System.err.println("任务状态不是PENDING，无法完成: taskId=" + currentTask.getTaskId() + ", status=" + currentTask.getTaskStatus());
            return false;
        }
        
        // 更新当前任务状态为已完成
        LocalDateTime now = LocalDateTime.now();
        currentTask.setTaskStatus("COMPLETED");
        currentTask.setComment(comment);
        currentTask.setCompleteTime(now); // 完成时间
        currentTask.setUpdateTime(now); // 更新时间
        // 如果提供了签名，保存签名
        if (approverSignature != null && !approverSignature.trim().isEmpty()) {
            currentTask.setApproverSignature(approverSignature);
        }
        
        int updateResult = processTaskMapper.updateById(currentTask);
        if (updateResult <= 0) {
            System.err.println("更新任务状态失败: taskId=" + currentTask.getTaskId());
            return false;
        }
        
        System.out.println("当前任务已完成: taskId=" + currentTask.getTaskId() + ", taskName=" + currentTask.getTaskName());
        
        // 根据审批类型处理同一节点的其他任务
        // 或签（OR_SIGN）：多人中任意一人完成即可，其他人自动完成
        // 会签（COUNTERSIGN）：需要所有人都完成，才能进入下一节点
        String approvalType = currentTask.getApprovalType();
        if (approvalType != null && "OR_SIGN".equals(approvalType)) {
            // 或签场景：一个人完成时，同一节点的其他PENDING任务也应该完成
            List<ProcessTask> allTasks = processTaskMapper.selectByTaskKey(currentTask.getTaskKey());
            List<ProcessTask> sameNodePendingTasks = new java.util.ArrayList<>();
            for (ProcessTask t : allTasks) {
                if (currentTask.getTaskName().equals(t.getTaskName()) && 
                    currentTask.getTaskKey().equals(t.getTaskKey()) &&
                    "PENDING".equals(t.getTaskStatus()) &&
                    !t.getTaskId().equals(currentTask.getTaskId())) { // 排除当前任务（已经更新）
                    sameNodePendingTasks.add(t);
                }
            }
            
            // 将同一节点的所有PENDING任务都标记为已完成状态
            // 注意：对于或签场景，只有当前审批人的comment需要更新，其他人的comment保持为空
            for (ProcessTask taskToComplete : sameNodePendingTasks) {
                taskToComplete.setTaskStatus("COMPLETED");
                // 不更新其他任务的comment，只有当前审批人的任务才有comment
                // taskToComplete.setComment(comment); // 或签场景下，其他人的comment不更新
                taskToComplete.setCompleteTime(now); // 完成时间
                taskToComplete.setUpdateTime(now); // 更新时间
                // 不更新其他任务的签名，只有当前审批人的任务才有签名
                // if (approverSignature != null && !approverSignature.trim().isEmpty()) {
                //     taskToComplete.setApproverSignature(approverSignature);
                // }
                processTaskMapper.updateById(taskToComplete);
                System.out.println("或签场景：完成同一节点任务: taskId=" + taskToComplete.getTaskId() + 
                    ", taskName=" + taskToComplete.getTaskName() + ", assigneeUserName=" + taskToComplete.getAssigneeUserName() + 
                    "（不更新comment和签名，只有当前审批人的任务才有）");
            }
            
            if (!sameNodePendingTasks.isEmpty()) {
                System.out.println("或签场景：已完成同一节点的 " + sameNodePendingTasks.size() + " 个任务");
            }
        } else if (approvalType != null && "COUNTERSIGN".equals(approvalType)) {
            // 会签场景：需要检查是否所有人都完成了
            List<ProcessTask> allTasks = processTaskMapper.selectByTaskKey(currentTask.getTaskKey());
            List<ProcessTask> sameNodeTasks = new java.util.ArrayList<>();
            for (ProcessTask t : allTasks) {
                if (currentTask.getTaskName().equals(t.getTaskName()) && 
                    currentTask.getTaskKey().equals(t.getTaskKey())) {
                    sameNodeTasks.add(t);
                }
            }
            
            // 检查同一节点的所有任务是否都已完成
            boolean allCompleted = true;
            for (ProcessTask t : sameNodeTasks) {
                if (!"COMPLETED".equals(t.getTaskStatus())) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                System.out.println("会签场景：同一节点的所有任务都已完成，可以进入下一节点");
            } else {
                int completedCount = 0;
                int totalCount = sameNodeTasks.size();
                for (ProcessTask t : sameNodeTasks) {
                    if ("COMPLETED".equals(t.getTaskStatus())) {
                        completedCount++;
                    }
                }
                System.out.println("会签场景：当前节点已完成 " + completedCount + "/" + totalCount + " 个任务，等待其他人完成");
            }
        }
        
        // 如果有下一个任务，激活它（状态已经是PENDING，不需要额外操作）
        // 如果这是最后一个任务，流程结束，需要在业务层面处理（如更新BudgetApply状态为APPROVED）
        
        System.out.println("=== 任务完成成功 ===");
        return true;
    }
    
    @Override
    @Transactional
    public String addSign(String taskId, String newAssigneeUserId, String newAssigneeUserName, String newAssigneeEmpCode, String taskName) {
        System.out.println("=== 开始加签 ===");
        System.out.println("taskId: " + taskId + ", newAssigneeUserId: " + newAssigneeUserId);
        
        if (taskId == null || taskId.trim().isEmpty()) {
            System.err.println("任务ID为空，无法加签");
            return null;
        }
        
        // 获取当前任务
        ProcessTask currentTask = processTaskMapper.selectById(taskId);
        if (currentTask == null) {
            System.err.println("任务不存在: taskId=" + taskId);
            return null;
        }
        
        // 检查是否有加签权限
        if (currentTask.getAllowAddsign() == null || currentTask.getAllowAddsign() != 1) {
            System.err.println("当前任务不允许加签: taskId=" + taskId);
            return null;
        }
        
        // 检查任务状态
        if (!"PENDING".equals(currentTask.getTaskStatus())) {
            System.err.println("只有待处理的任务才能加签: taskId=" + taskId + ", status=" + currentTask.getTaskStatus());
            return null;
        }
        
        // 生成新任务的taskId
        String newTaskId = java.util.UUID.randomUUID().toString();
        
        // 创建新任务
        ProcessTask newTask = new ProcessTask();
        newTask.setTaskId(newTaskId);
        newTask.setTaskKey(currentTask.getTaskKey()); // 使用相同的taskKey（apply_no）
        newTask.setTaskName(taskName != null && !taskName.trim().isEmpty() ? taskName : "加签审批");
        newTask.setProcessInstanceId(currentTask.getProcessInstanceId());
        newTask.setTaskStatus("PENDING");
        
        // 设置审批人信息
        newTask.setAssigneeUserId(newAssigneeUserId);
        newTask.setAssigneeUserName(newAssigneeUserName);
        newTask.setAssigneeEmpCode(newAssigneeEmpCode);
        newTask.setAssigneeType("user"); // 加签任务默认是指定用户
        
        // 设置加签相关属性
        newTask.setParentTaskId(currentTask.getTaskId());
        newTask.setIsAddsignTask(1);
        
        // 复制其他属性
        newTask.setAllowAddsign(0); // 加签任务默认不允许再加签
        newTask.setAllowTransfer(currentTask.getAllowTransfer());
        newTask.setAllowReject(currentTask.getAllowReject());
        newTask.setNeedPrint(currentTask.getNeedPrint());
        newTask.setTaskType(currentTask.getTaskType());
        newTask.setApprovalType("SINGLE"); // 加签任务默认是单人审批，因为只有一个审批人
        
        // 加签任务的approver_list应该只包含加签人自己，不包含原任务的审批人列表
        // 加签是一个独立的审批任务，只由加签人审批
        newTask.setApproverList(newAssigneeUserName);
        
        // 加签任务的print_order继承原节点的print_order（例如2）
        Integer currentPrintOrder = currentTask.getPrintOrder();
        newTask.setPrintOrder(currentPrintOrder);
        
        // 加签任务的next_task_id应该指向原任务（当前任务）
        newTask.setNextTaskId(currentTask.getTaskId());
        
        // 找到指向当前任务的上一个任务，将其next_task_id改为指向加签任务
        // 查询所有任务，找到next_task_id指向当前任务的任务
        List<ProcessTask> allTasks = processTaskMapper.selectByTaskKey(currentTask.getTaskKey());
        ProcessTask prevTask = null;
        for (ProcessTask task : allTasks) {
            if (currentTask.getTaskId().equals(task.getNextTaskId())) {
                prevTask = task;
                break;
            }
        }
        
        if (prevTask != null) {
            // 更新上一个任务的next_task_id指向加签任务
            prevTask.setNextTaskId(newTaskId);
            prevTask.setUpdateTime(LocalDateTime.now());
            int updatePrevResult = processTaskMapper.updateById(prevTask);
            if (updatePrevResult <= 0) {
                System.err.println("更新上一个任务的next_task_id失败: taskId=" + prevTask.getTaskId());
                return null;
            }
            System.out.println("已更新上一个任务的next_task_id指向加签任务: prevTaskId=" + prevTask.getTaskId() + ", nextTaskId=" + newTaskId);
        } else {
            System.out.println("警告: 未找到指向当前任务的上一个任务，可能当前任务是第一个任务");
        }
        
        // 原任务的next_task_id保持不变，不需要修改
        
        // 保存新任务
        int insertResult = processTaskMapper.insert(newTask);
        if (insertResult <= 0) {
            System.err.println("插入新任务失败");
            return null;
        }
        
        // 更新当前任务的print_order（原节点print_order加1，例如从2变成3）
        if (currentPrintOrder != null) {
            currentTask.setPrintOrder(currentPrintOrder + 1);
            currentTask.setUpdateTime(LocalDateTime.now());
            // 更新当前任务（主要是print_order）
            int updateResult = processTaskMapper.updateById(currentTask);
            if (updateResult <= 0) {
                System.err.println("更新当前任务失败");
                return null;
            }
        }
        
        // 更新所有后续节点的print_order（都加1）
        // 注意：只更新同一个taskKey和processInstanceId下的后续节点，确保只影响同一个流程实例
        if (currentPrintOrder != null) {
            allTasks = processTaskMapper.selectByTaskKey(currentTask.getTaskKey());
            Long currentProcessInstanceId = currentTask.getProcessInstanceId();
            String currentTaskName = currentTask.getTaskName();
            
            System.out.println("开始更新后续节点print_order: taskKey=" + currentTask.getTaskKey() + 
                ", taskName=" + currentTaskName + ", printOrder=" + currentPrintOrder);
            
            for (ProcessTask task : allTasks) {
                // 跳过加签任务本身和当前任务（当前任务已更新）
                if (task.getTaskId().equals(newTaskId) || task.getTaskId().equals(currentTask.getTaskId())) {
                    continue;
                }
                
                // 严格检查：只更新同一个taskKey、同一个processInstanceId、非加签任务、且print_order >= 原节点print_order的任务
                Integer taskPrintOrder = task.getPrintOrder();
                boolean shouldUpdate = taskPrintOrder != null 
                    && taskPrintOrder >= currentPrintOrder 
                    && (task.getIsAddsignTask() == null || task.getIsAddsignTask() == 0)
                    && currentTask.getTaskKey().equals(task.getTaskKey())
                    && currentProcessInstanceId != null 
                    && currentProcessInstanceId.equals(task.getProcessInstanceId());
                
                if (shouldUpdate) {
                    task.setPrintOrder(taskPrintOrder + 1);
                    processTaskMapper.updateById(task);
                    System.out.println("更新后续节点print_order: taskId=" + task.getTaskId() + 
                        ", taskName=" + task.getTaskName() + 
                        ", taskKey=" + task.getTaskKey() +
                        ", processInstanceId=" + task.getProcessInstanceId() +
                        ", printOrder从" + taskPrintOrder + "改为" + (taskPrintOrder + 1));
                }
            }
        }
        
        System.out.println("加签成功: newTaskId=" + newTaskId + ", taskName=" + newTask.getTaskName());
        System.out.println("=== 加签完成 ===");
        
        return newTaskId;
    }
    
    @Override
    @Transactional
    public boolean returnTask(String taskKey, String taskId, String returnType, String applicantUserId, String applicantUserName, String applicantEmpCode, String comment) {
        System.out.println("=== 开始退回任务 ===");
        System.out.println("taskKey: " + taskKey + ", taskId: " + taskId + ", returnType: " + returnType);
        
        if (taskKey == null || taskKey.trim().isEmpty()) {
            System.err.println("任务KEY为空，无法退回");
            return false;
        }
        
        // 退回意见必填
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }
        
        if (returnType == null || (!"RETURN_TO_START".equals(returnType) && !"RETURN_TO_CURRENT".equals(returnType))) {
            System.err.println("退回类型无效: " + returnType);
            return false;
        }
        
        // 更新业务表状态为REJECTED（已拒绝）
        BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(taskKey);
        String businessType = businessTypeInfo != null ? businessTypeInfo.getBusinessType() : inferBusinessType(taskKey);
        
        // 使用通用方法获取业务数据并更新状态
        Object businessData = getBusinessDataByType(taskKey, businessType);
        if (businessData instanceof BudgetApply && budgServiceClient != null) {
            try {
                BudgetApply apply = (BudgetApply) businessData;
                apply.setStatus("REJECTED");
                Result<BudgetApply> updateResult = budgServiceClient.updateBudgetApply(apply);
                if (updateResult != null && updateResult.getCode() == 200) {
                    System.out.println("成功更新预算申请状态为REJECTED: businessKey=" + taskKey);
                } else {
                    System.err.println("更新预算申请状态为REJECTED失败: businessKey=" + taskKey);
                }
            } catch (Exception e) {
                System.err.println("更新业务表状态失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (businessData instanceof CtrlPayout && reimbServiceClient != null) {
            try {
                CtrlPayout payout = (CtrlPayout) businessData;
                payout.setStatus("REJECTED");
                Result<CtrlPayout> updateResult = reimbServiceClient.updateCtrlPayout(payout);
                if (updateResult != null && updateResult.getCode() == 200) {
                    System.out.println("成功更新报账申请状态为REJECTED: businessKey=" + taskKey);
                } else {
                    System.err.println("更新报账申请状态为REJECTED失败: businessKey=" + taskKey);
                }
            } catch (Exception e) {
                System.err.println("更新业务表状态失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (businessData instanceof PactMain && contractServiceClient != null) {
            try {
                PactMain contract = (PactMain) businessData;
                contract.setStatus("REJECTED");
                Result<Void> updateResult = contractServiceClient.updatePactMain(contract);
                if (updateResult != null && updateResult.getCode() == 200) {
                    System.out.println("成功更新合同状态为REJECTED: businessKey=" + taskKey);
                } else {
                    System.err.println("更新合同状态为REJECTED失败: businessKey=" + taskKey);
                }
            } catch (Exception e) {
                System.err.println("更新业务表状态失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // RETURN_TO_START：删除所有任务，重新提交时重新启动流程
        if ("RETURN_TO_START".equals(returnType)) {
            int deletedCount = deleteByTaskKey(taskKey);
            System.out.println("退回方式：退回后重新走流程，已删除 " + deletedCount + " 条任务记录");
            return deletedCount >= 0; // 即使没有任务也算成功
        }
        
        // RETURN_TO_CURRENT：退回后重新提交到本节点
        // 不删除流程任务，在任务表中记录退回状态，并修改提交节点的next_task_id为本节点id
        ProcessTask currentTask = null;
        
        // 如果提供了taskId，直接使用；否则查找当前待处理的任务
        if (taskId != null && !taskId.trim().isEmpty()) {
            currentTask = processTaskMapper.selectById(taskId);
            if (currentTask == null || !taskKey.equals(currentTask.getTaskKey())) {
                System.err.println("任务不存在或taskKey不匹配: taskId=" + taskId + ", taskKey=" + taskKey);
                return false;
            }
        } else {
            // 查找当前待处理的任务（按print_order排序，取最小的）
            List<ProcessTask> tasks = processTaskMapper.selectByTaskKey(taskKey);
            if (tasks == null || tasks.isEmpty()) {
                System.err.println("未找到任务记录: taskKey=" + taskKey);
                return false;
            }
            currentTask = tasks.stream()
                .filter(t -> "PENDING".equals(t.getTaskStatus()))
                .sorted((t1, t2) -> {
                    Integer order1 = t1.getPrintOrder();
                    Integer order2 = t2.getPrintOrder();
                    if (order1 != null && order2 != null) {
                        return order1.compareTo(order2);
                    }
                    if (t1.getCreateTime() != null && t2.getCreateTime() != null) {
                        return t1.getCreateTime().compareTo(t2.getCreateTime());
                    }
                    return 0;
                })
                .findFirst()
                .orElse(null);
            if (currentTask == null) {
                System.err.println("未找到待处理的任务: taskKey=" + taskKey);
                return false;
            }
        }
        
        // 检查任务状态
        if (!"PENDING".equals(currentTask.getTaskStatus())) {
            System.err.println("任务状态不是PENDING，无法退回: taskId=" + currentTask.getTaskId() + ", status=" + currentTask.getTaskStatus());
            return false;
        }
        
        System.out.println("找到当前任务: taskId=" + currentTask.getTaskId() + ", taskName=" + currentTask.getTaskName());
        
        // 找到"提交"节点（task_name为"提交"的任务）
        List<ProcessTask> allTasks = processTaskMapper.selectByTaskKey(taskKey);
        ProcessTask submitTask = null;
        for (ProcessTask task : allTasks) {
            if ("提交".equals(task.getTaskName()) || "submit".equalsIgnoreCase(task.getTaskName())) {
                submitTask = task;
                break;
            }
        }
        
        if (submitTask == null) {
            System.err.println("未找到提交节点任务，无法设置next_task_id");
            return false;
        }
        
        // 同一节点（相同taskName）的所有任务是一个整体
        // 如果一个人退回，应该将同一节点的所有PENDING任务都标记为退回状态
        // 查找同一节点的所有PENDING任务（相同taskName和taskKey）
        List<ProcessTask> sameNodePendingTasks = new java.util.ArrayList<>();
        for (ProcessTask t : allTasks) {
            if (currentTask.getTaskName().equals(t.getTaskName()) && 
                currentTask.getTaskKey().equals(t.getTaskKey()) &&
                "PENDING".equals(t.getTaskStatus())) {
                sameNodePendingTasks.add(t);
            }
        }
        
        // 将同一节点的所有PENDING任务都标记为退回状态
        for (ProcessTask taskToReturn : sameNodePendingTasks) {
            taskToReturn.setTaskStatus("RETURNED");
            taskToReturn.setReturnType(returnType);
            taskToReturn.setComment(comment);
            taskToReturn.setUpdateTime(LocalDateTime.now());
            processTaskMapper.updateById(taskToReturn);
            System.out.println("退回同一节点任务: taskId=" + taskToReturn.getTaskId() + 
                ", taskName=" + taskToReturn.getTaskName() + ", assigneeUserName=" + taskToReturn.getAssigneeUserName());
        }
        
        System.out.println("已退回同一节点的 " + sameNodePendingTasks.size() + " 个任务");
        
        // 修改提交节点的next_task_id为当前节点（退回到的节点）的task_id
        submitTask.setNextTaskId(currentTask.getTaskId());
        submitTask.setUpdateTime(LocalDateTime.now());
        int updateResult = processTaskMapper.updateById(submitTask);
        if (updateResult <= 0) {
            System.err.println("更新提交节点失败: taskId=" + submitTask.getTaskId());
            return false;
        }
        
        System.out.println("退回任务成功: taskId=" + currentTask.getTaskId() + ", 退回类型=" + returnType);
        System.out.println("已修改提交节点的next_task_id为: " + currentTask.getTaskId());
        System.out.println("=== 退回任务完成 ===");
        
        return true;
    }
    
    // ==================== 流程实例相关方法（从ProcessInstanceService迁移） ====================
    
    @Override
    public List<ProcessInstance> getAllProcessInstances() {
        // 从任务表查询所有当前任务，按task_key去重，每个task_key对应一个流程实例
        List<ProcessTask> allTasks = processTaskMapper.selectAllCurrentTasks();
        
        // 提取唯一的task_key（business_key），每个task_key对应一个流程实例
        java.util.Map<String, ProcessInstance> instanceMap = new java.util.HashMap<>();
        for (ProcessTask task : allTasks) {
            String businessKey = task.getTaskKey(); // task_key就是business_key
            if (businessKey == null || businessKey.trim().isEmpty()) {
                continue;
            }
            
            // 如果这个businessKey已经处理过，跳过
            if (instanceMap.containsKey(businessKey)) {
                continue;
            }
            
            // 确定business_type（从模板设置表获取business_type字段）
            BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(businessKey);
            String businessType = null;
            if (businessTypeInfo != null && businessTypeInfo.getBusinessType() != null) {
                businessType = businessTypeInfo.getBusinessType(); // 从template_config表的business_type字段获取
            } else {
                // 如果无法从模板设置表获取，使用备用方法推断
                businessType = inferBusinessType(businessKey);
            }
            if (businessType == null || businessType.trim().isEmpty()) {
                continue; // 无法获取业务类型，跳过
            }
            
            // 创建流程实例对象（不查询流程实例表，而是从任务和业务表组装）
            ProcessInstance instance = new ProcessInstance();
            instance.setInstanceId(null); // 不使用流程实例表的ID
            instance.setBusinessKey(businessKey);
            instance.setBusinessType(businessType);
            instance.setProcessStatus("RUNNING"); // 有当前任务就是运行中
            
            // 从业务表获取启动人、启动时间等信息
            loadInstanceInfoFromBusinessTable(instance, businessKey, businessType, task);
            
            instanceMap.put(businessKey, instance);
        }
        
        java.util.List<ProcessInstance> instances = new java.util.ArrayList<>(instanceMap.values());
        // 按创建时间倒序排序
        instances.sort((a, b) -> {
            if (a.getStartTime() != null && b.getStartTime() != null) {
                return b.getStartTime().compareTo(a.getStartTime());
            }
            return 0;
        });
        return instances;
    }
    
    @Override
    public PageResult<ProcessInstance> getAllProcessInstancesPage(Long page, Long size) {
        // 先查询所有流程实例（不分页）
        List<ProcessInstance> allInstances = getAllProcessInstances();
        
        // 手动分页
        long total = allInstances.size();
        long start = (page - 1) * size;
        long end = Math.min(start + size, total);
        
        List<ProcessInstance> pageList;
        if (start >= total) {
            pageList = new java.util.ArrayList<>();
        } else {
            pageList = allInstances.subList((int)start, (int)end);
        }
        
        return new PageResult<>(pageList, total, size, page);
    }
    
    /**
     * 根据business_key推断business_type（通过尝试查询不同的业务表）
     */
    private String inferBusinessType(String businessKey) {
        if (businessKey == null || businessKey.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试查询预算申请表
            if (budgServiceClient != null) {
                Result<BudgetApply> result = budgServiceClient.getBudgetApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return "BUDGET_APPLY";
                }
            }
            // 尝试查询报账申请表
            if (reimbServiceClient != null) {
                Result<CtrlPayout> result = reimbServiceClient.getCtrlPayoutByBillcode(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return "PAYOUT_APPLY";
                }
            }
            // 尝试查询采购申请表
            if (assetServiceClient != null) {
                Result<AssetPurchaseApplyMain> result = assetServiceClient.getAssetPurchaseApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return "ASSET_PURCHASE_APPLY";
                }
            }
            // 尝试查询合同表
            if (contractServiceClient != null) {
                Result<PactMain> result = contractServiceClient.getContractByContractNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    return "CONTRACT";
                }
            }
            // 可以根据需要扩展其他业务类型的推断逻辑
        } catch (Exception e) {
            System.err.println("推断业务类型失败: " + e.getMessage());
        }
        
        // 默认返回BUDGET_APPLY（根据实际情况调整）
        return "BUDGET_APPLY";
    }
    
    /**
     * 从业务表加载流程实例信息（审批人、创建时间等）
     */
    private void loadInstanceInfoFromBusinessTable(ProcessInstance instance, String businessKey, String businessType, ProcessTask task) {
        try {
            // 审批人信息直接从任务中获取（当前办理人）
            instance.setStartUserId(task.getAssigneeUserId()); // 使用审批人作为启动人（流程实例显示需要）
            instance.setStartUserName(task.getAssigneeUserName()); // 当前审批人
            
            // 创建时间从任务中获取
            instance.setStartTime(task.getCreateTime());
            
            // 如果需要从业务表获取其他信息（如申请人信息），可以在这里添加
            // 但目前流程实例只需要显示：业务类型、业务主键、审批人、创建时间
        } catch (Exception e) {
            System.err.println("从业务表加载流程实例信息失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<ProcessVariable> getVariablesByBusinessKey(String businessKey, String businessType) {
        java.util.List<ProcessVariable> variables = new java.util.ArrayList<>();
        
        try {
            // 如果businessType为空，尝试从业务主键推断业务类型
            if (businessType == null || businessType.trim().isEmpty()) {
                BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(businessKey);
                if (businessTypeInfo != null && businessTypeInfo.getBusinessType() != null) {
                    businessType = businessTypeInfo.getBusinessType();
                } else {
                    // 如果无法从模板配置获取，使用推断方法
                    businessType = inferBusinessType(businessKey);
                }
            }
            
            // 使用通用方法获取业务数据
            Object businessData = getBusinessDataByType(businessKey, businessType);
            if (businessData == null) {
                return variables;
            }
            
            // 使用反射提取所有字段作为变量
            java.lang.reflect.Field[] fields = businessData.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(businessData);
                    String fieldName = field.getName();
                    
                    // 跳过不需要的字段
                    if (fieldName.equals("serialVersionUID") || 
                        (businessData instanceof BudgetApply && fieldName.equals("currentApprover")) ||
                        (businessData instanceof PactMain && (fieldName.equals("empName") || fieldName.equals("deptName") || fieldName.equals("empPhone")))) {
                        // 合同表的 empName、deptName、empPhone 是用于显示的，不持久化，跳过
                        continue;
                    }
                    
                    ProcessVariable variable = new ProcessVariable();
                    variable.setVariableKey(fieldName);
                    variable.setVariableValue(value != null ? value.toString() : null);
                    // 根据字段类型设置variableType
                    String typeName = field.getType().getSimpleName();
                    if (typeName.equals("String")) {
                        variable.setVariableType("STRING");
                    } else if (typeName.equals("Integer") || typeName.equals("Long") || typeName.equals("int") || typeName.equals("long")) {
                        variable.setVariableType("INTEGER");
                    } else if (typeName.equals("BigDecimal") || typeName.equals("Double") || typeName.equals("Float")) {
                        variable.setVariableType("DOUBLE");
                    } else if (typeName.equals("Boolean") || typeName.equals("boolean")) {
                        variable.setVariableType("BOOLEAN");
                    } else if (typeName.equals("LocalDateTime") || typeName.equals("Date")) {
                        variable.setVariableType("DATE");
                    } else {
                        variable.setVariableType("STRING");
                    }
                    variables.add(variable);
                } catch (Exception e) {
                    System.err.println("提取变量失败: " + field.getName() + ", " + e.getMessage());
                }
            }
            
            // 如果是申请单（PAYOUT_APPLY或PAYOUT），查询预算明细并添加到变量中
            if ((businessType != null && (businessType.contains("PAYOUT") || businessType.contains("APPLY"))) 
                && reimbServiceClient != null) {
                try {
                    Result<java.util.List<BudgetDetailRecord>> budgetDetailsResult = 
                        reimbServiceClient.getBudgetDetailsByBusinessNo(businessKey);
                    if (budgetDetailsResult != null && budgetDetailsResult.getCode() == 200 
                        && budgetDetailsResult.getData() != null) {
                        java.util.List<BudgetDetailRecord> budgetDetails = budgetDetailsResult.getData();
                        if (budgetDetails != null && !budgetDetails.isEmpty()) {
                            // 将预算明细转换为JSON字符串，作为变量值
                            String budgetDetailsJson = objectMapper.writeValueAsString(budgetDetails);
                            ProcessVariable budgetDetailsVariable = new ProcessVariable();
                            budgetDetailsVariable.setVariableKey("budgetDetails");
                            budgetDetailsVariable.setVariableValue(budgetDetailsJson);
                            budgetDetailsVariable.setVariableType("STRING");
                            variables.add(budgetDetailsVariable);
                            
                            // 同时添加每个预算明细的单独变量（方便查看）
                            for (int i = 0; i < budgetDetails.size(); i++) {
                                BudgetDetailRecord detail = budgetDetails.get(i);
                                String prefix = "budgetDetail[" + i + "].";
                                
                                // 添加主要字段（只使用BudgetDetailRecord中存在的字段）
                                addBudgetDetailVariable(variables, prefix + "itemName", detail.getItemName());
                                addBudgetDetailVariable(variables, prefix + "itemCode", detail.getItemCode());
                                addBudgetDetailVariable(variables, prefix + "subjectName", detail.getSubjectName());
                                addBudgetDetailVariable(variables, prefix + "subjectCode", detail.getSubjectCode());
                                addBudgetDetailVariable(variables, prefix + "amount", detail.getAmount());
                                addBudgetDetailVariable(variables, prefix + "detailType", detail.getDetailType());
                                addBudgetDetailVariable(variables, prefix + "budgetYear", detail.getBudgetYear());
                                addBudgetDetailVariable(variables, prefix + "deptName", detail.getDeptName());
                                addBudgetDetailVariable(variables, prefix + "empName", detail.getEmpName());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("查询预算明细失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 如果是报账单（PAYOUT类型且billType为PAYOUT），查询发票信息和支付清单并添加到变量中
            if (businessType != null && businessType.contains("PAYOUT") && reimbServiceClient != null) {
                try {
                    // 先获取报账单主表信息，判断billType
                    Result<CtrlPayout> payoutResult = reimbServiceClient.getCtrlPayoutByBillcode(businessKey);
                    if (payoutResult != null && payoutResult.getCode() == 200 && payoutResult.getData() != null) {
                        CtrlPayout payout = payoutResult.getData();
                        // 只有billType为PAYOUT的才需要发票和支付清单
                        if ("PAYOUT".equals(payout.getBillType())) {
                            // 获取完整信息（包括发票和支付清单）
                            Result<com.hrp.common.entity.CtrlPayoutDTO> dtoResult = 
                                reimbServiceClient.getCtrlPayoutDTOByBillcode(businessKey);
                            if (dtoResult != null && dtoResult.getCode() == 200 && dtoResult.getData() != null) {
                                com.hrp.common.entity.CtrlPayoutDTO dto = dtoResult.getData();
                                
                                // 添加发票信息变量
                                if (dto.getInvoices() != null && !dto.getInvoices().isEmpty()) {
                                    String invoicesJson = objectMapper.writeValueAsString(dto.getInvoices());
                                    ProcessVariable invoicesVariable = new ProcessVariable();
                                    invoicesVariable.setVariableKey("invoices");
                                    invoicesVariable.setVariableValue(invoicesJson);
                                    invoicesVariable.setVariableType("STRING");
                                    variables.add(invoicesVariable);
                                    
                                    // 同时添加每个发票的单独变量（方便查看和修改）
                                    for (int i = 0; i < dto.getInvoices().size(); i++) {
                                        com.hrp.common.entity.CtrlPayoutInvoice invoice = dto.getInvoices().get(i);
                                        String prefix = "invoice[" + i + "].";
                                        
                                        addInvoiceVariable(variables, prefix + "invoiceCode", invoice.getInvoiceCode());
                                        addInvoiceVariable(variables, prefix + "invoiceNumber", invoice.getInvoiceNumber());
                                        addInvoiceVariable(variables, prefix + "invoiceDate", invoice.getInvoiceDate());
                                        addInvoiceVariable(variables, prefix + "invoiceAmount", invoice.getInvoiceAmount());
                                        addInvoiceVariable(variables, prefix + "invoiceType", invoice.getInvoiceType());
                                        addInvoiceVariable(variables, prefix + "taxAmount", invoice.getTaxAmount());
                                        addInvoiceVariable(variables, prefix + "remark", invoice.getRemark());
                                    }
                                }
                                
                                // 添加支付清单变量
                                if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
                                    String paymentsJson = objectMapper.writeValueAsString(dto.getPayments());
                                    ProcessVariable paymentsVariable = new ProcessVariable();
                                    paymentsVariable.setVariableKey("payments");
                                    paymentsVariable.setVariableValue(paymentsJson);
                                    paymentsVariable.setVariableType("STRING");
                                    variables.add(paymentsVariable);
                                    
                                    // 同时添加每个支付的单独变量（方便查看和修改）
                                    for (int i = 0; i < dto.getPayments().size(); i++) {
                                        com.hrp.common.entity.CtrlPayoutPayment payment = dto.getPayments().get(i);
                                        String prefix = "payment[" + i + "].";
                                        
                                        addPaymentVariable(variables, prefix + "paymentObject", payment.getPaymentObject());
                                        addPaymentVariable(variables, prefix + "paymentMethod", payment.getPaymentMethod());
                                        addPaymentVariable(variables, prefix + "paymentAmount", payment.getPaymentAmount());
                                        addPaymentVariable(variables, prefix + "bankName", payment.getBankName());
                                        addPaymentVariable(variables, prefix + "bankAccount", payment.getBankAccount());
                                        addPaymentVariable(variables, prefix + "accountName", payment.getAccountName());
                                        addPaymentVariable(variables, prefix + "remark", payment.getRemark());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("查询发票信息和支付清单失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 如果是采购申请（ASSET_TYPE、ASSET_PURCHASE_APPLY或ASSET类型），查询明细表并添加到变量中
            if (businessType != null && (businessType.equals("ASSET_TYPE") || businessType.equals("ASSET_PURCHASE_APPLY") 
                || businessType.equals("ASSET") || businessType.contains("ASSET_PURCHASE")) 
                && assetServiceClient != null) {
                try {
                    Result<java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail>> detailsResult = 
                        assetServiceClient.getAssetPurchaseApplyDetailsByNo(businessKey);
                    if (detailsResult != null && detailsResult.getCode() == 200 && detailsResult.getData() != null) {
                        java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail> details = detailsResult.getData();
                        if (details != null && !details.isEmpty()) {
                            // 将明细表转换为JSON字符串，作为变量值
                            String detailsJson = objectMapper.writeValueAsString(details);
                            ProcessVariable detailsVariable = new ProcessVariable();
                            detailsVariable.setVariableKey("assetDetails");
                            detailsVariable.setVariableValue(detailsJson);
                            detailsVariable.setVariableType("STRING");
                            variables.add(detailsVariable);
                            
                            // 同时添加每个明细的单独变量（方便查看和修改）
                            for (int i = 0; i < details.size(); i++) {
                                com.hrp.common.entity.AssetPurchaseApplyDetail detail = details.get(i);
                                String prefix = "assetDetail[" + i + "].";
                                
                                // 添加主要字段（包括id字段，用于更新时识别记录）
                                addAssetDetailVariable(variables, prefix + "id", detail.getId());
                                addAssetDetailVariable(variables, prefix + "applyId", detail.getApplyId());
                                addAssetDetailVariable(variables, prefix + "assetCode", detail.getAssetCode());
                                addAssetDetailVariable(variables, prefix + "assetName", detail.getAssetName());
                                addAssetDetailVariable(variables, prefix + "spec", detail.getSpec());
                                addAssetDetailVariable(variables, prefix + "manufacturer", detail.getManufacturer());
                                addAssetDetailVariable(variables, prefix + "unit", detail.getUnit());
                                addAssetDetailVariable(variables, prefix + "applyQuantity", detail.getApplyQuantity());
                                addAssetDetailVariable(variables, prefix + "price", detail.getPrice());
                                addAssetDetailVariable(variables, prefix + "totalPrice", detail.getTotalPrice());
                                addAssetDetailVariable(variables, prefix + "remark", detail.getRemark());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("查询采购申请明细失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("从业务表获取变量失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return variables;
    }
    
    /**
     * 添加预算明细变量（辅助方法）
     */
    private void addBudgetDetailVariable(java.util.List<ProcessVariable> variables, String key, Object value) {
        ProcessVariable variable = new ProcessVariable();
        variable.setVariableKey(key);
        variable.setVariableValue(value != null ? value.toString() : null);
        if (value instanceof java.math.BigDecimal || value instanceof Double || value instanceof Float) {
            variable.setVariableType("DOUBLE");
        } else if (value instanceof Integer || value instanceof Long) {
            variable.setVariableType("INTEGER");
        } else {
            variable.setVariableType("STRING");
        }
        variables.add(variable);
    }
    
    /**
     * 添加发票变量（辅助方法）
     */
    private void addInvoiceVariable(java.util.List<ProcessVariable> variables, String key, Object value) {
        ProcessVariable variable = new ProcessVariable();
        variable.setVariableKey(key);
        variable.setVariableValue(value != null ? value.toString() : null);
        if (value instanceof java.math.BigDecimal || value instanceof Double || value instanceof Float) {
            variable.setVariableType("DOUBLE");
        } else if (value instanceof Integer || value instanceof Long) {
            variable.setVariableType("INTEGER");
        } else if (value instanceof java.time.LocalDate || value instanceof java.time.LocalDateTime) {
            variable.setVariableType("DATE");
        } else {
            variable.setVariableType("STRING");
        }
        variables.add(variable);
    }
    
    /**
     * 添加采购申请明细变量（辅助方法）
     */
    private void addAssetDetailVariable(java.util.List<ProcessVariable> variables, String key, Object value) {
        ProcessVariable variable = new ProcessVariable();
        variable.setVariableKey(key);
        variable.setVariableValue(value != null ? value.toString() : null);
        if (value instanceof java.math.BigDecimal || value instanceof Double || value instanceof Float) {
            variable.setVariableType("DOUBLE");
        } else if (value instanceof Integer || value instanceof Long) {
            variable.setVariableType("INTEGER");
        } else if (value instanceof java.time.LocalDate || value instanceof java.time.LocalDateTime) {
            variable.setVariableType("DATE");
        } else {
            variable.setVariableType("STRING");
        }
        variables.add(variable);
    }
    
    /**
     * 添加支付变量（辅助方法）
     */
    private void addPaymentVariable(java.util.List<ProcessVariable> variables, String key, Object value) {
        ProcessVariable variable = new ProcessVariable();
        variable.setVariableKey(key);
        variable.setVariableValue(value != null ? value.toString() : null);
        if (value instanceof java.math.BigDecimal || value instanceof Double || value instanceof Float) {
            variable.setVariableType("DOUBLE");
        } else if (value instanceof Integer || value instanceof Long) {
            variable.setVariableType("INTEGER");
        } else {
            variable.setVariableType("STRING");
        }
        variables.add(variable);
    }
    
    @Override
    @Transactional
    public boolean terminateProcessInstance(String businessKey) {
        if (businessKey == null || businessKey.trim().isEmpty()) {
            System.err.println("businessKey为空，无法终止流程");
            return false;
        }
        
        // 获取该业务主键的所有任务
        List<ProcessTask> tasks = processTaskMapper.selectByBusinessKey(businessKey);
        if (tasks == null || tasks.isEmpty()) {
            System.err.println("未找到与业务主键 " + businessKey + " 相关的任务，无法终止。");
            return false;
        }
        
        // 将所有待处理的任务状态改为TERMINATED
        int updatedCount = 0;
        for (ProcessTask task : tasks) {
            if ("PENDING".equals(task.getTaskStatus())) {
                task.setTaskStatus("TERMINATED");
        task.setUpdateTime(LocalDateTime.now());
                processTaskMapper.updateById(task);
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            System.out.println("成功终止流程，businessKey=" + businessKey + "，终止了" + updatedCount + "个任务");
            // 更新业务表的状态（例如，将预算申请状态改为TERMINATED）
            BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(businessKey);
            String businessType = businessTypeInfo != null ? businessTypeInfo.getBusinessType() : inferBusinessType(businessKey);
            
            // 使用通用方法获取业务数据并更新状态
            Object businessData = getBusinessDataByType(businessKey, businessType);
            if (businessData instanceof BudgetApply && budgServiceClient != null) {
                try {
                    BudgetApply apply = (BudgetApply) businessData;
                    apply.setStatus("TERMINATED");
                    Result<BudgetApply> updateResult = budgServiceClient.updateBudgetApply(apply);
                    if (updateResult != null && updateResult.getCode() == 200) {
                        System.out.println("成功更新预算申请状态为TERMINATED for businessKey: " + businessKey);
                    } else {
                        System.err.println("更新预算申请状态为TERMINATED失败 for businessKey: " + businessKey + 
                            (updateResult != null ? ", code: " + updateResult.getCode() + ", message: " + updateResult.getMessage() : ", updateResult为null"));
                    }
                } catch (Exception e) {
                    System.err.println("更新预算申请状态失败 for businessKey: " + businessKey + ", error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (businessData instanceof CtrlPayout && reimbServiceClient != null) {
                try {
                    CtrlPayout payout = (CtrlPayout) businessData;
                    payout.setStatus("TERMINATED");
                    Result<CtrlPayout> updateResult = reimbServiceClient.updateCtrlPayout(payout);
                    if (updateResult != null && updateResult.getCode() == 200) {
                        System.out.println("成功更新报账申请状态为TERMINATED for businessKey: " + businessKey);
                    } else {
                        System.err.println("更新报账申请状态为TERMINATED失败 for businessKey: " + businessKey);
                    }
                } catch (Exception e) {
                    System.err.println("更新报账申请状态失败 for businessKey: " + businessKey + ", error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // 任务已经成功终止，返回true
            return true;
        } else {
            System.err.println("未找到待终止的任务，businessKey=" + businessKey);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateVariablesByBusinessKey(String businessKey, String businessType, List<ProcessVariable> variables) {
        if (variables == null || variables.isEmpty()) {
            return true;
        }
        
        try {
            // 如果businessType为空，尝试从业务主键推断业务类型
            if (businessType == null || businessType.trim().isEmpty()) {
                BusinessTypeInfo businessTypeInfo = getBusinessTypeByBusinessKey(businessKey);
                if (businessTypeInfo != null && businessTypeInfo.getBusinessType() != null) {
                    businessType = businessTypeInfo.getBusinessType();
                } else {
                    // 如果无法从模板配置获取，使用推断方法
                    businessType = inferBusinessType(businessKey);
                }
            }
            
            // 使用通用方法获取业务数据
            Object businessData = getBusinessDataByType(businessKey, businessType);
            if (businessData == null) {
                return false;
            }
            
            boolean needUpdate = false;
            
            // 使用反射更新字段
            java.lang.reflect.Field[] fields = businessData.getClass().getDeclaredFields();
            java.util.Map<String, java.lang.reflect.Field> fieldMap = new java.util.HashMap<>();
            for (java.lang.reflect.Field field : fields) {
                fieldMap.put(field.getName(), field);
            }
            
            // 处理预算明细更新
            // 方式1：如果存在budgetDetails变量（JSON格式），直接使用
            // 方式2：如果存在budgetDetail[*]变量，从这些变量构建预算明细列表
            ProcessVariable budgetDetailsVariable = null;
            java.util.List<ProcessVariable> budgetDetailVariables = new java.util.ArrayList<>();
            
            for (ProcessVariable variable : variables) {
                String fieldName = variable.getVariableKey();
                if (fieldName.equals("budgetDetails")) {
                    budgetDetailsVariable = variable;
                } else if (fieldName.startsWith("budgetDetail[")) {
                    budgetDetailVariables.add(variable);
                }
            }
            
            // 如果存在budgetDetails变量或budgetDetail[*]变量，处理预算明细更新
            // 优先处理budgetDetail[*]变量（因为这是更新操作，应该保留现有记录）
            java.util.List<BudgetDetailRecord> budgetDetails = null;
            boolean isFromDetailVariables = false; // 标记是否从budgetDetail[*]更新
            
            if (!budgetDetailVariables.isEmpty()) {
                // 方式2：从budgetDetail[*]变量更新预算明细列表（优先处理，因为是更新操作）
                isFromDetailVariables = true;
                // 方式2：从budgetDetail[*]变量更新预算明细列表
                // 先从数据库加载现有的预算明细记录，然后只更新被修改的字段
                try {
                    // 从数据库加载现有的预算明细
                    java.util.List<BudgetDetailRecord> existingDetails = null;
                    if (reimbServiceClient != null) {
                        Result<java.util.List<BudgetDetailRecord>> existingResult = 
                            reimbServiceClient.getBudgetDetailsByBusinessNo(businessKey);
                        if (existingResult != null && existingResult.getCode() == 200 
                            && existingResult.getData() != null) {
                            existingDetails = existingResult.getData();
                        }
                    }
                    
                    // 如果没有现有记录，创建一个新的列表
                    if (existingDetails == null) {
                        existingDetails = new java.util.ArrayList<>();
                    }
                    
                    // 如果现有记录为空，说明没有预算明细，不需要更新
                    if (existingDetails.isEmpty()) {
                        System.err.println("没有现有的预算明细记录，跳过budgetDetail[*]变量更新");
                        budgetDetails = null;
                    } else {
                        // 按索引分组，收集所有被修改的字段
                        java.util.Map<Integer, java.util.Map<String, String>> detailMap = new java.util.HashMap<>();
                        for (ProcessVariable variable : budgetDetailVariables) {
                            String fieldName = variable.getVariableKey();
                            // 解析格式：budgetDetail[0].itemName
                            int startIndex = fieldName.indexOf('[');
                            int endIndex = fieldName.indexOf(']');
                            if (startIndex > 0 && endIndex > startIndex) {
                                try {
                                    int index = Integer.parseInt(fieldName.substring(startIndex + 1, endIndex));
                                    String field = fieldName.substring(endIndex + 2); // 跳过 "]. "
                                    
                                    detailMap.computeIfAbsent(index, k -> new java.util.HashMap<>())
                                        .put(field, variable.getVariableValue());
                                } catch (NumberFormatException e) {
                                    System.err.println("解析预算明细索引失败: " + fieldName);
                                }
                            }
                        }
                        
                        // 更新现有记录（只更新已存在的记录，不创建新记录）
                        for (java.util.Map.Entry<Integer, java.util.Map<String, String>> entry : detailMap.entrySet()) {
                            int index = entry.getKey();
                            java.util.Map<String, String> detailFields = entry.getValue();
                            
                            // 只更新已存在的记录，不创建新记录（避免创建不完整的记录）
                            if (index >= existingDetails.size()) {
                                System.err.println("跳过索引 " + index + " 的更新，因为该索引的记录不存在（现有记录数：" + existingDetails.size() + "）");
                                continue;
                            }
                            
                            BudgetDetailRecord record = existingDetails.get(index);
                            
                            // 只更新被修改的字段
                            if (detailFields.containsKey("itemName")) {
                                record.setItemName(detailFields.get("itemName"));
                            }
                            if (detailFields.containsKey("itemCode")) {
                                record.setItemCode(detailFields.get("itemCode"));
                            }
                            if (detailFields.containsKey("subjectName")) {
                                record.setSubjectName(detailFields.get("subjectName"));
                            }
                            if (detailFields.containsKey("subjectCode")) {
                                record.setSubjectCode(detailFields.get("subjectCode"));
                            }
                            if (detailFields.containsKey("amount")) {
                                try {
                                    String amountStr = detailFields.get("amount");
                                    if (amountStr != null && !amountStr.trim().isEmpty()) {
                                        record.setAmount(new java.math.BigDecimal(amountStr));
                                    }
                                } catch (Exception e) {
                                    System.err.println("解析金额失败: " + detailFields.get("amount"));
                                }
                            }
                            if (detailFields.containsKey("detailType")) {
                                record.setDetailType(detailFields.get("detailType"));
                            }
                            if (detailFields.containsKey("budgetYear")) {
                                record.setBudgetYear(detailFields.get("budgetYear"));
                            }
                            if (detailFields.containsKey("deptName")) {
                                record.setDeptName(detailFields.get("deptName"));
                            }
                            if (detailFields.containsKey("empName")) {
                                record.setEmpName(detailFields.get("empName"));
                            }
                            if (detailFields.containsKey("subjectId")) {
                                try {
                                    String subjectIdStr = detailFields.get("subjectId");
                                    if (subjectIdStr != null && !subjectIdStr.trim().isEmpty()) {
                                        record.setSubjectId(Long.parseLong(subjectIdStr));
                                    }
                                } catch (Exception e) {
                                    System.err.println("解析subjectId失败: " + detailFields.get("subjectId"));
                                }
                            }
                            if (detailFields.containsKey("itemId")) {
                                try {
                                    String itemIdStr = detailFields.get("itemId");
                                    if (itemIdStr != null && !itemIdStr.trim().isEmpty()) {
                                        record.setItemId(Long.parseLong(itemIdStr));
                                    }
                                } catch (Exception e) {
                                    System.err.println("解析itemId失败: " + detailFields.get("itemId"));
                                }
                            }
                            if (detailFields.containsKey("deptId")) {
                                try {
                                    String deptIdStr = detailFields.get("deptId");
                                    if (deptIdStr != null && !deptIdStr.trim().isEmpty()) {
                                        record.setDeptId(Long.parseLong(deptIdStr));
                                    }
                                } catch (Exception e) {
                                    System.err.println("解析deptId失败: " + detailFields.get("deptId"));
                                }
                            }
                            if (detailFields.containsKey("empId")) {
                                try {
                                    String empIdStr = detailFields.get("empId");
                                    if (empIdStr != null && !empIdStr.trim().isEmpty()) {
                                        record.setEmpId(Long.parseLong(empIdStr));
                                    }
                                } catch (Exception e) {
                                    System.err.println("解析empId失败: " + detailFields.get("empId"));
                                }
                            }
                        }
                        
                        // 使用更新后的列表
                        budgetDetails = existingDetails;
                    }
                } catch (Exception e) {
                    System.err.println("从budgetDetail[*]变量更新预算明细失败: " + e.getMessage());
                    e.printStackTrace();
                    budgetDetails = null;
                }
            } else if (budgetDetailsVariable != null && budgetDetailsVariable.getVariableValue() != null 
                && !budgetDetailsVariable.getVariableValue().trim().isEmpty()) {
                // 方式1：从JSON字符串解析（只有在没有budgetDetail[*]变量时才处理）
                isFromDetailVariables = false;
                try {
                    String budgetDetailsJson = budgetDetailsVariable.getVariableValue();
                    budgetDetails = objectMapper.readValue(budgetDetailsJson, 
                        objectMapper.getTypeFactory().constructCollectionType(
                            java.util.List.class, BudgetDetailRecord.class));
                } catch (Exception e) {
                    System.err.println("解析budgetDetails JSON失败: " + e.getMessage());
                }
            }
            
            // 如果构建了预算明细列表，更新预算明细
            if (budgetDetails != null && !budgetDetails.isEmpty() && budgServiceClient != null) {
                try {
                    System.out.println("处理预算明细更新，businessKey=" + businessKey + 
                        "，记录数=" + budgetDetails.size() + "，是否从budgetDetail[*]更新=" + isFromDetailVariables);
                    
                    // 从业务对象中获取必要信息，填充预算明细记录
                    Long businessId = null;
                    Long deptId = null;
                    String deptCode = null;
                    String deptName = null;
                    Long empId = null;
                    String empCode = null;
                    String empName = null;
                    String createUser = null;
                    
                    if (businessData instanceof CtrlPayout) {
                        CtrlPayout payout = (CtrlPayout) businessData;
                        businessId = payout.getPayoutId();
                        deptId = payout.getDeptId();
                        deptName = payout.getDeptName();
                        empId = payout.getEmpId();
                        empCode = payout.getEmpCode();
                        empName = payout.getEmpName();
                        createUser = payout.getCreateUser();
                        if (createUser == null || createUser.isEmpty()) {
                            createUser = empCode;
                        }
                    } else if (businessData instanceof BudgetApply) {
                        BudgetApply apply = (BudgetApply) businessData;
                        businessId = apply.getApplyId();
                        deptId = apply.getDeptId();
                        deptCode = apply.getDeptCode();
                        deptName = apply.getDeptName();
                        empId = apply.getApplicantId();
                        empCode = apply.getApplicantCode();
                        empName = apply.getApplicantName();
                        createUser = apply.getCreateUser();
                        if (createUser == null || createUser.isEmpty()) {
                            createUser = empCode;
                        }
                    }
                    
                    // 确保每个预算明细记录都有必要的字段
                    // 对于从数据库加载的记录（通过budgetDetail[*]更新），保留所有记录，只填充缺失字段
                    // 对于从JSON解析的记录（通过budgetDetails更新），过滤掉不完整的记录
                    int originalSize = budgetDetails.size();
                    java.util.List<BudgetDetailRecord> validRecords = new java.util.ArrayList<>();
                    for (BudgetDetailRecord record : budgetDetails) {
                        // 如果是从budgetDetail[*]更新的（即从数据库加载的），保留所有记录
                        // 如果是从budgetDetails JSON更新的，需要检查完整性
                        if (!isFromDetailVariables) {
                            // 检查记录是否完整（至少要有itemId和subjectId，或者itemName和subjectName）
                            boolean hasItem = (record.getItemId() != null) || 
                                            (record.getItemName() != null && !record.getItemName().isEmpty());
                            boolean hasSubject = (record.getSubjectId() != null) || 
                                               (record.getSubjectName() != null && !record.getSubjectName().isEmpty());
                            boolean hasAmount = record.getAmount() != null;
                            
                            // 如果记录不完整，跳过（不添加到有效记录列表）
                            if (!hasItem || !hasSubject || !hasAmount) {
                                System.err.println("跳过不完整的预算明细记录: itemId=" + record.getItemId() + 
                                    ", subjectId=" + record.getSubjectId() + ", amount=" + record.getAmount());
                                continue;
                            }
                        } else {
                            // 从budgetDetail[*]更新时，记录应该已经存在，确保必要字段不为空
                            // 如果某些字段被更新为null，我们需要确保它们有默认值
                            if (record.getItemId() == null && (record.getItemName() == null || record.getItemName().isEmpty())) {
                                System.err.println("警告：预算明细记录缺少itemId和itemName，但因为是budgetDetail[*]更新，仍保留该记录");
                            }
                            if (record.getSubjectId() == null && (record.getSubjectName() == null || record.getSubjectName().isEmpty())) {
                                System.err.println("警告：预算明细记录缺少subjectId和subjectName，但因为是budgetDetail[*]更新，仍保留该记录");
                            }
                            if (record.getAmount() == null) {
                                System.err.println("警告：预算明细记录缺少amount，但因为是budgetDetail[*]更新，仍保留该记录");
                            }
                        }
                        
                        // 填充必要字段
                        if (record.getBusinessNo() == null || record.getBusinessNo().isEmpty()) {
                            record.setBusinessNo(businessKey);
                        }
                        if (record.getBusinessId() == null && businessId != null) {
                            record.setBusinessId(businessId);
                        }
                        // 如果没有设置明细类型，默认为APPLY
                        if (record.getDetailType() == null || record.getDetailType().isEmpty()) {
                            record.setDetailType("APPLY");
                        }
                        // 填充部门和员工信息（如果记录中没有）
                        if (record.getDeptId() == null && deptId != null) {
                            record.setDeptId(deptId);
                        }
                        if ((record.getDeptCode() == null || record.getDeptCode().isEmpty()) && deptCode != null) {
                            record.setDeptCode(deptCode);
                        }
                        if ((record.getDeptName() == null || record.getDeptName().isEmpty()) && deptName != null) {
                            record.setDeptName(deptName);
                        }
                        if (record.getEmpId() == null && empId != null) {
                            record.setEmpId(empId);
                        }
                        if ((record.getEmpCode() == null || record.getEmpCode().isEmpty()) && empCode != null) {
                            record.setEmpCode(empCode);
                        }
                        if ((record.getEmpName() == null || record.getEmpName().isEmpty()) && empName != null) {
                            record.setEmpName(empName);
                        }
                        if ((record.getCreateUser() == null || record.getCreateUser().isEmpty()) && createUser != null) {
                            record.setCreateUser(createUser);
                        }
                        
                        validRecords.add(record);
                    }
                    
                    // 对于从budgetDetail[*]更新的，必须确保validRecords的数量与原始记录数量一致
                    if (isFromDetailVariables && validRecords.size() != originalSize) {
                        System.err.println("警告：从budgetDetail[*]更新时，有效记录数(" + validRecords.size() + 
                            ")与原始记录数(" + originalSize + ")不一致，跳过更新以避免数据丢失，businessKey=" + businessKey);
                        // 不执行删除和保存操作，避免数据丢失
                        return true;
                    }
                    
                    // 只有在有有效记录时才更新
                    if (!validRecords.isEmpty()) {
                        System.out.println("准备更新预算明细，businessKey=" + businessKey + 
                            "，原始记录数=" + originalSize + "，有效记录数=" + validRecords.size() + 
                            "，是否从budgetDetail[*]更新=" + isFromDetailVariables);
                        
                        // 如果是从budgetDetail[*]更新的，直接更新现有记录（不删除）
                        if (isFromDetailVariables) {
                            System.out.println("从budgetDetail[*]更新，使用直接更新方式，不删除记录，businessKey=" + businessKey);
                            
                            // 检查所有记录是否都有detailId（从数据库加载的记录应该有detailId）
                            boolean allHaveDetailId = true;
                            int missingDetailIdCount = 0;
                            for (BudgetDetailRecord record : validRecords) {
                                if (record.getDetailId() == null) {
                                    allHaveDetailId = false;
                                    missingDetailIdCount++;
                                    System.err.println("警告：记录缺少detailId，无法更新: itemName=" + record.getItemName() + 
                                        ", subjectName=" + record.getSubjectName() + ", amount=" + record.getAmount());
                                }
                            }
                            
                            if (allHaveDetailId) {
                                // 直接更新现有记录
                                System.out.println("开始直接更新预算明细，businessKey=" + businessKey + "，记录数=" + validRecords.size());
                                Result<Boolean> updateResult = budgServiceClient.updateBudgetDetailRecords(validRecords);
                                if (updateResult != null && updateResult.getCode() == 200 
                                    && Boolean.TRUE.equals(updateResult.getData())) {
                                    System.out.println("成功更新预算明细，businessKey=" + businessKey + 
                                        "，共" + validRecords.size() + "条记录");
                                } else {
                                    System.err.println("更新预算明细失败: " + 
                                        (updateResult != null ? updateResult.getMessage() : "updateResult为null"));
                                    throw new RuntimeException("更新预算明细失败，businessKey=" + businessKey);
                                }
                            } else {
                                System.err.println("错误：有" + missingDetailIdCount + "条记录缺少detailId，无法更新，businessKey=" + businessKey);
                                System.err.println("跳过更新操作，避免数据丢失");
                                // 不抛出异常，直接返回，避免删除数据
                                return true;
                            }
                        } else {
                            System.out.println("从budgetDetails JSON更新，使用删除+插入方式，businessKey=" + businessKey);
                            // 对于从budgetDetails JSON更新的情况，使用删除+插入的方式
                            // 先取消旧的预算明细，然后保存新的
                            try {
                                System.out.println("开始删除旧的预算明细，businessKey=" + businessKey);
                                budgServiceClient.cancelBudgetDetailsByBusinessNo(businessKey);
                                System.out.println("成功删除旧的预算明细，businessKey=" + businessKey);
                            } catch (Exception e) {
                                System.err.println("取消旧预算明细失败: " + e.getMessage());
                                // 如果取消失败，不继续保存，避免数据重复
                                throw e;
                            }
                            
                            // 保存新的预算明细
                            System.out.println("开始保存新的预算明细，businessKey=" + businessKey + "，记录数=" + validRecords.size());
                            Result<Boolean> saveResult = budgServiceClient.saveBudgetDetailRecords(validRecords);
                            if (saveResult != null && saveResult.getCode() == 200 
                                && Boolean.TRUE.equals(saveResult.getData())) {
                                System.out.println("成功保存预算明细，businessKey=" + businessKey + 
                                    "，共" + validRecords.size() + "条记录");
                            } else {
                                System.err.println("保存预算明细失败: " + 
                                    (saveResult != null ? saveResult.getMessage() : "saveResult为null"));
                                // 保存失败，但旧的已经删除，这是问题所在
                                // 应该回滚或者重新加载旧的记录
                                throw new RuntimeException("保存预算明细失败，但旧的记录已被删除，businessKey=" + businessKey);
                            }
                        }
                    } else {
                        System.err.println("没有有效的预算明细记录需要保存，跳过更新，businessKey=" + businessKey + 
                            "，原始记录数=" + originalSize + 
                            "，有效记录数=" + validRecords.size() +
                            "，是否从budgetDetail[*]更新=" + isFromDetailVariables);
                        if (isFromDetailVariables && originalSize > 0) {
                            System.err.println("错误：从budgetDetail[*]更新时，原始记录数=" + originalSize + 
                                "，但有效记录数为0，这不应该发生！");
                        }
                        // 如果没有有效记录，不取消旧记录，避免数据丢失
                    }
                } catch (Exception e) {
                    System.err.println("更新预算明细失败: " + e.getMessage());
                    e.printStackTrace();
                    // 发生异常时，不取消旧记录，避免数据丢失
                }
            }
            
            // 处理发票和支付清单更新（仅对报账单）
            if (businessData instanceof CtrlPayout && reimbServiceClient != null) {
                CtrlPayout payout = (CtrlPayout) businessData;
                // 只有billType为PAYOUT的才需要发票和支付清单
                if ("PAYOUT".equals(payout.getBillType()) && payout.getPayoutId() != null) {
                    try {
                        // 收集发票和支付清单相关的变量
                        ProcessVariable invoicesVariable = null;
                        ProcessVariable paymentsVariable = null;
                        java.util.List<ProcessVariable> invoiceVariables = new java.util.ArrayList<>();
                        java.util.List<ProcessVariable> paymentVariables = new java.util.ArrayList<>();
                        
                        for (ProcessVariable variable : variables) {
                            String fieldName = variable.getVariableKey();
                            if (fieldName.equals("invoices")) {
                                invoicesVariable = variable;
                            } else if (fieldName.equals("payments")) {
                                paymentsVariable = variable;
                            } else if (fieldName.startsWith("invoice[")) {
                                invoiceVariables.add(variable);
                            } else if (fieldName.startsWith("payment[")) {
                                paymentVariables.add(variable);
                            }
                        }
                        
                        // 处理发票信息
                        java.util.List<com.hrp.common.entity.CtrlPayoutInvoice> invoices = null;
                        // 优先处理 invoice[*] 变量（用户实际编辑的），如果没有则处理 invoices 变量（JSON格式）
                        if (!invoiceVariables.isEmpty()) {
                            // 方式2：从invoice[*]变量构建发票列表
                            // 从数据库加载现有的发票记录，然后只更新被修改的字段
                            try {
                                Result<com.hrp.common.entity.CtrlPayoutDTO> dtoResult = 
                                    reimbServiceClient.getCtrlPayoutDTOByBillcode(businessKey);
                                if (dtoResult != null && dtoResult.getCode() == 200 
                                        && dtoResult.getData() != null) {
                                    java.util.List<com.hrp.common.entity.CtrlPayoutInvoice> existingInvoices = 
                                        dtoResult.getData().getInvoices();
                                    if (existingInvoices != null && !existingInvoices.isEmpty()) {
                                        // 按索引分组，收集所有被修改的字段
                                        java.util.Map<Integer, java.util.Map<String, String>> invoiceMap = 
                                            new java.util.HashMap<>();
                                        for (ProcessVariable variable : invoiceVariables) {
                                            String fieldName = variable.getVariableKey();
                                            int startIndex = fieldName.indexOf('[');
                                            int endIndex = fieldName.indexOf(']');
                                            if (startIndex > 0 && endIndex > startIndex) {
                                                try {
                                                    int index = Integer.parseInt(fieldName.substring(startIndex + 1, endIndex));
                                                    String field = fieldName.substring(endIndex + 2); // 跳过 "]. "
                                                    invoiceMap.computeIfAbsent(index, k -> new java.util.HashMap<>())
                                                        .put(field, variable.getVariableValue());
                                                } catch (NumberFormatException e) {
                                                    System.err.println("解析发票索引失败: " + fieldName);
                                                }
                                            }
                                        }
                                        
                                        // 更新现有记录（只更新已存在的记录）
                                        for (java.util.Map.Entry<Integer, java.util.Map<String, String>> entry : invoiceMap.entrySet()) {
                                            int index = entry.getKey();
                                            if (index >= existingInvoices.size()) {
                                                continue;
                                            }
                                            com.hrp.common.entity.CtrlPayoutInvoice invoice = existingInvoices.get(index);
                                            java.util.Map<String, String> invoiceFields = entry.getValue();
                                            
                                            if (invoiceFields.containsKey("invoiceCode")) {
                                                invoice.setInvoiceCode(invoiceFields.get("invoiceCode"));
                                            }
                                            if (invoiceFields.containsKey("invoiceNumber")) {
                                                invoice.setInvoiceNumber(invoiceFields.get("invoiceNumber"));
                                            }
                                            if (invoiceFields.containsKey("invoiceDate")) {
                                                try {
                                                    String dateStr = invoiceFields.get("invoiceDate");
                                                    if (dateStr != null && !dateStr.trim().isEmpty()) {
                                                        invoice.setInvoiceDate(java.time.LocalDate.parse(dateStr));
                                                    }
                                                } catch (Exception e) {
                                                    System.err.println("解析发票日期失败: " + invoiceFields.get("invoiceDate"));
                                                }
                                            }
                                            if (invoiceFields.containsKey("invoiceAmount")) {
                                                try {
                                                    String amountStr = invoiceFields.get("invoiceAmount");
                                                    if (amountStr != null && !amountStr.trim().isEmpty()) {
                                                        invoice.setInvoiceAmount(new java.math.BigDecimal(amountStr));
                                                    }
                                                } catch (Exception e) {
                                                    System.err.println("解析发票金额失败: " + invoiceFields.get("invoiceAmount"));
                                                }
                                            }
                                            if (invoiceFields.containsKey("invoiceType")) {
                                                invoice.setInvoiceType(invoiceFields.get("invoiceType"));
                                            }
                                            if (invoiceFields.containsKey("taxAmount")) {
                                                try {
                                                    String taxStr = invoiceFields.get("taxAmount");
                                                    if (taxStr != null && !taxStr.trim().isEmpty()) {
                                                        invoice.setTaxAmount(new java.math.BigDecimal(taxStr));
                                                    }
                                                } catch (Exception e) {
                                                    System.err.println("解析税额失败: " + invoiceFields.get("taxAmount"));
                                                }
                                            }
                                            if (invoiceFields.containsKey("remark")) {
                                                invoice.setRemark(invoiceFields.get("remark"));
                                            }
                                        }
                                        invoices = existingInvoices;
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("从invoice[*]变量更新发票失败: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        // 处理支付清单
                        java.util.List<com.hrp.common.entity.CtrlPayoutPayment> payments = null;
                        // 优先处理 payment[*] 变量（用户实际编辑的），如果没有则处理 payments 变量（JSON格式）
                        if (!paymentVariables.isEmpty()) {
                            // 方式2：从payment[*]变量构建支付清单列表
                            // 从数据库加载现有的支付记录，然后只更新被修改的字段
                            try {
                                Result<com.hrp.common.entity.CtrlPayoutDTO> dtoResult = 
                                    reimbServiceClient.getCtrlPayoutDTOByBillcode(businessKey);
                                if (dtoResult != null && dtoResult.getCode() == 200 
                                        && dtoResult.getData() != null) {
                                    java.util.List<com.hrp.common.entity.CtrlPayoutPayment> existingPayments = 
                                        dtoResult.getData().getPayments();
                                    if (existingPayments != null && !existingPayments.isEmpty()) {
                                        // 按索引分组，收集所有被修改的字段
                                        java.util.Map<Integer, java.util.Map<String, String>> paymentMap = 
                                            new java.util.HashMap<>();
                                        for (ProcessVariable variable : paymentVariables) {
                                            String fieldName = variable.getVariableKey();
                                            int startIndex = fieldName.indexOf('[');
                                            int endIndex = fieldName.indexOf(']');
                                            if (startIndex > 0 && endIndex > startIndex) {
                                                try {
                                                    int index = Integer.parseInt(fieldName.substring(startIndex + 1, endIndex));
                                                    String field = fieldName.substring(endIndex + 2); // 跳过 "]. "
                                                    paymentMap.computeIfAbsent(index, k -> new java.util.HashMap<>())
                                                        .put(field, variable.getVariableValue());
                                                } catch (NumberFormatException e) {
                                                    System.err.println("解析支付索引失败: " + fieldName);
                                                }
                                            }
                                        }
                                        
                                        // 更新现有记录（只更新已存在的记录）
                                        for (java.util.Map.Entry<Integer, java.util.Map<String, String>> entry : paymentMap.entrySet()) {
                                            int index = entry.getKey();
                                            if (index >= existingPayments.size()) {
                                                continue;
                                            }
                                            com.hrp.common.entity.CtrlPayoutPayment payment = existingPayments.get(index);
                                            java.util.Map<String, String> paymentFields = entry.getValue();
                                            
                                            if (paymentFields.containsKey("paymentObject")) {
                                                payment.setPaymentObject(paymentFields.get("paymentObject"));
                                            }
                                            if (paymentFields.containsKey("paymentMethod")) {
                                                payment.setPaymentMethod(paymentFields.get("paymentMethod"));
                                            }
                                            if (paymentFields.containsKey("paymentAmount")) {
                                                try {
                                                    String amountStr = paymentFields.get("paymentAmount");
                                                    if (amountStr != null && !amountStr.trim().isEmpty()) {
                                                        payment.setPaymentAmount(new java.math.BigDecimal(amountStr));
                                                    }
                                                } catch (Exception e) {
                                                    System.err.println("解析支付金额失败: " + paymentFields.get("paymentAmount"));
                                                }
                                            }
                                            if (paymentFields.containsKey("bankName")) {
                                                payment.setBankName(paymentFields.get("bankName"));
                                            }
                                            if (paymentFields.containsKey("bankAccount")) {
                                                payment.setBankAccount(paymentFields.get("bankAccount"));
                                            }
                                            if (paymentFields.containsKey("accountName")) {
                                                payment.setAccountName(paymentFields.get("accountName"));
                                            }
                                            if (paymentFields.containsKey("remark")) {
                                                payment.setRemark(paymentFields.get("remark"));
                                            }
                                        }
                                        payments = existingPayments;
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("从payment[*]变量更新支付清单失败: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else if (paymentsVariable != null) {
                            // 方式2：从JSON格式的payments变量解析（当没有payment[*]变量时）
                            if (paymentsVariable.getVariableValue() != null 
                                    && !paymentsVariable.getVariableValue().trim().isEmpty()) {
                                try {
                                    String paymentsJson = paymentsVariable.getVariableValue();
                                    payments = objectMapper.readValue(paymentsJson, 
                                        objectMapper.getTypeFactory().constructCollectionType(
                                            java.util.List.class, com.hrp.common.entity.CtrlPayoutPayment.class));
                                } catch (Exception e) {
                                    System.err.println("解析支付清单JSON失败: " + e.getMessage());
                                }
                            } else {
                                // 如果 payments 变量存在但值为空，设置为空列表（表示删除所有支付清单）
                                payments = new java.util.ArrayList<>();
                            }
                        }
                        
                        // 如果发票或支付清单有更新，调用更新接口
                        // 只有在 invoices 或 payments 不为 null 时才调用（表示有明确的更新操作）
                        if (invoices != null || payments != null) {
                            com.hrp.common.entity.CtrlPayoutDTO dto = new com.hrp.common.entity.CtrlPayoutDTO();
                            // 如果从 invoices/payments 变量更新，使用解析后的列表（可能为空列表，表示删除所有）
                            // 如果从 invoice[*]/payment[*] 更新，使用更新后的列表
                            dto.setInvoices(invoices);
                            dto.setPayments(payments);
                            Result<Void> updateResult = reimbServiceClient.updateInvoicesAndPayments(payout.getPayoutId(), dto);
                            if (updateResult == null || updateResult.getCode() != 200) {
                                System.err.println("更新发票和支付清单失败: " + 
                                    (updateResult != null ? updateResult.getMessage() : "updateResult为null"));
                                throw new RuntimeException("更新发票和支付清单失败");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("更新发票和支付清单失败: " + e.getMessage());
                        e.printStackTrace();
                        // 不抛出异常，继续处理其他字段
                    }
                }
            }
            
            // 处理资产采购申请明细表更新（ASSET_TYPE、ASSET_PURCHASE_APPLY或ASSET类型）
            // 先收集明细表的更新，然后与主表一起更新
            java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail> assetDetailsForUpdate = null;
            boolean hasAssetDetailUpdate = false;
            
            if (businessType != null && (businessType.equals("ASSET_TYPE") || businessType.equals("ASSET_PURCHASE_APPLY") 
                || businessType.equals("ASSET") || businessType.contains("ASSET_PURCHASE")) 
                && assetServiceClient != null && businessData instanceof AssetPurchaseApplyMain) {
                try {
                    // 收集资产明细相关的变量
                    ProcessVariable assetDetailsVariable = null;
                    java.util.List<ProcessVariable> assetDetailVariables = new java.util.ArrayList<>();
                    
                    for (ProcessVariable variable : variables) {
                        String varKey = variable.getVariableKey();
                        if (varKey.equals("assetDetails")) {
                            assetDetailsVariable = variable;
                        } else if (varKey.startsWith("assetDetail[")) {
                            assetDetailVariables.add(variable);
                            hasAssetDetailUpdate = true;
                        }
                    }
                    
                    // 处理资产明细更新
                    // 方式1：如果存在assetDetails变量（JSON格式），直接使用
                    // 方式2：如果存在assetDetail[*]变量，从这些变量构建明细列表
                    if (!assetDetailVariables.isEmpty()) {
                        // 方式2：从assetDetail[*]变量更新明细列表（优先处理，因为是更新操作）
                        hasAssetDetailUpdate = true;
                        try {
                            // 从数据库加载现有的明细记录
                            Result<java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail>> existingResult = 
                                assetServiceClient.getAssetPurchaseApplyDetailsByNo(businessKey);
                            java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail> existingDetails = null;
                            if (existingResult != null && existingResult.getCode() == 200 
                                && existingResult.getData() != null) {
                                existingDetails = existingResult.getData();
                            }
                            
                            if (existingDetails == null) {
                                existingDetails = new java.util.ArrayList<>();
                            }
                            
                            if (!existingDetails.isEmpty()) {
                                // 按索引分组，收集所有被修改的字段
                                java.util.Map<Integer, java.util.Map<String, String>> detailMap = new java.util.HashMap<>();
                                for (ProcessVariable variable : assetDetailVariables) {
                                    String varKey = variable.getVariableKey();
                                    int startIndex = varKey.indexOf('[');
                                    int endIndex = varKey.indexOf(']');
                                    if (startIndex > 0 && endIndex > startIndex) {
                                        try {
                                            int index = Integer.parseInt(varKey.substring(startIndex + 1, endIndex));
                                            String field = varKey.substring(endIndex + 2); // 跳过 "]. "
                                            detailMap.computeIfAbsent(index, k -> new java.util.HashMap<>())
                                                .put(field, variable.getVariableValue());
                                        } catch (NumberFormatException e) {
                                            System.err.println("解析资产明细索引失败: " + varKey);
                                        }
                                    }
                                }
                                
                                // 更新现有记录（只更新已存在的记录，不创建新记录）
                                for (java.util.Map.Entry<Integer, java.util.Map<String, String>> entry : detailMap.entrySet()) {
                                    int index = entry.getKey();
                                    if (index >= existingDetails.size()) {
                                        continue;
                                    }
                                    com.hrp.common.entity.AssetPurchaseApplyDetail detail = existingDetails.get(index);
                                    java.util.Map<String, String> detailFields = entry.getValue();
                                    
                                    // 只更新被修改的字段（不更新id和applyId，这些字段应该保持不变）
                                    if (detailFields.containsKey("assetCode")) {
                                        detail.setAssetCode(detailFields.get("assetCode"));
                                    }
                                    if (detailFields.containsKey("assetName")) {
                                        detail.setAssetName(detailFields.get("assetName"));
                                    }
                                    if (detailFields.containsKey("spec")) {
                                        detail.setSpec(detailFields.get("spec"));
                                    }
                                    if (detailFields.containsKey("manufacturer")) {
                                        detail.setManufacturer(detailFields.get("manufacturer"));
                                    }
                                    if (detailFields.containsKey("unit")) {
                                        detail.setUnit(detailFields.get("unit"));
                                    }
                                    if (detailFields.containsKey("applyQuantity")) {
                                        try {
                                            String quantityStr = detailFields.get("applyQuantity");
                                            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                                                detail.setApplyQuantity(Integer.parseInt(quantityStr));
                                                // 如果数量变化，重新计算总价
                                                if (detail.getPrice() != null) {
                                                    detail.setTotalPrice(detail.getPrice().multiply(new java.math.BigDecimal(detail.getApplyQuantity())));
                                                }
                                            }
                                        } catch (Exception e) {
                                            System.err.println("解析申请数量失败: " + detailFields.get("applyQuantity"));
                                        }
                                    }
                                    if (detailFields.containsKey("price")) {
                                        try {
                                            String priceStr = detailFields.get("price");
                                            if (priceStr != null && !priceStr.trim().isEmpty()) {
                                                detail.setPrice(new java.math.BigDecimal(priceStr));
                                                // 如果单价变化，重新计算总价
                                                if (detail.getApplyQuantity() != null) {
                                                    detail.setTotalPrice(detail.getPrice().multiply(new java.math.BigDecimal(detail.getApplyQuantity())));
                                                }
                                            }
                                        } catch (Exception e) {
                                            System.err.println("解析单价失败: " + detailFields.get("price"));
                                        }
                                    }
                                    if (detailFields.containsKey("totalPrice")) {
                                        try {
                                            String totalPriceStr = detailFields.get("totalPrice");
                                            if (totalPriceStr != null && !totalPriceStr.trim().isEmpty()) {
                                                detail.setTotalPrice(new java.math.BigDecimal(totalPriceStr));
                                            }
                                        } catch (Exception e) {
                                            System.err.println("解析总价失败: " + detailFields.get("totalPrice"));
                                        }
                                    }
                                    if (detailFields.containsKey("remark")) {
                                        detail.setRemark(detailFields.get("remark"));
                                    }
                                }
                                
                                assetDetailsForUpdate = existingDetails;
                                System.out.println("从assetDetail[*]变量构建资产明细列表，businessKey=" + businessKey + 
                                    "，记录数=" + assetDetailsForUpdate.size());
                            }
                        } catch (Exception e) {
                            System.err.println("从assetDetail[*]变量更新资产明细失败: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else if (assetDetailsVariable != null && assetDetailsVariable.getVariableValue() != null 
                        && !assetDetailsVariable.getVariableValue().trim().isEmpty()) {
                        // 方式1：从JSON字符串解析（只有在没有assetDetail[*]变量时才处理）
                        hasAssetDetailUpdate = true;
                        try {
                            String assetDetailsJson = assetDetailsVariable.getVariableValue();
                            assetDetailsForUpdate = objectMapper.readValue(assetDetailsJson, 
                                objectMapper.getTypeFactory().constructCollectionType(
                                    java.util.List.class, com.hrp.common.entity.AssetPurchaseApplyDetail.class));
                            System.out.println("从assetDetails JSON解析资产明细列表，businessKey=" + businessKey + 
                                "，记录数=" + (assetDetailsForUpdate != null ? assetDetailsForUpdate.size() : 0));
                        } catch (Exception e) {
                            System.err.println("解析assetDetails JSON失败: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("处理资产明细更新失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            for (ProcessVariable variable : variables) {
                String fieldName = variable.getVariableKey();
                String valueStr = variable.getVariableValue();
                
                // 跳过预算明细、发票、支付清单和资产明细相关的变量（已单独处理）
                if (fieldName.equals("budgetDetails") || fieldName.startsWith("budgetDetail[")
                        || fieldName.equals("invoices") || fieldName.equals("payments")
                        || fieldName.startsWith("invoice[") || fieldName.startsWith("payment[")
                        || fieldName.equals("assetDetails") || fieldName.startsWith("assetDetail[")) {
                    continue;
                }
                
                // 跳过合同特有的非持久化字段（这些字段来自关联表，不应该直接更新）
                if (businessData instanceof PactMain) {
                    if (fieldName.equals("empName") || fieldName.equals("deptName") || fieldName.equals("empPhone")) {
                        continue;
                    }
                }
                
                java.lang.reflect.Field field = fieldMap.get(fieldName);
                if (field != null) {
                    try {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        Object value = null;
                        
                        // 根据字段类型转换值
                        if (valueStr != null && !valueStr.trim().isEmpty()) {
                            if (fieldType == String.class) {
                                value = valueStr;
                            } else if (fieldType == Integer.class || fieldType == int.class) {
                                value = Integer.parseInt(valueStr);
                            } else if (fieldType == Long.class || fieldType == long.class) {
                                value = Long.parseLong(valueStr);
                            } else if (fieldType == java.math.BigDecimal.class) {
                                value = new java.math.BigDecimal(valueStr);
                            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                                value = Boolean.parseBoolean(valueStr);
                            } else if (fieldType == java.time.LocalDateTime.class) {
                                value = java.time.LocalDateTime.parse(valueStr);
                            } else if (fieldType == java.time.LocalDate.class) {
                                value = java.time.LocalDate.parse(valueStr);
                            }
                        }
                        
                        field.set(businessData, value);
                        needUpdate = true;
                    } catch (Exception e) {
                        System.err.println("更新字段失败: " + fieldName + ", " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    // 字段不存在于业务对象中，可能是业务逻辑字段，忽略
                    System.out.println("警告：字段不存在于业务对象中，跳过: " + fieldName + ", 业务类型: " + businessType);
                }
            }
            
            if (needUpdate) {
                // 根据业务数据类型调用对应的更新方法
                if (businessData instanceof BudgetApply && budgServiceClient != null) {
                    Result<BudgetApply> updateResult = budgServiceClient.updateBudgetApply((BudgetApply) businessData);
                    return updateResult != null && updateResult.getCode() == 200;
                } else if (businessData instanceof CtrlPayout && reimbServiceClient != null) {
                    Result<CtrlPayout> updateResult = reimbServiceClient.updateCtrlPayout((CtrlPayout) businessData);
                    return updateResult != null && updateResult.getCode() == 200;
                } else if (businessData instanceof com.hrp.common.entity.HrApply && hrServiceClient != null) {
                    Result<com.hrp.common.entity.HrApply> updateResult = hrServiceClient.updateHrApply((com.hrp.common.entity.HrApply) businessData);
                    return updateResult != null && updateResult.getCode() == 200;
                } else if (businessData instanceof PactMain && contractServiceClient != null) {
                    Result<Void> updateResult = contractServiceClient.updatePactMain((PactMain) businessData);
                    return updateResult != null && updateResult.getCode() == 200;
                } else if (businessData instanceof AssetPurchaseApplyMain && assetServiceClient != null) {
                    // 对于资产采购申请，需要调用资产服务的更新接口
                    // 由于资产服务的update方法需要主表和明细表一起更新，我们需要：
                    // 1. 使用更新后的主表字段（已通过反射更新到businessData对象中）
                    // 2. 使用收集到的明细表数据（如果有更新）或现有的明细表数据（如果没有更新）
                    try {
                        AssetPurchaseApplyMain apply = (AssetPurchaseApplyMain) businessData;
                        
                        // 如果明细表没有更新操作，获取现有的明细表数据
                        java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail> details = assetDetailsForUpdate;
                        if (!hasAssetDetailUpdate || details == null) {
                            Result<java.util.List<com.hrp.common.entity.AssetPurchaseApplyDetail>> detailsResult = 
                                assetServiceClient.getAssetPurchaseApplyDetailsByNo(businessKey);
                            if (detailsResult != null && detailsResult.getCode() == 200 && detailsResult.getData() != null) {
                                details = detailsResult.getData();
                            }
                            if (details == null) {
                                details = new java.util.ArrayList<>();
                            }
                        }
                        
                        // 确保每个明细记录的applyId已设置
                        if (details != null && apply.getId() != null) {
                            for (com.hrp.common.entity.AssetPurchaseApplyDetail detail : details) {
                                if (detail.getApplyId() == null) {
                                    detail.setApplyId(apply.getId());
                                }
                            }
                        }
                        
                        // 将主表和明细表数据转换为Map，调用更新接口
                        java.util.Map<String, Object> updateRequest = new java.util.HashMap<>();
                        // 主表字段（使用反射更新后的值）
                        updateRequest.put("id", apply.getId());
                        updateRequest.put("applyNo", apply.getApplyNo());
                        updateRequest.put("applyEmpId", apply.getApplyEmpId());
                        updateRequest.put("applyDeptId", apply.getApplyDeptId());
                        updateRequest.put("applyTime", apply.getApplyTime());
                        updateRequest.put("applyMoney", apply.getApplyMoney());
                        updateRequest.put("applyReason", apply.getApplyReason());
                        updateRequest.put("demandDate", apply.getDemandDate());
                        updateRequest.put("status", apply.getStatus());
                        updateRequest.put("templateConfigId", apply.getTemplateConfigId());
                        updateRequest.put("mainAttachId", apply.getMainAttachId());
                        updateRequest.put("applyReason", apply.getApplyReason());
                        // 将明细表转换为Map列表（包含id字段，用于更新时识别记录）
                        java.util.List<java.util.Map<String, Object>> detailsList = new java.util.ArrayList<>();
                        if (details != null) {
                            for (com.hrp.common.entity.AssetPurchaseApplyDetail detail : details) {
                                java.util.Map<String, Object> detailMap = new java.util.HashMap<>();
                                // 包含id字段，用于更新时识别现有记录
                                if (detail.getId() != null) {
                                    detailMap.put("id", detail.getId());
                                }
                                if (detail.getApplyId() != null) {
                                    detailMap.put("applyId", detail.getApplyId());
                                }
                                detailMap.put("assetCode", detail.getAssetCode());
                                detailMap.put("assetName", detail.getAssetName());
                                detailMap.put("spec", detail.getSpec());
                                detailMap.put("manufacturer", detail.getManufacturer());
                                detailMap.put("unit", detail.getUnit());
                                detailMap.put("applyQuantity", detail.getApplyQuantity());
                                detailMap.put("price", detail.getPrice());
                                detailMap.put("totalPrice", detail.getTotalPrice());
                                detailMap.put("remark", detail.getRemark());
                                detailsList.add(detailMap);
                            }
                        }
                        updateRequest.put("details", detailsList);
                        
                        // 调用资产服务的更新接口（主表和明细表一起更新）
                        System.out.println("调用资产服务更新接口，businessKey=" + businessKey + 
                            "，主表字段已更新，明细表记录数=" + detailsList.size());
                        Result<AssetPurchaseApplyMain> updateResult = assetServiceClient.updateAssetPurchaseApply(updateRequest);
                        if (updateResult != null && updateResult.getCode() == 200) {
                            System.out.println("成功更新资产采购申请主表和明细表，businessKey=" + businessKey);
                            return true;
                        } else {
                            System.err.println("更新资产采购申请失败: " + 
                                (updateResult != null ? updateResult.getMessage() : "updateResult为null"));
                            return false;
                        }
                    } catch (Exception e) {
                        System.err.println("更新资产采购申请失败: " + e.getMessage());
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("更新业务表变量失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean restoreReturnedTasks(String taskKey) {
        if (taskKey == null || taskKey.trim().isEmpty()) {
            System.err.println("任务KEY为空，无法恢复退回任务");
            return false;
        }
        
        try {
            // 查找所有退回状态且退回类型为RETURN_TO_CURRENT的任务
            List<ProcessTask> allTasks = processTaskMapper.selectByTaskKey(taskKey);
            int restoredCount = 0;
            
            for (ProcessTask task : allTasks) {
                if ("RETURNED".equals(task.getTaskStatus()) && "RETURN_TO_CURRENT".equals(task.getReturnType())) {
                    task.setTaskStatus("PENDING");
                    task.setUpdateTime(LocalDateTime.now());
                    processTaskMapper.updateById(task);
                    restoredCount++;
                    System.out.println("恢复退回任务为待审批状态: taskId=" + task.getTaskId() + ", taskName=" + task.getTaskName());
                }
            }
            
            if (restoredCount > 0) {
                System.out.println("成功恢复 " + restoredCount + " 个退回任务为待审批状态: taskKey=" + taskKey);
            } else {
                System.out.println("未找到需要恢复的退回任务: taskKey=" + taskKey);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("恢复退回任务失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}



