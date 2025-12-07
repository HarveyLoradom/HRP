package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备数据采集实体
 */
@Data
public class DataCollectionEquipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long collectionId;
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private LocalDate collectionDate;
    private String collectionType;
    private String collectionData;
    private LocalDateTime createTime;
}

