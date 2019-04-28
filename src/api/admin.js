import service from '@/utils/service'

const baseUrl = '/api/admin'

const adminApi = {}

adminApi.counts = () => {
  return service({
    url: `${baseUrl}/counts`,
    method: 'get'
  })
}

adminApi.install = data => {
  return service({
    url: `${baseUrl}/installations`,
    data: data,
    method: 'post'
  })
}
export default adminApi
