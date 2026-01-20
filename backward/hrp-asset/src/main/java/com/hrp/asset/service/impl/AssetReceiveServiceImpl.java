package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.AssetAccountMapper;
import com.hrp.asset.mapper.AssetReceiveDetailMapper;
import com.hrp.asset.mapper.AssetReceiveMainMapper;
import com.hrp.asset.service.AssetReceiveService;
import com.hrp.common.entity.AssetAccount;
import com.hrp.common.entity.AssetReceiveDetail;
import com.hrp.common.entity.AssetReceiveMain;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 资产领用服务实现类
 */
@Service
public class AssetReceiveServiceImpl implements AssetReceiveService {

    @Autowired
    private AssetReceiveMainMapper receiveMainMapper;
    
    @Autowired
    private AssetReceiveDetailMapper receiveDetailMapper;
    
    @Autowired
    private AssetAccountMapper assetAccountMapper;

    @Override
    public PageResult<AssetReceiveMain> getPage(Long page, Long size, String receiveNo, Long deptId,
                                                String applyEmpId, String operatorCode) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetReceiveMain> list = receiveMainMapper.selectByConditions(receiveNo, deptId, applyEmpId, operatorCode);
        PageInfo<AssetReceiveMain> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public AssetReceiveMain getById(Long id) {
        AssetReceiveMain receiveMain = receiveMainMapper.selectById(id);
        if (receiveMain != null) {
            List<AssetReceiveDetail> details = receiveDetailMapper.selectByReceiveId(id);
            receiveMain.setDetails(details);
        }
        return receiveMain;
    }

    @Override
    public AssetReceiveMain getByReceiveNo(String receiveNo) {
        AssetReceiveMain receiveMain = receiveMainMapper.selectByReceiveNo(receiveNo);
        if (receiveMain != null) {
            List<AssetReceiveDetail> details = receiveDetailMapper.selectByReceiveNo(receiveNo);
            receiveMain.setDetails(details);
        }
        return receiveMain;
    }

    @Override
    @Transactional
    public AssetReceiveMain save(AssetReceiveMain receiveMain, List<AssetReceiveDetail> details, String currentUserId) {
        // 生成领用单号
        if (receiveMain.getReceiveNo() == null || receiveMain.getReceiveNo().isEmpty()) {
            receiveMain.setReceiveNo(generateReceiveNo());
        }
        
        // 验证明细不能为空
        if (details == null || details.isEmpty()) {
            throw new BusinessException("领用明细不能为空");
        }
        
        // 验证每个明细的领用数量不能超过库存数量，并更新库存
        for (AssetReceiveDetail detail : details) {
            AssetAccount account = assetAccountMapper.selectByAssetCode(detail.getAssetCode());
            if (account == null) {
                throw new BusinessException("资产编码不存在：" + detail.getAssetCode());
            }
            
            int stockNum = account.getStockNum() != null ? account.getStockNum() : 0;
            int receiveNum = detail.getReceiveNum() != null ? detail.getReceiveNum() : 0;
            
            if (receiveNum <= 0) {
                throw new BusinessException("资产【" + detail.getAssetCode() + "】的领用数量必须大于0");
            }
            
            if (receiveNum > stockNum) {
                throw new BusinessException("资产【" + detail.getAssetCode() + "】的领用数量（" + receiveNum + "）超过库存数量（" + stockNum + "）");
            }
            
            // 设置剩余库存数量
            int remainNum = stockNum - receiveNum;
            detail.setRemainNum(remainNum);
            
            // 更新资产台账的库存数量
            account.setStockNum(remainNum);
            assetAccountMapper.updateByAssetCode(account);
        }
        
        // 保存主表
        int result = receiveMainMapper.insert(receiveMain);
        if (result <= 0) {
            throw new BusinessException("保存领用主表失败");
        }
        
        // 保存明细表
        for (AssetReceiveDetail detail : details) {
            detail.setReceiveId(receiveMain.getId());
            detail.setReceiveNo(receiveMain.getReceiveNo());
            receiveDetailMapper.insert(detail);
        }
        
        return receiveMain;
    }

    @Override
    public String generateReceiveNo() {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String prefix = "ZCLY" + year + month + day;
        
        // 查询当前日期的最大领用单号
        String maxReceiveNo = receiveMainMapper.selectMaxReceiveNoByPrefix(prefix);
        
        int sequence = 1;
        if (maxReceiveNo != null && maxReceiveNo.startsWith(prefix)) {
            // 提取序号部分（最后4位）
            try {
                String seqStr = maxReceiveNo.substring(prefix.length());
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        // 格式化为4位序号
        return prefix + String.format("%04d", sequence);
    }
}

