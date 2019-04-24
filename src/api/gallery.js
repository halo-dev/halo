import service from '@/utils/service'

const baseUrl = '/api/admin/galleries'

const galleryApi = {}

galleryApi.query = params => {
  return service({
    url: baseUrl,
    params: params,
    method: 'get'
  })
}

galleryApi.create = (gallery) => {
  return service({
    url: baseUrl,
    data: gallery,
    method: 'post'
  })
}

export default galleryApi
