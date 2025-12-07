import request from './request'

export function getAllContracts() {
  return request({
    url: '/contract/list',
    method: 'get'
  })
}

export function getContractsByStatus(status) {
  return request({
    url: `/contract/status/${status}`,
    method: 'get'
  })
}

export function getContractById(id) {
  return request({
    url: `/contract/${id}`,
    method: 'get'
  })
}

export function saveContract(data) {
  return request({
    url: '/contract',
    method: 'post',
    data
  })
}

export function updateContract(data) {
  return request({
    url: '/contract',
    method: 'put',
    data
  })
}

export function deleteContract(id) {
  return request({
    url: `/contract/${id}`,
    method: 'delete'
  })
}


