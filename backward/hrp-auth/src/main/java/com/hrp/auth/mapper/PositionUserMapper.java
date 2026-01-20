package com.hrp.auth.mapper;

import com.hrp.common.entity.PositionUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位人员关联数据访问接口
 */
public interface PositionUserMapper {
    List<PositionUser> selectByPositionId(@Param("positionId") Long positionId);
    List<PositionUser> selectByUserId(@Param("userId") String userId);
    int insert(PositionUser positionUser);
    int deleteByPositionId(@Param("positionId") Long positionId);
    int deleteByUserId(@Param("userId") String userId);
    int deleteByPositionIdAndUserId(@Param("positionId") Long positionId, @Param("userId") String userId);
    int batchInsert(@Param("positionId") Long positionId, @Param("userIds") List<String> userIds);
}

