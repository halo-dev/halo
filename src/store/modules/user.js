import Vue from 'vue'
import { ACCESS_TOKEN } from '@/store/mutation-types'
import adminApi from '@/api/admin'

const user = {
  state: {
    token: '',
    name: '',
    avatar: '',
    roles: [],
    info: {}
  },
  mutations: {
    SET_TOKEN: (state, token) => {
      Vue.ls.set(ACCESS_TOKEN, token)
      state.token = token
    },
    SET_NAME: (state, { name }) => {
      state.name = name
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_INFO: (state, info) => {
      state.info = info
    }
  },
  actions: {
    login({ commit }, { username, password }) {
      return new Promise((resolve, reject) => {
        adminApi
          .login(username, password)
          .then(response => {
            const token = response.data.data
            Vue.$log.debug('Got token', token)
            commit('SET_TOKEN', token)

            resolve(response)
          })
          .catch(error => {
            reject(error)
          })
      })
    }
  }
}

export default user
