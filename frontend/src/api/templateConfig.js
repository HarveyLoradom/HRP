import request from './request'

// 模板设置管理
export const getTemplateConfigList = () => request.get('/auth/template-config/list')
export const getTemplateConfigPage = (page, size) => request.get(`/auth/template-config/page?page=${page}&size=${size}`)
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

