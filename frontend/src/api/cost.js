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

