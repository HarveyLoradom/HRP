import request from './request'

// 修改密码
export function changePassword(data) {
  return request({
    url: '/auth/password/change',
    method: 'post',
    data: {
      userId: data.userId || JSON.parse(localStorage.getItem('userInfo') || '{}').userId || JSON.parse(localStorage.getItem('userInfo') || '{}').id,
      oldPassword: data.oldPassword,
      newPassword: data.newPassword
    }
  })
}

// 角色管理
export const getRoleList = () => request.get('/user/role/list')
export const getRoleById = (roleId) => request.get(`/user/role/${roleId}`)
export const saveRole = (data) => request.post('/user/role/save', data)
export const updateRole = (data) => request.put('/user/role/update', data)
export const deleteRole = (roleId) => request.delete(`/user/role/${roleId}`)

// 用户角色关联
export const getUserRoles = (userId) => request.get(`/user/user-role/user/${userId}`)
export const assignUserRoles = (userId, roleIds) => request.post(`/user/user-role/assign?userId=${userId}`, roleIds)
export const removeUserRole = (userId, roleId) => request.delete(`/user/user-role/remove?userId=${userId}&roleId=${roleId}`)

// 角色菜单关联
export const getRoleMenus = (roleId) => request.get(`/user/role-menu/role/${roleId}`)
export const assignRoleMenus = (roleId, menuIds) => request.post(`/user/role-menu/assign?roleId=${roleId}`, menuIds)

// 系统字典
export const getCodeList = () => request.get('/user/code/list')
export const getCodeByType = (codeType) => request.get(`/user/code/type/${codeType}`)
export const saveCode = (data) => request.post('/user/code/save', data)
export const updateCode = (data) => request.put('/user/code/update', data)
export const deleteCode = (id) => request.delete(`/user/code/${id}`)

// 部门管理
export const getDeptList = () => request.get('/user/dept/list')
export const getDeptById = (deptId) => request.get(`/user/dept/${deptId}`)
export const getDeptByParent = (superDeptCode) => request.get(`/user/dept/parent/${superDeptCode}`)
export const saveDept = (data) => request.post('/user/dept/save', data)
export const updateDept = (data) => request.put('/user/dept/update', data)
export const deleteDept = (deptId) => request.delete(`/user/dept/${deptId}`)

// 用户管理
export const getUserList = () => request.get('/user/user/list')
export const getUserById = (id) => request.get(`/user/user/${id}`)
export const getUserByAccount = (account) => request.get(`/user/user/account/${account}`)
export const saveUser = (data) => request.post('/user/user/save', data)
export const updateUser = (data) => request.put('/user/user/update', data)
export const deleteUser = (id) => request.delete(`/user/user/${id}`)
export const toggleUserStatus = (id) => request.put(`/user/user/toggle-status/${id}`)
export const resetUserPassword = (id) => request.put(`/user/user/reset-password/${id}`)

// 职工管理
export const getEmployeeList = () => request.get('/user/employee/list')
export const getEmployeeById = (empId) => request.get(`/user/employee/${empId}`)
export const getEmployeeByCode = (empCode) => request.get(`/user/employee/code/${empCode}`)
export const getEmployeeByDeptId = (deptId) => request.get(`/user/employee/dept/${deptId}`)
export const saveEmployee = (data) => request.post('/user/employee/save', data)
export const updateEmployee = (data) => request.put('/user/employee/update', data)
export const deleteEmployee = (empId) => request.delete(`/user/employee/${empId}`)

// 用户菜单权限管理
export const getUserMenuIds = (userId) => request.get(`/user/user-menu/${userId}`)
export const assignUserMenus = (userId, menuIds) => request.post('/user/user-menu/assign', { userId, menuIds })
