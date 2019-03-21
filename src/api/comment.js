import service from '@/utils/service'

const baseUrl = '/admin/api/comments'

const commentApi = {}

commentApi.listLatest = () => {
  return service({
    url: `${baseUrl}/latest`,
    method: 'get'
  })
}

export default commentApi
