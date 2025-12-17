package com.hrp.auth.controller;

import com.hrp.common.entity.Dept;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.auth.service.DeptService;
import com.hrp.auth.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/auth/dept")
@CrossOrigin
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 根据ID查询部门
     */
    @GetMapping("/{deptId}")
    public Result<Dept> getById(@PathVariable Long deptId) {
        Dept dept = deptService.getById(deptId);
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }
        return Result.success(dept);
    }

    /**
     * 查询所有部门
     */
    @GetMapping("/list")
    public Result<List<Dept>> getAll() {
        List<Dept> list = deptService.getAll();
        return Result.success(list);
    }

    /**
     * 分页查询所有部门
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<Dept>> getAllPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<Dept> pageResult = deptService.getAllPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据上级部门编码查询部门列表
     */
    @GetMapping("/parent/{superDeptCode}")
    public Result<List<Dept>> getBySuperDeptCode(@PathVariable String superDeptCode) {
        List<Dept> list = deptService.getBySuperDeptCode(superDeptCode);
        return Result.success(list);
    }

    /**
     * 新增部门
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Dept dept) {
        // 检查编码是否已存在
        Dept existDept = deptService.getByCode(dept.getDeptCode());
        if (existDept != null) {
            throw new BusinessException(409, "部门编码已存在");
        }
        boolean success = deptService.save(dept);
        if (!success) {
            throw new BusinessException("新增失败");
        }
        return Result.success("新增成功");
    }

    /**
     * 更新部门
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Dept dept) {
        if (dept.getDeptId() == null) {
            throw new BusinessException(400, "部门ID不能为空");
        }
        boolean success = deptService.update(dept);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success("更新成功");
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    public Result<String> delete(@PathVariable Long deptId) {
        boolean success = deptService.delete(deptId);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success("删除成功");
    }

    /**
     * 启用/停用部门
     */
    @PutMapping("/toggle-status/{deptId}")
    public Result<String> toggleStatus(@PathVariable Long deptId) {
        com.hrp.common.entity.Result<String> result = deptService.toggleStatus(deptId);
        if (result.getCode() != 200) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
        return Result.success(result.getMessage());
    }

    /**
     * 下载部门导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("部门编码");
        headers.add("部门名称");
        headers.add("上级部门编码");
        headers.add("部门级别");
        headers.add("部门电话");

        List<List<String>> dataList = new ArrayList<>();
        // 添加示例数据
        List<String> exampleRow = new ArrayList<>();
        exampleRow.add("DEPT001");
        exampleRow.add("院办");
        exampleRow.add("");
        exampleRow.add("1");
        exampleRow.add("010-12345678");
        dataList.add(exampleRow);

        ExcelUtil.exportExcel(response, "部门导入模板", headers, dataList);
    }

    /**
     * 批量导入部门
     */
    @PostMapping("/import")
    public Result<String> importDepts(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "createUser", required = false) String createUser,
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

            // 如果没有传入createUser，从token中获取当前用户账号
            if (createUser == null || createUser.trim().isEmpty()) {
                createUser = getCurrentUserAccount(token);
                if (createUser == null || createUser.isEmpty()) {
                    createUser = "SYSTEM";
                }
            }

            return deptService.importDepts(dataList, createUser);
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
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 解析token获取账号
            String account = com.hrp.common.util.JwtUtil.getAccount(token);
            return account;
        } catch (Exception e) {
            return null;
        }
    }
}

