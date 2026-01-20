package com.hrp.cost.feign;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.User;
import com.hrp.common.entity.Dept;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Auth服务Feign客户端
 */
@FeignClient(name = "hrp-auth", path = "/auth")
public interface AuthServiceClient {
    
    /**
     * 根据账号查询用户信息
     * 
     * @param account 用户账号
     * @return 用户信息
     */
    @GetMapping("/user/account/{account}")
    Result<User> getUserByAccount(@PathVariable("account") String account);
    
    /**
     * 根据用户ID查询用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    Result<User> getUserById(@PathVariable("userId") String userId);
    
    /**
     * 根据部门编码查询部门信息
     * 
     * @param deptCode 部门编码
     * @return 部门信息
     */
    @GetMapping("/dept/code/{deptCode}")
    Result<Dept> getDeptByCode(@PathVariable("deptCode") String deptCode);
}

