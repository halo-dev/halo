import Vue from 'vue'
import {
  OPTIONS
} from '@/store/mutation-types'
import optionApi from '@/api/option'
const keys = [
  'blog_url',
  'developer_mode',
  'attachment_upload_image_preview_enable',
  'attachment_upload_max_parallel_uploads',
  'attachment_upload_max_files',
  'sheet_prefix',
  'post_permalink_type',
  'archives_prefix',
  'path_suffix',
  'default_editor'
]
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
          .listAllByKeys(keys)
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
