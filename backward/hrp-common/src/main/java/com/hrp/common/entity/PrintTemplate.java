package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 打印模板实体
 */
@Data
public class PrintTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long templateId;
    private String templateCode;
    private String templateName;
    private String templateType; // APPLY-申请单，PAYOUT-报账单，CONTRACT-合同，ASSET-资产审批，PROCUREMENT-采购审批
    private String templateContent; // HTML格式的模板内容（兼容旧版本）
    private String templateXml; // XML格式的模板内容
    private String templateJson; // JSON格式的模板内容（Fabric.js格式）
    private String templateFields; // JSON格式的字段配置
    private String pageSize; // A4、A3等
    private String orientation; // portrait-纵向，landscape-横向
    private Integer marginTop;
    private Integer marginBottom;
    private Integer marginLeft;
    private Integer marginRight;
    private String customCss; // 自定义CSS样式
    private String headerHtml; // 页眉HTML
    private String footerHtml; // 页脚HTML
    private Integer isDefault; // 0-否，1-是
    private Integer isActive; // 0-否，1-是
    private String remark;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

