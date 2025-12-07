package com.hrp.user.mapper;

import com.hrp.common.entity.Clinic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 科室数据访问接口
 */
public interface ClinicMapper {
    /**
     * 根据ID查询科室
     */
    Clinic selectById(@Param("clinicId") Long clinicId);

    /**
     * 根据编码查询科室
     */
    Clinic selectByCode(@Param("clinicCode") String clinicCode);

    /**
     * 查询所有科室
     */
    List<Clinic> selectAll();

    /**
     * 根据部门ID查询科室列表
     */
    List<Clinic> selectByDeptId(@Param("deptId") Long deptId);

    /**
     * 插入科室
     */
    int insert(Clinic clinic);

    /**
     * 更新科室
     */
    int updateById(Clinic clinic);

    /**
     * 删除科室
     */
    int deleteById(@Param("clinicId") Long clinicId);
}
