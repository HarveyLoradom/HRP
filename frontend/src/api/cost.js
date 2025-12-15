import request from './request'

export function getAllCostReports() {
  return request({
    url: '/cost/report/list',
    method: 'get'
  })
}

export function getCostReportsByPeriod(period) {
  return request({
    url: `/cost/report/period/${period}`,
    method: 'get'
  })
}

export function saveCostReport(data) {
  return request({
    url: '/cost/report',
    method: 'post',
    data
  })
}

export function getAllCostAnalyses() {
  return request({
    url: '/cost/analysis/list',
    method: 'get'
  })
}

export function saveCostAnalysis(data) {
  return request({
    url: '/cost/analysis',
    method: 'post',
    data
  })
}

export function getAllCostAccountings() {
  return request({
    url: '/cost/accounting/list',
    method: 'get'
  })
}

export function getCostAccountingsByPeriod(period) {
  return request({
    url: `/cost/accounting/period/${period}`,
    method: 'get'
  })
}

export function saveCostAccounting(data) {
  return request({
    url: '/cost/accounting',
    method: 'post',
    data
  })
}

export function deleteCostReport(reportId) {
  return request({
    url: `/cost/report/${reportId}`,
    method: 'delete'
  })
}

export function getCostReportDetail(reportId) {
  return request({
    url: `/cost/report/${reportId}`,
    method: 'get'
  })
}

export function generateCostReport(data) {
  return request({
    url: '/cost/report/generate',
    method: 'post',
    data
  })
}

export function getCostReportChart(reportId) {
  return request({
    url: `/cost/report/${reportId}/chart`,
    method: 'get'
  })
}

export function getCostAnalysisDetail(analysisId) {
  return request({
    url: `/cost/analysis/detail/${analysisId}`,
    method: 'get'
  })
}

export function getCostAnalysisCompare(data) {
  return request({
    url: '/cost/analysis/compare',
    method: 'post',
    data
  })
}

export function getCostAccountingDetail(accountingId) {
  return request({
    url: `/cost/accounting/detail/${accountingId}`,
    method: 'get'
  })
}

export function calculateCost(data) {
  return request({
    url: '/cost/accounting/calculate',
    method: 'post',
    data
  })
}

