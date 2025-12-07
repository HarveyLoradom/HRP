package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合同实体
 */
@Data
public class PactMain implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pactId;
    private String contractNo;
    private String contractName;
    private String contractType;
    private String partyA;
    private String partyB;
    private BigDecimal contractAmount;
    private LocalDateTime signDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Long deptId;
    private Long managerId;
    private String remark;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

