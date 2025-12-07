import request from './request'

export const getAllMenus = () => request.get('/menu/list')
export const getMenusByUserId = (userId) => request.get(`/menu/user/${userId}`)
export const getMenuTree = () => request.get('/menu/tree')
export const saveMenu = (data) => request.post('/menu', data)
export const updateMenu = (data) => request.put('/menu', data)
export const deleteMenu = (id) => request.delete(`/menu/${id}`)
