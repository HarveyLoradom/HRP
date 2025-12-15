import request from './request'

export function getAllEquipments() {
  return request({
    url: '/efficiency/equipment/list',
    method: 'get'
  })
}

export function saveEquipment(data) {
  return request({
    url: '/efficiency/equipment',
    method: 'post',
    data
  })
}

export function getAllIncomeHis() {
  return request({
    url: '/efficiency/income/his/list',
    method: 'get'
  })
}

export function getIncomeHisByDateRange(data) {
  return request({
    url: '/efficiency/income/his/range',
    method: 'post',
    data
  })
}

export function getAllIncomeEquipment() {
  return request({
    url: '/efficiency/income/equipment/list',
    method: 'get'
  })
}

export function getAllCostData() {
  return request({
    url: '/efficiency/cost/list',
    method: 'get'
  })
}

export function getCostDataByDateRange(data) {
  return request({
    url: '/efficiency/cost/range',
    method: 'post',
    data
  })
}

// 数据采集相关API
export function collectDeptData(data) {
  return request({
    url: '/efficiency/collection/dept',
    method: 'post',
    data
  })
}

export function collectEquipmentData(data) {
  return request({
    url: '/efficiency/collection/equipment',
    method: 'post',
    data
  })
}

export function getCollectionRecords(collectionType) {
  return request({
    url: '/efficiency/collection/records',
    method: 'get',
    params: { collectionType }
  })
}

// 收入数据相关API
export function getIncomeEquipmentByDateRange(data) {
  return request({
    url: '/efficiency/income/equipment/range',
    method: 'post',
    data
  })
}

// 成本数据相关API
export function getCostDataByEquipment(equipmentCode, startDate, endDate) {
  return request({
    url: '/efficiency/cost/equipment',
    method: 'get',
    params: { equipmentCode, startDate, endDate }
  })
}

// 设备分析报告相关API
export function getEquipmentReports() {
  return request({
    url: '/efficiency/equipment/report/list',
    method: 'get'
  })
}

export function generateEquipmentReport(data) {
  return request({
    url: '/efficiency/equipment/report/generate',
    method: 'post',
    data
  })
}

export function getEquipmentReportDetail(reportId) {
  return request({
    url: `/efficiency/equipment/report/${reportId}`,
    method: 'get'
  })
}

// 投资收益分析相关API
export function getInvestmentAnalyses() {
  return request({
    url: '/efficiency/investment/list',
    method: 'get'
  })
}

export function saveInvestmentAnalysis(data) {
  return request({
    url: '/efficiency/investment',
    method: 'post',
    data
  })
}

export function getInvestmentAnalysisDetail(analysisId) {
  return request({
    url: `/efficiency/investment/${analysisId}`,
    method: 'get'
  })
}

export function calculateReturnRate(investmentAmount, returnAmount) {
  return request({
    url: '/efficiency/investment/calculate',
    method: 'post',
    data: { investmentAmount, returnAmount }
  })
}

