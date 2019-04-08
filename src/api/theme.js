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
    url: `${baseUrl}/active?theme=${theme}`,
    method: 'get'
  })
}

themeApi.delete = key => {
  return service({
    url: `${baseUrl}/${key}`,
    method: 'delete'
  })
}

themeApi.listOptions = theme => {
  return service({
    url: `${baseUrl}/configurations?name=${theme}`
  })
}

export default themeApi
