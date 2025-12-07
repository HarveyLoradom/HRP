import request from './request'

export function getMyPayouts(empId) {
  return request({
    url: `/reimb/payout/my/${empId}`,
    method: 'get'
  })
}

export function getPayoutsByStatus(status) {
  return request({
    url: `/reimb/payout/status/${status}`,
    method: 'get'
  })
}

export function getPayoutById(id) {
  return request({
    url: `/reimb/payout/${id}`,
    method: 'get'
  })
}

export function savePayout(data) {
  return request({
    url: '/reimb/payout',
    method: 'post',
    data
  })
}

export function updatePayout(data) {
  return request({
    url: '/reimb/payout',
    method: 'put',
    data
  })
}

export function deletePayout(id) {
  return request({
    url: `/reimb/payout/${id}`,
    method: 'delete'
  })
}

export function submitPayout(id) {
  return request({
    url: `/reimb/payout/${id}/submit`,
    method: 'post'
  })
}

