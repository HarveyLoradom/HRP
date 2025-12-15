package com.hrp.auth.mapper;

import com.hrp.common.entity.Position;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位数据访问接口
 */
public interface PositionMapper {
    Position selectById(@Param("positionId") Long positionId);
    Position selectByCode(@Param("positionCode") String positionCode);
    List<Position> selectAll();
    int insert(Position position);
    int updateById(Position position);
    int deleteById(@Param("positionId") Long positionId);
}

