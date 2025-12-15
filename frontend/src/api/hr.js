import request from './request'

export function getAllSalaries() {
  return request({
    url: '/hr/salary/list',
    method: 'get'
  })
}

export function getSalariesByEmpId(empId) {
  return request({
    url: `/hr/salary/emp/${empId}`,
    method: 'get'
  })
}

export function getSalariesByMonth(salaryMonth) {
  return request({
    url: `/hr/salary/month/${salaryMonth}`,
    method: 'get'
  })
}

export function getSalaryById(id) {
  return request({
    url: `/hr/salary/${id}`,
    method: 'get'
  })
}

export function saveSalary(data) {
  return request({
    url: '/hr/salary',
    method: 'post',
    data
  })
}

export function calculateSalary(empId, salaryMonth) {
  return request({
    url: `/hr/salary/${empId}/calculate/${salaryMonth}`,
    method: 'post'
  })
}

// 业务申请相关API
export function getBusinessApplyMyList(empId) {
  return request({
    url: '/hr/business-apply/my-list',
    method: 'get',
    params: { empId }
  })
}

export function saveBusinessApply(data) {
  return request({
    url: '/hr/business-apply',
    method: 'post',
    data
  })
}

export function updateBusinessApply(data) {
  return request({
    url: '/hr/business-apply',
    method: 'put',
    data
  })
}

export function deleteBusinessApply(id) {
  return request({
    url: `/hr/business-apply/${id}`,
    method: 'delete'
  })
}

export function submitBusinessApply(id) {
  return request({
    url: `/hr/business-apply/submit/${id}`,
    method: 'post'
  })
}

export function withdrawBusinessApply(id) {
  return request({
    url: `/hr/business-apply/withdraw/${id}`,
    method: 'post'
  })
}

export function getBusinessApplyDetail(id) {
  return request({
    url: `/hr/business-apply/detail/${id}`,
    method: 'get'
  })
}

export function getBusinessApplyRecords(id) {
  return request({
    url: `/hr/business-apply/records/${id}`,
    method: 'get'
  })
}

// 考勤管理相关API
export function getMyAttendanceList(empId, month) {
  return request({
    url: '/hr/attendance/my-list',
    method: 'get',
    params: { empId, month }
  })
}

// 请假管理相关API
export function getMyLeaveList(empId) {
  return request({
    url: '/hr/leave/my-list',
    method: 'get',
    params: { empId }
  })
}

export function getLeaveDetail(id) {
  return request({
    url: `/hr/leave/detail/${id}`,
    method: 'get'
  })
}

export function getLeaveRecords(id) {
  return request({
    url: `/hr/leave/records/${id}`,
    method: 'get'
  })
}

export function approveLeave(data) {
  return request({
    url: '/hr/leave/approve',
    method: 'post',
    data
  })
}

export function rejectLeave(data) {
  return request({
    url: '/hr/leave/reject',
    method: 'post',
    data
  })
}

// 绩效管理相关API
export function getMyPerformanceList(empId, year, quarter) {
  return request({
    url: '/hr/performance/my-list',
    method: 'get',
    params: { empId, year, quarter }
  })
}

// 入转调离管理相关API
export function getMyTransferList(empId) {
  return request({
    url: '/hr/transfer/my-list',
    method: 'get',
    params: { empId }
  })
}

export function getTransferDetail(id) {
  return request({
    url: `/hr/transfer/detail/${id}`,
    method: 'get'
  })
}

export function getTransferRecords(id) {
  return request({
    url: `/hr/transfer/records/${id}`,
    method: 'get'
  })
}

export function approveTransfer(data) {
  return request({
    url: '/hr/transfer/approve',
    method: 'post',
    data
  })
}

export function rejectTransfer(data) {
  return request({
    url: '/hr/transfer/reject',
    method: 'post',
    data
  })
}

