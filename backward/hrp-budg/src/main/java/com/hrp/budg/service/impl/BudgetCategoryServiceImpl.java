package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetCategoryMapper;
import com.hrp.budg.service.BudgetCategoryService;
import com.hrp.common.entity.BudgetCategory;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预算分类服务实现类
 */
@Service
public class BudgetCategoryServiceImpl implements BudgetCategoryService {

    @Autowired
    private BudgetCategoryMapper budgetCategoryMapper;

    @Override
    public PageResult<BudgetCategory> getLevel1Page(Long page, Long size, String budgetYear, String categoryType) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<BudgetCategory> list = budgetCategoryMapper.selectLevel1ByConditions(budgetYear, categoryType);
        PageInfo<BudgetCategory> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public PageResult<BudgetCategory> getLevel2Page(Long page, Long size, String budgetYear, String categoryType, Long parentCategoryId) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<BudgetCategory> list = budgetCategoryMapper.selectLevel2ByConditions(budgetYear, categoryType, parentCategoryId);
        PageInfo<BudgetCategory> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<BudgetCategory> getLevel1List(String budgetYear, String categoryType) {
        return budgetCategoryMapper.selectLevel1ByConditions(budgetYear, categoryType);
    }

    @Override
    public List<BudgetCategory> getLevel2List(String budgetYear, String categoryType, Long parentCategoryId) {
        return budgetCategoryMapper.selectLevel2ByConditions(budgetYear, categoryType, parentCategoryId);
    }

    @Override
    public BudgetCategory getById(Long id) {
        return budgetCategoryMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(BudgetCategory category) {
        // 生成编码
        if (category.getCategoryLevel() == 1) {
            category.setCategoryCode(generateLevel1Code(category.getBudgetYear(), category.getCategoryType()));
        } else if (category.getCategoryLevel() == 2) {
            if (category.getParentCategoryId() == null) {
                throw new BusinessException("二级分类必须选择上级分类");
            }
            BudgetCategory parent = budgetCategoryMapper.selectById(category.getParentCategoryId());
            if (parent == null) {
                throw new BusinessException("上级分类不存在");
            }
            category.setParentCategoryCode(parent.getCategoryCode());
            category.setBudgetYear(parent.getBudgetYear());
            category.setCategoryType(parent.getCategoryType());
            category.setCategoryCode(generateLevel2Code(parent.getCategoryCode()));
        }
        
        if (category.getIsStop() == null) {
            category.setIsStop(0L);
        }
        
        return budgetCategoryMapper.insert(category) > 0;
    }

    @Override
    @Transactional
    public boolean update(BudgetCategory category) {
        return budgetCategoryMapper.updateById(category) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        BudgetCategory category = budgetCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查是否可以删除
        if (category.getCategoryLevel() == 1) {
            // 一级分类：检查是否有二级分类
            int count = budgetCategoryMapper.countLevel2ByParentId(id);
            if (count > 0) {
                throw new BusinessException("该一级分类下存在二级分类，无法删除");
            }
        } else if (category.getCategoryLevel() == 2) {
            // 二级分类：检查是否有项目预算
            int count = budgetCategoryMapper.countItemsByCategoryId(id);
            if (count > 0) {
                throw new BusinessException("该二级分类下存在项目预算，无法删除");
            }
        }
        
        return budgetCategoryMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean stop(Long id) {
        BudgetCategory category = budgetCategoryMapper.selectById(id);
        if (category == null) {
            return false;
        }
        category.setIsStop(1L);
        return budgetCategoryMapper.updateById(category) > 0;
    }

    @Override
    @Transactional
    public boolean start(Long id) {
        BudgetCategory category = budgetCategoryMapper.selectById(id);
        if (category == null) {
            return false;
        }
        category.setIsStop(0L);
        return budgetCategoryMapper.updateById(category) > 0;
    }

    @Override
    public String generateLevel1Code(String budgetYear, String categoryType) {
        // 规则：年度+预算分类（大写首字母）+01+001
        // 例如：2025SR01（2025年收入预算第一个一级分类）
        String typePrefix = "";
        if ("INCOME".equals(categoryType)) {
            typePrefix = "SR"; // 收入预算：ShouRu
        } else if ("EXPENSE".equals(categoryType)) {
            typePrefix = "ZC"; // 支出预算：ZhiChu
        } else {
            typePrefix = categoryType.substring(0, 2).toUpperCase();
        }
        
        // 查询最大编码
        String maxCode = budgetCategoryMapper.selectMaxLevel1Code(budgetYear, categoryType);
        int sequence = 1;
        
        if (maxCode != null && maxCode.length() >= 8) {
            // 提取序号部分（最后2位）
            try {
                String seqStr = maxCode.substring(maxCode.length() - 2);
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 生成编码：年度(4位) + 类型前缀(2位) + 序号(2位)
        return String.format("%s%s%02d", budgetYear, typePrefix, sequence);
    }

    @Override
    public String generateLevel2Code(String parentCategoryCode) {
        // 规则：在上级分类编码后追加3位序号
        // 例如：2025SR01001（2025年收入预算第一个一级分类下的第一个二级分类）
        String maxCode = budgetCategoryMapper.selectMaxLevel2Code(parentCategoryCode);
        int sequence = 1;
        
        if (maxCode != null && maxCode.length() >= parentCategoryCode.length() + 3) {
            // 提取序号部分（最后3位）
            try {
                String seqStr = maxCode.substring(maxCode.length() - 3);
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 生成编码：父编码 + 序号(3位)
        return String.format("%s%03d", parentCategoryCode, sequence);
    }
}

