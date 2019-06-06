import service from '@/utils/service'

const baseUrl = '/api/admin'

const adminApi = {}

adminApi.counts = () => {
  return service({
    url: `${baseUrl}/counts`,
    method: 'get',
    mute: true
  })
}

adminApi.environments = () => {
  return service({
    url: `${baseUrl}/environments`,
    method: 'get'
  })
}

adminApi.install = data => {
  return service({
    url: `${baseUrl}/installations`,
    data: data,
    method: 'post'
  })
}

adminApi.login = (username, password) => {
  return service({
    url: `${baseUrl}/login`,
    data: {
      username: username,
      password: password
    },
    method: 'post'
  })
}

adminApi.logout = () => {
  return service({
    url: `${baseUrl}/logout`,
    method: 'post'
  })
}

adminApi.refreshToken = refreshToken => {
  return service({
    url: `${baseUrl}/refresh/${refreshToken}`,
    method: 'post'
  })
}
export default adminApi
