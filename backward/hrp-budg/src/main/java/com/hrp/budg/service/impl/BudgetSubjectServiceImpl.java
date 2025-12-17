package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetSubjectMapper;
import com.hrp.budg.mapper.BudgetSubjectDeptMapper;
import com.hrp.budg.service.BudgetSubjectService;
import com.hrp.common.entity.BudgetSubject;
import com.hrp.common.entity.BudgetSubjectDept;
import com.hrp.common.entity.Dept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetSubjectServiceImpl implements BudgetSubjectService {

    @Autowired
    private BudgetSubjectMapper budgetSubjectMapper;
    
    @Autowired
    private BudgetSubjectDeptMapper budgetSubjectDeptMapper;

    @Override
    public BudgetSubject getById(Long id) {
        BudgetSubject subject = budgetSubjectMapper.selectById(id);
        if (subject != null) {
            // 加载关联的科室
            List<BudgetSubjectDept> relations = budgetSubjectDeptMapper.selectBySubjectId(id);
            List<Dept> depts = relations.stream()
                .map(rel -> {
                    Dept dept = new Dept();
                    dept.setDeptId(rel.getDeptId());
                    dept.setDeptCode(rel.getDeptCode());
                    dept.setDeptName(rel.getDeptName());
                    return dept;
                })
                .collect(Collectors.toList());
            subject.setRelatedDepts(depts);
        }
        return subject;
    }

    @Override
    public BudgetSubject getByCode(String code) {
        return budgetSubjectMapper.selectByCode(code);
    }

    @Override
    public List<BudgetSubject> getAll() {
        List<BudgetSubject> subjects = budgetSubjectMapper.selectAll();
        // 为每个主体加载关联的科室
        for (BudgetSubject subject : subjects) {
            List<BudgetSubjectDept> relations = budgetSubjectDeptMapper.selectBySubjectId(subject.getSubjectId());
            List<Dept> depts = relations.stream()
                .map(rel -> {
                    Dept dept = new Dept();
                    dept.setDeptId(rel.getDeptId());
                    dept.setDeptCode(rel.getDeptCode());
                    dept.setDeptName(rel.getDeptName());
                    return dept;
                })
                .collect(Collectors.toList());
            subject.setRelatedDepts(depts);
        }
        return subjects;
    }

    @Override
    public List<BudgetSubject> getByParentId(Long parentId) {
        return budgetSubjectMapper.selectByParentId(parentId);
    }

    @Override
    public List<BudgetSubject> getByDeptId(Long deptId) {
        return budgetSubjectMapper.selectByDeptId(deptId);
    }

    @Override
    public List<BudgetSubject> getTree() {
        return budgetSubjectMapper.selectTree();
    }

    @Override
    @Transactional
    public boolean save(BudgetSubject budgetSubject) {
        if (budgetSubject.getIsStop() == null) {
            budgetSubject.setIsStop(0L);
        }
        boolean success = budgetSubjectMapper.insert(budgetSubject) > 0;
        
        // 保存关联的科室
        if (success && budgetSubject.getRelatedDeptIds() != null && !budgetSubject.getRelatedDeptIds().isEmpty()) {
            // 通过SQL查询获取部门信息
            List<Dept> depts = budgetSubjectDeptMapper.selectDeptsByIds(budgetSubject.getRelatedDeptIds());
            for (Long deptId : budgetSubject.getRelatedDeptIds()) {
                Dept dept = depts.stream()
                    .filter(d -> d.getDeptId().equals(deptId))
                    .findFirst()
                    .orElse(null);
                if (dept != null) {
                    BudgetSubjectDept relation = new BudgetSubjectDept();
                    relation.setSubjectId(budgetSubject.getSubjectId());
                    relation.setDeptId(deptId);
                    relation.setDeptCode(dept.getDeptCode());
                    relation.setDeptName(dept.getDeptName());
                    relation.setCreateUser(budgetSubject.getCreateUser());
                    budgetSubjectDeptMapper.insert(relation);
                }
            }
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean update(BudgetSubject budgetSubject) {
        boolean success = budgetSubjectMapper.updateById(budgetSubject) > 0;
        
        // 更新关联的科室
        if (success) {
            // 先删除旧的关联
            budgetSubjectDeptMapper.deleteBySubjectId(budgetSubject.getSubjectId());
            // 插入新的关联
            if (budgetSubject.getRelatedDeptIds() != null && !budgetSubject.getRelatedDeptIds().isEmpty()) {
                // 通过SQL查询获取部门信息
                List<Dept> depts = budgetSubjectDeptMapper.selectDeptsByIds(budgetSubject.getRelatedDeptIds());
                for (Long deptId : budgetSubject.getRelatedDeptIds()) {
                    Dept dept = depts.stream()
                        .filter(d -> d.getDeptId().equals(deptId))
                        .findFirst()
                        .orElse(null);
                    if (dept != null) {
                        BudgetSubjectDept relation = new BudgetSubjectDept();
                        relation.setSubjectId(budgetSubject.getSubjectId());
                        relation.setDeptId(deptId);
                        relation.setDeptCode(dept.getDeptCode());
                        relation.setDeptName(dept.getDeptName());
                        relation.setCreateUser(budgetSubject.getCreateUser());
                        budgetSubjectDeptMapper.insert(relation);
                    }
                }
            }
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        // 删除关联的科室
        budgetSubjectDeptMapper.deleteBySubjectId(id);
        return budgetSubjectMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean stop(Long id) {
        BudgetSubject subject = budgetSubjectMapper.selectById(id);
        if (subject == null) {
            return false;
        }
        subject.setIsStop(1L);
        return budgetSubjectMapper.updateById(subject) > 0;
    }

    @Override
    @Transactional
    public boolean start(Long id) {
        BudgetSubject subject = budgetSubjectMapper.selectById(id);
        if (subject == null) {
            return false;
        }
        subject.setIsStop(0L);
        return budgetSubjectMapper.updateById(subject) > 0;
    }

    @Override
    public List<Dept> getRelatedDepts(Long subjectId) {
        List<BudgetSubjectDept> relations = budgetSubjectDeptMapper.selectBySubjectId(subjectId);
        return relations.stream()
            .map(rel -> {
                Dept dept = new Dept();
                dept.setDeptId(rel.getDeptId());
                dept.setDeptCode(rel.getDeptCode());
                dept.setDeptName(rel.getDeptName());
                return dept;
            })
            .collect(Collectors.toList());
    }
}













