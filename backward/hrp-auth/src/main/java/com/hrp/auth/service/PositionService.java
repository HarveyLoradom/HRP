package com.hrp.auth.service;

import com.hrp.common.entity.Position;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface PositionService {
    Position getById(Long positionId);
    Position getByCode(String positionCode);
    List<Position> getAll();
    boolean save(Position position);
    boolean update(Position position);
    boolean delete(Long positionId);
    boolean assignUsers(Long positionId, List<String> userIds);
    List<com.hrp.common.entity.User> getUsersByPositionId(Long positionId);
}

