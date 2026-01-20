package com.hrp.auth.mapper;

import com.hrp.common.entity.PrintTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 打印模板数据访问接口
 */
public interface PrintTemplateMapper {
    /**
     * 根据ID查询打印模板
     */
    PrintTemplate selectById(@Param("templateId") Long templateId);

    /**
     * 根据编码查询打印模板
     */
    PrintTemplate selectByCode(@Param("templateCode") String templateCode);

    /**
     * 根据类型查询打印模板列表
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    List<PrintTemplate> selectByType(@Param("templateType") String templateType, @Param("isActive") Integer isActive);

    /**
     * 查询所有打印模板
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    List<PrintTemplate> selectAll(@Param("isActive") Integer isActive);

    /**
     * 查询默认模板
     */
    PrintTemplate selectDefaultByType(@Param("templateType") String templateType);

    /**
     * 插入打印模板
     */
    int insert(PrintTemplate template);

    /**
     * 更新打印模板
     */
    int updateById(PrintTemplate template);

    /**
     * 删除打印模板
     */
    int deleteById(@Param("templateId") Long templateId);

    /**
     * 根据模板类型和业务主键查询数据
     */
    java.util.Map<String, Object> selectDataByBusinessKey(@Param("templateType") String templateType, @Param("businessKey") String businessKey);
    
    /**
     * 查询支付明细（根据payout_id）
     */
    List<Map<String, Object>> selectPaymentsByPayoutId(@Param("payoutId") Long payoutId);
    
    /**
     * 查询发票信息（根据payout_id）
     */
    List<Map<String, Object>> selectInvoicesByPayoutId(@Param("payoutId") Long payoutId);
    
    /**
     * 查询预算明细（根据business_no）
     */
    List<Map<String, Object>> selectBudgetDetailsByBusinessNo(@Param("businessNo") String businessNo);
    
    /**
     * 查询资产采购申请明细（根据apply_no）
     */
    List<Map<String, Object>> selectAssetPurchaseApplyDetailsByApplyNo(@Param("applyNo") String applyNo);
}

