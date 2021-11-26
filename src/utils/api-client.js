import { AdminApiClient, Axios, HaloRestAPIClient } from '@halo-dev/admin-api'
import store from '@/store'
import { message, notification } from 'ant-design-vue'
import { isObject } from './util'

const apiUrl = process.env.VUE_APP_API_URL ? process.env.VUE_APP_API_URL : 'http://localhost:8080'

const haloRestApiClient = new HaloRestAPIClient({
  baseUrl: apiUrl
})

const apiClient = new AdminApiClient(haloRestApiClient)

haloRestApiClient.interceptors.request.use(
  config => {
    const token = store.getters.token
    if (token && token.access_token) {
      config.headers['Admin-Authorization'] = token.access_token
    }
    return config
  },
  error => {
    console.log('request error', error)
    return Promise.reject(error)
  }
)

let isRefreshingToken = false
let pendingRequests = []

haloRestApiClient.interceptors.response.use(
  response => {
    return response
  },
  async error => {
    if (Axios.isCancel(error)) {
      return Promise.reject(error)
    }

    if (/Network Error/.test(error.message)) {
      message.error('网络错误，请检查网络连接')
      return Promise.reject(error)
    }

    const token = store.getters.token
    const originalRequest = error.config

    const response = error.response

    const data = response ? response.data : null

    if (data) {
      if (data.status === 400) {
        const params = data.data

        if (isObject(params)) {
          const paramMessages = Object.keys(params || {}).map(key => params[key])
          notification.error({
            message: data.message,
            description: h => {
              const errorNodes = paramMessages.map(errorDetail => {
                return h('a-alert', {
                  props: {
                    message: errorDetail,
                    banner: true,
                    showIcon: false,
                    type: 'error'
                  }
                })
              })
              return h('div', errorNodes)
            },
            duration: 10
          })
        } else {
          message.error(data.message)
        }

        return Promise.reject(error)
      }
      if (data.status === 401) {
        if (!isRefreshingToken) {
          isRefreshingToken = true
          try {
            await store.dispatch('refreshToken', token.refresh_token)

            pendingRequests.forEach(callback => callback())
            pendingRequests = []

            return Axios(originalRequest)
          } catch (e) {
            message.warning('当前登录状态已失效，请重新登录')
            await store.dispatch('ToggleLoginModal', true)
            return Promise.reject(e)
          } finally {
            isRefreshingToken = false
          }
        } else {
          return new Promise(resolve => {
            pendingRequests.push(() => {
              resolve(Axios(originalRequest))
            })
          })
        }
      }
      message.error(data.message || '服务器错误')
      return Promise.reject(error)
    }

    message.error('网络异常')
    return Promise.reject(error)
  }
)

export default apiClient

export { haloRestApiClient }
