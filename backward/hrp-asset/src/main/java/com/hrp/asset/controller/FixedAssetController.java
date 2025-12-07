package com.hrp.asset.controller;

import com.hrp.common.entity.FixedAsset;
import com.hrp.common.entity.Result;
import com.hrp.asset.service.FixedAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asset")
@CrossOrigin
public class FixedAssetController {

    @Autowired
    private FixedAssetService fixedAssetService;

    @GetMapping("/list")
    public Result<List<FixedAsset>> getAll() {
        List<FixedAsset> assets = fixedAssetService.getAll();
        return Result.success(assets);
    }

    @GetMapping("/status/{status}")
    public Result<List<FixedAsset>> getByStatus(@PathVariable String status) {
        List<FixedAsset> assets = fixedAssetService.getByStatus(status);
        return Result.success(assets);
    }

    @GetMapping("/{id}")
    public Result<FixedAsset> getById(@PathVariable Long id) {
        FixedAsset asset = fixedAssetService.getById(id);
        return Result.success(asset);
    }

    @PostMapping
    public Result<Void> save(@RequestBody FixedAsset fixedAsset) {
        boolean success = fixedAssetService.save(fixedAsset);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody FixedAsset fixedAsset) {
        boolean success = fixedAssetService.update(fixedAsset);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = fixedAssetService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

