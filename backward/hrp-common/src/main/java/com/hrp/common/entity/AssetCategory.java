package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产分类实体
 */
@Data
public class AssetCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String categoryCode;
    private String categoryName;
    private Long parentId;
    private Integer level; // 1-一级分类，2-二级分类，3-三级分类
    private Integer status; // 1-启用，0-禁用
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
