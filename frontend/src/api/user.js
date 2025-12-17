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
export const getRoleList = () => request.get('/auth/role/list')
export const getRoleById = (roleId) => request.get(`/auth/role/${roleId}`)
export const saveRole = (data) => request.post('/auth/role/save', data)
export const updateRole = (data) => request.put('/auth/role/update', data)
export const deleteRole = (roleId) => request.delete(`/auth/role/${roleId}`)

// 用户角色关联
export const getUserRoles = (userId) => request.get(`/auth/user-role/user/${userId}`)
export const assignUserRoles = (userId, roleIds) => request.post(`/auth/user-role/assign?userId=${userId}`, roleIds)
export const batchAssignUserRoles = (userIds, roleIds) => request.post('/auth/user-role/batch-assign', { userIds, roleIds })
export const removeUserRole = (userId, roleId) => request.delete(`/auth/user-role/remove?userId=${userId}&roleId=${roleId}`)


// 用户类型菜单关联
export const getUserTypeMenus = (userType) => request.get(`/auth/user-type-menu/user-type/${userType}`)
export const assignUserTypeMenus = (userType, menuIds) => request.post(`/auth/user-type-menu/assign?userType=${userType}`, menuIds)

// 系统字典
export const getCodeList = () => request.get('/auth/code/list')
export const getCodeByType = (codeType) => request.get(`/auth/code/type/${codeType}`)
export const getCodePage = (page, size) => request.get(`/auth/code/page?page=${page}&size=${size}`)
export const getCodeByTypePage = (codeType, page, size) => request.get(`/auth/code/type/${codeType}/page?page=${page}&size=${size}`)
export const saveCode = (data) => request.post('/auth/code/save', data)
export const saveCodeBatch = (dataList) => request.post('/auth/code/saveBatch', dataList)
export const updateCode = (data) => request.put('/auth/code/update', data)
export const deleteCode = (id) => request.delete(`/auth/code/${id}`)

// 部门管理
export const getDeptList = () => request.get('/auth/dept/list')
export const getDeptById = (deptId) => request.get(`/auth/dept/${deptId}`)
export const getDeptByParent = (superDeptCode) => request.get(`/auth/dept/parent/${superDeptCode}`)
export const saveDept = (data) => request.post('/auth/dept/save', data)
export const updateDept = (data) => request.put('/auth/dept/update', data)
export const deleteDept = (deptId) => request.delete(`/auth/dept/${deptId}`)
export const toggleDeptStatus = (deptId) => request.put(`/auth/dept/toggle-status/${deptId}`)

// 用户管理
export const getUserList = () => request.get('/auth/user/list')
export const getUserById = (id) => request.get(`/auth/user/${id}`)
export const getUserByAccount = (account) => request.get(`/auth/user/account/${account}`)
export const saveUser = (data) => request.post('/auth/user/save', data)
export const updateUser = (data) => request.put('/auth/user/update', data)
export const deleteUser = (id) => request.delete(`/auth/user/${id}`)
export const toggleUserStatus = (id) => request.put(`/auth/user/toggle-status/${id}`)
export const resetUserPassword = (id) => request.put(`/auth/user/reset-password/${id}`)
export const unlockUser = (id) => request.put(`/auth/user/unlock/${id}`)

// 用户管理（显示所有员工）
export const getAllEmployeesWithUser = () => request.get('/auth/user/employees')
export const searchEmployees = (keyword) => request.get(`/auth/user/employees/search?keyword=${keyword}`)

// 根据工号查询员工信息（sys_emp表）
export const getEmployeeByCode = (empCode) => request.get(`/auth/user/employee/${empCode}`)

// 查询所有员工列表（供其他功能使用，如部门管理选择部门负责人等）
export const getEmployeeList = () => request.get('/auth/user/employees/list')

// 更新员工信息（用于个人中心）
export const updateEmployee = (data) => request.put('/auth/user/employee/update', data)

// 上传员工照片
export const uploadEmployeePhoto = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/auth/attachment/upload-photo',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除员工照片
export const deleteEmployeePhoto = (photoPath) => {
  return request({
    url: `/auth/attachment/delete-photo?photoPath=${encodeURIComponent(photoPath)}`,
    method: 'delete'
  })
}

// 用户菜单权限管理
export const getUserMenuIds = (userId) => request.get(`/auth/user-menu/${userId}`)
export const assignUserMenus = (userId, menuIds) => request.post('/auth/user-menu/assign', { userId, menuIds })

// 岗位管理
export const getPositionList = () => request.get('/auth/position/list')
export const getPositionById = (positionId) => request.get(`/auth/position/${positionId}`)
export const getPositionByCode = (positionCode) => request.get(`/auth/position/code/${positionCode}`)
export const savePosition = (data) => request.post('/auth/position/save', data)
export const updatePosition = (data) => request.put('/auth/position/update', data)
export const deletePosition = (positionId) => request.delete(`/auth/position/${positionId}`)
export const assignPositionUsers = (positionId, userIds) => request.post(`/auth/position/${positionId}/assign-users`, userIds)
export const getPositionUsers = (positionId) => request.get(`/auth/position/${positionId}/users`)
