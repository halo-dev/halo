import service from '@/utils/service'

const baseUrl = '/admin/api/posts'

const postApi = {}

postApi.listLatest = () => {
  return service({
    url: `${baseUrl}/latest`,
    method: 'get'
  })
}

export default postApi
