package com.hrp.cost.mapper;

import com.hrp.common.entity.CostMain;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 成本主表数据访问接口
 */
public interface CostMainMapper {
    CostMain selectById(@Param("id") Long id);
    CostMain selectByNo(@Param("costNo") String costNo);
    List<CostMain> selectAll();
    List<CostMain> selectByConditions(@Param("cycleId") Long cycleId,
                                      @Param("deptId") Long deptId,
                                      @Param("elementType") String elementType,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate);
    List<CostMain> selectByDeptId(@Param("deptId") Long deptId,
                                  @Param("cycleId") Long cycleId,
                                  @Param("elementType") String elementType);
    long countByConditions(@Param("cycleId") Long cycleId,
                          @Param("deptId") Long deptId,
                          @Param("elementType") String elementType,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);
    long countByDeptId(@Param("deptId") Long deptId, @Param("cycleId") Long cycleId);
    String selectMaxCostNoByPrefix(@Param("prefix") String prefix);
    int insert(CostMain costMain);
    int updateById(CostMain costMain);
    int deleteById(@Param("id") Long id);
    int batchInsert(@Param("list") List<CostMain> list);
}

