package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资收益分析实体
 */
@Data
public class InvestmentReturnAnalysis implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long analysisId;
    private String analysisNo;
    private String analysisName;
    private Long equipmentId;
    private BigDecimal investmentAmount;
    private BigDecimal returnAmount;
    private BigDecimal returnRate;
    private String analysisPeriod;
    private String analysisResult;
    private String createUser;
    private LocalDateTime createTime;
}

