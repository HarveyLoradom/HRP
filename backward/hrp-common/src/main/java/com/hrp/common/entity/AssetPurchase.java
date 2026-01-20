package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购主表实体
 */
@Data
public class AssetPurchase implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo; // 采购单号（ZCCG年月日0001）
    private String applyNo; // 申请单号（关联 asset_purchase_apply_main.apply_no）
    private Long applyId; // 申请ID（关联 asset_purchase_apply_main.id）
    private LocalDate purchaseDate; // 采购日期
    private String purchaseStatus; // 采购状态（从 sys_code 的 PURCHASE_STATUS 获取）：PURCHASING-采购中，COMPLETED-已完成，CANCELLED-已取消
    private String contractNo; // 合同编号（关联 pact_main.contract_no，完成采购时必填，且合同类型必须是采购合同）
    private Long supplierId; // 供应商ID
    private String supplierName; // 供应商名称
    private BigDecimal totalAmount; // 采购总金额
    private Long operatorId; // 操作人ID
    private String operatorName; // 操作人姓名
    private String remark; // 备注
    private String createUser; // 创建人
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    
    // 扩展字段，用于前端显示（不映射到数据库）
    private String applyEmpName; // 申请人姓名
    private String applyDeptName; // 申请部门名称
    private String operatorDeptName; // 操作人科室名称（关联查询）
    private String operatorPhone; // 操作人手机号（关联查询）
}

