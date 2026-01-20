package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.AssetCategoryMapper;
import com.hrp.asset.service.AssetCategoryService;
import com.hrp.common.entity.AssetCategory;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 资产分类服务实现类
 */
@Service
public class AssetCategoryServiceImpl implements AssetCategoryService {

    @Autowired
    private AssetCategoryMapper assetCategoryMapper;

    @Override
    public PageResult<AssetCategory> getLevel1Page(Long page, Long size, Integer status, String categoryName) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetCategory> list = assetCategoryMapper.selectLevel1ByConditions(status, categoryName);
        PageInfo<AssetCategory> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<AssetCategory> getLevel2Page(Long page, Long size, Long parentId, Integer status, String categoryName) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetCategory> list = assetCategoryMapper.selectLevel2ByConditions(parentId, status, categoryName);
        PageInfo<AssetCategory> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<AssetCategory> getLevel3Page(Long page, Long size, Long parentId, Long level1Id, Integer status, String categoryName) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetCategory> list = assetCategoryMapper.selectLevel3ByConditions(parentId, level1Id, status, categoryName);
        PageInfo<AssetCategory> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<AssetCategory> getLevel1List(Integer status) {
        return assetCategoryMapper.selectAllLevel1(status);
    }

    @Override
    public List<AssetCategory> getLevel2List(Long parentId, Integer status) {
        return assetCategoryMapper.selectAllLevel2ByParentId(parentId, status);
    }

    @Override
    public List<AssetCategory> getLevel3List(Long parentId, Integer status) {
        return assetCategoryMapper.selectAllLevel3ByParentId(parentId, status);
    }

    @Override
    public AssetCategory getById(Long id) {
        return assetCategoryMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(AssetCategory category) {
        // 一级分类：编码手动输入，不需要自动生成
        if (category.getLevel() == 1) {
            category.setParentId(0L);
            // 检查编码是否已存在
            AssetCategory existing = assetCategoryMapper.selectByCode(category.getCategoryCode());
            if (existing != null) {
                throw new BusinessException("分类编码已存在");
            }
        } else if (category.getLevel() == 2) {
            // 二级分类：自动生成编码（一级编码+001）
            if (category.getParentId() == null) {
                throw new BusinessException("二级分类必须选择上级分类");
            }
            AssetCategory parent = assetCategoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new BusinessException("上级分类不存在");
            }
            if (parent.getLevel() != 1) {
                throw new BusinessException("二级分类的上级分类必须是一级分类");
            }
            category.setCategoryCode(generateLevel2Code(category.getParentId()));
        } else if (category.getLevel() == 3) {
            // 三级分类：自动生成编码（二级编码+001）
            if (category.getParentId() == null) {
                throw new BusinessException("三级分类必须选择上级分类");
            }
            AssetCategory parent = assetCategoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new BusinessException("上级分类不存在");
            }
            if (parent.getLevel() != 2) {
                throw new BusinessException("三级分类的上级分类必须是二级分类");
            }
            category.setCategoryCode(generateLevel3Code(category.getParentId()));
        }
        
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        return assetCategoryMapper.insert(category) > 0;
    }

    @Override
    @Transactional
    public boolean update(AssetCategory category) {
        return assetCategoryMapper.updateById(category) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        AssetCategory category = assetCategoryMapper.selectById(id);
        if (category == null) {
            return false;
        }
        
        // 检查是否有子分类（任何状态的子分类都不能存在）
        int childCount = assetCategoryMapper.countChildrenByParentId(id);
        if (childCount > 0) {
            String levelName = category.getLevel() == 1 ? "一级分类" : (category.getLevel() == 2 ? "二级分类" : "三级分类");
            String childLevelName = category.getLevel() == 1 ? "二级分类" : "三级分类";
            throw new BusinessException("该" + levelName + "下存在" + childLevelName + "，无法删除");
        }
        
        return assetCategoryMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean stop(Long id) {
        AssetCategory category = assetCategoryMapper.selectById(id);
        if (category == null) {
            return false;
        }
        
        // 检查是否有未停用的子分类
        // 一级分类：检查是否有未停用的二级分类
        // 二级分类：检查是否有未停用的三级分类
        int activeChildCount = assetCategoryMapper.countActiveChildrenByParentId(id);
        if (activeChildCount > 0) {
            String levelName = category.getLevel() == 1 ? "一级分类" : "二级分类";
            String childLevelName = category.getLevel() == 1 ? "二级分类" : "三级分类";
            throw new BusinessException("该" + levelName + "下存在未停用的" + childLevelName + "，无法停用");
        }
        
        category.setStatus(0);
        return assetCategoryMapper.updateById(category) > 0;
    }

    @Override
    @Transactional
    public boolean start(Long id) {
        AssetCategory category = assetCategoryMapper.selectById(id);
        if (category == null) {
            return false;
        }
        category.setStatus(1);
        return assetCategoryMapper.updateById(category) > 0;
    }

    @Override
    public String generateLevel2Code(Long parentId) {
        AssetCategory parent = assetCategoryMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("上级分类不存在");
        }
        
        String maxCode = assetCategoryMapper.selectMaxCodeByParentId(parentId);
        if (maxCode == null || maxCode.isEmpty()) {
            // 如果没有子分类，从001开始
            return parent.getCategoryCode() + "001";
        }
        
        // 提取最后3位数字，加1
        String parentCode = parent.getCategoryCode();
        String suffix = maxCode.substring(parentCode.length());
        int nextNum = Integer.parseInt(suffix) + 1;
        
        return parentCode + String.format("%03d", nextNum);
    }

    @Override
    public String generateLevel3Code(Long parentId) {
        AssetCategory parent = assetCategoryMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("上级分类不存在");
        }
        
        String maxCode = assetCategoryMapper.selectMaxCodeByParentId(parentId);
        if (maxCode == null || maxCode.isEmpty()) {
            // 如果没有子分类，从001开始
            return parent.getCategoryCode() + "001";
        }
        
        // 提取最后3位数字，加1
        String parentCode = parent.getCategoryCode();
        String suffix = maxCode.substring(parentCode.length());
        int nextNum = Integer.parseInt(suffix) + 1;
        
        return parentCode + String.format("%03d", nextNum);
    }
}

