import service from '@/utils/service'

const baseUrl = '/api/admin/comments'

const commentApi = {}

commentApi.listLatest = () => {
  return service({
    url: `${baseUrl}/latest`,
    method: 'get'
  })
}

commentApi.query = params => {
  return service({
    url: baseUrl,
    params: params,
    method: 'get'
  })
}

commentApi.commentStatus = {
  PUBLISHED: {
    color: 'green',
    status: 'success',
    text: '已发布'
  },
  AUDITING: {
    color: 'yellow',
    status: 'warning',
    text: '待审核'
  },
  RECYCLE: {
    color: 'red',
    status: 'error',
    text: '回收站'
  }
}

export default commentApi
