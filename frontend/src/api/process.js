import request from './request'

// 流程定义管理
export const getProcessDefinitionList = () => request.get('/auth/process-definition/list')
export const getProcessDefinitionById = (id) => request.get(`/auth/process-definition/${id}`)
export const getProcessDefinitionByKey = (key) => request.get(`/auth/process-definition/key/${key}`)
export const getProcessDefinitionByType = (type) => request.get(`/auth/process-definition/type/${type}`)
export const saveProcessDefinition = (data) => request.post('/auth/process-definition/save', data)
export const updateProcessDefinition = (data) => request.put('/auth/process-definition/update', data)
export const deleteProcessDefinition = (id) => request.delete(`/auth/process-definition/${id}`)

// 流程实例管理
export const getProcessInstanceList = () => request.get('/auth/process-instance/list')
export const getProcessInstanceById = (id) => request.get(`/auth/process-instance/${id}`)
export const getProcessInstanceByBusinessKey = (businessKey) => request.get(`/auth/process-instance/business-key/${businessKey}`)
export const getProcessInstanceByStatus = (status) => request.get(`/auth/process-instance/status/${status}`)
export const getProcessInstanceVariables = (id) => request.get(`/auth/process-instance/${id}/variables`)

// 流程任务管理
export const getProcessTaskList = () => request.get('/auth/process-task/list')
export const getProcessTaskById = (id) => request.get(`/auth/process-task/${id}`)
export const getProcessTaskByInstanceId = (instanceId) => request.get(`/auth/process-task/instance/${instanceId}`)
export const getProcessTaskByAssignee = (userId) => request.get(`/auth/process-task/assignee/${userId}`)
export const getProcessTaskByBusinessKey = (businessKey) => request.get(`/auth/process-task/business-key/${businessKey}`)
export const getProcessTaskByStatus = (status) => request.get(`/auth/process-task/status/${status}`)
export const transferProcessTask = (data) => request.put('/auth/process-task/transfer', data)



