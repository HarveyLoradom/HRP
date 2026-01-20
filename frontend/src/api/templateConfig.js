import request from './request'

// 模板设置管理
export const getTemplateConfigList = (isActive) => {
  const params = isActive !== null && isActive !== undefined ? { isActive } : {}
  return request.get('/auth/template-config/list', { params })
}
export const getTemplateConfigPage = (businessType, isActive, page, size) => {
  const params = { page, size }
  if (businessType !== null && businessType !== undefined) {
    params.businessType = businessType
  }
  if (isActive !== null && isActive !== undefined) {
    params.isActive = isActive
  }
  return request.get('/auth/template-config/page', { params })
}
export const getTemplateConfigById = (id) => request.get(`/auth/template-config/${id}`)
export const getTemplateConfigByBusinessType = (businessType, businessTypeValue) => {
  return request.get(`/auth/template-config/business-type?businessType=${businessType}&businessTypeValue=${businessTypeValue}`)
}
export const getTemplateConfigByBusinessTypeOnly = (businessType) => {
  return request.get(`/auth/template-config/business-type/${businessType}`)
}
export const saveTemplateConfig = (data) => request.post('/auth/template-config/save', data)
export const updateTemplateConfig = (data) => request.put('/auth/template-config/update', data)
export const deleteTemplateConfig = (id) => request.delete(`/auth/template-config/${id}`)

