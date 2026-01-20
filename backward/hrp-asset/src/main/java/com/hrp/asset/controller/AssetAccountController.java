package com.hrp.asset.controller;

import com.hrp.asset.service.AssetAccountService;
import com.hrp.common.entity.AssetAccount;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 资产台账管理控制器
 */
@RestController
@RequestMapping("/asset/account")
@CrossOrigin
public class AssetAccountController {

    @Autowired
    private AssetAccountService assetAccountService;

    /**
     * 分页查询资产台账
     */
    @GetMapping("/page")
    public Result<PageResult<AssetAccount>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "assetCode", required = false) String assetCode,
            @RequestParam(value = "assetName", required = false) String assetName,
            @RequestParam(value = "spec", required = false) String spec,
            @RequestParam(value = "manufacturer", required = false) String manufacturer,
            @RequestParam(value = "level1Id", required = false) Long level1Id,
            @RequestParam(value = "level2Id", required = false) Long level2Id,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "hasStock", required = false) Boolean hasStock) {
        PageResult<AssetAccount> pageResult = assetAccountService.getPage(page, size, assetCode, assetName, 
                                                                         spec, manufacturer, level1Id, level2Id, 
                                                                         categoryId, hasStock);
        return Result.success(pageResult);
    }

    /**
     * 根据资产编码查询资产台账
     */
    @GetMapping("/code/{assetCode}")
    public Result<AssetAccount> getByAssetCode(@PathVariable("assetCode") String assetCode) {
        AssetAccount account = assetAccountService.getByAssetCode(assetCode);
        return Result.success(account);
    }

    /**
     * 根据资产编码查询对应的入库单号、采购单号、申请单号
     */
    @GetMapping("/storage-info/{assetCode}")
    public Result<List<Map<String, Object>>> getStorageInfoByAssetCode(@PathVariable("assetCode") String assetCode) {
        try {
            List<Map<String, Object>> list = assetAccountService.getStorageInfoByAssetCode(assetCode);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据资产编码查询对应的领用单号和领用数量
     */
    @GetMapping("/receive-info/{assetCode}")
    public Result<List<Map<String, Object>>> getReceiveInfoByAssetCode(@PathVariable("assetCode") String assetCode) {
        try {
            List<Map<String, Object>> list = assetAccountService.getReceiveInfoByAssetCode(assetCode);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}

