import service from '@/utils/service'

const baseUrl = '/api/admin/statics'

const staticApi = {}

staticApi.list = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

staticApi.delete = path => {
  return service({
    url: baseUrl,
    params: {
      path: path
    },
    method: 'delete'
  })
}

staticApi.createFolder = (basePath, folderName) => {
  return service({
    url: baseUrl,
    params: {
      basePath: basePath,
      folderName: folderName
    },
    method: 'post'
  })
}

staticApi.upload = (formData, uploadProgress, cancelToken, basePath) => {
  return service({
    url: `${baseUrl}/upload`,
    timeout: 8640000,
    data: formData,
    params: {
      basePath: basePath
    },
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'post'
  })
}

export default staticApi
