package com.hrp.asset.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.asset.mapper.AssetInStorageDetailMapper;
import com.hrp.asset.mapper.AssetInStorageMapper;
import com.hrp.asset.mapper.AssetAccountMapper;
import com.hrp.asset.mapper.AssetPurchaseMapper;
import com.hrp.asset.service.AssetInStorageService;
import com.hrp.common.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库服务实现类
 */
@Service
public class AssetInStorageServiceImpl implements AssetInStorageService {

    @Autowired
    private AssetInStorageMapper inStorageMapper;
    
    @Autowired
    private AssetInStorageDetailMapper inStorageDetailMapper;
    
    @Autowired
    private AssetAccountMapper assetAccountMapper;
    
    @Autowired
    private AssetPurchaseMapper purchaseMapper;

    @Override
    public AssetInStorage getById(Long id) {
        return inStorageMapper.selectById(id);
    }

    @Override
    public AssetInStorage getByStorageNo(String storageNo) {
        return inStorageMapper.selectByStorageNo(storageNo);
    }

    @Override
    public List<AssetInStorage> getByPurchaseId(Long purchaseId) {
        return inStorageMapper.selectByPurchaseId(purchaseId);
    }

    @Override
    public List<AssetInStorage> getByOrderNo(String orderNo) {
        return inStorageMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult<AssetInStorage> getPage(Long page, Long size, String storageNo, String orderNo, String applyNo, String storageStatus, String startDate, String endDate) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetInStorage> list = inStorageMapper.selectByConditions(storageNo, orderNo, applyNo, storageStatus, startDate, endDate);
        PageInfo<AssetInStorage> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional
    public AssetInStorage save(AssetInStorage storage, List<AssetInStorageDetail> details) {
        // 生成入库单号：RKD年月日0001
        if (storage.getStorageNo() == null || storage.getStorageNo().isEmpty()) {
            storage.setStorageNo(generateStorageNo());
        }
        
        // 设置默认值
        if (storage.getStorageStatus() == null || storage.getStorageStatus().isEmpty()) {
            storage.setStorageStatus("NOT_STORED"); // 默认未入库
        }
        if (storage.getStorageDate() == null) {
            storage.setStorageDate(LocalDate.now());
        }
        
        // 计算总金额
        if (details != null && !details.isEmpty()) {
            BigDecimal totalAmount = details.stream()
                    .map(detail -> detail.getTotalPrice() != null ? detail.getTotalPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            storage.setTotalAmount(totalAmount);
        } else {
            storage.setTotalAmount(BigDecimal.ZERO);
        }
        
        // 插入主表
        int result = inStorageMapper.insert(storage);
        if (result > 0 && storage.getId() != null) {
            // 插入明细表
            if (details != null && !details.isEmpty()) {
                for (AssetInStorageDetail detail : details) {
                    detail.setStorageId(storage.getId());
                    detail.setStorageNo(storage.getStorageNo());
                    // 计算总价
                    if (detail.getStorageQuantity() != null && detail.getStorageQuantity() > 0
                            && detail.getPrice() != null) {
                        detail.setTotalPrice(detail.getPrice().multiply(new BigDecimal(detail.getStorageQuantity())));
                    }
                }
                inStorageDetailMapper.insertBatch(details);
            }
            return storage;
        }
        return null;
    }

    @Override
    @Transactional
    public AssetInStorage update(AssetInStorage storage, List<AssetInStorageDetail> details) {
        // 更新主表
        int result = inStorageMapper.updateById(storage);
        if (result > 0 && storage.getId() != null) {
            // 删除旧明细
            inStorageDetailMapper.deleteByStorageId(storage.getId());
            // 插入新明细
            if (details != null && !details.isEmpty()) {
                String storageNo = storage.getStorageNo();
                if (storageNo == null || storageNo.isEmpty()) {
                    AssetInStorage existing = inStorageMapper.selectById(storage.getId());
                    if (existing != null) {
                        storageNo = existing.getStorageNo();
                    }
                }
                for (AssetInStorageDetail detail : details) {
                    detail.setStorageId(storage.getId());
                    detail.setStorageNo(storageNo);
                    // 计算总价
                    if (detail.getStorageQuantity() != null && detail.getStorageQuantity() > 0
                            && detail.getPrice() != null) {
                        detail.setTotalPrice(detail.getPrice().multiply(new BigDecimal(detail.getStorageQuantity())));
                    }
                }
                inStorageDetailMapper.insertBatch(details);
                
                // 重新计算总金额
                BigDecimal totalAmount = details.stream()
                        .map(detail -> detail.getTotalPrice() != null ? detail.getTotalPrice() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                storage.setTotalAmount(totalAmount);
                inStorageMapper.updateById(storage);
            }
            return storage;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        // 先获取入库单信息，以便后续更新采购单状态
        AssetInStorage storage = inStorageMapper.selectById(id);
        if (storage == null) {
            return false;
        }
        
        Long purchaseId = storage.getPurchaseId();
        
        // 物理删除：先删除明细，再删除主表
        inStorageDetailMapper.deleteByStorageId(id);
        int result = inStorageMapper.deleteById(id);
        
        // 如果删除成功，且有关联的采购单，检查是否需要更新采购单状态
        if (result > 0 && purchaseId != null) {
            // 检查该采购单是否还有其他入库单
            List<AssetInStorage> otherStorages = inStorageMapper.selectByPurchaseId(purchaseId);
            // 如果没有其他入库单，将采购单状态改回"采购中"
            if (otherStorages == null || otherStorages.isEmpty()) {
                AssetPurchase purchase = purchaseMapper.selectById(purchaseId);
                if (purchase != null && "COMPLETED".equals(purchase.getPurchaseStatus())) {
                    purchase.setPurchaseStatus("PURCHASING");
                    purchaseMapper.updateById(purchase);
                }
            }
        }
        
        return result > 0;
    }

    @Override
    @Transactional
    public AssetInStorage completeStorage(Long storageId) {
        AssetInStorage storage = inStorageMapper.selectById(storageId);
        if (storage == null) {
            throw new RuntimeException("入库单不存在");
        }
        
        // 更新入库状态为已入库
        storage.setStorageStatus("STORED");
        storage.setStorageDate(LocalDate.now());
        inStorageMapper.updateById(storage);
        
        // 更新资产账户的库存数量
        List<AssetInStorageDetail> details = inStorageDetailMapper.selectByStorageId(storageId);
        if (details != null && !details.isEmpty()) {
            for (AssetInStorageDetail detail : details) {
                // 查询资产账户
                AssetAccount account = assetAccountMapper.selectByAssetCode(detail.getAssetCode());
                if (account != null) {
                    // 更新库存数量
                    int currentStock = account.getStockNum() != null ? account.getStockNum() : 0;
                    int addQuantity = detail.getStorageQuantity() != null ? detail.getStorageQuantity() : 0;
                    account.setStockNum(currentStock + addQuantity);
                    assetAccountMapper.updateByAssetCode(account);
                } else {
                    // 如果资产账户不存在，创建新的资产账户记录
                    account = new AssetAccount();
                    account.setAssetCode(detail.getAssetCode());
                    account.setAssetName(detail.getAssetName());
                    account.setSpec(detail.getSpec());
                    account.setManufacturer(detail.getManufacturer());
                    account.setUnit(detail.getUnit());
                    account.setStockNum(detail.getStorageQuantity() != null ? detail.getStorageQuantity() : 0);
                    account.setPrice(detail.getPrice());
                    assetAccountMapper.insert(account);
                }
            }
        }
        
        return storage;
    }

    @Override
    public List<AssetInStorageDetail> getDetailsByStorageId(Long storageId) {
        return inStorageDetailMapper.selectByStorageId(storageId);
    }

    @Override
    public List<AssetInStorageDetail> getDetailsByStorageNo(String storageNo) {
        return inStorageDetailMapper.selectByStorageNo(storageNo);
    }

    /**
     * 生成入库单号：RKD年月日0001
     * 例如：RKD202501050001, RKD202501050002
     */
    private String generateStorageNo() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String prefix = "RKD" + year + month + day;
        
        // 查询当前日期的最大入库单号
        String maxStorageNo = inStorageMapper.selectMaxStorageNoByPrefix(prefix);
        
        int sequence = 1;
        if (maxStorageNo != null && maxStorageNo.startsWith(prefix)) {
            // 提取序号部分（最后4位）
            try {
                String seqStr = maxStorageNo.substring(prefix.length());
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 格式化为4位序号
        return prefix + String.format("%04d", sequence);
    }
}

