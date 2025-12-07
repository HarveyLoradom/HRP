import request from './request'

export function getAllMenus() {
  return request({
    url: '/menu/list',
    method: 'get'
  })
}

export function getMenusByUserId(userId) {
  return request({
    url: `/menu/user/${userId}`,
    method: 'get'
  })
}
