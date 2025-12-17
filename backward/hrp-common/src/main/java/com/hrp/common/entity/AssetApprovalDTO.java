package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 资产审批DTO（包含明细和审批记录）
 */
@Data
public class AssetApprovalDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private AssetApproval approval; // 主表信息
    private List<AssetApprovalDetail> details; // 审批明细
    private List<AssetApprovalRecord> records; // 审批记录
}












