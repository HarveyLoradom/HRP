package com.hrp.user.mapper;

import com.hrp.common.entity.Dept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门数据访问接口
 */
public interface DeptMapper {
    /**
     * 根据ID查询部门
     */
    Dept selectById(@Param("deptId") Long deptId);

    /**
     * 根据编码查询部门
     */
    Dept selectByCode(@Param("deptCode") String deptCode);

    /**
     * 查询所有部门
     */
    List<Dept> selectAll();

    /**
     * 根据上级部门编码查询部门列表
     */
    List<Dept> selectBySuperDeptCode(@Param("superDeptCode") String superDeptCode);

    /**
     * 插入部门
     */
    int insert(Dept dept);

    /**
     * 更新部门
     */
    int updateById(Dept dept);

    /**
     * 删除部门
     */
    int deleteById(@Param("deptId") Long deptId);
}
