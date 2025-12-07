package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成本分析实体
 */
@Data
public class CostAnalysis implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long analysisId;
    private String analysisNo;
    private String analysisName;
    private String analysisType;
    private String analysisPeriod;
    private Long deptId;
    private String analysisResult;
    private String createUser;
    private LocalDateTime createTime;
}

