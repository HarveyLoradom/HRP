package com.hrp.auth.controller;

import com.hrp.common.entity.Position;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.User;
import com.hrp.auth.service.PositionService;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 岗位管理控制器
 */
@RestController
@RequestMapping("/auth/position")
@CrossOrigin
public class PositionController {

    @Autowired
    private PositionService positionService;

    /**
     * 根据ID查询岗位
     */
    @GetMapping("/{positionId}")
    public Result<Position> getById(@PathVariable Long positionId) {
        Position position = positionService.getById(positionId);
        if (position == null) {
            return Result.error("岗位不存在");
        }
        return Result.success(position);
    }

    /**
     * 根据编码查询岗位
     */
    @GetMapping("/code/{positionCode}")
    public Result<Position> getByCode(@PathVariable String positionCode) {
        Position position = positionService.getByCode(positionCode);
        if (position == null) {
            return Result.error("岗位不存在");
        }
        return Result.success(position);
    }

    /**
     * 查询所有岗位
     */
    @GetMapping("/list")
    public Result<List<Position>> getAll() {
        List<Position> list = positionService.getAll();
        return Result.success(list);
    }

    /**
     * 新增岗位
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Position position, HttpServletRequest request) {
        try {
            // 从token中获取当前用户
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String account = JwtUtil.getAccount(token);
                position.setCreateUser(account);
            }

            boolean success = positionService.save(position);
            if (success) {
                return Result.success("新增成功");
            }
            return Result.error("新增失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新岗位
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Position position) {
        try {
            boolean success = positionService.update(position);
            if (success) {
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{positionId}")
    public Result<String> delete(@PathVariable Long positionId) {
        try {
            boolean success = positionService.delete(positionId);
            if (success) {
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为岗位分配人员
     */
    @PostMapping("/{positionId}/assign-users")
    public Result<String> assignUsers(@PathVariable Long positionId, @RequestBody List<String> userIds) {
        try {
            boolean success = positionService.assignUsers(positionId, userIds);
            if (success) {
                return Result.success("分配成功");
            }
            return Result.error("分配失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据岗位ID查询人员列表
     */
    @GetMapping("/{positionId}/users")
    public Result<List<User>> getUsersByPositionId(@PathVariable Long positionId) {
        List<User> users = positionService.getUsersByPositionId(positionId);
        return Result.success(users);
    }
}

