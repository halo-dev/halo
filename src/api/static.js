import service from '@/utils/service'

const baseUrl = '/api/admin/statics'

const staticApi = {}

staticApi.list = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

export default staticApi
