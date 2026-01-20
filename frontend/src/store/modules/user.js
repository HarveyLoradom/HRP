import Cookies from 'js-cookie'
import { login, logout } from '@/api/auth'

const state = {
  token: Cookies.get('token') || '',
  userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
    Cookies.set('token', token)
  },
  SET_USER_INFO: (state, userInfo) => {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  REMOVE_TOKEN: (state) => {
    state.token = ''
    Cookies.remove('token')
  },
  REMOVE_USER_INFO: (state) => {
    state.userInfo = {}
    localStorage.removeItem('userInfo')
  }
}

const actions = {
  login({ commit }, loginForm) {
    return new Promise((resolve, reject) => {
      login(loginForm).then(response => {
        if (response.code === 200) {
          const { token, ...userInfo } = response.data
          commit('SET_TOKEN', token)
          commit('SET_USER_INFO', userInfo)
          resolve(response)
        } else {
          reject(new Error(response.message || '登录失败'))
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  logout({ commit }) {
    return new Promise((resolve) => {
      logout().then(() => {
        commit('REMOVE_TOKEN')
        commit('REMOVE_USER_INFO')
        resolve()
      }).catch((error) => {
        // 即使后端服务不可用，也要清除本地状态
        commit('REMOVE_TOKEN')
        commit('REMOVE_USER_INFO')
        // 如果是连接错误，静默处理（不显示错误）
        if (error.message && error.message.includes('无法连接到服务器')) {
          resolve()
        } else {
          // 其他错误也静默处理，确保能退出登录
          resolve()
        }
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
