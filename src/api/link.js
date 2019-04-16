import service from '@/utils/service'

const baseUrl = '/admin/api/links'

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

export default linkApi
