package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备分析报告实体
 */
@Data
public class EquipmentAnalysisReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long reportId;
    private String reportNo;
    private String reportName;
    private Long equipmentId;
    private String equipmentCode;
    private String reportPeriod;
    private String reportContent;
    private String analysisResult;
    private String createUser;
    private LocalDateTime createTime;
}

