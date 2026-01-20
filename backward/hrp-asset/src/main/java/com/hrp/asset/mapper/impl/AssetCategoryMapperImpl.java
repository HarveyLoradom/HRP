package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetCategoryMapper;
import com.hrp.common.entity.AssetCategory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssetCategoryMapperImpl implements AssetCategoryMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetCategoryMapper";

    @Override
    public List<AssetCategory> selectLevel1ByConditions(Integer status, String categoryName) {
        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status);
        }
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            params.put("categoryName", categoryName);
        }
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectLevel1ByConditions", params);
    }

    @Override
    public List<AssetCategory> selectLevel2ByConditions(Long parentId, Integer status, String categoryName) {
        Map<String, Object> params = new HashMap<>();
        if (parentId != null) {
            params.put("parentId", parentId);
        }
        if (status != null) {
            params.put("status", status);
        }
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            params.put("categoryName", categoryName);
        }
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectLevel2ByConditions", params);
    }

    @Override
    public List<AssetCategory> selectLevel3ByConditions(Long parentId, Long level1Id, Integer status, String categoryName) {
        Map<String, Object> params = new HashMap<>();
        
        // 如果指定了parentId，使用parentId过滤
        if (parentId != null && parentId != 0) {
            params.put("parentId", parentId);
            params.put("useParentId", "true");
        } 
        // 否则如果指定了level1Id，使用level1Id过滤
        else if (level1Id != null && level1Id != 0) {
            params.put("level1Id", level1Id);
            params.put("useLevel1Id", "true");
        }
        
        if (status != null) {
            params.put("status", status);
        }
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            params.put("categoryName", categoryName);
        }
        
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectLevel3ByConditions", params);
    }

    @Override
    public AssetCategory selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public AssetCategory selectByCode(String code) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", code);
    }

    @Override
    public int insert(AssetCategory category) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", category);
    }

    @Override
    public int updateById(AssetCategory category) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", category);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public String selectMaxCodeByParentId(Long parentId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxCodeByParentId", parentId);
    }

    @Override
    public int countChildrenByParentId(Long parentId) {
        Integer count = sqlSessionTemplate.selectOne(NAMESPACE + ".countChildrenByParentId", parentId);
        return count != null ? count : 0;
    }

    @Override
    public int countActiveChildrenByParentId(Long parentId) {
        Integer count = sqlSessionTemplate.selectOne(NAMESPACE + ".countActiveChildrenByParentId", parentId);
        return count != null ? count : 0;
    }

    @Override
    public List<AssetCategory> selectAllLevel1(Integer status) {
        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status);
        }
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllLevel1", params);
    }

    @Override
    public List<AssetCategory> selectAllLevel2ByParentId(Long parentId, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", parentId);
        if (status != null) {
            params.put("status", status);
        }
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllLevel2ByParentId", params);
    }

    @Override
    public List<AssetCategory> selectAllLevel3ByParentId(Long parentId, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", parentId);
        if (status != null) {
            params.put("status", status);
        }
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllLevel3ByParentId", params);
    }

    @Override
    public AssetCategory selectByNameAndLevel(String categoryName, Integer level, Long parentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("categoryName", categoryName);
        params.put("level", level);
        if (parentId != null) {
            params.put("parentId", parentId);
        }
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByNameAndLevel", params);
    }
}

