package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.UserMapper;
import com.hrp.auth.mapper.UserLoginMapper;
import com.hrp.auth.mapper.UserRoleMapper;
import com.hrp.auth.mapper.UserMenuMapper;
import com.hrp.auth.mapper.UserTypeMenuMapper;
import com.hrp.auth.service.UserService;
import com.hrp.auth.service.UserEmployeeService;
import com.hrp.auth.service.DeptService;
import com.hrp.common.entity.Employee;
import com.hrp.common.entity.User;
import com.hrp.common.entity.UserLogin;
import com.hrp.common.entity.Dept;
import com.hrp.common.entity.UserTypeMenu;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.PasswordUtil;
import com.hrp.common.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserLoginMapper userLoginMapper;

    @Autowired
    private UserEmployeeService userEmployeeService;

    @Autowired
    private DeptService deptService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserMenuMapper userMenuMapper;

    @Autowired
    private UserTypeMenuMapper userTypeMenuMapper;

    /**
     * 根据账号查询用户
     */
    @Override
    public User getByAccount(String account) {
        // selectByAccount方法已经在SQL中检查了sys_user和sys_emp的is_stop字段
        // 所以这里直接返回即可
        return userMapper.selectByAccount(account);
    }

    /**
     * 获取或创建用户登录信息
     */
    @Override
    public UserLogin getOrCreateUserLogin(String userId, String account) {
        UserLogin userLogin = userLoginMapper.selectByUserId(userId);
        
        if (userLogin == null) {
            userLogin = new UserLogin();
            userLogin.setUserId(userId);
            userLogin.setAccount(account);
            userLogin.setLocked(0);
            userLogin.setLoginFailCount(0);
            userLoginMapper.insert(userLogin);
        }
        return userLogin;
    }

    /**
     * 更新登录失败次数
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLoginFailCount(String userId, Integer count) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        // 先尝试获取现有记录
        UserLogin userLogin = userLoginMapper.selectByUserId(userId);
        if (userLogin == null) {
            // 如果不存在，创建新记录
            userLogin = new UserLogin();
            userLogin.setUserId(userId);
            userLogin.setAccount(user.getAccount());
            userLogin.setLocked(0);
            userLogin.setLoginFailCount(count);
            userLoginMapper.insert(userLogin);
        } else {
            // 如果存在，更新失败次数
        userLogin.setLoginFailCount(count);
            // 确保 userId 被设置，用于 WHERE 条件
            userLogin.setUserId(userId);
        userLoginMapper.updateById(userLogin);
        }
    }

    /**
     * 锁定用户
     */
    @Override
    @Transactional
    public void lockUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        UserLogin userLogin = getOrCreateUserLogin(userId, user.getAccount());
        userLogin.setLocked(1);
        userLogin.setLockTime(LocalDateTime.now());
        userLoginMapper.updateById(userLogin);
    }

    /**
     * 解锁用户并重置失败次数
     */


    /**
     * 更新最后登录时间
     */
    @Override
    @Transactional
    public void updateLastLoginTime(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        UserLogin userLogin = getOrCreateUserLogin(userId, user.getAccount());
        userLogin.setLastLoginTime(LocalDateTime.now());
        userLogin.setLoginFailCount(0);
        userLoginMapper.updateById(userLogin);
    }

    /**
     * 获取用户登录信息
     */
    @Override
    public UserLogin getUserLogin(String userId) {
        return userLoginMapper.selectByUserId(userId);
    }

    /**
     * 验证密码
     * 支持PBKDF2和BCrypt两种格式（向后兼容）
     */
    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        // 去除可能的空格和换行符
        encodedPassword = encodedPassword.trim();
        // PasswordUtil.matches方法内部已经处理了BCrypt和PBKDF2两种格式
        return PasswordUtil.matches(rawPassword, encodedPassword);
    }

    /**
     * 修改密码
     */
    @Override
    @Transactional
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 验证原密码
        if (!validatePassword(oldPassword, user.getPassword())) {
            return false;
        }

        // 加密新密码
        String encodedNewPassword = PasswordUtil.encode(newPassword);
        user.setPassword(encodedNewPassword);
        
        // 更新密码
        return userMapper.updateById(user) > 0;
    }

    /**
     * 强制修改密码（不需要原密码）
     */
    @Override
    @Transactional
    public boolean forceChangePassword(String userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 加密新密码
        String encodedNewPassword = PasswordUtil.encode(newPassword);
        user.setPassword(encodedNewPassword);
        
        // 更新密码
        return userMapper.updateById(user) > 0;
    }

    @Override
    public User getById(String id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> getAll() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean save(User user) {
        // 检查账号是否已存在
        User existUser = userMapper.selectByAccount(user.getAccount());
        if (existUser != null) {
            throw new BusinessException(409, "账号已存在");
        }

        // 检查员工是否已存在
        Employee existEmployee = userEmployeeService.getEmployeeByCode(user.getAccount());
        if (existEmployee != null) {
            throw new BusinessException(409, "工号已存在");
        }

        // 生成用户ID
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UuidUtil.generateUuid());
        }

        // 加密密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode("123456"));
        } else {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }

        // 设置默认值
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        // 设置默认停用状态为0（启用）
        if (user.getIsStop() == null) {
            user.setIsStop(0L);
        }

        // 插入用户记录
        int userResult = userMapper.insert(user);
        if (userResult <= 0) {
            return false;
        }

        // 创建员工记录（sys_emp表）
        Employee employee = new Employee();
        employee.setEmpCode(user.getAccount()); // account对应emp_code
        employee.setEmpName(user.getName());
        employee.setEmpSex(user.getEmpSex() != null ? user.getEmpSex() : 1L); // 性别，默认为1（男）
        employee.setIdCard(user.getIdCard());
        employee.setEmpBirthday(user.getEmpBirthday());
        employee.setEmpPhone(user.getPhone());
        employee.setEmpEmail(user.getEmpEmail());
        // 根据deptId查询部门信息，设置deptId和deptCode
        if (user.getDeptId() != null) {
            Dept dept = deptService.getById(user.getDeptId());
            if (dept != null) {
                employee.setDeptId(dept.getDeptId());
                employee.setDeptCode(dept.getDeptCode());
            }
        }
        employee.setEmpTypeId(user.getEmpTypeId()); // 从user中获取empTypeId
        employee.setIsStop(0L);
        employee.setCreateUser(user.getCreateUser());
        employee.setCreateTime(user.getCreateTime() != null ? user.getCreateTime() : LocalDateTime.now());

        // 插入员工记录
        boolean empResult = userEmployeeService.saveEmployee(employee);
        if (!empResult) {
            throw new BusinessException("创建员工记录失败");
        }

        // 创建用户登录信息
        getOrCreateUserLogin(user.getId(), user.getAccount());

        // 根据用户类型自动分配菜单权限
        if (user.getType() != null) {
            String userType = String.valueOf(user.getType());
            List<UserTypeMenu> userTypeMenus = userTypeMenuMapper.selectByUserType(userType);
            if (userTypeMenus != null && !userTypeMenus.isEmpty()) {
                List<Long> menuIds = userTypeMenus.stream()
                    .map(UserTypeMenu::getMenuId)
                    .collect(java.util.stream.Collectors.toList());
                if (!menuIds.isEmpty()) {
                    userMenuMapper.batchInsert(user.getId(), menuIds);
                }
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean update(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 如果密码不为空，则加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        } else {
            // 保留原密码
            user.setPassword(existUser.getPassword());
        }

        user.setUpdateTime(LocalDateTime.now());

        // 检查用户类型是否发生变化
        Long oldType = existUser.getType();
        Long newType = user.getType();
        boolean userTypeChanged = false;
        
        if (oldType == null && newType != null) {
            // 从null变为有值
            userTypeChanged = true;
        } else if (oldType != null && newType == null) {
            // 从有值变为null
            userTypeChanged = true;
        } else if (oldType != null && newType != null) {
            // 两个都不为null，比较是否相等（使用equals确保类型一致）
            userTypeChanged = !oldType.equals(newType);
        }
        // 如果两个都为null，则userTypeChanged保持为false

        // 更新用户记录
        int userResult = userMapper.updateById(user);
        if (userResult <= 0) {
            return false;
        }

        // 如果用户类型发生变化，自动更新菜单权限
        // 注意：这里使用newType（更新后的值）而不是重新查询，因为事务还未提交
        if (userTypeChanged && newType != null) {
            // 先删除用户现有的菜单权限
            userMenuMapper.deleteByUserId(user.getId());
            
            // 根据新的用户类型分配菜单权限
            String userType = String.valueOf(newType);
            List<UserTypeMenu> userTypeMenus = userTypeMenuMapper.selectByUserType(userType);
            if (userTypeMenus != null && !userTypeMenus.isEmpty()) {
                List<Long> menuIds = userTypeMenus.stream()
                    .map(UserTypeMenu::getMenuId)
                    .collect(java.util.stream.Collectors.toList());
                if (!menuIds.isEmpty()) {
                    userMenuMapper.batchInsert(user.getId(), menuIds);
                }
            }
        }

        // 更新员工记录（如果存在）
        Employee existEmployee = userEmployeeService.getEmployeeByCode(existUser.getAccount());
        if (existEmployee != null) {
            Employee employee = new Employee();
            employee.setEmpCode(existUser.getAccount());
            employee.setEmpName(user.getName());
            employee.setEmpSex(user.getEmpSex() != null ? user.getEmpSex() : existEmployee.getEmpSex());
            employee.setIdCard(user.getIdCard());
            employee.setEmpBirthday(user.getEmpBirthday());
            employee.setEmpPhone(user.getPhone());
            employee.setEmpEmail(user.getEmpEmail());
            // 根据deptId查询部门信息，设置deptId和deptCode
            if (user.getDeptId() != null) {
                Dept dept = deptService.getById(user.getDeptId());
                if (dept != null) {
                    employee.setDeptId(dept.getDeptId());
                    employee.setDeptCode(dept.getDeptCode());
                }
            }
            employee.setEmpTypeId(user.getEmpTypeId());
            employee.setUpdateTime(LocalDateTime.now());
            userEmployeeService.updateEmployee(employee);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean delete(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }

        String account = user.getAccount();

        // 1. 删除用户角色关联
        userRoleMapper.deleteByUserId(id);

        // 2. 删除用户菜单关联
        userMenuMapper.deleteByUserId(id);

        // 3. 删除用户登录信息
        userLoginMapper.deleteByUserId(id);

        // 4. 删除用户记录（sys_user表）
        int userResult = userMapper.deleteById(id);
        if (userResult <= 0) {
            throw new BusinessException("删除用户记录失败");
        }

        // 5. 删除员工记录（sys_emp表）
        if (account != null && !account.isEmpty()) {
            userEmployeeService.deleteEmployeeByCode(account);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean toggleStatus(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }

        // 切换用户状态（sys_user表的is_stop字段）
        Long currentUserStatus = user.getIsStop() != null ? user.getIsStop() : 0L;
        Long newUserStatus = currentUserStatus == 0 ? 1L : 0L;
        user.setIsStop(newUserStatus);
        int userResult = userMapper.updateById(user);
        if (userResult <= 0) {
            return false;
        }

        // 同时切换员工状态（sys_emp表的is_stop字段）
        Employee existEmployee = userEmployeeService.getEmployeeByCode(user.getAccount());
        if (existEmployee != null) {
            existEmployee.setIsStop(newUserStatus);
            return userEmployeeService.updateEmployee(existEmployee);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean resetPassword(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }

        // 获取系统配置的原始密码（从sys_code表获取）
        // 这里简化处理，使用默认密码123456
        String defaultPassword = "123456";
        String encodedPassword = PasswordUtil.encode(defaultPassword);
        user.setPassword(encodedPassword);

        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean unlockUser(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }

        UserLogin userLogin = getOrCreateUserLogin(id, user.getAccount());
        userLogin.setLocked(0);
        userLogin.setLoginFailCount(0);
        userLogin.setLockTime(null);
        userLoginMapper.updateById(userLogin);

        return true;
    }

    @Override
    @Transactional
    public com.hrp.common.entity.Result<String> importUsers(List<List<String>> dataList, String createUser) {
        if (dataList == null || dataList.isEmpty()) {
            return com.hrp.common.entity.Result.error("导入数据为空");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();

        // 跳过表头，从第二行开始
        for (int i = 1; i < dataList.size(); i++) {
            List<String> row = dataList.get(i);
            if (row == null || row.isEmpty()) {
                continue;
            }

            try {
                // Excel列顺序：工号、姓名、性别、身份证号、出生日期、手机号、邮箱、部门编码、用户类型、职工类型
                if (row.size() < 10) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：数据列数不足，请检查Excel格式\n");
                    continue;
                }

                String empCode = row.get(0) != null ? row.get(0).trim() : "";
                String empName = row.get(1) != null ? row.get(1).trim() : "";
                String empSexStr = row.get(2) != null ? row.get(2).trim() : "";
                String idCard = row.get(3) != null ? row.get(3).trim() : "";
                String empBirthdayStr = row.get(4) != null ? row.get(4).trim() : "";
                String phone = row.get(5) != null ? row.get(5).trim() : "";
                String email = row.get(6) != null ? row.get(6).trim() : "";
                String deptCodeStr = row.get(7) != null ? row.get(7).trim() : "";
                String typeStr = row.get(8) != null ? row.get(8).trim() : "";
                String empTypeIdStr = row.get(9) != null ? row.get(9).trim() : "";

                // 验证必填字段
                if (empCode.isEmpty() || empName.isEmpty() || empSexStr.isEmpty() || typeStr.isEmpty() || empTypeIdStr.isEmpty()) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：工号、姓名、性别、用户类型、职工类型为必填字段\n");
                    continue;
                }

                // 检查员工是否已存在
                Employee existEmployee = userEmployeeService.getEmployeeByCode(empCode);
                if (existEmployee != null) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：工号").append(empCode).append("已存在\n");
                    continue;
                }

                // 解析必填字段
                Long empSex;
                Long userType;
                Long empTypeId;
                try {
                    empSex = Long.parseLong(empSexStr);
                    if (empSex != 1 && empSex != 2) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：性别必须为1（男）或2（女）\n");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：性别格式错误，必须为1（男）或2（女）\n");
                    continue;
                }
                try {
                    userType = Long.parseLong(typeStr);
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：用户类型格式错误\n");
                    continue;
                }
                try {
                    empTypeId = Long.parseLong(empTypeIdStr);
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：职工类型格式错误\n");
                    continue;
                }

                // 解析出生日期
                LocalDateTime empBirthday = null;
                if (!empBirthdayStr.isEmpty()) {
                    try {
                        // 支持格式：YYYY-MM-DD
                        java.time.LocalDate date = java.time.LocalDate.parse(empBirthdayStr);
                        empBirthday = date.atStartOfDay();
                    } catch (Exception e) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：出生日期格式错误，应为YYYY-MM-DD格式\n");
                        continue;
                    }
                }

                // 1. 先创建员工记录（sys_emp表）
                Employee employee = new Employee();
                employee.setEmpCode(empCode);
                employee.setEmpName(empName);
                employee.setEmpSex(empSex); // 必填
                employee.setIdCard(idCard.isEmpty() ? null : idCard);
                employee.setEmpBirthday(empBirthday);
                employee.setEmpPhone(phone.isEmpty() ? null : phone);
                employee.setEmpEmail(email.isEmpty() ? null : email);
                // 解析部门编码，查询出部门ID和编码，一并写入 sys_emp / sys_user
                Long deptId = null;
                String deptCode = null;
                if (!deptCodeStr.isEmpty()) {
                    Dept dept = deptService.getByCode(deptCodeStr);
                    if (dept == null) {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：部门编码").append(deptCodeStr).append("不存在\n");
                        continue;
                    }
                    deptId = dept.getDeptId();
                    deptCode = dept.getDeptCode();
                }
                employee.setDeptId(deptId);
                employee.setDeptCode(deptCode);
                employee.setEmpTypeId(empTypeId);
                employee.setIsStop(0L);
                employee.setCreateUser(createUser);
                employee.setCreateTime(now);

                boolean empSuccess = userEmployeeService.saveEmployee(employee);
                if (!empSuccess) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：创建员工记录失败\n");
                    continue;
                }

                // 2. 自动为员工创建用户账号（sys_user表）
                User user = new User();
                user.setId(UuidUtil.generateUuid());
                user.setAccount(empCode); // account对应emp_code
                user.setName(empName);
                user.setType(userType);
                user.setEmpTypeId(empTypeId);
                user.setPhone(phone);
                user.setPassword(PasswordUtil.encode("123456")); // 默认密码
                user.setDeptId(deptId);
                user.setEmpSex(empSex);
                user.setIdCard(idCard.isEmpty() ? null : idCard);
                user.setEmpBirthday(empBirthday);
                user.setEmpEmail(email.isEmpty() ? null : email);
                user.setIsStop(0L); // 默认启用状态
                user.setCreateUser(createUser);
                user.setCreateTime(now);

                // 插入用户记录
                int userResult = userMapper.insert(user);
                if (userResult <= 0) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：创建用户账号失败\n");
                    // 回滚员工记录（可选，如果希望保持一致性）
                    continue;
                }

                // 3. 创建用户登录信息
                getOrCreateUserLogin(user.getId(), user.getAccount());

                // 4. 根据用户类型自动分配菜单权限
                if (user.getType() != null) {
                    String userTypeStr = String.valueOf(user.getType());
                    List<UserTypeMenu> userTypeMenus = userTypeMenuMapper.selectByUserType(userTypeStr);
                    if (userTypeMenus != null && !userTypeMenus.isEmpty()) {
                        List<Long> menuIds = userTypeMenus.stream()
                            .map(UserTypeMenu::getMenuId)
                            .collect(java.util.stream.Collectors.toList());
                        if (!menuIds.isEmpty()) {
                            userMenuMapper.batchInsert(user.getId(), menuIds);
                        }
                    }
                }

                successCount++;
            } catch (Exception e) {
                failCount++;
                errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
            }
        }

        String message = String.format("导入完成：成功%d条，失败%d条", successCount, failCount);
        if (failCount > 0 && errorMsg.length() > 0) {
            message += "\n错误详情：\n" + errorMsg.toString();
        }

        if (failCount == 0) {
            return com.hrp.common.entity.Result.success(message);
        } else if (successCount > 0) {
            return com.hrp.common.entity.Result.error(message);
        } else {
            return com.hrp.common.entity.Result.error(message);
        }
    }
}

