import service from '@/utils/service'

const baseUrl = '/api/admin/logs'

const logApi = {}

logApi.listLatest = (top) => {
  return service({
    url: `${baseUrl}/latest`,
    params: {
      top: top
    },
    method: 'get'
  })
}

export default logApi
