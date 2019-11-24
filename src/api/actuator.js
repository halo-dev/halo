import service from '@/utils/service'

const baseUrl = '/api/admin/actuator'

const actuatorApi = {}

actuatorApi.logfile = () => {
  return service({
    url: `${baseUrl}/logfile`,
    method: 'get'
  })
}

actuatorApi.env = () => {
  return service({
    url: `${baseUrl}/env`,
    method: 'get'
  })
}

export default actuatorApi
