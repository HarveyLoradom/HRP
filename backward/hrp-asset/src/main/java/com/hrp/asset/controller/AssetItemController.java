package com.hrp.asset.controller;

import com.hrp.asset.service.AssetItemService;
import com.hrp.common.util.ExcelUtil;
import com.hrp.common.entity.AssetItem;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 资产信息维护控制器
 */
@RestController
@RequestMapping("/asset/item")
@CrossOrigin
public class AssetItemController {

    @Autowired
    private AssetItemService assetItemService;

    /**
     * 分页查询资产信息
     */
    @GetMapping("/page")
    public Result<PageResult<AssetItem>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "assetCode", required = false) String assetCode,
            @RequestParam(value = "assetName", required = false) String assetName,
            @RequestParam(value = "level1Id", required = false) Long level1Id,
            @RequestParam(value = "level2Id", required = false) Long level2Id,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<AssetItem> pageResult = assetItemService.getPage(page, size, assetCode, assetName, level1Id, level2Id, categoryId, status);
        return Result.success(pageResult);
    }

    /**
     * 查询资产信息列表
     */
    @GetMapping("/list")
    public Result<java.util.List<AssetItem>> getList(
            @RequestParam(value = "assetCode", required = false) String assetCode,
            @RequestParam(value = "assetName", required = false) String assetName,
            @RequestParam(value = "level1Id", required = false) Long level1Id,
            @RequestParam(value = "level2Id", required = false) Long level2Id,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) Integer status) {
        java.util.List<AssetItem> list = assetItemService.getList(assetCode, assetName, level1Id, level2Id, categoryId, status);
        return Result.success(list);
    }

    /**
     * 根据ID查询资产信息
     */
    @GetMapping("/{id}")
    public Result<AssetItem> getById(@PathVariable("id") Long id) {
        AssetItem item = assetItemService.getById(id);
        if (item == null) {
            return Result.error("资产信息不存在");
        }
        return Result.success(item);
    }

    /**
     * 新增资产信息
     */
    @PostMapping
    public Result<Void> save(@RequestBody AssetItem item,
                             @RequestHeader(value = "Authorization", required = false) String token) {
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            item.setCreateUser(createUser);
        } else {
            item.setCreateUser("SYSTEM");
        }
        // 如果状态为空，默认为启用
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        boolean success = assetItemService.save(item);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 更新资产信息
     */
    @PutMapping
    public Result<Void> update(@RequestBody AssetItem item) {
        if (item.getId() == null) {
            return Result.error("资产ID不能为空");
        }
        boolean success = assetItemService.update(item);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除资产信息（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            boolean success = assetItemService.delete(id);
            return success ? Result.success() : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 停用资产信息
     */
    @PostMapping("/{id}/stop")
    public Result<Void> stop(@PathVariable("id") Long id) {
        try {
            boolean success = assetItemService.stop(id);
            return success ? Result.success() : Result.error("停用失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启用资产信息
     */
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable("id") Long id) {
        boolean success = assetItemService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }

    /**
     * 下载资产信息导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("一级分类*");
        headers.add("二级分类*");
        headers.add("三级分类*");
        headers.add("资产名称*");
        headers.add("规格");
        headers.add("生产厂家");
        headers.add("计量单位*");
        headers.add("单价（元）*");

        List<List<String>> dataList = new ArrayList<>();
        // 添加示例数据
        List<String> exampleRow = new ArrayList<>();
        exampleRow.add("办公设备");
        exampleRow.add("打印设备");
        exampleRow.add("多功能复印扫描设备");
        exampleRow.add("HP LaserJet Pro M404dn");
        exampleRow.add("A4黑白激光打印机，打印速度38页/分钟");
        exampleRow.add("惠普（HP）");
        exampleRow.add("台");
        exampleRow.add("1999.00");
        dataList.add(exampleRow);

        ExcelUtil.exportExcel(response, "资产信息导入模板", headers, dataList);
    }

    /**
     * 批量导入资产信息
     */
    @PostMapping("/import")
    public Result<String> importAssetItems(@RequestParam("file") MultipartFile file,
                                           @RequestHeader(value = "Authorization", required = false) String token) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要导入的文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException(400, "文件格式错误，请上传Excel文件");
        }

        try {
            List<List<String>> dataList = ExcelUtil.readExcel(file);
            if (dataList == null || dataList.isEmpty()) {
                throw new BusinessException(400, "文件内容为空");
            }

            String createUser = getCurrentUserAccount(token);
            if (createUser == null || createUser.isEmpty()) {
                createUser = "SYSTEM";
            }

            return assetItemService.importAssetItems(dataList, createUser);
        } catch (IOException e) {
            throw new BusinessException(500, "读取文件失败：" + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(500, "导入失败：" + e.getMessage());
        }
    }

    /**
     * 从token中获取当前用户账号
     */
    private String getCurrentUserAccount(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return JwtUtil.getAccount(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

