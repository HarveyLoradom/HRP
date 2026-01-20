package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库明细表实体
 */
@Data
public class AssetInStorageDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long storageId; // 入库ID（关联 asset_in_storage.id）
    private String storageNo; // 入库单号（冗余字段，方便查询）
    private Long purchaseDetailId; // 采购明细ID（关联 asset_purchase_detail.id）
    private String assetCode; // 资产编码
    private String assetName; // 资产名称
    private String spec; // 规格型号
    private String manufacturer; // 生产厂家
    private String unit; // 单位
    private Integer storageQuantity; // 入库数量（从采购明细导入）
    private BigDecimal price; // 单价
    private BigDecimal totalPrice; // 总价（入库数量 * 单价）
    private String remark; // 备注
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}

