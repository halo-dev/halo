import service from '@/utils/service'

const baseUrl = '/api/admin'

const commentApi = {}

/**
 * Lists comment.
 * @param {String} target
 * @param {Object} view
 */
function latestComment(target, top, status) {
  return service({
    url: `${baseUrl}/${target}/comments/latest`,
    params: {
      top: top,
      status: status
    },
    method: 'get'
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

// List latest comment

commentApi.latestPostComment = (top, status) => {
  return latestComment('posts', top, status)
}
commentApi.latestSheetComment = (top, status) => {
  return latestComment('sheets', top, status)
}
commentApi.latestJournalComment = (top, status) => {
  return latestComment('journals', top, status)
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
