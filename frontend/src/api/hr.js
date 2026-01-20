import request from './request'

// ==================== 业务申请 ====================
export const getHrApplyPage = (params) => request.get('/hr/apply/page', { params })
export const getHrApplyById = (id) => request.get(`/hr/apply/${id}`)
export const getHrApplyByApplyNo = (applyNo) => request.get(`/hr/apply/apply-no/${applyNo}`)
export const getMyHrApplies = (empId) => request.get(`/hr/apply/my/${empId}`)
export const getMyHrAppliesPage = (empId, page, size) => request.get(`/hr/apply/my/${empId}/page?page=${page}&size=${size}`)
export const getMyApprovalHrApplies = (userId) => request.get(`/hr/apply/my-approval/${userId}`)
export const getMyApprovalHrAppliesPage = (userId, page, size) => request.get(`/hr/apply/my-approval/${userId}/page?page=${page}&size=${size}`)
export const saveHrApply = (data) => request.post('/hr/apply', data)
export const updateHrApply = (data) => request.put('/hr/apply', data)
export const deleteHrApply = (id) => request.delete(`/hr/apply/${id}`)
export const submitHrApply = (id) => request.post(`/hr/apply/${id}/submit`)
export const withdrawHrApply = (id) => request.post(`/hr/apply/${id}/withdraw`)
export const approveHrApply = (id, userId, opinion, approverSignature) => {
  const data = { userId, opinion: opinion || '' }
  if (approverSignature) {
    data.approverSignature = approverSignature
  }
  return request.post(`/hr/apply/${id}/approve`, data)
}
export const rejectHrApply = (id, userId, opinion) => request.post(`/hr/apply/${id}/reject?userId=${userId}&opinion=${opinion || ''}`)
export const returnHrApply = (id, returnType, opinion) => request.post(`/hr/apply/${id}/return`, { returnType, opinion })
export const generateHrApplyNo = (hrApplyType) => request.get('/hr/apply/generate-apply-no', { params: { hrApplyType } })

// ==================== 考勤记录 ====================
export const getHrAttRecordPage = (params) => request.get('/hr/attendance/record/page', { params })
export const getHrAttRecordById = (id) => request.get(`/hr/attendance/record/${id}`)
export const saveHrAttRecord = (data) => request.post('/hr/attendance/record', data)
export const updateHrAttRecord = (data) => request.put('/hr/attendance/record', data)
export const deleteHrAttRecord = (id) => request.delete(`/hr/attendance/record/${id}`)
export const getTodayAttRecord = (empId) => request.get('/hr/attendance/record/today', { params: { empId } })
export const clockIn = (empId) => request.post('/hr/attendance/record/clock-in', null, { params: { empId } })
export const clockOut = (empId) => request.post('/hr/attendance/record/clock-out', null, { params: { empId } })

// ==================== 考勤台账 ====================
export const getHrAttLedgerPage = (params) => request.get('/hr/attendance/ledger/page', { params })
export const getHrAttLedgerById = (id) => request.get(`/hr/attendance/ledger/${id}`)
export const getHrAttLedgerByEmpIdAndMonth = (empId, attMonth) => request.get(`/hr/attendance/ledger/emp-month`, { params: { empId, attMonth } })
export const calculateHrAttLedger = (attMonth, startDate, endDate, monthWorkDays) =>
  request.post('/hr/attendance/ledger/calculate', { attMonth, startDate, endDate, monthWorkDays })
export const downloadHrAttLedgerTemplate = () => window.open('/api/hr/attendance/ledger/template', '_blank')
export const importHrAttLedger = (file, createUser) => {
  const formData = new FormData()
  formData.append('file', file)
  if (createUser) {
    formData.append('createUser', createUser)
  }
  return request.post('/hr/attendance/ledger/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ==================== 薪酬规则 ====================
export const getHrAttRuleList = () => request.get('/hr/attendance/rule/list')
export const getHrAttRuleByType = (ruleType) => request.get(`/hr/attendance/rule/type/${ruleType}`)
export const getHrAttRuleById = (id) => request.get(`/hr/attendance/rule/${id}`)
export const saveHrAttRule = (data) => request.post('/hr/attendance/rule', data)
export const updateHrAttRule = (data) => request.put('/hr/attendance/rule', data)
export const deleteHrAttRule = (id) => request.delete(`/hr/attendance/rule/${id}`)

// ==================== 薪酬配置 ====================
export const getHrSalConfigPage = (params) => request.get('/hr/salary/config/page', { params })
export const getHrSalConfigById = (id) => request.get(`/hr/salary/config/${id}`)
export const getHrSalConfigByEmpId = (empId) => request.get(`/hr/salary/config/emp/${empId}`)
export const saveHrSalConfig = (data) => request.post('/hr/salary/config', data)
export const updateHrSalConfig = (data) => request.put('/hr/salary/config', data)
export const deleteHrSalConfig = (id) => request.delete(`/hr/salary/config/${id}`)
export const getHrSalConfigEmpIds = () => request.get('/hr/salary/config/emp-ids')
export const batchCreateHrSalConfig = (empIds, createUser) => request.post('/hr/salary/config/batch-create', { empIds, createUser })

// ==================== 薪酬计算 ====================
export const getHrSalCalculatePage = (params) => request.get('/hr/salary/calculate/page', { params })
export const getHrSalCalculateById = (id) => request.get(`/hr/salary/calculate/${id}`)
export const getHrSalCalculateByEmpIdAndMonth = (empId, calcMonth) => request.get(`/hr/salary/calculate/emp-month`, { params: { empId, calcMonth } })
export const calculateSalary = (empId, calcMonth) => request.post('/hr/salary/calculate', null, { params: { empId, calcMonth } })
export const batchCalculateSalary = (calcMonth, empIds) => request.post('/hr/salary/calculate/batch', { calcMonth, empIds })
export const paySalary = (calcId) => request.post(`/hr/salary/calculate/pay/${calcId}`)
export const batchPaySalary = (calcIds) => request.post('/hr/salary/calculate/batch-pay', { calcIds })
