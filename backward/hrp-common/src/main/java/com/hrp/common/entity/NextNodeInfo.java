package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 下一个审批节点信息
 */
@Data
public class NextNodeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskName; // 节点名称
    private String approvalPositionName; // 审批岗位名称（如：归口审批人、财务经办人等）
    private String approverName; // 审批人姓名
    private String approverCode; // 审批人工号
    private String approverList; // 审批人列表（逗号分隔的多个审批人姓名）
    private String assigneeType; // 审批人类型：position-岗位，user-用户，dept-部门负责人，manage_dept-归口审批人
}

