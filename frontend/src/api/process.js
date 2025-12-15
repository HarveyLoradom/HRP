import request from './request'

// 流程定义管理
export const getProcessDefinitionList = () => request.get('/auth/process-definition/list')
export const getProcessDefinitionPage = (page, size) => request.get(`/auth/process-definition/page?page=${page}&size=${size}`)
export const getProcessDefinitionById = (id) => request.get(`/auth/process-definition/${id}`)
export const getProcessDefinitionByKey = (key) => request.get(`/auth/process-definition/key/${key}`)
export const getProcessDefinitionByType = (type) => request.get(`/auth/process-definition/type/${type}`)
export const getProcessDefinitionByTypePage = (type, page, size) => request.get(`/auth/process-definition/type/${type}/page?page=${page}&size=${size}`)
export const saveProcessDefinition = (data) => request.post('/auth/process-definition/save', data)
export const updateProcessDefinition = (data) => request.put('/auth/process-definition/update', data)
export const deleteProcessDefinition = (id) => request.delete(`/auth/process-definition/${id}`)

// 流程实例管理
export const getProcessInstanceList = () => request.get('/auth/process-instance/list')
export const getProcessInstancePage = (page, size) => request.get(`/auth/process-instance/page?page=${page}&size=${size}`)
export const getProcessInstanceById = (id) => request.get(`/auth/process-instance/${id}`)
export const getProcessInstanceByBusinessKey = (businessKey) => request.get(`/auth/process-instance/business-key/${businessKey}`)
export const getProcessInstanceByStatus = (status) => request.get(`/auth/process-instance/status/${status}`)
export const getProcessInstanceByStatusPage = (status, page, size) => request.get(`/auth/process-instance/status/${status}/page?page=${page}&size=${size}`)
export const getProcessInstanceVariables = (id) => request.get(`/auth/process-instance/${id}/variables`)

// 流程任务管理
export const getProcessTaskList = () => request.get('/auth/process-task/list')
export const getProcessTaskById = (id) => request.get(`/auth/process-task/${id}`)
export const getProcessTaskByInstanceId = (instanceId) => request.get(`/auth/process-task/instance/${instanceId}`)
export const getProcessTaskByAssignee = (userId) => request.get(`/auth/process-task/assignee/${userId}`)
export const getProcessTaskByBusinessKey = (businessKey) => request.get(`/auth/process-task/business-key/${businessKey}`)
export const getProcessTaskByBusinessKeyPage = (businessKey, page, size) => request.get(`/auth/process-task/business-key/${businessKey}/page?page=${page}&size=${size}`)
export const getProcessTaskByStatus = (status) => request.get(`/auth/process-task/status/${status}`)
export const getProcessTaskByStatusPage = (status, page, size) => request.get(`/auth/process-task/status/${status}/page?page=${page}&size=${size}`)
export const transferProcessTask = (data) => request.put('/auth/process-task/transfer', data)

// 加签管理
export const createAddSign = (data) => request.post('/auth/addsign/create', data)
export const completeAddSign = (data) => request.post('/auth/addsign/complete', data)
export const getAddSignRecords = (parentTaskId) => request.get(`/auth/addsign/parent-task/${parentTaskId}`)

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



