import service from '@/utils/service'

const baseUrl = '/api/admin/posts'

const postApi = {}

postApi.listLatest = () => {
  return service({
    url: `${baseUrl}/latest`,
    method: 'get'
  })
}

postApi.query = params => {
  return service({
    url: baseUrl,
    params: params,
    method: 'get'
  })
}

postApi.get = postId => {
  return service({
    url: `${baseUrl}/${postId}`,
    method: 'get'
  })
}

postApi.create = postToCreate => {
  return service({
    url: baseUrl,
    method: 'post',
    data: postToCreate
  })
}

postApi.update = (postId, postToUpdate) => {
  return service({
    url: `${baseUrl}/${postId}`,
    method: 'put',
    data: postToUpdate
  })
}

postApi.updateStatus = (postId, status) => {
  return service({
    url: `${baseUrl}/${postId}/${status}`,
    method: 'put'
  })
}

postApi.delete = postId => {
  return service({
    url: `${baseUrl}/${postId}`,
    method: 'delete'
  })
}

postApi.postStatus = {
  PUBLISHED: {
    color: 'green',
    status: 'success',
    text: '已发布'
  },
  DRAFT: {
    color: 'yellow',
    status: 'warning',
    text: '草稿'
  },
  RECYCLE: {
    color: 'red',
    status: 'error',
    text: '回收站'
  }
}
export default postApi
