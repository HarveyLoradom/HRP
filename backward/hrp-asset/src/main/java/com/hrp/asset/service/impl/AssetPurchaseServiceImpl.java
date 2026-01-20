package com.hrp.asset.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.asset.feign.AuthServiceClient;
import com.hrp.asset.mapper.AssetPurchaseDetailMapper;
import com.hrp.asset.mapper.AssetPurchaseMapper;
import com.hrp.asset.mapper.AssetPurchaseApplyMainMapper;
import com.hrp.asset.mapper.AssetPurchaseApplyDetailMapper;
import com.hrp.asset.mapper.AssetInStorageMapper;
import com.hrp.asset.mapper.AssetInStorageDetailMapper;
import com.hrp.asset.service.AssetPurchaseService;
import com.hrp.common.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购服务实现类
 */
@Service
public class AssetPurchaseServiceImpl implements AssetPurchaseService {

    @Autowired
    private AssetPurchaseMapper purchaseMapper;
    
    @Autowired
    private AssetPurchaseDetailMapper purchaseDetailMapper;
    
    @Autowired
    private AssetPurchaseApplyMainMapper applyMainMapper;
    
    @Autowired
    private AssetPurchaseApplyDetailMapper applyDetailMapper;
    
    @Autowired
    private AssetInStorageMapper inStorageMapper;
    
    @Autowired
    private AssetInStorageDetailMapper inStorageDetailMapper;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;
    
    @Autowired(required = false)
    private com.hrp.asset.feign.ContractServiceClient contractServiceClient;

    @Override
    public AssetPurchase getById(Long id) {
        return purchaseMapper.selectById(id);
    }

    @Override
    public AssetPurchase getByOrderNo(String orderNo) {
        return purchaseMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<AssetPurchase> getByApplyNo(String applyNo) {
        return purchaseMapper.selectByApplyNo(applyNo);
    }

    @Override
    public PageResult<AssetPurchase> getPage(Long page, Long size, String orderNo, String applyNo, String purchaseStatus, String startDate, String endDate) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetPurchase> list = purchaseMapper.selectByConditions(orderNo, applyNo, purchaseStatus, startDate, endDate);
        PageInfo<AssetPurchase> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional
    public AssetPurchase save(AssetPurchase purchase, List<AssetPurchaseDetail> details) {
        // 生成采购单号：ZCCG年月日0001
        if (purchase.getOrderNo() == null || purchase.getOrderNo().isEmpty()) {
            purchase.setOrderNo(generateOrderNo());
        }
        
        // 设置默认值
        if (purchase.getPurchaseStatus() == null || purchase.getPurchaseStatus().isEmpty()) {
            purchase.setPurchaseStatus("PURCHASING"); // 默认采购中
        }
        if (purchase.getPurchaseDate() == null) {
            purchase.setPurchaseDate(LocalDate.now());
        }
        
        // 从申请单导入主表信息
        if (purchase.getApplyNo() != null && !purchase.getApplyNo().isEmpty()) {
            AssetPurchaseApplyMain apply = applyMainMapper.selectByApplyNo(purchase.getApplyNo());
            if (apply != null) {
                purchase.setApplyId(apply.getId());
                if (purchase.getApplyEmpName() == null) {
                    purchase.setApplyEmpName(apply.getApplyEmpName());
                }
                if (purchase.getApplyDeptName() == null) {
                    purchase.setApplyDeptName(apply.getApplyDeptName());
                }
            }
        }
        
        // 计算总金额（如果前端已传递总金额，优先使用前端值；否则根据明细计算）
        if (purchase.getTotalAmount() == null || purchase.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            if (details != null && !details.isEmpty()) {
                BigDecimal totalAmount = details.stream()
                        .map(detail -> detail.getTotalPrice() != null ? detail.getTotalPrice() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                purchase.setTotalAmount(totalAmount);
            } else {
                purchase.setTotalAmount(BigDecimal.ZERO);
            }
        }
        
        // 插入主表
        int result = purchaseMapper.insert(purchase);
        if (result > 0 && purchase.getId() != null) {
            // 插入明细表
            if (details != null && !details.isEmpty()) {
                for (AssetPurchaseDetail detail : details) {
                    detail.setPurchaseId(purchase.getId());
                    detail.setOrderNo(purchase.getOrderNo());
                    // 从申请单明细导入信息
                    if (detail.getApplyDetailId() != null) {
                        AssetPurchaseApplyDetail applyDetail = applyDetailMapper.selectById(detail.getApplyDetailId());
                        if (applyDetail != null) {
                            if (detail.getAssetCode() == null) {
                                detail.setAssetCode(applyDetail.getAssetCode());
                            }
                            if (detail.getAssetName() == null) {
                                detail.setAssetName(applyDetail.getAssetName());
                            }
                            if (detail.getSpec() == null) {
                                detail.setSpec(applyDetail.getSpec());
                            }
                            if (detail.getManufacturer() == null) {
                                detail.setManufacturer(applyDetail.getManufacturer());
                            }
                            if (detail.getUnit() == null) {
                                detail.setUnit(applyDetail.getUnit());
                            }
                            if (detail.getApplyQuantity() == null || detail.getApplyQuantity() == 0) {
                                detail.setApplyQuantity(applyDetail.getApplyQuantity());
                            }
                            if (detail.getPrice() == null || detail.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                                detail.setPrice(applyDetail.getPrice());
                            }
                        }
                    }
                    // 计算总价
                    if (detail.getPurchaseQuantity() != null && detail.getPurchaseQuantity() > 0
                            && detail.getPrice() != null) {
                        detail.setTotalPrice(detail.getPrice().multiply(new BigDecimal(detail.getPurchaseQuantity())));
                    } else if (detail.getApplyQuantity() != null && detail.getApplyQuantity() > 0
                            && detail.getPrice() != null) {
                        // 如果没有设置采购数量，使用申请数量
                        detail.setPurchaseQuantity(detail.getApplyQuantity());
                        detail.setTotalPrice(detail.getPrice().multiply(new BigDecimal(detail.getPurchaseQuantity())));
                    }
                }
                purchaseDetailMapper.insertBatch(details);
            }
            return purchase;
        }
        return null;
    }

    @Override
    @Transactional
    public AssetPurchase update(AssetPurchase purchase, List<AssetPurchaseDetail> details) {
        // 更新主表
        int result = purchaseMapper.updateById(purchase);
        if (result > 0 && purchase.getId() != null) {
            // 删除旧明细
            purchaseDetailMapper.deleteByPurchaseId(purchase.getId());
            // 插入新明细
            if (details != null && !details.isEmpty()) {
                String orderNo = purchase.getOrderNo();
                if (orderNo == null || orderNo.isEmpty()) {
                    AssetPurchase existing = purchaseMapper.selectById(purchase.getId());
                    if (existing != null) {
                        orderNo = existing.getOrderNo();
                    }
                }
                for (AssetPurchaseDetail detail : details) {
                    detail.setPurchaseId(purchase.getId());
                    detail.setOrderNo(orderNo);
                    // 计算总价
                    if (detail.getPurchaseQuantity() != null && detail.getPurchaseQuantity() > 0
                            && detail.getPrice() != null) {
                        detail.setTotalPrice(detail.getPrice().multiply(new BigDecimal(detail.getPurchaseQuantity())));
                    }
                }
                purchaseDetailMapper.insertBatch(details);
                
                // 重新计算总金额
                BigDecimal totalAmount = details.stream()
                        .map(detail -> detail.getTotalPrice() != null ? detail.getTotalPrice() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                purchase.setTotalAmount(totalAmount);
                purchaseMapper.updateById(purchase);
            }
            return purchase;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        // 物理删除：先删除明细，再删除主表
        purchaseDetailMapper.deleteByPurchaseId(id);
        int result = purchaseMapper.deleteById(id);
        return result > 0;
    }

    @Override
    @Transactional
    public AssetPurchase completePurchase(Long purchaseId, String contractNo) {
        AssetPurchase purchase = purchaseMapper.selectById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }
        
        // 验证合同号
        if (contractNo == null || contractNo.isEmpty()) {
            throw new RuntimeException("完成采购时，合同号必填");
        }
        
        // 验证合同存在（只查询采购合同类型的合同，由前端限制用户只能选择采购合同）
        if (contractServiceClient != null) {
            try {
                com.hrp.common.entity.Result<com.hrp.common.entity.PactMain> contractResult = contractServiceClient.getByContractNo(contractNo);
                if (contractResult == null || contractResult.getCode() != 200 || contractResult.getData() == null) {
                    throw new RuntimeException("合同不存在或查询失败");
                }
                // 合同存在即可，不做类型强制验证
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                }
                throw new RuntimeException("查询合同信息失败: " + e.getMessage());
            }
        }
        
        // 更新采购状态为已完成
        purchase.setPurchaseStatus("COMPLETED");
        purchase.setContractNo(contractNo);
        purchaseMapper.updateById(purchase);
        
        // 生成入库单（主表和明细表）
        AssetInStorage storage = new AssetInStorage();
        String storageNo = generateStorageNo();
        storage.setStorageNo(storageNo);
        storage.setPurchaseId(purchase.getId());
        storage.setOrderNo(purchase.getOrderNo());
        storage.setApplyNo(purchase.getApplyNo());
        storage.setStorageDate(LocalDate.now());
        storage.setStorageStatus("NOT_STORED"); // 默认未入库
        storage.setOperatorId(purchase.getOperatorId());
        storage.setOperatorName(purchase.getOperatorName());
        storage.setRemark("由采购单" + purchase.getOrderNo() + "自动生成");
        storage.setCreateUser(purchase.getCreateUser());
        
        // 从采购明细生成入库明细
        List<AssetPurchaseDetail> purchaseDetails = purchaseDetailMapper.selectByPurchaseId(purchaseId);
        if (purchaseDetails != null && !purchaseDetails.isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            java.util.List<AssetInStorageDetail> storageDetails = new java.util.ArrayList<>();
            for (AssetPurchaseDetail purchaseDetail : purchaseDetails) {
                AssetInStorageDetail storageDetail = new AssetInStorageDetail();
                storageDetail.setPurchaseDetailId(purchaseDetail.getId());
                storageDetail.setAssetCode(purchaseDetail.getAssetCode());
                storageDetail.setAssetName(purchaseDetail.getAssetName());
                storageDetail.setSpec(purchaseDetail.getSpec());
                storageDetail.setManufacturer(purchaseDetail.getManufacturer());
                storageDetail.setUnit(purchaseDetail.getUnit());
                storageDetail.setStorageQuantity(purchaseDetail.getPurchaseQuantity()); // 使用采购数量
                storageDetail.setPrice(purchaseDetail.getPrice());
                storageDetail.setTotalPrice(purchaseDetail.getTotalPrice());
                storageDetails.add(storageDetail);
                totalAmount = totalAmount.add(purchaseDetail.getTotalPrice() != null ? purchaseDetail.getTotalPrice() : BigDecimal.ZERO);
            }
            storage.setTotalAmount(totalAmount);
            
            // 插入入库主表和明细表
            inStorageMapper.insert(storage);
            if (storage.getId() != null) {
                for (AssetInStorageDetail detail : storageDetails) {
                    detail.setStorageId(storage.getId());
                    detail.setStorageNo(storageNo);
                }
                inStorageDetailMapper.insertBatch(storageDetails);
            }
        }
        
        return purchase;
    }

    @Override
    public List<AssetPurchaseDetail> getDetailsByPurchaseId(Long purchaseId) {
        return purchaseDetailMapper.selectByPurchaseId(purchaseId);
    }

    @Override
    public List<AssetPurchaseDetail> getDetailsByOrderNo(String orderNo) {
        return purchaseDetailMapper.selectByOrderNo(orderNo);
    }

    /**
     * 生成采购单号：ZCCG年月日0001
     * 例如：ZCCG202501050001, ZCCG202501050002
     */
    private String generateOrderNo() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String prefix = "ZCCG" + year + month + day;
        
        // 查询当前日期的最大采购单号
        String maxOrderNo = purchaseMapper.selectMaxOrderNoByPrefix(prefix);
        
        int sequence = 1;
        if (maxOrderNo != null && maxOrderNo.startsWith(prefix)) {
            // 提取序号部分（最后4位）
            try {
                String seqStr = maxOrderNo.substring(prefix.length());
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 格式化为4位序号
        return prefix + String.format("%04d", sequence);
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

