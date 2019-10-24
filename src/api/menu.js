import service from '@/utils/service'

const baseUrl = '/api/admin/menus'

const menuApi = {}

menuApi.listAll = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

menuApi.listTree = () => {
  return service({
    url: `${baseUrl}/tree_view`,
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

menuApi.get = menuId => {
  return service({
    url: `${baseUrl}/${menuId}`,
    method: 'get'
  })
}

menuApi.update = (menuId, menu) => {
  return service({
    url: `${baseUrl}/${menuId}`,
    data: menu,
    method: 'put'
  })
}

menuApi.listTeams = () => {
  return service({
    url: `${baseUrl}/teams`,
    method: 'get'
  })
}

export default menuApi
