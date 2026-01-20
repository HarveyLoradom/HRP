package com.hrp.auth.service;

import com.hrp.common.entity.Position;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface PositionService {
    Position getById(Long positionId);
    Position getByCode(String positionCode);
    /**
     * 查询所有岗位
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Position> getAll(Long isStop);
    boolean save(Position position);
    boolean update(Position position);
    boolean delete(Long positionId);
    boolean assignUsers(Long positionId, List<String> userIds);
    List<com.hrp.common.entity.User> getUsersByPositionId(Long positionId);
    /**
     * 根据岗位代码获取该岗位下的所有员工
     */
    List<com.hrp.common.entity.User> getUsersByPositionCode(String positionCode);
}

