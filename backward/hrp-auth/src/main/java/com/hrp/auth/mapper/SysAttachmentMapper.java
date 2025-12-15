package com.hrp.auth.mapper;

import com.hrp.common.entity.SysAttachment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 附件数据访问接口
 */
public interface SysAttachmentMapper {
    SysAttachment selectById(@Param("id") Long id);
    List<SysAttachment> selectByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);
    int insert(SysAttachment attachment);
    int updateById(SysAttachment attachment);
    int updateBusinessIdById(@Param("attachmentId") Long attachmentId, @Param("businessId") Long businessId);
    int updateBusinessIdByTypeAndNull(@Param("businessType") String businessType, 
                                      @Param("businessId") Long businessId,
                                      @Param("attachmentIds") List<Long> attachmentIds);
    int deleteById(@Param("id") Long id);
    int deleteByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);
}

