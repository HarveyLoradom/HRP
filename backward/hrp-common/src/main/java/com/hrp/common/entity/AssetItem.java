package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产规格型号实体
 */
@Data
public class AssetItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String assetCode;
    private Long categoryId;
    private String assetName;
    private String spec;
    private String manufacturer;
    private String unit;
    private BigDecimal price;
    private Integer status; // 1-启用，0-禁用
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    // 扩展字段，用于前端显示（关联查询结果）
    private String categoryName;
}
