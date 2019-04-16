import service from '@/utils/service'

const baseUrl = '/api/admin/comments'

const commentApi = {}

commentApi.listLatest = () => {
  return service({
    url: `${baseUrl}/latest`,
    method: 'get'
  })
}

export default commentApi
