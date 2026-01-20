package com.hrp.cost.controller;

import com.hrp.common.entity.CostMain;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.User;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.ExcelUtil;
import com.hrp.cost.feign.AuthServiceClient;
import com.hrp.cost.service.CostMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 成本主表控制器
 */
@RestController
@RequestMapping("/cost/main")
@CrossOrigin
public class CostMainController {

    @Autowired
    private CostMainService costMainService;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;

    @GetMapping("/list")
    public Result<PageResult<CostMain>> getList(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "cycleId", required = false) Long cycleId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "elementType", required = false) String elementType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestHeader(value = "Authorization", required = false) String token) {
        PageResult<CostMain> pageResult = costMainService.getPage(page, size, cycleId, deptId, elementType, startDate, endDate);
        return Result.success(pageResult);
    }

    @GetMapping("/ledger")
    public Result<PageResult<CostMain>> getLedger(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "cycleId", required = false) Long cycleId,
            @RequestParam(value = "elementType", required = false) String elementType,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token获取用户信息
        Long currentDeptId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 从JWT获取用户账号
                String account = com.hrp.common.util.JwtUtil.getAccount(token);
                if (account != null && !account.trim().isEmpty() && authServiceClient != null) {
                    // 调用用户服务获取用户信息
                    Result<User> userResult = authServiceClient.getUserByAccount(account);
                    if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                        User user = userResult.getData();
                        currentDeptId = user.getDeptId();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取用户部门信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (currentDeptId == null) {
            return Result.error("未获取到用户部门信息，请确保已登录");
        }
        
        PageResult<CostMain> pageResult = costMainService.getPageByDept(page, size, currentDeptId, cycleId, elementType);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<CostMain> getById(@PathVariable("id") Long id) {
        CostMain costMain = costMainService.getById(id);
        return Result.success(costMain);
    }

    @PostMapping
    public Result<CostMain> save(@RequestBody CostMain costMain, 
                                 @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token获取当前用户账号
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String account = com.hrp.common.util.JwtUtil.getAccount(token);
                costMain.setCreateUser(account);
            }
            CostMain saved = costMainService.save(costMain);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<CostMain> update(@RequestBody CostMain costMain) {
        try {
            CostMain updated = costMainService.update(costMain);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean success = costMainService.delete(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    @PostMapping("/import")
    public Result<String> importCostMain(@RequestParam("file") MultipartFile file,
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

            // 从token获取当前用户账号
            String createUser = "SYSTEM";
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                createUser = com.hrp.common.util.JwtUtil.getAccount(token);
                if (createUser == null || createUser.isEmpty()) {
                    createUser = "SYSTEM";
                }
            }

            return costMainService.importCostMain(dataList, createUser);
        } catch (IOException e) {
            throw new BusinessException(500, "读取文件失败：" + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(500, "导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("周期编码*");
        headers.add("部门编码*");
        headers.add("成本要素*");
        headers.add("成本金额*");
        headers.add("发生日期*(yyyy-MM-dd)");
        headers.add("付款方式");
        headers.add("备注");

        List<List<String>> dataList = new ArrayList<>();
        // 添加示例数据
        List<String> exampleRow = new ArrayList<>();
        exampleRow.add("202510");
        exampleRow.add("DEPT001");
        exampleRow.add("人员成本");
        exampleRow.add("10000.00");
        exampleRow.add("2025-10-15");
        exampleRow.add("现金");
        exampleRow.add("10月员工工资");
        dataList.add(exampleRow);

        ExcelUtil.exportExcel(response, "成本导入模板", headers, dataList);
    }
}

