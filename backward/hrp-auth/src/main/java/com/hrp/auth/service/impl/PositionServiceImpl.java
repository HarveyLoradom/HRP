package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.PositionMapper;
import com.hrp.auth.mapper.PositionUserMapper;
import com.hrp.auth.mapper.UserMapper;
import com.hrp.auth.service.PositionService;
import com.hrp.common.entity.Position;
import com.hrp.common.entity.PositionUser;
import com.hrp.common.entity.User;
import com.hrp.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位服务实现类
 */
@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private PositionUserMapper positionUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Position getById(Long positionId) {
        return positionMapper.selectById(positionId);
    }

    @Override
    public Position getByCode(String positionCode) {
        return positionMapper.selectByCode(positionCode);
    }

    @Override
    public List<Position> getAll() {
        return positionMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean save(Position position) {
        // 检查岗位编码是否已存在
        Position existPosition = positionMapper.selectByCode(position.getPositionCode());
        if (existPosition != null) {
            throw new BusinessException(400, "岗位编码已存在");
        }

        if (position.getIsStop() == null) {
            position.setIsStop(0L);
        }

        return positionMapper.insert(position) > 0;
    }

    @Override
    @Transactional
    public boolean update(Position position) {
        if (position.getPositionId() == null) {
            throw new BusinessException(400, "岗位ID不能为空");
        }

        Position existPosition = positionMapper.selectById(position.getPositionId());
        if (existPosition == null) {
            throw new BusinessException(404, "岗位不存在");
        }

        // 如果修改了岗位编码，检查新编码是否已存在
        if (position.getPositionCode() != null && !position.getPositionCode().equals(existPosition.getPositionCode())) {
            Position codeExist = positionMapper.selectByCode(position.getPositionCode());
            if (codeExist != null) {
                throw new BusinessException(400, "岗位编码已存在");
            }
        }

        return positionMapper.updateById(position) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long positionId) {
        if (positionId == null) {
            throw new BusinessException(400, "岗位ID不能为空");
        }

        // 删除岗位人员关联
        positionUserMapper.deleteByPositionId(positionId);

        // 删除岗位
        return positionMapper.deleteById(positionId) > 0;
    }

    @Override
    @Transactional
    public boolean assignUsers(Long positionId, List<String> userIds) {
        if (positionId == null) {
            throw new BusinessException(400, "岗位ID不能为空");
        }

        Position position = positionMapper.selectById(positionId);
        if (position == null) {
            throw new BusinessException(404, "岗位不存在");
        }

        // 先删除该岗位的所有人员关联
        positionUserMapper.deleteByPositionId(positionId);

        // 如果有新的人员，批量插入
        if (userIds != null && !userIds.isEmpty()) {
            // 验证用户是否存在
            for (String userId : userIds) {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    throw new BusinessException(404, "用户不存在: " + userId);
                }
            }
            positionUserMapper.batchInsert(positionId, userIds);
        }

        return true;
    }

    @Override
    public List<User> getUsersByPositionId(Long positionId) {
        List<PositionUser> positionUsers = positionUserMapper.selectByPositionId(positionId);
        if (positionUsers == null || positionUsers.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<String> userIds = positionUsers.stream()
                .map(PositionUser::getUserId)
                .collect(Collectors.toList());

        return userIds.stream()
                .map(userMapper::selectById)
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }
}

