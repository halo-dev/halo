import Vue from 'vue'
import {
  OPTIONS
} from '@/store/mutation-types'
import optionApi from '@/api/option'
const keys = ['blog_url']
const option = {
  state: {
    options: []
  },
  mutations: {
    SET_OPTIONS: (state, options) => {
      Vue.ls.set(OPTIONS, options)
      state.options = options
    }
  },
  actions: {
    loadOptions({
      commit
    }) {
      return new Promise((resolve, reject) => {
        optionApi
          .listAll(keys)
          .then(response => {
            commit('SET_OPTIONS', response.data.data)
            resolve(response)
          })
          .catch(error => {
            reject(error)
          })
      })
    }
  }
}

export default option
