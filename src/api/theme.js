import service from '@/utils/service'

const baseUrl = '/admin/api/themes'

const themeApi = {}

themeApi.listFiles = () => {
  return service({
    url: `${baseUrl}/files`,
    method: 'get'
  })
}

export default themeApi
