import { getMenusByUserId } from '@/api/menu'

const state = {
  menus: []
}

const mutations = {
  SET_MENUS: (state, menus) => {
    state.menus = menus
  }
}

const actions = {
  getMenus({ commit }, userId) {
    return new Promise((resolve, reject) => {
      getMenusByUserId(userId).then(response => {
        if (response.code === 200) {
          commit('SET_MENUS', response.data)
          resolve(response.data)
        } else {
          reject(new Error(response.message || '获取菜单失败'))
        }
      }).catch(error => {
        reject(error)
      })
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
