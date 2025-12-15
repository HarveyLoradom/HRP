package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模板设置实体
 */
@Data
public class TemplateConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long configId;
    private String businessType; // 业务类型（如：PAYOUT_TYPE、APPLY_TYPE等）
    private String businessTypeValue; // 业务类型值（如：专项资金、差旅费等）
    private String businessTypeName; // 业务类型名称
    private Long printTemplateId; // 打印模板ID
    private String printTemplateName; // 打印模板名称
    private Long processDefinitionId; // 流程定义ID
    private String processDefinitionName; // 流程定义名称
    private Integer isActive; // 是否启用：0-否，1-是
    private String remark; // 备注
    private String createUser; // 创建人
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}

