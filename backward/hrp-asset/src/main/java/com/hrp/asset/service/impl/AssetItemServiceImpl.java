package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.AssetAccountMapper;
import com.hrp.asset.mapper.AssetCategoryMapper;
import com.hrp.asset.mapper.AssetItemMapper;
import com.hrp.asset.service.AssetItemService;
import com.hrp.common.entity.AssetAccount;
import com.hrp.common.entity.AssetCategory;
import com.hrp.common.entity.AssetItem;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产信息维护服务实现类
 */
@Service
public class AssetItemServiceImpl implements AssetItemService {

    @Autowired
    private AssetItemMapper assetItemMapper;
    
    @Autowired
    private AssetCategoryMapper assetCategoryMapper;
    
    @Autowired
    private AssetAccountMapper assetAccountMapper;

    @Override
    public PageResult<AssetItem> getPage(Long page, Long size, String assetCode, String assetName, Long level1Id, Long level2Id, Long categoryId, Integer status) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetItem> list = assetItemMapper.selectByConditions(assetCode, assetName, level1Id, level2Id, categoryId, status);
        PageInfo<AssetItem> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<AssetItem> getList(String assetCode, String assetName, Long level1Id, Long level2Id, Long categoryId, Integer status) {
        return assetItemMapper.selectByConditions(assetCode, assetName, level1Id, level2Id, categoryId, status);
    }

    @Override
    public AssetItem getById(Long id) {
        return assetItemMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(AssetItem item) {
        // 如果资产编码为空，自动生成
        if (item.getAssetCode() == null || item.getAssetCode().isEmpty()) {
            item.setAssetCode(generateAssetCode(item.getCategoryId()));
        } else {
            // 检查编码是否已存在
            AssetItem existing = assetItemMapper.selectByCode(item.getAssetCode());
            if (existing != null) {
                throw new BusinessException("资产编码已存在");
            }
        }
        
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        
        // 保存资产信息
        boolean result = assetItemMapper.insert(item) > 0;
        
        // 如果保存成功，同时保存到资产台账表
        if (result) {
            saveOrUpdateAssetAccount(item);
        }
        
        return result;
    }
    
    /**
     * 保存或更新资产台账
     */
    private void saveOrUpdateAssetAccount(AssetItem item) {
        AssetAccount account = new AssetAccount();
        account.setAssetCode(item.getAssetCode());
        account.setAssetName(item.getAssetName());
        account.setSpec(item.getSpec());
        account.setUnit(item.getUnit());
        account.setStockNum(0); // 库存默认为0
        account.setPrice(item.getPrice());
        account.setManufacturer(item.getManufacturer());
        
        // 检查是否已存在
        AssetAccount existing = assetAccountMapper.selectByAssetCode(item.getAssetCode());
        if (existing != null) {
            // 如果已存在，更新
            assetAccountMapper.updateByAssetCode(account);
        } else {
            // 如果不存在，插入
            assetAccountMapper.insert(account);
        }
    }

    @Override
    @Transactional
    public boolean update(AssetItem item) {
        // 获取原始数据，用于判断资产编码是否改变
        AssetItem originalItem = null;
        if (item.getId() != null) {
            originalItem = assetItemMapper.selectById(item.getId());
            if (originalItem == null) {
                throw new BusinessException("资产信息不存在");
            }
        }
        
        // 如果修改了资产编码，检查新编码是否已存在
        if (item.getAssetCode() != null && !item.getAssetCode().isEmpty()) {
            AssetItem existing = assetItemMapper.selectByCode(item.getAssetCode());
            if (existing != null && !existing.getId().equals(item.getId())) {
                throw new BusinessException("资产编码已存在");
            }
        }
        
        // 如果资产编码改变了，需要检查旧编码对应的台账记录是否有库存
        if (originalItem != null && originalItem.getAssetCode() != null && !originalItem.getAssetCode().isEmpty()) {
            if (item.getAssetCode() == null || item.getAssetCode().isEmpty() || 
                !originalItem.getAssetCode().equals(item.getAssetCode())) {
                // 资产编码改变了，检查旧编码对应的台账记录是否有库存
                AssetAccount oldAccount = assetAccountMapper.selectByAssetCode(originalItem.getAssetCode());
                if (oldAccount != null && oldAccount.getStockNum() != null && oldAccount.getStockNum() > 0) {
                    throw new BusinessException("该资产信息在资产台账中存在库存（库存数量：" + oldAccount.getStockNum() + "），无法修改资产编码。请先将库存数量清零后再修改。");
                }
            }
        }
        
        boolean result = assetItemMapper.updateById(item) > 0;
        
        // 如果更新成功，同步更新资产台账
        if (result && originalItem != null) {
            // 如果资产编码改变了，需要更新台账中的编码
            if (originalItem.getAssetCode() != null && !originalItem.getAssetCode().isEmpty() &&
                (item.getAssetCode() == null || item.getAssetCode().isEmpty() || 
                 !originalItem.getAssetCode().equals(item.getAssetCode()))) {
                // 删除旧的台账记录（已在上面检查过库存，确保没有库存）
                assetAccountMapper.deleteByAssetCode(originalItem.getAssetCode());
                // 创建新的台账记录
                if (item.getAssetCode() != null && !item.getAssetCode().isEmpty()) {
                    saveOrUpdateAssetAccount(item);
                }
            } else if (item.getAssetCode() != null && !item.getAssetCode().isEmpty()) {
                // 资产编码未改变，直接更新台账
                saveOrUpdateAssetAccount(item);
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        AssetItem item = assetItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("资产信息不存在");
        }
        
        // 检查 asset_account 表中是否有库存
        if (item.getAssetCode() != null && !item.getAssetCode().isEmpty()) {
            AssetAccount account = assetAccountMapper.selectByAssetCode(item.getAssetCode());
            if (account != null && account.getStockNum() != null && account.getStockNum() > 0) {
                throw new BusinessException("该资产信息在资产台账中存在库存（库存数量：" + account.getStockNum() + "），无法删除。请先将库存数量清零后再删除。");
            }
        }
        
        // 删除资产信息
        boolean result = assetItemMapper.deleteById(id) > 0;
        
        // 如果删除成功，同时删除资产台账中的记录
        if (result && item.getAssetCode() != null && !item.getAssetCode().isEmpty()) {
            assetAccountMapper.deleteByAssetCode(item.getAssetCode());
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean stop(Long id) {
        AssetItem item = assetItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("资产信息不存在");
        }
        
        // 检查 asset_account 表中是否有库存
        if (item.getAssetCode() != null && !item.getAssetCode().isEmpty()) {
            AssetAccount account = assetAccountMapper.selectByAssetCode(item.getAssetCode());
            if (account != null && account.getStockNum() != null && account.getStockNum() > 0) {
                throw new BusinessException("该资产信息在资产台账中存在库存（库存数量：" + account.getStockNum() + "），无法禁用。请先将库存数量清零后再禁用。");
            }
        }
        
        item.setStatus(0);
        boolean result = assetItemMapper.updateById(item) > 0;
        
        return result;
    }

    @Override
    @Transactional
    public boolean start(Long id) {
        AssetItem item = assetItemMapper.selectById(id);
        if (item == null) {
            return false;
        }
        item.setStatus(1);
        return assetItemMapper.updateById(item) > 0;
    }

    @Override
    public String generateAssetCode(Long categoryId) {
        // 生成规则：三级分类编码 + 4位序号（0001, 0002, ...）
        // 例如：BGSB0010010001, BGSB0010010002
        
        // 获取三级分类信息
        AssetCategory category = assetCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在，无法生成资产编码");
        }
        if (category.getLevel() != 3) {
            throw new BusinessException("只能为三级分类生成资产编码");
        }
        
        String categoryCode = category.getCategoryCode();
        if (categoryCode == null || categoryCode.isEmpty()) {
            throw new BusinessException("分类编码为空，无法生成资产编码");
        }
        
        // 查找该分类下最大的编码
        String maxCode = assetItemMapper.selectMaxCodeByCategoryId(categoryId);
        int sequence = 1;
        if (maxCode != null && !maxCode.isEmpty() && maxCode.startsWith(categoryCode)) {
            // 提取序号部分（最后4位）
            try {
                String seqStr = maxCode.substring(categoryCode.length());
                if (seqStr.length() >= 4) {
                    sequence = Integer.parseInt(seqStr.substring(0, 4)) + 1;
                } else if (seqStr.length() > 0) {
                    sequence = Integer.parseInt(seqStr) + 1;
                }
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        return categoryCode + String.format("%04d", sequence);
    }

    @Override
    @Transactional
    public Result<String> importAssetItems(List<List<String>> dataList, String createUser) {
        if (dataList == null || dataList.isEmpty()) {
            return Result.error("导入数据为空");
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
                // Excel列顺序：一级分类、二级分类、三级分类、资产名称、规格、生产厂家、计量单位、单价（元）
                if (row.size() < 8) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：数据列数不足，请检查Excel格式\n");
                    continue;
                }

                String level1Name = row.get(0) != null ? row.get(0).trim() : "";
                String level2Name = row.get(1) != null ? row.get(1).trim() : "";
                String level3Name = row.get(2) != null ? row.get(2).trim() : "";
                String assetName = row.get(3) != null ? row.get(3).trim() : "";
                String spec = row.get(4) != null ? row.get(4).trim() : "";
                String manufacturer = row.get(5) != null ? row.get(5).trim() : "";
                String unit = row.get(6) != null ? row.get(6).trim() : "";
                String priceStr = row.get(7) != null ? row.get(7).trim() : "";

                // 验证必填字段
                if (level1Name.isEmpty() || level2Name.isEmpty() || level3Name.isEmpty() || 
                    assetName.isEmpty() || unit.isEmpty() || priceStr.isEmpty()) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：必填字段不能为空\n");
                    continue;
                }

                // 根据一级分类名称查找一级分类
                AssetCategory level1Category = assetCategoryMapper.selectByNameAndLevel(level1Name, 1, null);
                if (level1Category == null) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：一级分类【").append(level1Name).append("】不存在\n");
                    continue;
                }

                // 根据二级分类名称和一级分类ID查找二级分类
                AssetCategory level2Category = assetCategoryMapper.selectByNameAndLevel(level2Name, 2, level1Category.getId());
                if (level2Category == null) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：二级分类【").append(level2Name).append("】在一级分类【").append(level1Name).append("】下不存在\n");
                    continue;
                }

                // 根据三级分类名称和二级分类ID查找三级分类
                AssetCategory level3Category = assetCategoryMapper.selectByNameAndLevel(level3Name, 3, level2Category.getId());
                if (level3Category == null) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：三级分类【").append(level3Name).append("】在二级分类【").append(level2Name).append("】下不存在\n");
                    continue;
                }

                // 解析单价
                BigDecimal price;
                try {
                    price = new BigDecimal(priceStr);
                    if (price.compareTo(BigDecimal.ZERO) < 0) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：单价不能为负数\n");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：单价格式错误【").append(priceStr).append("】\n");
                    continue;
                }

                // 创建资产信息对象
                AssetItem item = new AssetItem();
                item.setCategoryId(level3Category.getId());
                item.setAssetName(assetName);
                item.setSpec(spec);
                item.setManufacturer(manufacturer);
                item.setUnit(unit);
                item.setPrice(price);
                item.setStatus(1);
                item.setCreateUser(createUser);
                
                // 自动生成资产编码
                item.setAssetCode(generateAssetCode(level3Category.getId()));

                // 检查编码是否已存在
                AssetItem existing = assetItemMapper.selectByCode(item.getAssetCode());
                if (existing != null) {
                    // 如果编码已存在，重新生成
                    item.setAssetCode(generateAssetCode(level3Category.getId()));
                }

                // 保存资产信息
                if (assetItemMapper.insert(item) > 0) {
                    // 同时保存到资产台账表
                    try {
                        saveOrUpdateAssetAccount(item);
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：保存资产台账失败：").append(e.getMessage()).append("\n");
                    }
                } else {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：保存失败\n");
                }
            } catch (Exception e) {
                failCount++;
                errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
            }
        }

        String message = String.format("导入完成：成功 %d 条，失败 %d 条", successCount, failCount);
        if (failCount > 0 && errorMsg.length() > 0) {
            message += "\n错误详情：\n" + errorMsg;
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

