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
    /**
     * 查询所有岗位
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Position> selectAll(@Param("isStop") Long isStop);
    int insert(Position position);
    int updateById(Position position);
    int deleteById(@Param("positionId") Long positionId);
}

