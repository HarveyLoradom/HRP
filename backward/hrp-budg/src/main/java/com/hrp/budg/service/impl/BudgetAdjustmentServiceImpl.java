package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetAdjustmentMapper;
import com.hrp.budg.mapper.BudgetDetailRecordMapper;
import com.hrp.budg.mapper.BudgetMapper;
import com.hrp.budg.service.BudgetAdjustmentService;
import com.hrp.budg.service.BudgetService;
import com.hrp.common.entity.Budget;
import com.hrp.common.entity.BudgetAdjustment;
import com.hrp.common.entity.BudgetDetailRecord;
import com.hrp.common.entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预算调整服务实现
 */
@Service
public class BudgetAdjustmentServiceImpl implements BudgetAdjustmentService {

    @Autowired
    private BudgetAdjustmentMapper budgetAdjustmentMapper;

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetDetailRecordMapper budgetDetailRecordMapper;

    @Override
    public BudgetAdjustment getById(Long id) {
        return budgetAdjustmentMapper.selectById(id);
    }

    @Override
    public BudgetAdjustment getByNo(String no) {
        return budgetAdjustmentMapper.selectByNo(no);
    }

    @Override
    public PageResult<BudgetAdjustment> getPage(Long page, Long size, Map<String, Object> params) {
        int offset = (page.intValue() - 1) * size.intValue();
        List<BudgetAdjustment> list = budgetAdjustmentMapper.selectPage(offset, size.intValue(), params);
        long total = budgetAdjustmentMapper.countByConditions(params);
        return new PageResult<>(list, total, size, page);
    }

    /**
     * 生成调整单号：ADJUST+年月日+0001格式
     */
    private String generateAdjustmentNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "ADJUST" + dateStr;
        
        // 查询当天已有的调整单号
        Map<String, Object> params = new HashMap<>();
        params.put("adjustmentNo", prefix);
        List<BudgetAdjustment> todayAdjustments = budgetAdjustmentMapper.selectPage(0, Integer.MAX_VALUE, params);
        
        // 找出当天的最大序号
        int maxSeq = 0;
        for (BudgetAdjustment adj : todayAdjustments) {
            String no = adj.getAdjustmentNo();
            if (no != null && no.startsWith(prefix) && no.length() == prefix.length() + 4) {
                try {
                    int seq = Integer.parseInt(no.substring(prefix.length()));
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的单号
                }
            }
        }
        
        // 生成新的序号（从0001开始）
        int nextSeq = maxSeq + 1;
        return prefix + String.format("%04d", nextSeq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(BudgetAdjustment budgetAdjustment) {
        // 生成调整单号
        if (budgetAdjustment.getAdjustmentNo() == null || budgetAdjustment.getAdjustmentNo().isEmpty()) {
            budgetAdjustment.setAdjustmentNo(generateAdjustmentNo());
        }
        
        // 设置预算相关信息
        if (budgetAdjustment.getBudgetId() != null) {
            Budget budget = budgetService.getById(budgetAdjustment.getBudgetId());
            if (budget != null) {
                budgetAdjustment.setBudgetNo(budget.getBudgetNo());
                budgetAdjustment.setSubjectId(budget.getSubjectId());
                budgetAdjustment.setSubjectCode(budget.getSubjectCode());
                budgetAdjustment.setItemId(budget.getItemId());
                budgetAdjustment.setItemCode(budget.getItemCode());
                
                // 根据调整类型设置原始金额
                String adjustmentType = budgetAdjustment.getAdjustmentType();
                if ("QUOTA_INCREASE".equals(adjustmentType) || "QUOTA_DECREASE".equals(adjustmentType)) {
                    // 额度调增/调减：原始金额为budget表的amount字段（调整前的预算金额）
                    // 注意：这里获取的是调整前的预算金额，用于记录和计算
                    budgetAdjustment.setOriginalAmount(budget.getBudgetAmount());
                } else if ("APPLY_OFFSET".equals(adjustmentType)) {
                    // 申请冲销：原始金额需要从budget_detail表查询该申请单对应项目的申请金额
                    if (budgetAdjustment.getRelatedBillNo() != null) {
                        List<BudgetDetailRecord> detailRecords = budgetDetailRecordMapper.selectByBusinessNo(budgetAdjustment.getRelatedBillNo());
                        BigDecimal totalAmount = BigDecimal.ZERO;
                        for (BudgetDetailRecord record : detailRecords) {
                            if ("APPLY".equals(record.getDetailType()) 
                                    && budget.getSubjectId().equals(record.getSubjectId())
                                    && budget.getItemId().equals(record.getItemId())) {
                                if (record.getAmount() != null) {
                                    totalAmount = totalAmount.add(record.getAmount());
                                }
                            }
                        }
                        budgetAdjustment.setOriginalAmount(totalAmount);
                    }
                }
            }
        }
        
        // 设置创建时间和创建人（如果未设置）
        if (budgetAdjustment.getCreateTime() == null) {
            budgetAdjustment.setCreateTime(LocalDateTime.now());
        }
        if (budgetAdjustment.getCreateUser() == null || budgetAdjustment.getCreateUser().isEmpty()) {
            budgetAdjustment.setCreateUser("SYSTEM");
        }
        
        // 计算调整后金额（调整金额都是正数，根据类型决定加减）
        // 确保originalAmount和adjustmentAmount都不为空才进行计算
        if (budgetAdjustment.getOriginalAmount() != null && budgetAdjustment.getAdjustmentAmount() != null) {
            BigDecimal adjustedAmount;
            String adjustmentType = budgetAdjustment.getAdjustmentType();
            if ("QUOTA_INCREASE".equals(adjustmentType)) {
                // 额度调增：调整后金额 = 原始金额 + 调整金额
                // 例如：原始金额10000，调整金额5000，调整后金额=15000
                adjustedAmount = budgetAdjustment.getOriginalAmount().add(budgetAdjustment.getAdjustmentAmount());
            } else if ("QUOTA_DECREASE".equals(adjustmentType)) {
                // 额度调减：调整后金额 = 原始金额 - 调整金额
                // 例如：原始金额10000，调整金额5000，调整后金额=5000
                adjustedAmount = budgetAdjustment.getOriginalAmount().subtract(budgetAdjustment.getAdjustmentAmount());
            } else if ("APPLY_OFFSET".equals(adjustmentType)) {
                // 申请冲销：调整后金额 = 原始金额 - 调整金额（表示剩余可申请金额）
                adjustedAmount = budgetAdjustment.getOriginalAmount().subtract(budgetAdjustment.getAdjustmentAmount());
            } else {
                // 其他类型（如报账冲销）：使用原始金额
                adjustedAmount = budgetAdjustment.getOriginalAmount();
            }
            budgetAdjustment.setAdjustedAmount(adjustedAmount);
        } else {
            // 如果原始金额或调整金额为空，抛出异常
            if (budgetAdjustment.getOriginalAmount() == null) {
                throw new RuntimeException("原始金额未设置，无法保存调整单");
            }
            if (budgetAdjustment.getAdjustmentAmount() == null) {
                throw new RuntimeException("调整金额未设置，无法保存调整单");
            }
        }
        
        budgetAdjustment.setCreateTime(LocalDateTime.now());
        budgetAdjustment.setUpdateTime(LocalDateTime.now());
        
        return budgetAdjustmentMapper.insert(budgetAdjustment) > 0;
    }

    @Override
    @Transactional
    public boolean update(BudgetAdjustment budgetAdjustment) {
        budgetAdjustment.setUpdateTime(LocalDateTime.now());
        return budgetAdjustmentMapper.updateById(budgetAdjustment) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        BudgetAdjustment adjustment = budgetAdjustmentMapper.selectById(id);
        if (adjustment == null) {
            throw new RuntimeException("调整单不存在，ID: " + id);
        }
        
        String adjustmentType = adjustment.getAdjustmentType();
        
        // 根据调整类型还原相关操作
        if ("QUOTA_INCREASE".equals(adjustmentType) || "QUOTA_DECREASE".equals(adjustmentType)) {
            // 额度调增/调减：将budget表的amount还原为originalAmount
            if (adjustment.getBudgetId() != null && adjustment.getOriginalAmount() != null) {
                Budget budget = budgetService.getById(adjustment.getBudgetId());
                if (budget != null) {
                    budget.setBudgetAmount(adjustment.getOriginalAmount());
                    boolean updated = budgetService.update(budget);
                    if (!updated) {
                        throw new RuntimeException("还原预算金额失败");
                    }
                }
            }
        } else if ("APPLY_OFFSET".equals(adjustmentType)) {
            // 申请冲销：删除budget_detail表中那条负数金额的记录
            if (adjustment.getRelatedBillNo() != null 
                    && adjustment.getSubjectId() != null 
                    && adjustment.getItemId() != null 
                    && adjustment.getAdjustmentAmount() != null) {
                // 查找负数金额的记录（调整金额的负数）
                BigDecimal negativeAmount = adjustment.getAdjustmentAmount().negate();
                List<BudgetDetailRecord> records = budgetDetailRecordMapper.selectByBusinessNoAndSubjectItemAndAmount(
                    adjustment.getRelatedBillNo(),
                    adjustment.getSubjectId(),
                    adjustment.getItemId(),
                    negativeAmount
                );
                
                if (records != null && !records.isEmpty()) {
                    // 删除找到的记录（应该只有一条）
                    for (BudgetDetailRecord record : records) {
                        int deleted = budgetDetailRecordMapper.deleteById(record.getDetailId());
                        if (deleted <= 0) {
                            throw new RuntimeException("删除预算明细记录失败");
                        }
                    }
                }
            }
        }
        
        // 物理删除调整单
        int deleted = budgetAdjustmentMapper.deleteById(id);
        if (deleted <= 0) {
            throw new RuntimeException("删除调整单失败");
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submit(BudgetAdjustment budgetAdjustment) {
        String adjustmentType = budgetAdjustment.getAdjustmentType();
        
        // 如果是额度调整（调增/调减），直接更新budget表，不走流程
        if ("QUOTA_INCREASE".equals(adjustmentType) || "QUOTA_DECREASE".equals(adjustmentType)) {
            // 直接更新budget表的amount字段
            Budget budget = budgetService.getById(budgetAdjustment.getBudgetId());
            if (budget == null) {
                throw new RuntimeException("预算不存在，预算ID: " + budgetAdjustment.getBudgetId());
            }
            if (budgetAdjustment.getAdjustedAmount() == null) {
                throw new RuntimeException("调整后金额不能为空");
            }
            budget.setBudgetAmount(budgetAdjustment.getAdjustedAmount());
            boolean updated = budgetService.update(budget);
            if (!updated) {
                throw new RuntimeException("更新预算金额失败");
            }
        } 
        // 如果是申请冲销，插入budget_detail表
        else if ("APPLY_OFFSET".equals(adjustmentType)) {
            // 获取关联的申请单明细记录
            if (budgetAdjustment.getRelatedBillNo() == null) {
                throw new RuntimeException("申请冲销必须关联申请单号");
            }
            List<BudgetDetailRecord> sourceRecords = budgetDetailRecordMapper.selectByBusinessNo(budgetAdjustment.getRelatedBillNo());
            if (sourceRecords == null || sourceRecords.isEmpty()) {
                throw new RuntimeException("未找到关联的申请单记录，申请单号: " + budgetAdjustment.getRelatedBillNo());
            }
            
            // 找到对应的申请单记录（根据subject_id和item_id匹配）
            Budget budget = budgetService.getById(budgetAdjustment.getBudgetId());
            if (budget == null) {
                throw new RuntimeException("预算不存在，预算ID: " + budgetAdjustment.getBudgetId());
            }
            
            boolean found = false;
            for (BudgetDetailRecord sourceRecord : sourceRecords) {
                if ("APPLY".equals(sourceRecord.getDetailType())
                        && budget.getSubjectId().equals(sourceRecord.getSubjectId())
                        && budget.getItemId().equals(sourceRecord.getItemId())) {
                    
                    // 复制记录，除了detail_id、create_time、create_user、amount字段
                    BudgetDetailRecord newRecord = new BudgetDetailRecord();
                    newRecord.setBudgetYear(sourceRecord.getBudgetYear());
                    newRecord.setSubjectId(sourceRecord.getSubjectId());
                    newRecord.setSubjectCode(sourceRecord.getSubjectCode());
                    newRecord.setSubjectName(sourceRecord.getSubjectName());
                    newRecord.setItemId(sourceRecord.getItemId());
                    newRecord.setItemCode(sourceRecord.getItemCode());
                    newRecord.setItemName(sourceRecord.getItemName());
                    // amount字段为调整金额的负数（如用户输入2000，插入-2000）
                    newRecord.setAmount(budgetAdjustment.getAdjustmentAmount().negate());
                    newRecord.setDetailType("APPLY");
                    newRecord.setBusinessId(sourceRecord.getBusinessId());
                    newRecord.setBusinessNo(sourceRecord.getBusinessNo());
                    newRecord.setDeptId(sourceRecord.getDeptId());
                    newRecord.setDeptCode(sourceRecord.getDeptCode());
                    newRecord.setDeptName(sourceRecord.getDeptName());
                    newRecord.setEmpId(sourceRecord.getEmpId());
                    newRecord.setEmpCode(sourceRecord.getEmpCode());
                    newRecord.setEmpName(sourceRecord.getEmpName());
                    newRecord.setRemark(sourceRecord.getRemark());
                    newRecord.setCreateUser(budgetAdjustment.getCreateUser() != null ? 
                            budgetAdjustment.getCreateUser() : "SYSTEM");
                    
                    int insertResult = budgetDetailRecordMapper.insert(newRecord);
                    if (insertResult <= 0) {
                        throw new RuntimeException("插入预算明细记录失败");
                    }
                    found = true;
                    break; // 只插入一条记录
                }
            }
            if (!found) {
                throw new RuntimeException("未找到匹配的申请单明细记录");
            }
        }
        
        budgetAdjustment.setUpdateTime(LocalDateTime.now());
        int updateResult = budgetAdjustmentMapper.updateById(budgetAdjustment);
        if (updateResult <= 0) {
            throw new RuntimeException("更新调整单失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAndSubmit(BudgetAdjustment budgetAdjustment) {
        // 先保存
        boolean saved = save(budgetAdjustment);
        if (!saved) {
            throw new RuntimeException("保存调整单失败");
        }
        // 再提交
        return submit(budgetAdjustment);
    }

}

