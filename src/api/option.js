import service from '@/utils/service'

const baseUrl = '/api/admin/options/map_view'

const optionApi = {}

optionApi.listAll = () => {
  return service({
    url: baseUrl,
    method: 'get'
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
