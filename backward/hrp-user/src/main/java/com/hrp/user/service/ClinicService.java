package com.hrp.user.service;

import com.hrp.common.entity.Clinic;

import java.util.List;

/**
 * 科室服务接口
 */
public interface ClinicService {
    /**
     * 根据ID查询科室
     */
    Clinic getById(Long clinicId);

    /**
     * 根据编码查询科室
     */
    Clinic getByCode(String clinicCode);

    /**
     * 查询所有科室
     */
    List<Clinic> getAll();

    /**
     * 根据部门ID查询科室列表
     */
    List<Clinic> getByDeptId(Long deptId);

    /**
     * 新增科室
     */
    boolean save(Clinic clinic);

    /**
     * 更新科室
     */
    boolean update(Clinic clinic);

    /**
     * 删除科室（逻辑删除）
     */
    boolean delete(Long clinicId);
}
