package com.hrp.cost.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.common.entity.*;
import com.hrp.cost.feign.AuthServiceClient;
import com.hrp.cost.mapper.CostMainMapper;
import com.hrp.cost.service.CostCycleService;
import com.hrp.cost.service.CostMainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CostMainServiceImpl implements CostMainService {
    
    private static final Logger logger = LoggerFactory.getLogger(CostMainServiceImpl.class);
    
    @Autowired
    private CostMainMapper costMainMapper;
    
    @Autowired
    private CostCycleService costCycleService;
    
    @Autowired
    private AuthServiceClient authServiceClient;

    @Override
    public CostMain getById(Long id) {
        return costMainMapper.selectById(id);
    }

    @Override
    public CostMain getByNo(String costNo) {
        return costMainMapper.selectByNo(costNo);
    }

    @Override
    public PageResult<CostMain> getPage(Long page, Long size, Long cycleId, Long deptId, String elementType, String startDate, String endDate) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<CostMain> list = costMainMapper.selectByConditions(cycleId, deptId, elementType, startDate, endDate);
        PageInfo<CostMain> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<CostMain> getPageByDept(Long page, Long size, Long deptId, Long cycleId, String elementType) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<CostMain> list = costMainMapper.selectByDeptId(deptId, cycleId, elementType);
        PageInfo<CostMain> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional
    public CostMain save(CostMain costMain) {
        // 生成成本编号
        if (costMain.getCostNo() == null || costMain.getCostNo().isEmpty()) {
            String prefix = "COST" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String maxNo = costMainMapper.selectMaxCostNoByPrefix(prefix);
            int seq = 1;
            if (maxNo != null && maxNo.length() > prefix.length()) {
                try {
                    seq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
                } catch (NumberFormatException e) {
                    seq = 1;
                }
            }
            costMain.setCostNo(prefix + String.format("%04d", seq));
        }
        int result = costMainMapper.insert(costMain);
        if (result > 0) {
            return costMainMapper.selectById(costMain.getCostId());
        }
        return null;
    }

    @Override
    @Transactional
    public CostMain update(CostMain costMain) {
        int result = costMainMapper.updateById(costMain);
        if (result > 0) {
            return costMainMapper.selectById(costMain.getCostId());
        }
        return null;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costMainMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> importCostMain(List<List<String>> dataList, String createUser) {
        if (dataList == null || dataList.isEmpty()) {
            return Result.error("导入数据为空");
        }

        logger.info("开始导入成本数据，总行数：{}，创建用户：{}", dataList.size(), createUser);
        
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 生成成本编号前缀（整个导入过程使用同一个前缀，避免重复查询）
        String prefix = "COST" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String maxNo = costMainMapper.selectMaxCostNoByPrefix(prefix);
        int currentSeq = 1;
        if (maxNo != null && maxNo.length() > prefix.length()) {
            try {
                currentSeq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                currentSeq = 1;
            }
        }
        logger.info("成本编号前缀：{}，起始序号：{}", prefix, currentSeq);

        // 跳过表头，从第二行开始
        for (int i = 1; i < dataList.size(); i++) {
            List<String> row = dataList.get(i);
            if (row == null || row.isEmpty()) {
                continue;
            }

            try {
                // Excel列顺序：周期编码、部门编码、成本要素、成本金额、发生日期、付款方式、备注
                if (row.size() < 5) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：数据列数不足\n");
                    continue;
                }

                String cycleCode = row.get(0) != null ? row.get(0).trim() : "";
                String deptCode = row.get(1) != null ? row.get(1).trim() : "";
                String elementType = row.get(2) != null ? row.get(2).trim() : "";
                String costAmountStr = row.get(3) != null ? row.get(3).trim() : "";
                String occurDateStr = row.get(4) != null ? row.get(4).trim() : "";
                String payType = row.size() > 5 && row.get(5) != null ? row.get(5).trim() : "";
                String remark = row.size() > 6 && row.get(6) != null ? row.get(6).trim() : "";

                // 验证必填字段
                if (cycleCode.isEmpty() || deptCode.isEmpty() || elementType.isEmpty() || costAmountStr.isEmpty() || occurDateStr.isEmpty()) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：周期编码、部门编码、成本要素、成本金额、发生日期不能为空\n");
                    continue;
                }

                // 验证周期编码是否存在
                CostCycle costCycle = costCycleService.getByCode(cycleCode);
                if (costCycle == null) {
                    failCount++;
                    String error = String.format("第%d行：周期编码不存在：%s\n", i + 1, cycleCode);
                    errorMsg.append(error);
                    logger.warn("第{}行：周期编码不存在：{}", i + 1, cycleCode);
                    continue;
                }
                
                // 验证周期状态是否启用
                if (costCycle.getStatus() == null || costCycle.getStatus() != 1) {
                    failCount++;
                    String error = String.format("第%d行：周期编码已禁用：%s（状态：%s）\n", i + 1, cycleCode, costCycle.getStatus());
                    errorMsg.append(error);
                    logger.warn("第{}行：周期编码已禁用：{}，状态：{}", i + 1, cycleCode, costCycle.getStatus());
                    continue;
                }
                
                logger.debug("第{}行：周期编码验证通过，周期ID：{}，周期名称：{}", i + 1, costCycle.getCycleId(), costCycle.getCycleName());

                // 验证部门编码是否存在
                Result<Dept> deptResult = null;
                try {
                    deptResult = authServiceClient.getDeptByCode(deptCode);
                    logger.debug("第{}行：部门编码查询结果，code：{}，data：{}", i + 1, 
                        deptResult != null ? deptResult.getCode() : "null", 
                        deptResult != null && deptResult.getData() != null ? deptResult.getData().getDeptName() : "null");
                } catch (Exception e) {
                    logger.error("第{}行：调用部门服务失败，部门编码：{}", i + 1, deptCode, e);
                    failCount++;
                    String error = String.format("第%d行：查询部门编码失败：%s（%s）\n", i + 1, deptCode, e.getMessage());
                    errorMsg.append(error);
                    continue;
                }
                
                Dept dept = null;
                if (deptResult != null && deptResult.getCode() == 200 && deptResult.getData() != null) {
                    dept = deptResult.getData();
                }
                if (dept == null) {
                    failCount++;
                    String error = String.format("第%d行：部门编码不存在：%s（返回code：%s）\n", i + 1, deptCode, 
                        deptResult != null ? String.valueOf(deptResult.getCode()) : "null");
                    errorMsg.append(error);
                    logger.warn("第{}行：部门编码不存在：{}，返回结果：{}", i + 1, deptCode, deptResult);
                    continue;
                }
                
                logger.debug("第{}行：部门编码验证通过，部门ID：{}，部门名称：{}", i + 1, dept.getDeptId(), dept.getDeptName());

                BigDecimal costAmount;
                try {
                    costAmount = new BigDecimal(costAmountStr);
                    if (costAmount.compareTo(BigDecimal.ZERO) < 0) {
                        failCount++;
                        String error = String.format("第%d行：成本金额不能为负数\n", i + 1);
                        errorMsg.append(error);
                        logger.warn("第{}行：成本金额不能为负数：{}", i + 1, costAmountStr);
                        continue;
                    }
                    logger.debug("第{}行：成本金额解析成功：{}", i + 1, costAmount);
                } catch (NumberFormatException e) {
                    failCount++;
                    String error = String.format("第%d行：成本金额格式错误：%s\n", i + 1, costAmountStr);
                    errorMsg.append(error);
                    logger.warn("第{}行：成本金额格式错误：{}", i + 1, costAmountStr, e);
                    continue;
                }


                LocalDate occurDate = null;
                if (!occurDateStr.isEmpty()) {
                    try {
                        java.time.LocalDate date = java.time.LocalDate.parse(occurDateStr);
                        occurDate = date;
                    } catch (Exception e) {
                        failCount++;
                        String error = String.format("第%d行：发生日期格式错误，应为yyyy-MM-dd格式，当前值：%s\n", i + 1, occurDateStr);
                        errorMsg.append(error);
                        logger.warn("第{}行：发生日期格式错误：{}", i + 1, occurDateStr, e);
                        continue;
                    }
                } else {
                    failCount++;
                    String error = String.format("第%d行：发生日期不能为空\n", i + 1);
                    errorMsg.append(error);
                    logger.warn("第{}行：发生日期为空", i + 1);
                    continue;
                }

                // 创建成本记录
                try {
                    CostMain costMain = new CostMain();
                    costMain.setCycleId(costCycle.getCycleId());
                    costMain.setDeptId(dept.getDeptId());
                    costMain.setElementType(elementType);
                    costMain.setCostAmount(costAmount);
                    costMain.setOccurDate(occurDate);
                    costMain.setPayType(payType.isEmpty() ? null : payType);
                    costMain.setRemark(remark.isEmpty() ? null : remark);
                    costMain.setCreateUser(createUser);
                    
                    // 生成成本编号（使用递增序号）
                    String costNo = prefix + String.format("%04d", currentSeq++);
                    costMain.setCostNo(costNo);
                    

                    // 插入成本记录
                    int result = costMainMapper.insert(costMain);

                    
                    if (result > 0) {
                        successCount++;

                    } else {
                        failCount++;
                        String error = String.format("第%d行：保存失败（insert返回%d）\n", i + 1, result);
                        errorMsg.append(error);
                    }
                } catch (Exception e) {
                    failCount++;
                    String error = String.format("第%d行：创建或插入数据时发生异常：%s\n", i + 1, e.getMessage());
                    errorMsg.append(error);
                }
            } catch (Exception e) {
                failCount++;
                String error = String.format("第%d行：处理异常：%s（类型：%s）\n", i + 1, e.getMessage(), e.getClass().getSimpleName());
                errorMsg.append(error);
            }
        }


        String msg = String.format("导入完成：成功%d条，失败%d条", successCount, failCount);
        if (failCount > 0 && errorMsg.length() > 0) {
            msg += "\n错误详情：\n" + errorMsg.toString();
        }
        
        // 如果有失败记录，返回Result.error，让前端能够识别并显示错误消息
        if (failCount == 0) {
            return Result.success(msg);
        } else if (successCount > 0) {
            // 部分成功，返回error但包含详细信息
            return Result.error(msg);
        } else {
            // 全部失败，返回error
            return Result.error(msg);
        }
    }
}

