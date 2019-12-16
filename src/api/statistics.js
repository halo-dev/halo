import service from '@/utils/service'

const baseUrl = '/api/admin/statistics'

const statisticsApi = {}

statisticsApi.statistics = () => {
  return service({
    url: `${baseUrl}`,
    method: 'get'
  })
}

statisticsApi.statisticsWithUser = () => {
  return service({
    url: `${baseUrl}/user`,
    method: 'get'
  })
}

export default statisticsApi
