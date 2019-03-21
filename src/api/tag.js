import service from '@/utils/service'

const baseUrl = '/admin/api/tags'

const tagApi = {}

tagApi.listAll = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

tagApi.listAllAddition = () => {
  return service({
    url: `${baseUrl}/addition`,
    method: 'get'
  })
}

tagApi.create = tag => {
  return service({
    url: `${baseUrl}`,
    method: 'post'
  })
}

export default tagApi
