package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.HrAttLedgerMapper;
import com.hrp.common.entity.HrAttLedger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工考勤台账表数据访问实现类
 */
@Repository
public class HrAttLedgerMapperImpl implements HrAttLedgerMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.hr.mapper.HrAttLedgerMapper";

    @Override
    public HrAttLedger selectById(Long ledgerId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", ledgerId);
    }

    @Override
    public HrAttLedger selectByEmpIdAndMonth(Long empId, String attMonth) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("attMonth", attMonth);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByEmpIdAndMonth", params);
    }

    @Override
    public List<HrAttLedger> selectByConditions(Long empId, String attMonth, String ledgerStatus,
                                                String empCode, String empName, Long deptId) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("attMonth", attMonth);
        params.put("ledgerStatus", ledgerStatus);
        params.put("empCode", empCode);
        params.put("empName", empName);
        params.put("deptId", deptId);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public int insert(HrAttLedger ledger) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", ledger);
    }

    @Override
    public int updateById(HrAttLedger ledger) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", ledger);
    }

    @Override
    public int deleteById(Long ledgerId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", ledgerId);
    }
}

