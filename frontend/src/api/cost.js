import request from './request'

// ==================== 成本周期管理 ====================
export function getCostCycleList(params) {
  return request({
    url: '/cost/cycle/list',
    method: 'get',
    params
  })
}

export function saveCostCycle(data) {
  return request({
    url: '/cost/cycle',
    method: 'post',
    data
  })
}

export function updateCostCycle(data) {
  return request({
    url: '/cost/cycle',
    method: 'put',
    data
  })
}

export function deleteCostCycle(id) {
  return request({
    url: `/cost/cycle/${id}`,
    method: 'delete'
  })
}

export function getCostCycleById(id) {
  return request({
    url: `/cost/cycle/${id}`,
    method: 'get'
  })
}

// ==================== 成本归集管理 ====================
export function getCostMainList(params) {
  return request({
    url: '/cost/main/list',
    method: 'get',
    params
  })
}

export function saveCostMain(data) {
  return request({
    url: '/cost/main',
    method: 'post',
    data
  })
}

export function updateCostMain(data) {
  return request({
    url: '/cost/main',
    method: 'put',
    data
  })
}

export function deleteCostMain(id) {
  return request({
    url: `/cost/main/${id}`,
    method: 'delete'
  })
}

export function getCostMainById(id) {
  return request({
    url: `/cost/main/${id}`,
    method: 'get'
  })
}

// 批量导入成本
export function importCostMain(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/cost/main/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载导入模板
export function downloadCostMainTemplate() {
  return request({
    url: '/cost/main/template',
    method: 'get',
    responseType: 'blob'
  })
}

// ==================== 成本台账 ====================
export function getCostLedgerList(params) {
  return request({
    url: '/cost/main/ledger',
    method: 'get',
    params
  })
}

