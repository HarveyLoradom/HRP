package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 打印模板字段映射实体
 */
@Data
public class PrintTemplateField implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long fieldId;
    private Long templateId;
    private String fieldName; // 模板中的变量名，如：{applicantName}
    private String fieldLabel; // 字段标签（显示名称）
    private String dataSource; // 数据来源：MAIN-主表，DETAIL-明细表，RELATED-关联表
    private String tableName; // 表名
    private String columnName; // 列名
    private String fieldType; // STRING-字符串，NUMBER-数字，DATE-日期，BOOLEAN-布尔值
    private String formatPattern; // 格式化模式（如日期格式：yyyy-MM-dd）
    private String defaultValue; // 默认值
    private Integer sortOrder;
    private LocalDateTime createTime;
}

