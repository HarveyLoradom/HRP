import request from './request'

export function getMyPayouts(empId) {
  return request({
    url: `/reimb/payout/my/${empId}`,
    method: 'get'
  })
}

export function getPayoutsByStatus(status) {
  return request({
    url: `/reimb/payout/status/${status}`,
    method: 'get'
  })
}

export function getPayoutById(id) {
  return request({
    url: `/reimb/payout/${id}`,
    method: 'get'
  })
}

export function savePayout(data) {
  return request({
    url: '/reimb/payout',
    method: 'post',
    data
  })
}

export function updatePayout(data) {
  return request({
    url: '/reimb/payout',
    method: 'put',
    data
  })
}

export function deletePayout(id) {
  return request({
    url: `/reimb/payout/${id}`,
    method: 'delete'
  })
}

export function submitPayout(id) {
  return request({
    url: `/reimb/payout/${id}/submit`,
    method: 'post'
  })
}

export function withdrawPayout(id) {
  return request({
    url: `/reimb/payout/${id}/withdraw`,
    method: 'post'
  })
}

export function getMyApprovalPayouts(userId) {
  return request({
    url: `/reimb/payout/my-approval/${userId}`,
    method: 'get'
  })
}

export function getAllPayouts() {
  return request({
    url: '/reimb/payout/all',
    method: 'get'
  })
}

export function approvePayout(id, userId, opinion) {
  return request({
    url: `/reimb/payout/${id}/approve?userId=${userId}&opinion=${opinion || ''}`,
    method: 'post'
  })
}

export function rejectPayout(id, userId, opinion) {
  return request({
    url: `/reimb/payout/${id}/reject?userId=${userId}&opinion=${opinion || ''}`,
    method: 'post'
  })
}

export function getNextApprover(id) {
  return request({
    url: `/reimb/payout/${id}/next-approver`,
    method: 'get'
  })
}

// 获取完整信息（包括明细、发票、支付清单、审批记录）
export function getPayoutDetail(id) {
  return request({
    url: `/reimb/payout/${id}/detail`,
    method: 'get'
  })
}

// 获取审批记录
export function getPayoutApprovals(id) {
  return request({
    url: `/reimb/payout/${id}/approvals`,
    method: 'get'
  })
}

// 保存报账单完整信息
export function savePayoutFull(data) {
  return request({
    url: '/reimb/payout/payout/save',
    method: 'post',
    data
  })
}

// 更新报账单完整信息
export function updatePayoutFull(data) {
  return request({
    url: '/reimb/payout/payout/update',
    method: 'put',
    data
  })
}

