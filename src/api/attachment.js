import axios from 'axios'
import service from '@/utils/service'

const baseUrl = '/api/admin/attachments'

const attachmentApi = {}

attachmentApi.query = params => {
  return service({
    url: baseUrl,
    params: params,
    method: 'get'
  })
}

attachmentApi.get = attachmentId => {
  return service({
    url: `${baseUrl}/${attachmentId}`,
    method: 'get'
  })
}

attachmentApi.delete = attachmentId => {
  return service({
    url: `${baseUrl}/${attachmentId}`,
    method: 'delete'
  })
}

attachmentApi.update = (attachmentId, attachment) => {
  return service({
    url: `${baseUrl}/${attachmentId}`,
    method: 'put',
    data: attachment
  })
}

attachmentApi.getMediaTypes = () => {
  return service({
    url: `${baseUrl}/mediaTypes`,
    method: 'get'
  })
}

attachmentApi.CancelToken = axios.CancelToken
attachmentApi.isCancel = axios.isCancel

attachmentApi.upload = (formData, uploadProgress, cancelToken) => {
  return service({
    url: `${baseUrl}/upload`,
    timeout: 8640000, // 24 hours
    data: formData, // form data
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'post'
  })
}

export default attachmentApi
