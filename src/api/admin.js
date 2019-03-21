import service from '@/utils/service'

const baseUrl = '/admin/api'

const adminApi = {}

adminApi.counts = () => {
  return service({
    url: `${baseUrl}/counts`,
    method: 'get'
  })
}

export default adminApi
