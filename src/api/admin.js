import service from '@/utils/service'

const baseUrl = '/api/admin'

const adminApi = {}

adminApi.counts = () => {
  return service({
    url: `${baseUrl}/counts`,
    method: 'get'
  })
}

export default adminApi
