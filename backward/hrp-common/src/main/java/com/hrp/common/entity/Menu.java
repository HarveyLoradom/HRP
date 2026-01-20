package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体
 */
@Data
public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String menuCode;
    private String menuName;
    private Long parentId;
    private Integer menuType;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 子菜单列表（用于树形结构）
    private List<Menu> children;
}
