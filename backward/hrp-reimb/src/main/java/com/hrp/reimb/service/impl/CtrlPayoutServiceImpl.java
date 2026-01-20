package com.hrp.reimb.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.common.entity.*;
import com.hrp.reimb.feign.AuthServiceClient;
import com.hrp.reimb.feign.BudgServiceClient;
import com.hrp.reimb.mapper.CtrlPayoutMapper;
import com.hrp.reimb.service.CtrlPayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CtrlPayoutServiceImpl implements CtrlPayoutService {

    @Autowired
    private CtrlPayoutMapper ctrlPayoutMapper;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;
    
    @Autowired(required = false)
    private BudgServiceClient budgServiceClient;
    
    @Autowired(required = false)
    private com.hrp.reimb.service.CtrlPayoutInvoiceService ctrlPayoutInvoiceService;
    
    @Autowired(required = false)
    private com.hrp.reimb.service.CtrlPayoutPaymentService ctrlPayoutPaymentService;

    @Override
    public List<CtrlPayout> getMyPayouts(Long empId) {
        return ctrlPayoutMapper.selectByEmpId(empId);
    }

    @Override
    public PageResult<CtrlPayout> getMyPayoutsPage(Long empId, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<CtrlPayout> list = ctrlPayoutMapper.selectByEmpId(empId);
        PageInfo<CtrlPayout> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<CtrlPayout> getPayoutsByStatus(String status) {
        return ctrlPayoutMapper.selectByStatus(status);
    }

    @Override
    public PageResult<CtrlPayout> getPayoutsByStatusPage(String status, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<CtrlPayout> list = ctrlPayoutMapper.selectByStatus(status);
        PageInfo<CtrlPayout> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
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
            // 生成编号：我的申请SQD年月日0001，我的报账BZD年月日0001
            ctrlPayout.setPayoutBillcode(generateBillcode(ctrlPayout.getBillType()));
        }
        if (ctrlPayout.getBillType() == null || ctrlPayout.getBillType().isEmpty()) {
            ctrlPayout.setBillType("APPLY"); // 默认为申请单
        }
        if (ctrlPayout.getApplyDate() == null) {
            ctrlPayout.setApplyDate(LocalDate.now());
        }
        if (ctrlPayout.getStatus() == null || ctrlPayout.getStatus().isEmpty()) {
            ctrlPayout.setStatus("DRAFT");
        }
        return ctrlPayoutMapper.insert(ctrlPayout) > 0;
    }
    
    /**
     * 生成报账单号
     * 我的申请：SQD年月日0001
     * 我的报账：BZD年月日0001
     */
    private String generateBillcode(String billType) {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        
        // 根据单据类型选择前缀
        String prefix;
        if ("APPLY".equals(billType)) {
            prefix = "SQD" + year + month + day;
        } else {
            prefix = "BZD" + year + month + day;
        }
        
        // 查询当前日期的最大单号
        String maxBillcode = ctrlPayoutMapper.selectMaxBillcodeByPrefix(prefix);
        
        int sequence = 1;
        if (maxBillcode != null && maxBillcode.startsWith(prefix)) {
            // 提取序号部分（最后4位）
            try {
                String seqStr = maxBillcode.substring(prefix.length());
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 格式化为4位序号
        return prefix + String.format("%04d", sequence);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(CtrlPayout ctrlPayout, List<Map<String, Object>> budgetDetails) {
        // 如果没有mainAttachId，生成一个时间戳作为主附件ID
        if (ctrlPayout.getMainAttachId() == null || ctrlPayout.getMainAttachId().isEmpty()) {
            ctrlPayout.setMainAttachId(String.valueOf(System.currentTimeMillis()));
        }
        // 先保存主表
        boolean success = save(ctrlPayout);
        if (!success) {
            return false;
        }
        
        // 保存预算明细（主表保存后，payoutId已生成，可以正确设置businessId）
        if (budgetDetails != null && !budgetDetails.isEmpty() && budgServiceClient != null) {
            try {
                // 重新设置businessId，因为保存后已生成payoutId
                List<BudgetDetailRecord> records = convertToBudgetDetailRecords(budgetDetails, ctrlPayout);
                // 更新所有记录的businessId为刚生成的payoutId
                for (BudgetDetailRecord record : records) {
                    record.setBusinessId(ctrlPayout.getPayoutId());
                }
                Result<Boolean> result = budgServiceClient.saveBudgetDetailRecords(records);
                if (result == null || result.getCode() != 200 || !Boolean.TRUE.equals(result.getData())) {
                    String errorMsg = "保存预算明细失败: " + (result != null ? result.getMessage() : "result is null");
                    System.err.println(errorMsg);
                    // 抛出运行时异常，让事务回滚
                    throw new RuntimeException(errorMsg);
                }
            } catch (RuntimeException e) {
                // 重新抛出运行时异常，让事务回滚
                throw e;
            } catch (Exception e) {
                // 包装为运行时异常，让事务回滚
                System.err.println("保存预算明细异常: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("保存预算明细异常: " + e.getMessage(), e);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CtrlPayout saveFull(CtrlPayout ctrlPayout, List<Map<String, Object>> budgetDetails,
                               List<com.hrp.common.entity.CtrlPayoutInvoice> invoices,
                               List<com.hrp.common.entity.CtrlPayoutPayment> payments) {
        // 如果没有mainAttachId，生成一个时间戳作为主附件ID
        if (ctrlPayout.getMainAttachId() == null || ctrlPayout.getMainAttachId().isEmpty()) {
            ctrlPayout.setMainAttachId(String.valueOf(System.currentTimeMillis()));
        }
        
        // 1. 先保存主表（本地事务）
        boolean mainSuccess = save(ctrlPayout);
        if (!mainSuccess) {
            throw new RuntimeException("保存主表失败");
        }
        
        // 2. 保存发票（本地事务，在同一事务中）
        if (invoices != null && !invoices.isEmpty() && ctrlPayoutInvoiceService != null) {
            for (com.hrp.common.entity.CtrlPayoutInvoice invoice : invoices) {
                invoice.setPayoutId(ctrlPayout.getPayoutId());
            }
            if (!ctrlPayoutInvoiceService.saveBatch(invoices)) {
                throw new RuntimeException("保存发票失败");
            }
        }
        
        // 3. 保存支付清单（本地事务，在同一事务中）
        if (payments != null && !payments.isEmpty() && ctrlPayoutPaymentService != null) {
            for (com.hrp.common.entity.CtrlPayoutPayment payment : payments) {
                payment.setPayoutId(ctrlPayout.getPayoutId());
            }
            if (!ctrlPayoutPaymentService.saveBatch(payments)) {
                throw new RuntimeException("保存支付清单失败");
            }
        }
        
        // 4. 最后保存预算明细（跨服务调用，如果前面的操作都成功，再调用远程服务）
        // 这样即使预算明细保存失败，主表、发票、支付清单都已经在同一事务中保存，可以一起回滚
        // 如果预算明细保存成功但后续出错，需要通过补偿机制删除预算明细
        String businessNo = ctrlPayout.getPayoutBillcode();
        boolean budgetDetailsSaved = false;
        
        if (budgetDetails != null && !budgetDetails.isEmpty() && budgServiceClient != null) {
            try {
                List<BudgetDetailRecord> records = convertToBudgetDetailRecords(budgetDetails, ctrlPayout);
                // 更新所有记录的businessId为刚生成的payoutId
                for (BudgetDetailRecord record : records) {
                    record.setBusinessId(ctrlPayout.getPayoutId());
                }
                Result<Boolean> result = budgServiceClient.saveBudgetDetailRecords(records);
                if (result == null || result.getCode() != 200 || !Boolean.TRUE.equals(result.getData())) {
                    String errorMsg = "保存预算明细失败: " + (result != null ? result.getMessage() : "result is null");
                    System.err.println(errorMsg);
                    // 预算明细保存失败，本地事务会回滚，主表、发票、支付清单都会回滚
                    throw new RuntimeException(errorMsg);
                }
                budgetDetailsSaved = true;
            } catch (Exception e) {
                System.err.println("保存预算明细异常: " + e.getMessage());
                e.printStackTrace();
                // 抛出异常，让本地事务回滚
                throw new RuntimeException("保存预算明细异常: " + e.getMessage(), e);
            }
        }
        
        return ctrlPayout;
    }
    
    @Override
    @Transactional
    public boolean update(CtrlPayout ctrlPayout) {
        return ctrlPayoutMapper.updateById(ctrlPayout) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(CtrlPayout ctrlPayout, List<Map<String, Object>> budgetDetails) {
        // 先更新主表
        boolean success = update(ctrlPayout);
        if (!success) {
            return false;
        }
        
        // 只有当budgetDetails不为null且不为空时，才执行删除+插入操作
        // 如果budgetDetails为null，说明只是更新主表字段，不应该删除预算明细
        if (budgetDetails != null && !budgetDetails.isEmpty() && budgServiceClient != null) {
            // 取消旧的预算明细
            if (ctrlPayout.getPayoutBillcode() != null && !ctrlPayout.getPayoutBillcode().isEmpty()) {
                try {
                    budgServiceClient.cancelBudgetDetailsByBusinessNo(ctrlPayout.getPayoutBillcode());
                } catch (Exception e) {
                    System.err.println("取消旧预算明细异常: " + e.getMessage());
                }
            }
            
            // 保存新的预算明细
            try {
                List<BudgetDetailRecord> records = convertToBudgetDetailRecords(budgetDetails, ctrlPayout);
                Result<Boolean> result = budgServiceClient.saveBudgetDetailRecords(records);
                if (result == null || result.getCode() != 200 || !Boolean.TRUE.equals(result.getData())) {
                    String errorMsg = "保存预算明细失败: " + (result != null ? result.getMessage() : "result is null");
                    System.err.println(errorMsg);
                    throw new RuntimeException(errorMsg); // 抛出异常，让事务回滚
                }
            } catch (Exception e) {
                System.err.println("保存预算明细异常: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("保存预算明细异常: " + e.getMessage(), e); // 重新抛出异常，让事务回滚
            }
        }
        // 如果budgetDetails为null或空，说明只是更新主表，不处理预算明细，直接返回成功
        
        return true;
    }
    
    /**
     * 将Map列表转换为BudgetDetailRecord列表
     */
    private List<BudgetDetailRecord> convertToBudgetDetailRecords(List<Map<String, Object>> budgetDetails, CtrlPayout ctrlPayout) {
        List<BudgetDetailRecord> records = new ArrayList<>();
        String businessNo = ctrlPayout.getPayoutBillcode();
        
        // 验证businessNo，如果为空则抛出异常
        if (businessNo == null || businessNo.trim().isEmpty()) {
            throw new IllegalArgumentException("业务单号不能为空，请先保存主表");
        }
        
        // 获取部门信息（deptCode和deptName）
        String deptCode = null;
        String deptName = null;
        if (ctrlPayout.getDeptId() != null && authServiceClient != null) {
            try {
                // 通过员工账号获取用户信息（用户信息中包含部门信息）
                if (ctrlPayout.getEmpCode() != null && !ctrlPayout.getEmpCode().isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(ctrlPayout.getEmpCode());
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        deptCode = user.getDeptCode();
                        deptName = user.getDeptName();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取部门信息失败: " + e.getMessage());
            }
        }
        
        // 如果deptName为空，尝试使用ctrlPayout的deptName
        if (deptName == null || deptName.isEmpty()) {
            deptName = ctrlPayout.getDeptName();
        }
        
        // 获取创建用户（优先使用CtrlPayout的createUser，否则使用empCode）
        String createUser = ctrlPayout.getCreateUser();
        if (createUser == null || createUser.isEmpty()) {
            createUser = ctrlPayout.getEmpCode();
        }
        if (createUser == null || createUser.isEmpty()) {
            createUser = "SYSTEM"; // 默认值
        }
        
        for (Map<String, Object> detail : budgetDetails) {
            BudgetDetailRecord record = new BudgetDetailRecord();
            
            // 验证必填字段
            if (detail.get("subjectId") == null) {
                throw new IllegalArgumentException("预算主体ID不能为空");
            }
            if (detail.get("itemId") == null) {
                throw new IllegalArgumentException("预算项目ID不能为空");
            }
            if (detail.get("amount") == null) {
                throw new IllegalArgumentException("金额不能为空");
            }
            
            record.setSubjectId(Long.valueOf(detail.get("subjectId").toString()));
            // 设置subjectCode，如果前端没有传递，尝试从budgetId获取
            if (detail.get("subjectCode") != null) {
                record.setSubjectCode(detail.get("subjectCode").toString());
            }
            if (detail.get("subjectName") != null) record.setSubjectName(detail.get("subjectName").toString());
            record.setItemId(Long.valueOf(detail.get("itemId").toString()));
            if (detail.get("itemCode") != null) record.setItemCode(detail.get("itemCode").toString());
            if (detail.get("itemName") != null) record.setItemName(detail.get("itemName").toString());
            // 设置budgetYear，如果前端没有传递，尝试从budgetId获取
            if (detail.get("budgetYear") != null) {
                record.setBudgetYear(detail.get("budgetYear").toString());
            } else if (detail.get("budgetId") != null) {
                // 如果前端没有传递budgetYear，但有budgetId，可以通过budgetId查询预算信息获取budgetYear
                // 但由于需要通过Feign调用，这里暂时不处理，要求前端必须传递budgetYear
                // 如果前端确实没有传递，这里会保持为null，数据库可能会报错（如果字段不允许为null）
            }
            record.setAmount(new BigDecimal(detail.get("amount").toString()));
            
            // 设置明细类型：根据billType判断，APPLY-申请单，PAYOUT-报账单
            String billType = ctrlPayout.getBillType();
            if (billType != null) {
                record.setDetailType(billType); // APPLY 或 PAYOUT
            } else {
                record.setDetailType("PAYOUT"); // 默认报账单
            }
            
            record.setBusinessNo(businessNo);
            // business_id在保存主表后会生成payoutId
            record.setBusinessId(ctrlPayout.getPayoutId() != null ? ctrlPayout.getPayoutId() : 0L);
            record.setDeptId(ctrlPayout.getDeptId());
            record.setDeptCode(deptCode);
            record.setDeptName(deptName);
            record.setEmpId(ctrlPayout.getEmpId());
            record.setEmpCode(ctrlPayout.getEmpCode());
            record.setEmpName(ctrlPayout.getEmpName());
            record.setCreateUser(createUser);
            records.add(record);
        }
        
        return records;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(id);
        if (payout == null) {
            return false;
        }
        // 只有草稿、撤回或拒绝状态才能删除
        if (!"DRAFT".equals(payout.getStatus()) && !"WITHDRAWN".equals(payout.getStatus()) && !"REJECTED".equals(payout.getStatus())) {
            return false;
        }
        
        // 删除关联的明细记录（如果有）
        if (payout.getPayoutBillcode() != null && !payout.getPayoutBillcode().isEmpty() && budgServiceClient != null) {
            try {
                Result<Boolean> result = budgServiceClient.cancelBudgetDetailsByBusinessNo(payout.getPayoutBillcode());
                if (result == null || result.getCode() != 200 || !Boolean.TRUE.equals(result.getData())) {
                    System.err.println("删除预算明细失败（通过business_no）");
                }
            } catch (Exception e) {
                System.err.println("删除预算明细异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 删除附件记录和文件（通过Feign调用auth服务）
        // 优先使用mainAttachId作为businessId删除附件（因为附件是使用mainAttachId存储的）
        // 如果没有mainAttachId，再尝试使用payoutBillcode（兼容旧数据）
        if (authServiceClient != null) {
            try {
                String businessId = null;
                if (payout.getMainAttachId() != null && !payout.getMainAttachId().isEmpty()) {
                    // 优先使用mainAttachId（附件实际使用的businessId）
                    businessId = payout.getMainAttachId();
                    System.out.println("删除报账申请的附件，使用mainAttachId=" + businessId);
                } else if (payout.getPayoutBillcode() != null && !payout.getPayoutBillcode().isEmpty()) {
                    // 如果没有mainAttachId，使用payoutBillcode（兼容旧数据）
                    businessId = payout.getPayoutBillcode();
                    System.out.println("删除报账申请的附件，使用payoutBillcode=" + businessId);
                }
                
                if (businessId != null && !businessId.isEmpty()) {
                    String businessType = "APPLY".equals(payout.getBillType()) ? "PAYOUT_APPLY" : "PAYOUT";
                    Result<Void> deleteResult = authServiceClient.deleteAttachmentsByBusiness(businessType, businessId);
                    if (deleteResult != null && deleteResult.getCode() == 200) {
                        System.out.println("附件删除成功，businessId=" + businessId);
                    } else {
                        System.err.println("附件删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                    }
                }
            } catch (Exception e) {
                System.err.println("删除附件失败: " + e.getMessage());
                e.printStackTrace();
                // 附件删除失败，不影响申请删除，只记录错误
            }
        }
        
        // 删除流程任务记录（审批记录）
        // 即使撤回时已经删除过，这里也再次删除，确保数据一致性（防止撤回时删除失败的情况）
        if (payout.getPayoutBillcode() != null && !payout.getPayoutBillcode().isEmpty() && authServiceClient != null) {
            try {
                System.out.println("删除报账申请的流程任务记录（审批记录），payoutBillcode=" + payout.getPayoutBillcode());
                Result<String> deleteResult = authServiceClient.deleteTasksByTaskKey(payout.getPayoutBillcode());
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    System.out.println("流程任务记录删除成功，payoutBillcode=" + payout.getPayoutBillcode());
                } else {
                    System.err.println("流程任务记录删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
                // 流程任务记录删除失败，不影响申请删除，只记录错误
            }
        }
        
        // 删除关联的发票记录
        if (ctrlPayoutInvoiceService != null) {
            try {
                boolean deleted = ctrlPayoutInvoiceService.deleteByPayoutId(payout.getPayoutId());
                if (deleted) {
                    System.out.println("删除发票记录成功，payoutId=" + payout.getPayoutId());
                } else {
                    System.err.println("删除发票记录失败或没有记录，payoutId=" + payout.getPayoutId());
                }
            } catch (Exception e) {
                System.err.println("删除发票记录异常: " + e.getMessage());
                e.printStackTrace();
                // 发票记录删除失败，不影响主表删除，只记录错误
            }
        }
        
        // 删除关联的支付清单记录
        if (ctrlPayoutPaymentService != null) {
            try {
                boolean deleted = ctrlPayoutPaymentService.deleteByPayoutId(payout.getPayoutId());
                if (deleted) {
                    System.out.println("删除支付清单记录成功，payoutId=" + payout.getPayoutId());
                } else {
                    System.err.println("删除支付清单记录失败或没有记录，payoutId=" + payout.getPayoutId());
                }
            } catch (Exception e) {
                System.err.println("删除支付清单记录异常: " + e.getMessage());
                e.printStackTrace();
                // 支付清单记录删除失败，不影响主表删除，只记录错误
            }
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
        if (!"DRAFT".equals(payout.getStatus()) && !"WITHDRAWN".equals(payout.getStatus()) && !"REJECTED".equals(payout.getStatus())) {
            return false; // 只有草稿、撤回或拒绝状态才能提交
        }
        
        // 如果没有单号，生成一个
        if (payout.getPayoutBillcode() == null || payout.getPayoutBillcode().isEmpty()) {
            payout.setPayoutBillcode(generateBillcode(payout.getBillType()));
            ctrlPayoutMapper.updateById(payout);
        }
        
        // 根据模板配置ID或报账类型从模板设置中获取流程定义
        Long processDefinitionId = null;
        if (authServiceClient != null) {
            try {
                TemplateConfig templateConfig = null;
                
                // 优先使用模板配置ID
                if (payout.getTemplateConfigId() != null) {
                    Result<TemplateConfig> configResult = authServiceClient.getTemplateConfigById(payout.getTemplateConfigId());
                    if (configResult != null && configResult.getCode() == 200 && configResult.getData() != null) {
                        templateConfig = configResult.getData();
                    }
                }
                
                // 如果没有模板配置ID或获取失败，则根据报账类型获取
                if (templateConfig == null && payout.getPayoutTypeId() != null && !payout.getPayoutTypeId().isEmpty()) {
                    Result<TemplateConfig> configResult = authServiceClient.getTemplateConfigByBusinessType("PAYOUT_TYPE", payout.getPayoutTypeId());
                    if (configResult != null && configResult.getCode() == 200 && configResult.getData() != null) {
                        templateConfig = configResult.getData();
                        // 保存模板配置ID
                        if (templateConfig.getConfigId() != null) {
                            payout.setTemplateConfigId(templateConfig.getConfigId());
                        }
                    }
                }
                
                if (templateConfig != null) {
                    processDefinitionId = templateConfig.getProcessDefinitionId();
                    payout.setProcessDefinitionId(processDefinitionId);
                }
            } catch (Exception e) {
                System.err.println("获取模板配置失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("=== 开始提交报账申请 ===");
        System.out.println("申请ID: " + payout.getPayoutId());
        System.out.println("申请单号: " + payout.getPayoutBillcode());
        System.out.println("流程定义ID: " + processDefinitionId);
        System.out.println("模板配置ID: " + payout.getTemplateConfigId());
        System.out.println("部门ID: " + payout.getDeptId());
        
        payout.setStatus("PENDING");
        payout.setUpdateTime(LocalDateTime.now());
        boolean updateSuccess = ctrlPayoutMapper.updateById(payout) > 0;
        
        if (!updateSuccess) {
            System.err.println("提交失败: 更新申请状态失败");
            return false;
        }
        
        if (processDefinitionId == null) {
            System.err.println("警告: processDefinitionId为空，无法启动流程");
            System.err.println("templateConfigId: " + payout.getTemplateConfigId());
            return updateSuccess; // 即使没有流程定义，也返回true表示状态更新成功
        }
        
        if (payout.getPayoutBillcode() == null || payout.getPayoutBillcode().isEmpty()) {
            System.err.println("警告: payoutBillcode为空，无法启动流程");
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
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> existingTasksResult = authServiceClient.getTasksByTaskKey(payout.getPayoutBillcode());
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
            System.out.println("准备处理旧的流程任务记录（如果存在），payoutBillcode=" + payout.getPayoutBillcode() + ", hasReturnToCurrent=" + hasReturnToCurrent);
            try {
                if (hasReturnToCurrent) {
                    // 恢复退回的任务为待审批状态，而不是删除
                    // 这样保留退回历史记录，同时让任务重新进入审批流程
                    Result<Boolean> restoreResult = authServiceClient.restoreReturnedTasks(payout.getPayoutBillcode());
                    if (restoreResult != null && restoreResult.getCode() == 200) {
                        System.out.println("成功恢复退回任务为待审批状态: payoutBillcode=" + payout.getPayoutBillcode());
                    } else {
                        System.err.println("恢复退回任务失败: " + (restoreResult != null ? restoreResult.getMessage() : "未知错误"));
                    }
                } else {
                    // 删除所有任务记录
                    authServiceClient.deleteTasksByTaskKey(payout.getPayoutBillcode());
                    System.out.println("旧的流程任务记录删除完成（如果有）");
                }
            } catch (Exception e) {
                // 如果处理失败（可能是因为没有旧记录），不影响后续流程
                System.out.println("处理旧任务记录时出现异常（可能没有旧记录）: " + e.getMessage());
            }
            
            System.out.println("准备生成流程任务记录，processDefinitionId=" + processDefinitionId + ", payoutBillcode=" + payout.getPayoutBillcode() + ", startFromNodeName=" + startFromNodeName);
            Result<java.util.List<com.hrp.common.entity.ProcessTask>> result = authServiceClient.generateTasks(processDefinitionId, payout.getPayoutBillcode(), startFromNodeName);
            if (result != null && result.getCode() == 200) {
                System.out.println("流程任务记录生成成功，payoutBillcode=" + payout.getPayoutBillcode());
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
    public boolean withdraw(Long payoutId) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        if (!"DRAFT".equals(payout.getStatus()) && !"PENDING".equals(payout.getStatus())) {
            return false; // 只有草稿或待审批状态才能撤回
        }
        
        // 删除流程任务记录
        if (payout.getPayoutBillcode() != null && !payout.getPayoutBillcode().isEmpty() && authServiceClient != null) {
            try {
                System.out.println("撤回报账申请，准备删除流程任务记录，payoutBillcode=" + payout.getPayoutBillcode());
                Result<String> deleteResult = authServiceClient.deleteTasksByTaskKey(payout.getPayoutBillcode());
                if (deleteResult != null && deleteResult.getCode() == 200) {
                    System.out.println("流程任务记录删除成功，payoutBillcode=" + payout.getPayoutBillcode());
                } else {
                    System.err.println("流程任务记录删除失败: " + (deleteResult != null ? deleteResult.getMessage() : "未知错误"));
                }
            } catch (Exception e) {
                System.err.println("删除流程任务记录异常: " + e.getMessage());
                e.printStackTrace();
                // 不影响撤回操作，只记录错误
            }
        }
        
        payout.setStatus("WITHDRAWN");
        payout.setUpdateTime(LocalDateTime.now());
        return ctrlPayoutMapper.updateById(payout) > 0;
    }

    @Override
    public List<CtrlPayout> getMyApprovalPayouts(String userId) {
        return ctrlPayoutMapper.selectByApprover(userId);
    }

    @Override
    public PageResult<CtrlPayout> getMyApprovalPayoutsPage(String userId, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<CtrlPayout> list = ctrlPayoutMapper.selectByApprover(userId);
        PageInfo<CtrlPayout> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<CtrlPayout> getAllPayouts() {
        return ctrlPayoutMapper.selectAll();
    }

    @Override
    public PageResult<CtrlPayout> getAllPayoutsPage(Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<CtrlPayout> list = ctrlPayoutMapper.selectAll();
        PageInfo<CtrlPayout> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long payoutId, String userId, String opinion, String approverSignature) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        
        // 完成任务（更新流程任务状态）
        if (authServiceClient != null && payout.getPayoutBillcode() != null) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("taskKey", payout.getPayoutBillcode());
                request.put("taskId", null); // 自动查找当前待处理任务
                request.put("comment", opinion);
                if (approverSignature != null && !approverSignature.trim().isEmpty()) {
                    request.put("approverSignature", approverSignature);
                }
                
                System.out.println("准备完成任务: taskKey=" + payout.getPayoutBillcode() + ", opinion=" + opinion);
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
        if (authServiceClient != null && payout.getPayoutBillcode() != null) {
            try {
                Result<java.util.List<com.hrp.common.entity.ProcessTask>> tasksResult = authServiceClient.getTasksByTaskKey(payout.getPayoutBillcode());
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
        
        // 只有所有任务都完成时，才将状态改为APPROVED
        if (allTasksCompleted) {
            payout.setStatus("APPROVED");
        } else {
            payout.setStatus("PENDING");
        }
        payout.setUpdateTime(LocalDateTime.now());
        return ctrlPayoutMapper.updateById(payout) > 0;
    }

    @Override
    @Transactional
    public boolean reject(Long payoutId, String userId, String opinion) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        
        // 获取申请人的用户信息（sys_user.id）
        String applicantUserId = null;
        String applicantUserName = payout.getEmpName();
        String applicantEmpCode = payout.getEmpCode();
        
        if (authServiceClient != null) {
            try {
                // 优先使用empCode（工号）获取用户信息
                if (applicantEmpCode != null && !applicantEmpCode.isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(applicantEmpCode);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
                // 如果通过empCode获取不到，尝试使用empId（用户ID）
                if (applicantUserId == null && payout.getEmpId() != null) {
                    Result<User> userResult = authServiceClient.getUserById(String.valueOf(payout.getEmpId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
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
        
        // 根据returnType决定处理方式（拒绝时使用RETURN_TO_START）
        if (authServiceClient != null && payout.getPayoutBillcode() != null && applicantUserId != null) {
            try {
                Map<String, Object> returnRequest = new HashMap<>();
                returnRequest.put("taskKey", payout.getPayoutBillcode());
                returnRequest.put("taskId", null); // 自动查找当前待处理任务
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
                
                System.out.println("退回任务成功: payoutBillcode=" + payout.getPayoutBillcode() + ", returnType=RETURN_TO_START");
            } catch (Exception e) {
                System.err.println("退回处理异常: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        // 将状态改为REJECTED（已拒绝），退回后的状态应该是已拒绝
        // 注意：虽然ProcessTaskServiceImpl.returnTask也会更新状态为REJECTED，但这里也设置一次确保状态正确
        payout.setStatus("REJECTED");
        payout.setUpdateTime(LocalDateTime.now());
        return ctrlPayoutMapper.updateById(payout) > 0;
    }

    @Override
    @Transactional
    public boolean returnPayout(Long payoutId, String returnType, String opinion) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        
        // 获取申请人的用户信息（sys_user.id）
        String applicantUserId = null;
        String applicantUserName = payout.getEmpName();
        String applicantEmpCode = payout.getEmpCode();
        
        if (authServiceClient != null) {
            try {
                // 优先使用empCode（工号）获取用户信息
                if (applicantEmpCode != null && !applicantEmpCode.isEmpty()) {
                    Result<User> userResult = authServiceClient.getUserByAccount(applicantEmpCode);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        applicantUserId = user.getId();
                        applicantUserName = user.getName();
                        applicantEmpCode = user.getAccount();
                    }
                }
                // 如果通过empCode获取不到，尝试使用empId（用户ID）
                if (applicantUserId == null && payout.getEmpId() != null) {
                    Result<User> userResult = authServiceClient.getUserById(String.valueOf(payout.getEmpId()));
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
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
        if (authServiceClient != null && payout.getPayoutBillcode() != null && applicantUserId != null) {
            try {
                Map<String, Object> returnRequest = new HashMap<>();
                returnRequest.put("taskKey", payout.getPayoutBillcode());
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
                
                System.out.println("退回任务成功: payoutBillcode=" + payout.getPayoutBillcode() + ", returnType=" + returnType);
            } catch (Exception e) {
                System.err.println("退回处理异常: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        // 将状态改为REJECTED（已拒绝），退回后的状态应该是已拒绝
        // 注意：虽然ProcessTaskServiceImpl.returnTask也会更新状态为REJECTED，但这里也设置一次确保状态正确
        payout.setStatus("REJECTED");
        payout.setUpdateTime(LocalDateTime.now());
        return ctrlPayoutMapper.updateById(payout) > 0;
    }

    @Override
    public List<CtrlPayout> getByItemId(Long itemId) {
        return ctrlPayoutMapper.selectByItemId(itemId);
    }

    @Override
    public List<CtrlPayout> getPayoutByItemId(Long itemId) {
        return ctrlPayoutMapper.selectPayoutByItemId(itemId);
    }
    
    /**
     * 根据业务单号获取预算明细记录（供Controller调用）
     */
    public Result<List<BudgetDetailRecord>> getBudgetDetailsByBusinessNo(String businessNo) {
        if (budgServiceClient == null) {
            return Result.success(new ArrayList<>());
        }
        try {
            return budgServiceClient.getBudgetDetailsByBusinessNo(businessNo);
        } catch (Exception e) {
            System.err.println("获取预算明细失败: " + e.getMessage());
            return Result.success(new ArrayList<>());
        }
    }

    @Override
    public PageResult<CtrlPayout> getPage(Long page, Long size, String payoutBillcode, String empName, String payoutTypeId, String status, String startDate, String endDate, String billTypePrefix) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<CtrlPayout> list = ctrlPayoutMapper.selectByConditions(payoutBillcode, empName, payoutTypeId, status, startDate, endDate, billTypePrefix);
        PageInfo<CtrlPayout> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<CtrlPayout> getPageByApprover(Long page, Long size, String currentUserId,
                                                     String payoutBillcode, String empName,
                                                     String payoutTypeId, String status,
                                                     String startDate, String endDate, String billTypePrefix) {
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
                System.err.println("查询审批任务失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果没有任务，返回空列表
        if (taskKeys.isEmpty()) {
            return new PageResult<>(new java.util.ArrayList<>(), 0L, size, page);
        }
        
        // 根据taskKey（payoutBillcode）列表查询报账申请，并应用查询条件
        // 注意：如果status不是PENDING或null，需要查询所有状态的记录，然后根据taskKeys过滤
        String queryStatus = ("PENDING".equals(status) || status == null || status.isEmpty()) ? "PENDING" : null; // 非PENDING状态时不限制status
        List<CtrlPayout> allPayouts = ctrlPayoutMapper.selectByConditions(payoutBillcode, empName, payoutTypeId, queryStatus, startDate, endDate, billTypePrefix);
        
        // 过滤出当前用户需要审批的申请单（taskKey在taskKeys中的），并填充当前审批人信息
        List<CtrlPayout> filteredPayouts = new java.util.ArrayList<>();
        for (CtrlPayout payout : allPayouts) {
            if (payout.getPayoutBillcode() != null && taskKeys.contains(payout.getPayoutBillcode())) {
                // 如果查询的是非PENDING状态，还需要匹配申请单的实际状态
                if (status != null && !status.isEmpty() && !"PENDING".equals(status)) {
                    if (!status.equals(payout.getStatus())) {
                        continue; // 状态不匹配，跳过
                    }
                }
                // 从任务中获取当前审批人信息
                com.hrp.common.entity.ProcessTask task = taskMap.get(payout.getPayoutBillcode());
                if (task != null && task.getAssigneeUserName() != null) {
                    // 设置当前审批人（taskMap中存储的已经是print_order最小的任务，即当前节点）
                    // 注意：CtrlPayout可能没有currentApprover字段，这里先不设置
                }
                filteredPayouts.add(payout);
            }
        }
        
        // 手动分页
        long total = filteredPayouts.size();
        long start = (page - 1) * size;
        long end = Math.min(start + size, total);
        List<CtrlPayout> pagedList = start < total ? filteredPayouts.subList((int)start, (int)end) : new java.util.ArrayList<>();
        
        return new PageResult<>(pagedList, total, size, page);
    }

    @Override
    public boolean isSourceApplyNoUsed(String sourceApplyNo, Long excludePayoutId) {
        if (sourceApplyNo == null || sourceApplyNo.trim().isEmpty()) {
            return false;
        }
        CtrlPayout existing = ctrlPayoutMapper.selectBySourceApplyNo(sourceApplyNo, excludePayoutId);
        return existing != null;
    }

    @Override
    public boolean isContractNoUsed(String contractNo, Long excludePayoutId) {
        if (contractNo == null || contractNo.trim().isEmpty()) {
            return false;
        }
        CtrlPayout existing = ctrlPayoutMapper.selectByContractNo(contractNo, excludePayoutId);
        return existing != null;
    }
}




