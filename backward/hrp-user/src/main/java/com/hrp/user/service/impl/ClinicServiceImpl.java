package com.hrp.user.service.impl;

import com.hrp.user.mapper.ClinicMapper;
import com.hrp.user.service.ClinicService;
import com.hrp.common.entity.Clinic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 科室服务实现类
 */
@Service
public class ClinicServiceImpl implements ClinicService {

    @Autowired
    private ClinicMapper clinicMapper;

    @Override
    public Clinic getById(Long clinicId) {
        return clinicMapper.selectById(clinicId);
    }

    @Override
    public Clinic getByCode(String clinicCode) {
        return clinicMapper.selectByCode(clinicCode);
    }

    @Override
    public List<Clinic> getAll() {
        return clinicMapper.selectAll();
    }

    @Override
    public List<Clinic> getByDeptId(Long deptId) {
        return clinicMapper.selectByDeptId(deptId);
    }

    @Override
    @Transactional
    public boolean save(Clinic clinic) {
        if (clinic.getIsStop() == null) {
            clinic.setIsStop(0L);
        }
        if (clinic.getCreateTime() == null) {
            clinic.setCreateTime(LocalDateTime.now());
        }
        return clinicMapper.insert(clinic) > 0;
    }

    @Override
    @Transactional
    public boolean update(Clinic clinic) {
        clinic.setUpdateTime(LocalDateTime.now());
        return clinicMapper.updateById(clinic) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long clinicId) {
        Clinic clinic = new Clinic();
        clinic.setClinicId(clinicId);
        clinic.setIsStop(1L);
        return clinicMapper.updateById(clinic) > 0;
    }
}
