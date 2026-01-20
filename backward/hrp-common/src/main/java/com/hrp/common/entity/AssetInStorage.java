package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库主表实体
 */
@Data
public class AssetInStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String storageNo; // 入库单号（RKD年月日0001）
    private Long purchaseId; // 采购ID（关联 asset_purchase.id，完成采购时生成入库单）
    private String orderNo; // 采购单号（关联 asset_purchase.order_no，冗余字段）
    private String applyNo; // 申请单号（关联 asset_purchase_apply_main.apply_no，冗余字段）
    private LocalDate storageDate; // 入库日期
    private String storageStatus; // 入库状态（从 sys_code 的 IN_STORAGE_STATUS 获取）：NOT_STORED-未入库，STORED-已入库
    private Long operatorId; // 操作人ID
    private String operatorName; // 操作人姓名
    private BigDecimal totalAmount; // 入库总金额
    private String remark; // 备注
    private String createUser; // 创建人
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}

