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

optionApi.save = options => {
  return service({
    url: `${baseUrl}/map_view/saving`,
    method: 'post',
    data: options
  })
}

export default optionApi
