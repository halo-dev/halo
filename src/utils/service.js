import axios from 'axios'
import NProgress from 'nprogress'
import Vue from 'vue'

const service = axios.create({
  baseURL:
    process.env.NODE_ENV === 'production'
      ? 'https://ryanc.cc/'
      : 'http://localhost:8090'
})

service.interceptors.request.use(
  config => {
    NProgress.start()
    // TODO set token
    return config
  },
  error => {
    NProgress.remove()
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    NProgress.done()
    return response
  },
  error => {
    Vue.$log.error('Response failed', error)
    NProgress.done()

    const response = error.response
    const status = response ? response.status : -1
    Vue.$log.error('Server response status', status)

    const data = response.data
    if (data) {
      // Business response
      Vue.$log.error('Business response status', data.status)
      if (data.status === 400) {
        // TODO handle 400 status error
      } else if (data.status === 401) {
        // TODO handle 401 status error
      } else if (data.status === 403) {
        // TODO handle 403 status error
      } else if (data.status === 404) {
        // TODO handle 404 status error
      } else if (data.status === 500) {
        // TODO handle 500 status error
      }

      Vue.$message.error(data.message)
    } else {
      Vue.$message.error('Unkown reason')
    }

    return Promise.reject(error)
  }
)

export default service
