package com.hrp.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 表字段信息DTO
 */
@Data
public class TableFieldInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tableName;
    private String tableLabel;
    private String columnName;
    private String columnLabel;
    private String dataType;
    private String fieldKey; // 用于模板中的字段标识，格式：tableName.columnName
}

