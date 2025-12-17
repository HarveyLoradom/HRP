package com.hrp.auth.controller;

import com.hrp.common.entity.Employee;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.User;
import com.hrp.common.entity.UserWithEmployee;
import com.hrp.common.exception.BusinessException;
import com.hrp.auth.service.UserService;
import com.hrp.auth.service.UserEmployeeService;
import com.hrp.auth.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/auth/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserEmployeeService userEmployeeService;

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable String id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 根据账号查询用户
     */
    @GetMapping("/account/{account}")
    public Result<User> getByAccount(@PathVariable String account) {
        User user = userService.getByAccount(account);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 查询所有用户
     */
    @GetMapping("/list")
    public Result<List<User>> getAll() {
        List<User> list = userService.getAll();
        return Result.success(list);
    }

    /**
     * 查询所有员工（关联用户信息）- 用于用户管理页面
     */
    @GetMapping("/employees")
    public Result<List<UserWithEmployee>> getAllEmployeesWithUser() {
        List<UserWithEmployee> list = userEmployeeService.getAllEmployeesWithUser();
        return Result.success(list);
    }

    /**
     * 根据工号或姓名查询员工（关联用户信息）
     */
    @GetMapping("/employees/search")
    public Result<List<UserWithEmployee>> searchEmployees(@RequestParam String keyword) {
        List<UserWithEmployee> list = userEmployeeService.getEmployeesByKeyword(keyword);
        return Result.success(list);
    }

    /**
     * 分页查询所有员工（关联用户信息）
     */
    @GetMapping("/employees/page")
    public Result<com.hrp.common.entity.PageResult<UserWithEmployee>> getAllEmployeesWithUserPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<UserWithEmployee> pageResult = userEmployeeService.getAllEmployeesWithUserPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 分页查询员工（关联用户信息）- 带关键词
     */
    @GetMapping("/employees/search/page")
    public Result<com.hrp.common.entity.PageResult<UserWithEmployee>> searchEmployeesPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<UserWithEmployee> pageResult = userEmployeeService.getEmployeesByKeywordPage(keyword, page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody User user, @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户账号（工号）作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            user.setCreateUser(createUser);
        } else {
            user.setCreateUser("SYSTEM");
        }
        
        // 检查账号是否已存在
        User existUser = userService.getByAccount(user.getAccount());
        if (existUser != null) {
            throw new BusinessException(409, "账号已存在");
        }
        boolean success = userService.save(user);
        if (!success) {
            throw new BusinessException("新增失败");
        }
        return Result.success("新增成功");
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

    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        boolean success = userService.update(user);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success("更新成功");
    }

    /**
     * 删除用户（物理删除，同时删除sys_user、sys_emp及相关关联数据）
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        boolean success = userService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success("删除成功");
    }

    /**
     * 启用/停用用户
     */
    @PutMapping("/toggle-status/{id}")
    public Result<String> toggleStatus(@PathVariable String id) {
        boolean success = userService.toggleStatus(id);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        return Result.success("操作成功");
    }

    /**
     * 重置用户密码（使用系统配置的原始密码）
     */
    @PutMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable String id) {
        boolean success = userService.resetPassword(id);
        if (!success) {
            throw new BusinessException("密码重置失败");
        }
        return Result.success("密码重置成功");
    }

    /**
     * 解锁用户（解除账户锁定）
     */
    @PutMapping("/unlock/{id}")
    public Result<String> unlockUser(@PathVariable String id) {
        boolean success = userService.unlockUser(id);
        if (!success) {
            throw new BusinessException("解锁失败");
        }
        return Result.success("解锁成功");
    }

    /**
     * 根据工号查询员工信息（sys_emp表）
     */
    @GetMapping("/employee/{empCode}")
    public Result<Employee> getEmployeeByCode(@PathVariable String empCode) {
        Employee employee = userEmployeeService.getEmployeeByCode(empCode);
        if (employee == null) {
            throw new BusinessException(404, "员工不存在");
        }
        return Result.success(employee);
    }

    /**
     * 查询所有员工列表（只查询sys_emp表，供其他功能使用）
     */
    @GetMapping("/employees/list")
    public Result<List<Employee>> getEmployeeList() {
        List<Employee> list = userEmployeeService.getAllEmployees();
        return Result.success(list);
    }

    /**
     * 下载用户导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("工号（账号）*");
        headers.add("姓名*");
        headers.add("性别*（1-男，2-女）");
        headers.add("身份证号");
        headers.add("出生日期（YYYY-MM-DD）");
        headers.add("手机号");
        headers.add("邮箱");
        headers.add("部门编码*");
        headers.add("用户类型*(默认2)");
        headers.add("职工类型*(默认1)");

        List<List<String>> dataList = new ArrayList<>();
        // 添加示例数据
        List<String> exampleRow = new ArrayList<>();
        exampleRow.add("001001");
        exampleRow.add("张三");
        exampleRow.add("1");
        exampleRow.add("110101199001011234");
        exampleRow.add("1990-01-01");
        exampleRow.add("13800138001");
        exampleRow.add("zhangsan@hospital.com");
        // 部门编码示例：院办 DEPT001
        exampleRow.add("DEPT001");
        exampleRow.add("2");
        exampleRow.add("1");
        dataList.add(exampleRow);

        ExcelUtil.exportExcel(response, "用户导入模板", headers, dataList);
    }

    /**
     * 批量导入用户
     */
    @PostMapping("/import")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file,
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

            return userService.importUsers(dataList, createUser);
        } catch (IOException e) {
            throw new BusinessException(500, "读取文件失败：" + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(500, "导入失败：" + e.getMessage());
        }
    }

    /**
     * 更新员工信息（用于个人中心）
     */
    @PutMapping("/employee/update")
    public Result<String> updateEmployee(@RequestBody Employee employee) {
        if (employee.getEmpCode() == null || employee.getEmpCode().isEmpty()) {
            throw new BusinessException(400, "工号不能为空");
        }
        boolean success = userEmployeeService.updateEmployee(employee);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success("更新成功");
    }
}

