import request from './request'

// 流程定义管理
export const getProcessDefinitionList = (isActive) => {
  const params = isActive !== null && isActive !== undefined ? { isActive } : {}
  return request.get('/auth/process-definition/list', { params })
}
export const getProcessDefinitionPage = (isActive, page, size) => {
  const params = { page, size }
  if (isActive !== null && isActive !== undefined) {
    params.isActive = isActive
  }
  return request.get('/auth/process-definition/page', { params })
}
export const getProcessDefinitionById = (id) => request.get(`/auth/process-definition/${id}`)
export const getProcessNodes = (id) => request.get(`/auth/process-definition/${id}/nodes`)
export const getProcessNodesWithBusiness = (id, businessKey) => request.get(`/auth/process-definition/${id}/nodes/business`, { params: { applyNo: businessKey } })
export const getProcessDefinitionByKey = (key) => request.get(`/auth/process-definition/key/${key}`)
export const getProcessDefinitionByType = (type, isActive) => {
  const params = isActive !== null && isActive !== undefined ? { isActive } : {}
  return request.get(`/auth/process-definition/type/${type}`, { params })
}
export const getProcessDefinitionByTypePage = (type, isActive, page, size) => {
  const params = { page, size }
  if (isActive !== null && isActive !== undefined) {
    params.isActive = isActive
  }
  return request.get(`/auth/process-definition/type/${type}/page`, { params })
}
export const saveProcessDefinition = (data) => request.post('/auth/process-definition/save', data)
export const updateProcessDefinition = (data) => request.put('/auth/process-definition/update', data)
export const deleteProcessDefinition = (id) => request.delete(`/auth/process-definition/${id}`)

// 流程实例管理（已迁移到流程任务服务）
export const getProcessInstanceList = (params) => {
  return request.get('/auth/process-task/instances', { params: params || {} })
}
export const getProcessInstanceListPage = (page, size) => {
  return request.get('/auth/process-task/instances/page', { params: { page, size } })
}
export const getProcessInstanceVariables = (businessKey, businessType) => {
  return request.get(`/auth/process-task/instance/variables`, { params: { businessKey, businessType } })
}
export const terminateProcessInstance = (businessKey) => request.post(`/auth/process-task/instance/terminate`, null, { params: { businessKey } })
export const updateProcessInstanceVariables = (businessKey, businessType, variables) => {
  return request.put(`/auth/process-task/instance/variables`, variables, { params: { businessKey, businessType } })
}

// 流程任务管理
export const getProcessTaskList = () => request.get('/auth/process-task/list')
export const getProcessTaskById = (id) => request.get(`/auth/process-task/${id}`)
export const getProcessTaskByInstanceId = (instanceId) => request.get(`/auth/process-task/instance/${instanceId}`)
export const getProcessTaskByAssignee = (userId) => request.get(`/auth/process-task/assignee/${userId}`)
export const getProcessTaskByAssigneePage = (userId, page, size) => request.get(`/auth/process-task/assignee/${userId}/page`, { params: { page, size } })
export const getAllCurrentTasks = () => request.get('/auth/process-task/current/all')
export const getAllCurrentTasksPage = (page, size) => request.get('/auth/process-task/current/all/page', { params: { page, size } })
export const getProcessTaskByAssigneeAndStatusPage = (userId, taskStatus, page, size) => request.get(`/auth/process-task/assignee/${userId}/status/${taskStatus}/page`, { params: { page, size } })
export const getProcessTaskByBusinessKey = (businessKey) => request.get(`/auth/process-task/business-key/${businessKey}`)
export const getProcessTaskByBusinessKeyPage = (businessKey, page, size) => request.get(`/auth/process-task/business-key/${businessKey}/page?page=${page}&size=${size}`)
export const getProcessTaskByStatus = (status) => request.get(`/auth/process-task/status/${status}`)
export const getProcessTaskByStatusPage = (status, page, size) => request.get(`/auth/process-task/status/${status}/page?page=${page}&size=${size}`)
export const transferProcessTask = (data) => request.put('/auth/process-task/transfer', data)
export const getNextNodeInfoByInstanceId = (instanceId) => request.get(`/auth/process-task/next-node/${instanceId}`)
export const getNextNodeInfoByBusinessKey = (businessKey) => request.get(`/auth/process-task/next-node/business-key/${businessKey}`)
export const getProcessTaskByTaskKey = (taskKey) => request.get(`/auth/process-task/task-key/${taskKey}`)

// 加签管理
export const createAddSign = (data) => request.post('/auth/addsign/create', data)
export const completeAddSign = (data) => request.post('/auth/addsign/complete', data)
export const getAddSignRecords = (parentTaskId) => request.get(`/auth/addsign/parent-task/${parentTaskId}`)

// 流程任务退回
export const returnTask = (data) => request.post('/auth/process-task/return-task', data)

// 流程定义启用/停用
export const toggleProcessDefinitionActive = (definitionId, isActive) => {
  return request({
    url: `/auth/process-definition/toggle-active/${definitionId}`,
    method: 'put',
    params: { isActive }
  })
}

// 流程定义导入导出
export const exportProcessDefinition = (definitionId) => {
  return request({
    url: `/auth/process-definition/export/${definitionId}`,
    method: 'get',
    responseType: 'blob' // 重要：设置响应类型为blob
  })
}

export const importProcessDefinition = (data) => {
  return request({
    url: '/auth/process-definition/import',
    method: 'post',
    data: data
  })
}

// 流程实例启动
export const startProcessInstance = (data) => {
  return request({
    url: '/auth/process-instance/start',
    method: 'post',
    data: data
  })
}



