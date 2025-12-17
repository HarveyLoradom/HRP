import request from './request'

// 获取附件
export function getAttachment(id) {
  return request({
    url: `/auth/attachment/${id}`,
    method: 'get'
  })
}

// 根据业务查询附件
export function getAttachmentsByBusiness(businessType, businessId) {
  return request({
    url: `/auth/attachment/business?businessType=${businessType}&businessId=${businessId}`,
    method: 'get'
  })
}

// 保存附件
export function saveAttachment(data) {
  return request({
    url: '/auth/attachment',
    method: 'post',
    data
  })
}

// 批量保存附件
export function saveAttachmentsBatch(data) {
  return request({
    url: '/auth/attachment/batch',
    method: 'post',
    data
  })
}

// 删除附件
export function deleteAttachment(id) {
  return request({
    url: `/auth/attachment/${id}`,
    method: 'delete'
  })
}

// 删除业务相关附件
export function deleteAttachmentsByBusiness(businessType, businessId) {
  return request({
    url: `/auth/attachment/business?businessType=${businessType}&businessId=${businessId}`,
    method: 'delete'
  })
}

// 上传文件
export function uploadFile(file, businessType, businessId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', businessType)
  // 只有当 businessId 存在且有效时才添加
  if (businessId !== null && businessId !== undefined && businessId !== '' && businessId !== 'null' && businessId !== 'undefined') {
    formData.append('businessId', String(businessId))
  }
  return request({
    url: '/auth/attachment/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 批量更新附件的 businessId（用于新增业务记录保存后更新附件）
export function updateAttachmentBusinessId(businessType, businessId, attachmentIds) {
  return request({
    url: '/auth/attachment/update-business-id',
    method: 'put',
    params: {
      businessType,
      businessId,
      attachmentIds: attachmentIds && attachmentIds.length > 0 ? attachmentIds.join(',') : undefined
    }
  })
}













