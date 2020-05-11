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

staticApi.rename = (basePath, newName) => {
  return service({
    url: `${baseUrl}/rename`,
    params: {
      basePath: basePath,
      newName: newName
    },
    method: 'post'
  })
}

staticApi.getContent = url => {
  return service({
    url: `${url}`,
    method: 'get'
  })
}

staticApi.save = (path, content) => {
  return service({
    url: `${baseUrl}/files`,
    data: {
      path: path,
      content: content
    },
    method: 'put'
  })
}

export default staticApi
