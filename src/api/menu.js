import service from '@/utils/service'

const baseUrl = '/admin/api/menus'

const menuApi = {}

menuApi.listAll = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

menuApi.create = menu => {
  return service({
    url: baseUrl,
    data: menu,
    method: 'post'
  })
}

menuApi.delete = menuId => {
  return service({
    url: `${baseUrl}/${menuId}`,
    method: 'delete'
  })
}

export default menuApi
