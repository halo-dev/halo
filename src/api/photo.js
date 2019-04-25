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

photoApi.delete = photoId => {
  return service({
    url: `${baseUrl}/${photoId}`,
    method: 'delete'
  })
}

export default photoApi
