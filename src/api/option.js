import service from '@/utils/service'

const baseUrl = '/api/admin/options'

const optionApi = {}

optionApi.listAll = keys => {
  return service({
    url: `${baseUrl}/map_view`,
    params: {
      key: keys
    },
    method: 'get'
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

optionApi.type = {
  INTERNAL: {
    type: 'internal',
    text: '系统'
  },
  CUSTOM: {
    type: 'custom',
    text: '自定义'
  }
}

export default optionApi
