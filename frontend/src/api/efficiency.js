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

