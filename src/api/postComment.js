import service from '@/utils/service'

const baseUrl = '/api/admin/posts/comments'

const commentApi = {}

commentApi.listLatest = (top, status) => {
  return service({
    url: `${baseUrl}/latest`,
    params: {
      top: top,
      status: status
    },
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

commentApi.updateStatus = (commentId, status) => {
  return service({
    url: `${baseUrl}/${commentId}/status/${status}`,
    method: 'put'
  })
}

commentApi.delete = commentId => {
  return service({
    url: `${baseUrl}/${commentId}`,
    method: 'delete'
  })
}

commentApi.create = comment => {
  return service({
    url: baseUrl,
    data: comment,
    method: 'post'
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
