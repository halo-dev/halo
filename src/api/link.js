import service from '@/utils/service'

const baseUrl = '/api/admin/links'

const linkApi = {}

linkApi.listAll = () => {
  return service({
    url: `${baseUrl}`,
    method: 'get'
  })
}

linkApi.create = (link) => {
  return service({
    url: baseUrl,
    data: link,
    method: 'post'
  })
}

linkApi.get = linkId => {
  return service({
    url: `${baseUrl}/${linkId}`,
    method: 'get'
  })
}

linkApi.getByParse = url => {
  return service({
    url: `${baseUrl}/parse`,
    params: {
      url: url
    },
    method: 'get'
  })
}

linkApi.update = (linkId, link) => {
  return service({
    url: `${baseUrl}/${linkId}`,
    data: link,
    method: 'put'
  })
}

linkApi.delete = linkId => {
  return service({
    url: `${baseUrl}/${linkId}`,
    method: 'delete'
  })
}

linkApi.listTeams = () => {
  return service({
    url: `${baseUrl}/teams`,
    method: 'get'
  })
}

export default linkApi
