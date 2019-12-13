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

attachmentApi.deleteInBatch = attachmentIds => {
  return service({
    url: `${baseUrl}`,
    method: 'delete',
    data: attachmentIds,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
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
    url: `${baseUrl}/media_types`,
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

attachmentApi.uploads = (formDatas, uploadProgress, cancelToken) => {
  return service({
    url: `${baseUrl}/uploads`,
    timeout: 8640000, // 24 hours
    data: formDatas, // form data
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'post'
  })
}

attachmentApi.type = {
  LOCAL: {
    type: 'local',
    text: '本地'
  },
  SMMS: {
    type: 'smms',
    text: 'SM.MS'
  },
  UPOSS: {
    type: 'uposs',
    text: '又拍云'
  },
  QINIUOSS: {
    type: 'qiniuoss',
    text: '七牛云'
  },
  ALIOSS: {
    type: 'alioss',
    text: '阿里云'
  },
  BAIDUBOS: {
    type: 'baidubos',
    text: '百度云'
  },
  TENCENTCOS: {
    type: 'tencentcos',
    text: '腾讯云'
  }
}

export default attachmentApi
