import request from './request'

export function getAllAssets() {
  return request({
    url: '/asset/list',
    method: 'get'
  })
}

export function getAssetsByStatus(status) {
  return request({
    url: `/asset/status/${status}`,
    method: 'get'
  })
}

export function getAssetById(id) {
  return request({
    url: `/asset/${id}`,
    method: 'get'
  })
}

export function saveAsset(data) {
  return request({
    url: '/asset',
    method: 'post',
    data
  })
}

export function updateAsset(data) {
  return request({
    url: '/asset',
    method: 'put',
    data
  })
}

export function deleteAsset(id) {
  return request({
    url: `/asset/${id}`,
    method: 'delete'
  })
}

export function getAllProcurements() {
  return request({
    url: '/asset/procurement/list',
    method: 'get'
  })
}

export function saveProcurement(data) {
  return request({
    url: '/asset/procurement',
    method: 'post',
    data
  })
}

