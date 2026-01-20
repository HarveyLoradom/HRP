package com.hrp.cost.service;

import com.hrp.common.entity.CostMain;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import java.util.List;

/**
 * 成本主表服务接口
 */
public interface CostMainService {
    CostMain getById(Long id);
    CostMain getByNo(String costNo);
    PageResult<CostMain> getPage(Long page, Long size, Long cycleId, Long deptId, String elementType, String startDate, String endDate);
    PageResult<CostMain> getPageByDept(Long page, Long size, Long deptId, Long cycleId, String elementType);
    CostMain save(CostMain costMain);
    CostMain update(CostMain costMain);
    boolean delete(Long id);
    Result<String> importCostMain(List<List<String>> dataList, String createUser);
}

