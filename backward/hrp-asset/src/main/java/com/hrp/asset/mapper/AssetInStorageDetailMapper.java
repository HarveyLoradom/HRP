package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetInStorageDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 入库明细表数据访问接口
 */
public interface AssetInStorageDetailMapper {
    List<AssetInStorageDetail> selectByStorageId(@Param("storageId") Long storageId);
    List<AssetInStorageDetail> selectByStorageNo(@Param("storageNo") String storageNo);
    int insert(AssetInStorageDetail detail);
    int insertBatch(@Param("details") List<AssetInStorageDetail> details);
    int updateById(AssetInStorageDetail detail);
    int deleteByStorageId(@Param("storageId") Long storageId);
    int deleteById(@Param("id") Long id);
}

