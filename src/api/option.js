import service from '@/utils/service'

const baseUrl = '/admin/api/options'

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
