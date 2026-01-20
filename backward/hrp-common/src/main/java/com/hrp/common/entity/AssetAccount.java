package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产台账实体
 */
@Data
public class AssetAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String assetCode;
    private String assetName;
    private String spec;
    private String unit;
    private Integer stockNum;
    private BigDecimal price;
    private String manufacturer;
    private LocalDateTime updateTime;
}
