package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科室数据采集实体
 */
@Data
public class DataCollectionClinic implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long collectionId;
    private Long clinicId;
    private String clinicCode;
    private String clinicName;
    private LocalDate collectionDate;
    private String collectionType;
    private String collectionData;
    private LocalDateTime createTime;
}

