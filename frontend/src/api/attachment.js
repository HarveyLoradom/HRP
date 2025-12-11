import request from './request'

// 获取附件
export function getAttachment(id) {
  return request({
    url: `/attachment/${id}`,
    method: 'get'
  })
}

// 根据业务查询附件
export function getAttachmentsByBusiness(businessType, businessId) {
  return request({
    url: `/attachment/business?businessType=${businessType}&businessId=${businessId}`,
    method: 'get'
  })
}

// 保存附件
export function saveAttachment(data) {
  return request({
    url: '/attachment',
    method: 'post',
    data
  })
}

// 批量保存附件
export function saveAttachmentsBatch(data) {
  return request({
    url: '/attachment/batch',
    method: 'post',
    data
  })
}

// 删除附件
export function deleteAttachment(id) {
  return request({
    url: `/attachment/${id}`,
    method: 'delete'
  })
}

// 删除业务相关附件
export function deleteAttachmentsByBusiness(businessType, businessId) {
  return request({
    url: `/attachment/business?businessType=${businessType}&businessId=${businessId}`,
    method: 'delete'
  })
}

// 上传文件
export function uploadFile(file, businessType, businessId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', businessType)
  formData.append('businessId', businessId)
  return request({
    url: '/attachment/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}




