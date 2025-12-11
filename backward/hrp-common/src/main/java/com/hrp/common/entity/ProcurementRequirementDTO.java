package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 采购需求DTO（包含明细和审批记录）
 */
@Data
public class ProcurementRequirementDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private ProcurementRequirement requirement; // 主表信息
    private List<ProcurementRequirementDetail> details; // 需求明细
    private List<ProcurementRequirementApproval> approvals; // 审批记录
}



