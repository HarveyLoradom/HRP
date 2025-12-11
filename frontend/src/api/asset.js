import request from './request'

export function getAllAssets() {
  return request({
    url: '/asset/list',
    method: 'get'
  })
}

export function getAssetsByStatus(status) {
  return request({
    url: `/asset/status/${status}`,
    method: 'get'
  })
}

export function getAssetById(id) {
  return request({
    url: `/asset/${id}`,
    method: 'get'
  })
}

export function saveAsset(data) {
  return request({
    url: '/asset',
    method: 'post',
    data
  })
}

export function updateAsset(data) {
  return request({
    url: '/asset',
    method: 'put',
    data
  })
}

export function deleteAsset(id) {
  return request({
    url: `/asset/${id}`,
    method: 'delete'
  })
}

export function getAllProcurements() {
  return request({
    url: '/asset/procurement/list',
    method: 'get'
  })
}

export function getProcurementsByStatus(status) {
  return request({
    url: `/asset/procurement/status/${status}`,
    method: 'get'
  })
}

export function getProcurementsByApplicant(applicantId) {
  return request({
    url: `/asset/procurement/applicant/${applicantId}`,
    method: 'get'
  })
}

export function getProcurementsByApprover(approverId) {
  return request({
    url: `/asset/procurement/approver/${approverId}`,
    method: 'get'
  })
}

export function getProcurementById(id) {
  return request({
    url: `/asset/procurement/${id}`,
    method: 'get'
  })
}

export function getProcurementDetail(id) {
  return request({
    url: `/asset/procurement/detail/${id}`,
    method: 'get'
  })
}

export function saveProcurement(data) {
  return request({
    url: '/asset/procurement',
    method: 'post',
    data
  })
}

export function submitProcurement(id) {
  return request({
    url: `/asset/procurement/submit/${id}`,
    method: 'post'
  })
}

export function withdrawProcurement(id) {
  return request({
    url: `/asset/procurement/withdraw/${id}`,
    method: 'post'
  })
}

export function approveProcurement(data) {
  return request({
    url: '/asset/procurement/approve',
    method: 'post',
    data
  })
}

export function rejectProcurement(data) {
  return request({
    url: '/asset/procurement/reject',
    method: 'post',
    data
  })
}

export function deleteProcurement(id) {
  return request({
    url: `/asset/procurement/${id}`,
    method: 'delete'
  })
}

// 资产审批相关API
export function getAssetApprovalMyList(approvalType, empId) {
  return request({
    url: '/asset/approval/my-list',
    method: 'get',
    params: { approvalType, empId }
  })
}

export function getAssetApprovalsByType(type) {
  return request({
    url: `/asset/approval/type/${type}`,
    method: 'get'
  })
}

export function getAssetApprovalsByApplicant(applicantId) {
  return request({
    url: `/asset/approval/applicant/${applicantId}`,
    method: 'get'
  })
}

export function getAssetApprovalsByApprover(approverId) {
  return request({
    url: `/asset/approval/approver/${approverId}`,
    method: 'get'
  })
}

export function getAssetApprovalById(id) {
  return request({
    url: `/asset/approval/${id}`,
    method: 'get'
  })
}

export function getAssetApprovalDetail(id) {
  return request({
    url: `/asset/approval/detail/${id}`,
    method: 'get'
  })
}

export function saveAssetApproval(data) {
  return request({
    url: '/asset/approval',
    method: 'post',
    data
  })
}

export function submitAssetApproval(id) {
  return request({
    url: `/asset/approval/submit/${id}`,
    method: 'post'
  })
}

export function withdrawAssetApproval(id) {
  return request({
    url: `/asset/approval/withdraw/${id}`,
    method: 'post'
  })
}

export function approveAssetApproval(data) {
  return request({
    url: '/asset/approval/approve',
    method: 'post',
    data
  })
}

export function rejectAssetApproval(data) {
  return request({
    url: '/asset/approval/reject',
    method: 'post',
    data
  })
}

export function deleteAssetApproval(id) {
  return request({
    url: `/asset/approval/${id}`,
    method: 'delete'
  })
}

export function getAssetApprovalRecords(id) {
  return request({
    url: `/asset/approval/records/${id}`,
    method: 'get'
  })
}

export function getProcurementMyList(empId) {
  return request({
    url: '/asset/procurement/my-list',
    method: 'get',
    params: { empId }
  })
}

export function getProcurementApprovals(id) {
  return request({
    url: `/asset/procurement/approvals/${id}`,
    method: 'get'
  })
}

