package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetCategoryMapper;
import com.hrp.budg.mapper.BudgetItemMapper;
import com.hrp.budg.mapper.BudgetItemSubjectMapper;
import com.hrp.budg.mapper.BudgetSubjectMapper;
import com.hrp.budg.service.BudgetItemService;
import com.hrp.common.entity.BudgetCategory;
import com.hrp.common.entity.BudgetItem;
import com.hrp.common.entity.BudgetItemSubject;
import com.hrp.common.entity.BudgetSubject;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetItemServiceImpl implements BudgetItemService {

    @Autowired
    private BudgetItemMapper budgetItemMapper;
    
    @Autowired
    private BudgetCategoryMapper budgetCategoryMapper;
    
    @Autowired
    private BudgetItemSubjectMapper budgetItemSubjectMapper;
    
    @Autowired
    private BudgetSubjectMapper budgetSubjectMapper;

    @Override
    public BudgetItem getById(Long id) {
        BudgetItem item = budgetItemMapper.selectById(id);
        if (item != null) {
            // 加载分配的主体列表
            item.setAssignedSubjects(budgetItemSubjectMapper.selectSubjectsByItemId(id));
        }
        return item;
    }

    @Override
    public BudgetItem getByCode(String code) {
        return budgetItemMapper.selectByCode(code);
    }

    @Override
    public List<BudgetItem> getAll() {
        List<BudgetItem> items = budgetItemMapper.selectAll();
        // 加载每个项目分配的主体列表
        for (BudgetItem item : items) {
            item.setAssignedSubjects(budgetItemSubjectMapper.selectSubjectsByItemId(item.getItemId()));
        }
        return items;
    }

    @Override
    public List<BudgetItem> getByCategoryId(Long categoryId) {
        return budgetItemMapper.selectByCategoryId(categoryId);
    }

    @Override
    public PageResult<BudgetItem> getPage(Long page, Long size, String budgetYear, String categoryType,
                                          Long level1CategoryId, Long level2CategoryId, String itemName, Long isStop) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<BudgetItem> list = budgetItemMapper.selectPageByConditions(budgetYear, categoryType, 
                                                                        level1CategoryId, level2CategoryId, itemName, isStop);
        PageInfo<BudgetItem> pageInfo = new PageInfo<>(list);
        // 加载每个项目分配的主体列表
        for (BudgetItem item : list) {
            item.setAssignedSubjects(budgetItemSubjectMapper.selectSubjectsByItemId(item.getItemId()));
        }
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional
    public boolean save(BudgetItem budgetItem) {
        // 验证上级分类（必须是二级分类）
        if (budgetItem.getParentCategoryId() == null) {
            throw new BusinessException("必须选择上级分类（二级分类）");
        }
        BudgetCategory parentCategory = budgetCategoryMapper.selectById(budgetItem.getParentCategoryId());
        if (parentCategory == null) {
            throw new BusinessException("上级分类不存在");
        }
        if (parentCategory.getCategoryLevel() != 2) {
            throw new BusinessException("只能选择二级分类作为上级分类");
        }
        
        // 设置分类相关信息
        // parentCategory 是二级分类
        budgetItem.setCategoryId(parentCategory.getCategoryId());  // 二级分类ID
        budgetItem.setCategoryCode(parentCategory.getCategoryCode());  // 二级分类编码
        budgetItem.setCategoryType(parentCategory.getCategoryType());  // 分类类型
        budgetItem.setBudgetYear(parentCategory.getBudgetYear());
        
        // 获取一级分类信息（通过二级分类的parent_category_id）
        if (parentCategory.getParentCategoryId() != null) {
            BudgetCategory level1Category = budgetCategoryMapper.selectById(parentCategory.getParentCategoryId());
            if (level1Category != null) {
                budgetItem.setParentCategoryId(level1Category.getCategoryId());  // 一级分类ID
                budgetItem.setParentCategoryCode(level1Category.getCategoryCode());  // 一级分类编码
            }
        }
        
        // 生成项目编码：年度+分类首字母+01+二级分类序号+3位项目序号
        // 例如：2025SR01001001（2025年收入预算，一级分类01，二级分类001，项目001）
        if (budgetItem.getItemCode() == null || budgetItem.getItemCode().isEmpty()) {
            budgetItem.setItemCode(generateItemCode(parentCategory.getCategoryCode(), parentCategory.getBudgetYear()));
        }
        
        // 设置默认值
        if (budgetItem.getIsCentralized() == null) {
            budgetItem.setIsCentralized(0L);
        }
        if (budgetItem.getAllowAdjust() == null) {
            budgetItem.setAllowAdjust(1L);
        }
        if (budgetItem.getIsStop() == null) {
            budgetItem.setIsStop(0L);
        }
        
        // 如果createUser为空，设置默认值
        if (budgetItem.getCreateUser() == null || budgetItem.getCreateUser().isEmpty()) {
            budgetItem.setCreateUser("SYSTEM");
        }
        
        return budgetItemMapper.insert(budgetItem) > 0;
    }

    @Override
    @Transactional
    public boolean update(BudgetItem budgetItem) {
        // 如果修改了分类，需要更新相关的分类信息
        if (budgetItem.getParentCategoryId() != null) {
            BudgetCategory parentCategory = budgetCategoryMapper.selectById(budgetItem.getParentCategoryId());
            if (parentCategory == null) {
                throw new BusinessException("上级分类不存在");
            }
            if (parentCategory.getCategoryLevel() != 2) {
                throw new BusinessException("只能选择二级分类作为上级分类");
            }
            
            // 设置分类相关信息
            budgetItem.setCategoryId(parentCategory.getCategoryId());  // 二级分类ID
            budgetItem.setCategoryCode(parentCategory.getCategoryCode());  // 二级分类编码
            budgetItem.setCategoryType(parentCategory.getCategoryType());  // 分类类型
            budgetItem.setBudgetYear(parentCategory.getBudgetYear());
            
            // 获取一级分类信息（通过二级分类的parent_category_id）
            if (parentCategory.getParentCategoryId() != null) {
                BudgetCategory level1Category = budgetCategoryMapper.selectById(parentCategory.getParentCategoryId());
                if (level1Category != null) {
                    budgetItem.setParentCategoryId(level1Category.getCategoryId());  // 一级分类ID
                    budgetItem.setParentCategoryCode(level1Category.getCategoryCode());  // 一级分类编码
                }
            }
            
            // 如果分类改变了，可能需要重新生成项目编码
            // 但为了数据安全，这里不自动重新生成编码，保持原有编码
            // 如果需要重新生成，可以在前端或业务层面单独处理
        }
        
        return budgetItemMapper.updateById(budgetItem) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        // 检查是否有预算编制或执行记录
        int count = budgetItemMapper.countBudgetsByItemId(id);
        if (count > 0) {
            throw new BusinessException("该项目存在预算编制或执行记录，无法删除");
        }
        
        // 删除分配的主体关系
        budgetItemSubjectMapper.deleteByItemId(id);
        
        return budgetItemMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean stop(Long id) {
        BudgetItem item = budgetItemMapper.selectById(id);
        if (item == null) {
            return false;
        }
        item.setIsStop(1L);
        return budgetItemMapper.updateById(item) > 0;
    }

    @Override
    @Transactional
    public boolean start(Long id) {
        BudgetItem item = budgetItemMapper.selectById(id);
        if (item == null) {
            return false;
        }
        item.setIsStop(0L);
        return budgetItemMapper.updateById(item) > 0;
    }

    @Override
    @Transactional
    public boolean assignSubjects(Long itemId, List<Long> subjectIds, String createUser) {
        // 删除旧的分配关系
        budgetItemSubjectMapper.deleteByItemId(itemId);
        
        if (subjectIds == null || subjectIds.isEmpty()) {
            return true;
        }
        
        // 如果没有传入createUser，使用默认值
        if (createUser == null || createUser.isEmpty()) {
            createUser = "SYSTEM";
        }
        
        // 插入新的分配关系
        List<BudgetItemSubject> list = new ArrayList<>();
        
        for (Long subjectId : subjectIds) {
            BudgetSubject subject = budgetSubjectMapper.selectById(subjectId);
            if (subject == null) {
                continue;
            }
            BudgetItemSubject itemSubject = new BudgetItemSubject();
            itemSubject.setItemId(itemId);
            itemSubject.setSubjectId(subjectId);
            itemSubject.setSubjectCode(subject.getSubjectCode());
            itemSubject.setSubjectName(subject.getSubjectName());
            itemSubject.setCreateUser(createUser);
            list.add(itemSubject);
        }
        
        if (!list.isEmpty()) {
            return budgetItemSubjectMapper.insertBatch(list) > 0;
        }
        return true;
    }

    @Override
    public List<BudgetSubject> getAssignedSubjects(Long itemId) {
        return budgetItemSubjectMapper.selectSubjectsByItemId(itemId);
    }

    @Override
    public List<BudgetItem> getAssignedItems(Long subjectId) {
        return budgetItemSubjectMapper.selectItemsBySubjectId(subjectId);
    }
    
    /**
     * 生成项目编码
     * 规则：二级分类编码 + 3位序号
     * 例如：2025SR01001 -> 2025SR01001001, 2025SR01001002, ...
     */
    private String generateItemCode(String categoryCode, String budgetYear) {
        String maxCode = budgetItemMapper.selectMaxItemCodeByCategoryCode(categoryCode);
        int sequence = 1;
        
        if (maxCode != null && maxCode.startsWith(categoryCode)) {
            // 提取序号部分（最后3位）
            String seqStr = maxCode.substring(categoryCode.length());
            try {
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 格式化为3位数字
        return categoryCode + String.format("%03d", sequence);
    }
}













