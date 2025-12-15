package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 附件实体
 */
@Data
public class SysAttachment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long attachmentId;
    private String businessType; // 业务类型：PAYOUT-报账，CONTRACT-合同，BUDGET_EXECUTION-预算执行，BUDGET_ADJUSTMENT-预算调整
    private Long businessId; // 业务ID
    private String fileName; // 文件名称
    private String filePath; // 文件路径
    private Long fileSize; // 文件大小（字节）
    private String fileType; // 文件类型（扩展名）
    private String uploadUser; // 上传人
    private LocalDateTime uploadTime; // 上传时间
    private String remark; // 备注
}








