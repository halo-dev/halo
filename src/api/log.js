import service from '@/utils/service'

const baseUrl = '/api/admin/logs'

const logApi = {}

logApi.listLatest = () => {
  return service({
    url: `${baseUrl}/latest`,
    method: 'get'
  })
}

export default logApi
