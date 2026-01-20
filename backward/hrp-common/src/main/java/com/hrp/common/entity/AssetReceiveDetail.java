package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产领用明细实体
 */
@Data
public class AssetReceiveDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long receiveId;
    private String receiveNo; // 领用单号（冗余字段，方便查询）
    private String assetCode;
    private String assetName;
    private String spec;
    private String unit;
    private Integer receiveNum;
    private String manufacturer;
    private Integer remainNum;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
