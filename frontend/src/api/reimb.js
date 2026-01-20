import request from './request'

export function getMyPayouts(empId) {
  return request({
    url: `/reimb/payout/my/${empId}`,
    method: 'get'
  })
}

export function getMyPayoutsPage(empId, page, size) {
  return request({
    url: `/reimb/payout/my/${empId}/page?page=${page}&size=${size}`,
    method: 'get'
  })
}

export function getPayoutsByStatus(status) {
  return request({
    url: `/reimb/payout/status/${status}`,
    method: 'get'
  })
}

export function getPayoutsByStatusPage(status, page, size) {
  return request({
    url: `/reimb/payout/status/${status}/page?page=${page}&size=${size}`,
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

export function getMyApprovalPayoutsPage(userId, page, size) {
  return request({
    url: `/reimb/payout/my-approval/${userId}/page?page=${page}&size=${size}`,
    method: 'get'
  })
}

export function getAllPayouts() {
  return request({
    url: '/reimb/payout/all',
    method: 'get'
  })
}

export function getAllPayoutsPage(page, size) {
  return request({
    url: `/reimb/payout/all/page?page=${page}&size=${size}`,
    method: 'get'
  })
}

export function getPayoutList(params) {
  return request({
    url: '/reimb/payout/list',
    method: 'get',
    params
  })
}

export function approvePayout(id, userId, opinion, approverSignature) {
  const data = {
    userId: userId,
    opinion: opinion || ''
  }
  if (approverSignature) {
    data.approverSignature = approverSignature
  }
  return request({
    url: `/reimb/payout/${id}/approve`,
    method: 'post',
    data
  })
}

export function rejectPayout(id, userId, opinion) {
  return request({
    url: `/reimb/payout/${id}/reject?userId=${userId}&opinion=${opinion || ''}`,
    method: 'post'
  })
}

export function returnPayout(payoutId, returnType, opinion) {
  return request({
    url: `/reimb/payout/${payoutId}/return`,
    method: 'post',
    data: { returnType, opinion }
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

// 获取预算明细
export function getBudgetDetailsByBusinessNo(businessNo) {
  return request({
    url: `/reimb/payout/budget-details/${businessNo}`,
    method: 'get'
  })
}

// 供应商相关API
export function getSuppliers(params) {
  return request({
    url: '/reimb/supplier/list',
    method: 'get',
    params
  })
}

export function getSupplierById(id) {
  return request({
    url: `/reimb/supplier/${id}`,
    method: 'get'
  })
}

export function saveSupplier(data) {
  return request({
    url: '/reimb/supplier',
    method: 'post',
    data
  })
}

export function updateSupplier(data) {
  return request({
    url: '/reimb/supplier',
    method: 'put',
    data
  })
}

export function deleteSupplier(id) {
  return request({
    url: `/reimb/supplier/${id}`,
    method: 'delete'
  })
}

export function stopSupplier(id) {
  return request({
    url: `/reimb/supplier/stop/${id}`,
    method: 'post'
  })
}

export function startSupplier(id) {
  return request({
    url: `/reimb/supplier/start/${id}`,
    method: 'post'
  })
}

// 获取本人的申请单列表（用于选择来源申请单）
export function getMyApplyList(params) {
  return request({
    url: '/reimb/payout/list',
    method: 'get',
    params: {
      ...params,
      billTypePrefix: 'SQD', // 申请单前缀
      status: 'APPROVED' // 只查询已审批的申请单
    }
  })
}

// 检查来源申请单号是否已被使用
export function checkSourceApplyNo(sourceApplyNo, excludePayoutId) {
  let url = `/reimb/payout/check-source-apply-no/${sourceApplyNo}`
  if (excludePayoutId) {
    url += `?excludePayoutId=${excludePayoutId}`
  }
  return request({
    url: url,
    method: 'get'
  })
}

// 检查合同编号是否已被其他报账单关联
export function checkContractNo(contractNo, excludePayoutId) {
  let url = `/reimb/payout/check-contract-no/${contractNo}`
  if (excludePayoutId) {
    url += `?excludePayoutId=${excludePayoutId}`
  }
  return request({
    url: url,
    method: 'get'
  })
}

