import service from '@/utils/service'

const baseUrl = '/api/admin/photos'

const photoApi = {}

photoApi.query = params => {
  return service({
    url: baseUrl,
    params: params,
    method: 'get'
  })
}

photoApi.create = (photo) => {
  return service({
    url: baseUrl,
    data: photo,
    method: 'post'
  })
}

photoApi.update = (photoId, photo) => {
  return service({
    url: `${baseUrl}/${photoId}`,
    method: 'put',
    data: photo
  })
}

photoApi.delete = photoId => {
  return service({
    url: `${baseUrl}/${photoId}`,
    method: 'delete'
  })
}

photoApi.listTeams = () => {
  return service({
    url: `${baseUrl}/teams`,
    method: 'get'
  })
}

export default photoApi
