import service from '@/utils/service'

const baseUrl = '/admin/api/themes'

const themeApi = {}

themeApi.listAll = () => {
  return service({
    url: `${baseUrl}`,
    method: 'get'
  })
}

themeApi.listFiles = () => {
  return service({
    url: `${baseUrl}/files`,
    method: 'get'
  })
}

themeApi.customTpls = () => {
  return service({
    url: `${baseUrl}/files/custom`,
    method: 'get'
  })
}

themeApi.active = theme => {
  return service({
    url: `${baseUrl}/${theme}/activation`,
    method: 'post'
  })
}

themeApi.getActivatedTheme = () => {
  return service({
    url: `${baseUrl}/activation`,
    method: 'get'
  })
}

themeApi.delete = key => {
  return service({
    url: `${baseUrl}/${key}`,
    method: 'delete'
  })
}

themeApi.fetchConfiguration = () => {
  return service({
    url: `${baseUrl}/activation/configurations`,
    method: 'get'
  })
}

themeApi.fetchSettings = () => {
  return service({
    url: `${baseUrl}/activation/settings`,
    method: 'get'
  })
}

themeApi.saveSettings = settings => {
  return service({
    url: `${baseUrl}/activation/settings`,
    data: settings,
    method: 'post'
  })
}

export default themeApi
