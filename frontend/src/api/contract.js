import request from './request'

// 合同管理
export const getAllContracts = () => request.get('/contract/list')
export const getAllContractsPage = (page, size) => request.get(`/contract/page?page=${page}&size=${size}`)
export const getContractsByStatus = (status) => request.get(`/contract/status/${status}`)
export const getContractsByStatusPage = (status, page, size) => request.get(`/contract/status/${status}/page?page=${page}&size=${size}`)
export const getContractById = (id) => request.get(`/contract/${id}`)
export const getContractByNo = (contractNo) => request.get(`/contract/no/${contractNo}`)
export const saveContract = (data) => request.post('/contract', data)
export const updateContract = (data) => request.put('/contract', data)
export const deleteContract = (id) => request.delete(`/contract/${id}`)
export const submitContract = (id) => request.post(`/contract/${id}/submit`)
export const withdrawContract = (id) => request.post(`/contract/${id}/withdraw`)
export const getMyApprovalContracts = (userId) => request.get(`/contract/my-approval/${userId}`)
export const approveContract = (id, userId, opinion) => request.post(`/contract/${id}/approve?userId=${userId}&opinion=${opinion || ''}`)
export const rejectContract = (id, userId, opinion) => request.post(`/contract/${id}/reject?userId=${userId}&opinion=${opinion || ''}`)
export const archiveContract = (id) => request.post(`/contract/${id}/archive`)
export const getNextApprover = (id) => request.get(`/contract/${id}/next-approver`)
