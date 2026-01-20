package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetReceiveMain;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产领用主表数据访问接口
 */
public interface AssetReceiveMainMapper {
    /**
     * 插入资产领用主表
     */
    int insert(AssetReceiveMain receiveMain);
    
    /**
     * 根据ID查询资产领用主表
     */
    AssetReceiveMain selectById(@Param("id") Long id);
    
    /**
     * 根据领用单号查询资产领用主表
     */
    AssetReceiveMain selectByReceiveNo(@Param("receiveNo") String receiveNo);
    
    /**
     * 根据条件查询资产领用主表列表
     */
    List<AssetReceiveMain> selectByConditions(
        @Param("receiveNo") String receiveNo,
        @Param("deptId") Long deptId,
        @Param("applyEmpId") String applyEmpId,
        @Param("operatorCode") String operatorCode
    );
    
    /**
     * 根据前缀查询最大领用单号
     */
    String selectMaxReceiveNoByPrefix(@Param("prefix") String prefix);
}

