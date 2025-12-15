package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.DeptMapper;
import com.hrp.auth.service.DeptService;
import com.hrp.common.entity.Dept;
import com.hrp.common.entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门服务实现类
 */
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public Dept getById(Long deptId) {
        return deptMapper.selectById(deptId);
    }

    @Override
    public Dept getByCode(String deptCode) {
        return deptMapper.selectByCode(deptCode);
    }

    @Override
    public List<Dept> getAll() {
        return deptMapper.selectAll();
    }

    @Override
    public List<Dept> getBySuperDeptCode(String superDeptCode) {
        return deptMapper.selectBySuperDeptCode(superDeptCode);
    }

    @Override
    @Transactional
    public boolean save(Dept dept) {
        if (dept.getIsStop() == null) {
            dept.setIsStop(0L);
        }
        if (dept.getCreateTime() == null) {
            dept.setCreateTime(LocalDateTime.now());
        }
        return deptMapper.insert(dept) > 0;
    }

    @Override
    @Transactional
    public boolean update(Dept dept) {
        dept.setUpdateTime(LocalDateTime.now());
        return deptMapper.updateById(dept) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long deptId) {
        Dept dept = new Dept();
        dept.setDeptId(deptId);
        dept.setIsStop(1L);
        return deptMapper.updateById(dept) > 0;
    }

    @Override
    public PageResult<Dept> getAllPage(Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<Dept> records = deptMapper.selectAllPage(offset, size);
        Long total = deptMapper.countAll();
        return PageResult.of(records, total, size, page);
    }

    @Override
    @Transactional
    public com.hrp.common.entity.Result<String> importDepts(List<List<String>> dataList, String createUser) {
        if (dataList == null || dataList.isEmpty()) {
            return com.hrp.common.entity.Result.error("导入数据为空");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        // 跳过表头，从第二行开始
        for (int i = 1; i < dataList.size(); i++) {
            List<String> row = dataList.get(i);
            if (row == null || row.isEmpty()) {
                continue;
            }

            try {
                // Excel列顺序：部门编码、部门名称、上级部门编码、部门级别、部门电话
                if (row.size() < 3) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：数据列数不足\n");
                    continue;
                }

                String deptCode = row.get(0) != null ? row.get(0).trim() : "";
                String deptName = row.get(1) != null ? row.get(1).trim() : "";
                String superDeptCode = row.size() > 2 && row.get(2) != null ? row.get(2).trim() : "";
                String deptLevelStr = row.size() > 3 && row.get(3) != null ? row.get(3).trim() : "1";
                String deptPhone = row.size() > 4 && row.get(4) != null ? row.get(4).trim() : "";

                // 验证必填字段
                if (deptCode.isEmpty() || deptName.isEmpty()) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：部门编码和部门名称不能为空\n");
                    continue;
                }

                // 检查部门编码是否已存在
                Dept existDept = deptMapper.selectByCode(deptCode);
                if (existDept != null) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：部门编码").append(deptCode).append("已存在\n");
                    continue;
                }

                // 创建部门对象
                Dept dept = new Dept();
                dept.setDeptCode(deptCode);
                dept.setDeptName(deptName);
                dept.setSuperDeptCode(superDeptCode.isEmpty() ? null : superDeptCode);
                try {
                    dept.setDeptLevel(Long.parseLong(deptLevelStr));
                } catch (NumberFormatException e) {
                    dept.setDeptLevel(1L);
                }
                dept.setDeptPhone(deptPhone);
                dept.setCreateUser(createUser);
                dept.setCreateTime(LocalDateTime.now());
                dept.setIsStop(0L);

                // 保存部门
                boolean success = save(dept);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：保存失败\n");
                }
            } catch (Exception e) {
                failCount++;
                errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
            }
        }

        String message = String.format("导入完成：成功%d条，失败%d条", successCount, failCount);
        if (failCount > 0 && errorMsg.length() > 0) {
            message += "\n错误详情：\n" + errorMsg.toString();
        }

        if (failCount == 0) {
            return com.hrp.common.entity.Result.success(message);
        } else if (successCount > 0) {
            return com.hrp.common.entity.Result.error(message);
        } else {
            return com.hrp.common.entity.Result.error(message);
        }
    }
}

