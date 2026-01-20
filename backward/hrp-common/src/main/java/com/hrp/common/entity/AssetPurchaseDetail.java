package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购明细表实体
 */
@Data
public class AssetPurchaseDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long purchaseId; // 采购ID（关联 asset_purchase.id）
    private String orderNo; // 采购单号（冗余字段，方便查询）
    private Long applyDetailId; // 申请明细ID（关联 asset_purchase_apply_detail.id）
    private String assetCode; // 资产编码
    private String assetName; // 资产名称
    private String spec; // 规格型号
    private String manufacturer; // 生产厂家
    private String unit; // 单位
    private Integer applyQuantity; // 申请数量（从申请单明细导入）
    private Integer purchaseQuantity; // 采购数量
    private BigDecimal price; // 单价
    private BigDecimal totalPrice; // 总价（采购数量 * 单价）
    private String remark; // 备注
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}

