import service from '@/utils/service'

const baseUrl = '/api/admin/options'

const optionApi = {}

optionApi.listAll = () => {
  return service({
    url: `${baseUrl}/map_view`,
    method: 'get'
  })
}

optionApi.listAllByKeys = keys => {
  return service({
    url: `${baseUrl}/map_view/keys`,
    data: keys,
    method: 'post'
  })
}

optionApi.query = params => {
  return service({
    url: `${baseUrl}/list_view`,
    params: params,
    method: 'get'
  })
}

optionApi.save = options => {
  return service({
    url: `${baseUrl}/map_view/saving`,
    method: 'post',
    data: options
  })
}

optionApi.create = option => {
  return service({
    url: baseUrl,
    data: option,
    method: 'post'
  })
}

optionApi.delete = optionId => {
  return service({
    url: `${baseUrl}/${optionId}`,
    method: 'delete'
  })
}

optionApi.get = optionId => {
  return service({
    url: `${baseUrl}/${optionId}`,
    method: 'get'
  })
}

optionApi.update = (optionId, option) => {
  return service({
    url: `${baseUrl}/${optionId}`,
    data: option,
    method: 'put'
  })
}

optionApi.type = {
  INTERNAL: {
    value: 'INTERNAL',
    text: '系统'
  },
  CUSTOM: {
    value: 'CUSTOM',
    text: '自定义'
  }
}

export default optionApi
