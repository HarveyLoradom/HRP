import request from './request'

// 合同管理
export const getAllContracts = () => request.get('/contract/list')
export const getAllContractsPage = (page, size) => request.get(`/contract/page?page=${page}&size=${size}&skipEmpIdFilter=true`)
export const getContractsByStatus = (status) => request.get(`/contract/status/${status}`)
export const getContractsByStatusPage = (status, page, size) => request.get(`/contract/status/${status}/page?page=${page}&size=${size}`)
export const getContractById = (id) => request.get(`/contract/${id}`)
export const getContractByNo = (contractNo) => request.get(`/contract/contract-no/${contractNo}`)
export const saveContract = (data) => request.post('/contract', data)
export const updateContract = (data) => request.put('/contract', data)
export const deleteContract = (id) => request.delete(`/contract/${id}`)
export const submitContract = (id) => request.post(`/contract/${id}/submit`)
export const submitContractByNo = (contractNo) => request.post(`/contract/submit-by-no/${contractNo}`)
export const withdrawContract = (id) => request.post(`/contract/${id}/withdraw`)
export const getMyApprovalContracts = (userId) => request.get(`/contract/my-approval/${userId}`)
export const getContractsPage = (params) => request.get('/contract/page', { params })
export const approveContract = (id, userId, opinion, signature) => {
  const data = {
    userId: userId,
    opinion: opinion || ''
  }
  if (signature) {
    data.signature = signature
  }
  return request.post(`/contract/${id}/approve`, data)
}
export const rejectContract = (id, userId, opinion) => request.post(`/contract/${id}/reject?userId=${userId}&opinion=${opinion || ''}`)
export const returnContract = (id, returnType, opinion) => {
  const data = {
    returnType: returnType || 'RETURN_TO_CURRENT',
    opinion: opinion || ''
  }
  return request.post(`/contract/${id}/return`, data)
}
export const archiveContract = (id) => request.post(`/contract/${id}/archive`)
export const getNextApprover = (id) => request.get(`/contract/${id}/next-approver`)

// 合同执行相关
export const getApprovedContractsPage = (params) => request.get('/contract/approved/page', { params })
export const invalidateContract = (id) => request.post(`/contract/${id}/invalidate`)
export const archiveContractManual = (id) => request.post(`/contract/${id}/archive-manual`)
export const updateContractExecutionStatus = () => request.post('/contract/update-execution-status')

// 查询采购合同列表（只查询合同类型为PURCHASE的合同）
export const getPurchaseContracts = (contractNo) => {
  const params = {}
  if (contractNo) {
    params.contractNo = contractNo
  }
  return request.get('/contract/purchase/list', { params })
}
