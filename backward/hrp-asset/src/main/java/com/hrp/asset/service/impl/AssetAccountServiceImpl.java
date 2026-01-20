package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.AssetAccountMapper;
import com.hrp.asset.service.AssetAccountService;
import com.hrp.common.entity.AssetAccount;
import com.hrp.common.entity.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 资产台账服务实现类
 */
@Service
public class AssetAccountServiceImpl implements AssetAccountService {

    @Autowired
    private AssetAccountMapper assetAccountMapper;

    @Override
    public PageResult<AssetAccount> getPage(Long page, Long size, String assetCode, String assetName,
                                            String spec, String manufacturer, Long level1Id, Long level2Id,
                                            Long categoryId, Boolean hasStock) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<AssetAccount> list = assetAccountMapper.selectByConditions(assetCode, assetName, spec, 
                                                                        manufacturer, level1Id, level2Id, 
                                                                        categoryId, hasStock);
        PageInfo<AssetAccount> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public AssetAccount getByAssetCode(String assetCode) {
        return assetAccountMapper.selectByAssetCode(assetCode);
    }

    @Override
    public List<Map<String, Object>> getStorageInfoByAssetCode(String assetCode) {
        return assetAccountMapper.selectStorageInfoByAssetCode(assetCode);
    }

    @Override
    public List<Map<String, Object>> getReceiveInfoByAssetCode(String assetCode) {
        return assetAccountMapper.selectReceiveInfoByAssetCode(assetCode);
    }
}

