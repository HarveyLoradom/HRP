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
      }).catch(() => {
        commit('REMOVE_TOKEN')
        commit('REMOVE_USER_INFO')
        resolve()
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
