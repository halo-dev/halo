import service from '@/utils/service'

const baseUrl = '/api/admin'

const adminApi = {}

adminApi.counts = () => {
  return service({
    url: `${baseUrl}/counts`,
    method: 'get'
  })
}

adminApi.isInstalled = () => {
  return service({
    url: `${baseUrl}/is_installed`,
    method: 'get'
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

adminApi.loginPreCheck = (username, password) => {
  return service({
    url: `${baseUrl}/login/precheck`,
    data: {
      username: username,
      password: password
    },
    method: 'post'
  })
}

adminApi.login = (username, password, authcode) => {
  return service({
    url: `${baseUrl}/login`,
    data: {
      username: username,
      password: password,
      authcode: authcode
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

adminApi.sendResetCode = param => {
  return service({
    url: `${baseUrl}/password/code`,
    data: param,
    method: 'post'
  })
}

adminApi.resetPassword = param => {
  return service({
    url: `${baseUrl}/password/reset`,
    data: param,
    method: 'put'
  })
}

adminApi.updateAdminAssets = () => {
  return service({
    url: `${baseUrl}/halo-admin`,
    method: 'put',
    timeout: 600 * 1000
  })
}

adminApi.getApplicationConfig = () => {
  return service({
    url: `${baseUrl}/spring/application.yaml`,
    method: 'get'
  })
}

adminApi.updateApplicationConfig = content => {
  return service({
    url: `${baseUrl}/spring/application.yaml`,
    params: {
      content: content
    },
    method: 'put'
  })
}

adminApi.restartApplication = () => {
  return service({
    url: `${baseUrl}/spring/restart`,
    method: 'post'
  })
}

adminApi.getLogFiles = lines => {
  return service({
    url: `${baseUrl}/halo/logfile`,
    params: {
      lines: lines
    },
    method: 'get'
  })
}

adminApi.downloadLogFiles = lines => {
  return service({
    url: `${baseUrl}/halo/logfile/download`,
    params: {
      lines: lines
    },
    method: 'get'
  })
}

export default adminApi
