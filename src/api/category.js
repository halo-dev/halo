import service from '@/utils/service'

const baseUrl = '/admin/api/categories'

const categoryApi = {}

categoryApi.listAll = () => {
  return service({
    url: `${baseUrl}`,
    method: 'get'
  })
}

export default categoryApi
