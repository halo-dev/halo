import service from '@/utils/service'

const baseUrl = '/api/admin/options/map_view'

const optionApi = {}

optionApi.listAll = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

optionApi.listByKeys = keys => {
  return service({
    url: `/api/admin/options/map_keys`,
    method: 'get',
    params: {
      keys: keys
    }
  })
}

optionApi.save = options => {
  return service({
    url: `${baseUrl}/saving`,
    method: 'post',
    data: options
  })
}

export default optionApi
