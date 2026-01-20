package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商实体
 */
@Data
public class Supplier implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long supplierId;
    private String supplierCode; // 供应商编码
    private String supplierName; // 供应商名称
    private String contactPerson; // 联系人
    private String contactPhone; // 联系电话
    private String contactEmail; // 联系邮箱
    private String address; // 地址
    private String bankName; // 银行名称
    private String bankAccount; // 银行账号
    private String accountName; // 账户名称
    private String taxNumber; // 税号
    private Long isStop; // 是否停用：0-启用，1-停用
    private String createUser; // 创建人
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}

