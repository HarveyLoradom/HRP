package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 手写签名信息实体
 */
@Data
public class SignatureInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long empId;
    private String empCode;
    private String empName;
    private String signatureName;
    private byte[] signatureBlob; // 签名图片二进制数据
    private Integer fileSize;
    private String fileType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

