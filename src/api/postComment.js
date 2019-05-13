import service from '@/utils/service'

const baseUrl = '/api/admin/posts/comments'

const postCommentApi = {}

postCommentApi.listLatest = (top, status) => {
  return service({
    url: `${baseUrl}/latest`,
    params: {
      top: top,
      status: status
    },
    method: 'get'
  })
}

postCommentApi.query = params => {
  return service({
    url: baseUrl,
    params: params,
    method: 'get'
  })
}

postCommentApi.updateStatus = (commentId, status) => {
  return service({
    url: `${baseUrl}/${commentId}/status/${status}`,
    method: 'put'
  })
}

postCommentApi.delete = commentId => {
  return service({
    url: `${baseUrl}/${commentId}`,
    method: 'delete'
  })
}

postCommentApi.create = comment => {
  return service({
    url: baseUrl,
    data: comment,
    method: 'post'
  })
}

postCommentApi.commentStatus = {
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

export default postCommentApi
