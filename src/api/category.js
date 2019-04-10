import service from '@/utils/service'

const baseUrl = '/admin/api/categories'

const categoryApi = {}

categoryApi.listAll = () => {
  return service({
    url: `${baseUrl}`,
    method: 'get'
  })
}

categoryApi.listTree = () => {
  return service({
    url: `${baseUrl}/tree_view`,
    method: 'get'
  })
}

categoryApi.create = (category) => {
  return service({
    url: baseUrl,
    data: category,
    method: 'post'
  })
}

categoryApi.delete = categoryId => {
  return service({
    url: `${baseUrl}/${categoryId}`,
    method: 'delete'
  })
}

export default categoryApi
