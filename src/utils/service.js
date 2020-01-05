import axios from 'axios'
import Vue from 'vue'
import { message, notification } from 'ant-design-vue'
import store from '@/store'
import router from '@/router'
import { isObject } from './util'

const service = axios.create({
  timeout: 10000,
  withCredentials: true
})

function setTokenToHeader(config) {
  // set token
  const token = store.getters.token
  Vue.$log.debug('Got token from store', token)
  if (token && token.access_token) {
    config.headers['Admin-Authorization'] = token.access_token
  }
}

async function reRequest(error) {
  const config = error.response.config
  setTokenToHeader(config)
  const res = await axios.request(config)
  return res
}

let refreshTask = null

async function refreshToken(error) {
  const refreshToken = store.getters.token.refresh_token
  try {
    if (refreshTask === null) {
      refreshTask = store.dispatch('refreshToken', refreshToken)
    }

    await refreshTask
  } catch (err) {
    if (err.response && err.response.data && err.response.data.data === refreshToken) {
      router.push({ name: 'Login' })
    }
    Vue.$log.error('Failed to refresh token', err)
  } finally {
    refreshTask = null
  }
  // Rerequest the request
  return reRequest(error)
}

function getFieldValidationError(data) {
  if (!isObject(data) || !isObject(data.data)) {
    return null
  }

  const errorDetail = data.data

  return Object.keys(errorDetail).map(key => errorDetail[key])
}

service.interceptors.request.use(
  config => {
    config.baseURL = store.getters.apiUrl
    // TODO set token
    setTokenToHeader(config)
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (axios.isCancel(error)) {
      Vue.$log.debug('Cancelled uploading by user.')
      return Promise.reject(error)
    }

    Vue.$log.error('Response failed', error)

    const response = error.response
    const status = response ? response.status : -1
    Vue.$log.error('Server response status', status)

    const data = response ? response.data : null
    if (data) {
      let handled = false
      // Business response
      Vue.$log.error('Business response status', data.status)
      if (data.status === 400) {
        // TODO handle 400 status error
        const errorDetails = getFieldValidationError(data)
        if (errorDetails) {
          handled = true

          notification.error({
            message: data.message,
            description: h => {
              const errorNodes = errorDetails.map(errorDetail => {
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
        }
      } else if (data.status === 401) {
        // TODO handle 401 status error
        if (store.getters.token && store.getters.token.access_token === data.data) {
          const res = refreshToken(error)
          if (res !== error) {
            return res
          }
        } else {
          // Login
          router.push({ name: 'Login' })
        }
      } else if (data.status === 403) {
        // TODO handle 403 status error
      } else if (data.status === 404) {
        // TODO handle 404 status error
      } else if (data.status === 500) {
        // TODO handle 500 status error
      }

      if (!handled) {
        message.error(data.message)
      }
    } else {
      message.error('网络异常')
    }

    return Promise.reject(error)
  }
)

export default service
