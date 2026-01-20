package com.hrp.hr.service;

import com.hrp.common.entity.HrSalCalculate;
import java.util.List;

/**
 * 薪酬核算服务接口
 */
public interface HrSalCalculateService {
    /**
     * 根据ID查询
     */
    HrSalCalculate getById(Long calcId);
    
    /**
     * 根据员工ID和月份查询
     */
    HrSalCalculate getByEmpIdAndMonth(Long empId, String calcMonth);
    
    /**
     * 分页查询
     */
    com.hrp.common.entity.PageResult<HrSalCalculate> getPage(Long page, Long size, Long empId,
                                                             String empCode, String empName,
                                                             String calcMonth, String calcStatus);
    
    /**
     * 计算单个员工薪酬
     */
    HrSalCalculate calculateSalary(Long empId, String calcMonth);
    
    /**
     * 批量计算薪酬
     */
    List<HrSalCalculate> batchCalculateSalary(String calcMonth, List<Long> empIds);
    
    /**
     * 更新薪酬核算
     */
    HrSalCalculate update(HrSalCalculate calculate);
    
    /**
     * 删除薪酬核算
     */
    boolean delete(Long calcId);
    
    /**
     * 发放薪酬（将状态改为已发放）
     */
    boolean paySalary(Long calcId);
    
    /**
     * 批量发放薪酬
     */
    boolean batchPaySalary(List<Long> calcIds);
}
