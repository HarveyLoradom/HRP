package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetReceiveDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产领用明细表数据访问接口
 */
public interface AssetReceiveDetailMapper {
    /**
     * 插入资产领用明细
     */
    int insert(AssetReceiveDetail detail);
    
    /**
     * 批量插入资产领用明细
     */
    int insertBatch(List<AssetReceiveDetail> details);
    
    /**
     * 根据领用ID查询明细列表
     */
    List<AssetReceiveDetail> selectByReceiveId(@Param("receiveId") Long receiveId);
    
    /**
     * 根据领用单号查询明细列表
     */
    List<AssetReceiveDetail> selectByReceiveNo(@Param("receiveNo") String receiveNo);
    
    /**
     * 根据ID查询明细
     */
    AssetReceiveDetail selectById(@Param("id") Long id);
    
    /**
     * 根据领用ID删除明细
     */
    int deleteByReceiveId(@Param("receiveId") Long receiveId);
}

