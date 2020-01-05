import service from '@/utils/service'

const baseUrl = '/api/admin'

const commentApi = {}

commentApi.latestComment = (target, top, status) => {
  return service({
    url: `${baseUrl}/${target}/comments/latest`,
    params: {
      top: top,
      status: status
    },
    method: 'get'
  })
}

commentApi.queryComment = (target, params) => {
  return service({
    url: `${baseUrl}/${target}/comments`,
    params: params,
    method: 'get'
  })
}

commentApi.commentTree = (target, id, params) => {
  return service({
    url: `${baseUrl}/${target}/comments/${id}/tree_view`,
    params: params,
    method: 'get'
  })
}

commentApi.updateStatus = (target, commentId, status) => {
  return service({
    url: `${baseUrl}/${target}/comments/${commentId}/status/${status}`,
    method: 'put'
  })
}

commentApi.updateStatusInBatch = (target, ids, status) => {
  return service({
    url: `${baseUrl}/${target}/comments/status/${status}`,
    data: ids,
    method: 'put'
  })
}

commentApi.delete = (target, commentId) => {
  return service({
    url: `${baseUrl}/${target}/comments/${commentId}`,
    method: 'delete'
  })
}

commentApi.deleteInBatch = (target, ids) => {
  return service({
    url: `${baseUrl}/${target}/comments`,
    data: ids,
    method: 'delete'
  })
}

commentApi.create = (target, comment) => {
  return service({
    url: `${baseUrl}/${target}/comments`,
    data: comment,
    method: 'post'
  })
}

commentApi.update = (target, commentId, comment) => {
  return service({
    url: `${baseUrl}/${target}/comments/${commentId}`,
    data: comment,
    method: 'put'
  })
}

/**
 * Creates a comment.
 * @param {String} target
 * @param {Object} comment
 */
function createComment(target, comment) {
  return service({
    url: `${baseUrl}/${target}/comments`,
    method: 'post',
    data: comment
  })
}

// Creation api

commentApi.createPostComment = comment => {
  return createComment('posts', comment)
}

commentApi.createSheetComment = comment => {
  return createComment('sheets', comment)
}

commentApi.createJournalComment = comment => {
  return createComment('journals', comment)
}

commentApi.createComment = (comment, type) => {
  if (type === 'sheet') {
    return commentApi.createSheetComment(comment)
  }

  if (type === 'journal') {
    return commentApi.createJournalComment(comment)
  }

  return commentApi.createPostComment(comment)
}

commentApi.commentStatus = {
  PUBLISHED: {
    value: 'PUBLISHED',
    color: 'green',
    status: 'success',
    text: '已发布'
  },
  AUDITING: {
    value: 'AUDITING',
    color: 'yellow',
    status: 'warning',
    text: '待审核'
  },
  RECYCLE: {
    value: 'RECYCLE',
    color: 'red',
    status: 'error',
    text: '回收站'
  }
}

export default commentApi
