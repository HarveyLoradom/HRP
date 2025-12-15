import request from './request'

// 打印模板管理
export const getPrintTemplateList = () => request.get('/auth/print-template/list')
export const getPrintTemplateById = (id) => request.get(`/auth/print-template/${id}`)
export const getPrintTemplateByCode = (code) => request.get(`/auth/print-template/code/${code}`)
export const getPrintTemplateByType = (type) => request.get(`/auth/print-template/type/${type}`)
export const getDefaultPrintTemplate = (type) => request.get(`/auth/print-template/default/${type}`)
export const savePrintTemplate = (data) => request.post('/auth/print-template/save', data)
export const updatePrintTemplate = (data) => request.put('/auth/print-template/update', data)
export const deletePrintTemplate = (id) => request.delete(`/auth/print-template/${id}`)
export const getTableFields = (templateType) => request.get(`/auth/print-template/fields/${templateType}`)
export const getAllTableNames = () => request.get('/auth/print-template/tables')
export const getTableFieldsByTableName = (tableName) => {
  // 对表名进行URL编码，避免特殊字符问题
  const encodedTableName = encodeURIComponent(tableName)
  return request.get(`/auth/print-template/table-fields/${encodedTableName}`)
}

// 打印功能（使用Vue Print.js）
export const generatePrintContent = (data) => {
  return request({
    url: '/auth/print-template/generate',
    method: 'post',
    data: data
  })
}

export const previewPrintTemplate = (data) => {
  return request({
    url: '/auth/print-template/preview',
    method: 'post',
    data: data
  })
}

// 模板启用/停用
export const togglePrintTemplateActive = (templateId, isActive) => {
  return request({
    url: `/auth/print-template/toggle-active/${templateId}`,
    method: 'put',
    params: { isActive }
  })
}

// 模板导入导出
export const exportPrintTemplate = (templateId) => {
  return request({
    url: `/auth/print-template/export/${templateId}`,
    method: 'get',
    responseType: 'blob' // 重要：设置响应类型为blob
  })
}

export const importPrintTemplate = (data) => {
  return request({
    url: '/auth/print-template/import',
    method: 'post',
    data: data
  })
}

