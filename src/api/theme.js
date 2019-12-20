import service from '@/utils/service'

const baseUrl = '/api/admin/themes'

const themeApi = {}

themeApi.listAll = () => {
  return service({
    url: `${baseUrl}`,
    method: 'get'
  })
}

themeApi.listFilesActivated = () => {
  return service({
    url: `${baseUrl}/activation/files`,
    method: 'get'
  })
}

themeApi.listFiles = themeId => {
  return service({
    url: `${baseUrl}/${themeId}/files`,
    method: 'get'
  })
}

themeApi.customSheetTpls = () => {
  return service({
    url: `${baseUrl}/activation/template/custom/sheet`,
    method: 'get'
  })
}

themeApi.customPostTpls = () => {
  return service({
    url: `${baseUrl}/activation/template/custom/post`,
    method: 'get'
  })
}

themeApi.active = theme => {
  return service({
    url: `${baseUrl}/${theme}/activation`,
    method: 'post'
  })
}

themeApi.getActivatedTheme = () => {
  return service({
    url: `${baseUrl}/activation`,
    method: 'get'
  })
}

themeApi.update = themeId => {
  return service({
    url: `${baseUrl}/fetching/${themeId}`,
    timeout: 60000,
    method: 'put'
  })
}

themeApi.delete = key => {
  return service({
    url: `${baseUrl}/${key}`,
    method: 'delete'
  })
}

themeApi.fetchConfiguration = themeId => {
  return service({
    url: `${baseUrl}/${themeId}/configurations`,
    method: 'get'
  })
}

themeApi.fetchSettings = themeId => {
  return service({
    url: `${baseUrl}/${themeId}/settings`,
    method: 'get'
  })
}

themeApi.saveSettings = (themeId, settings) => {
  return service({
    url: `${baseUrl}/${themeId}/settings`,
    data: settings,
    method: 'post'
  })
}

themeApi.getProperty = themeId => {
  return service({
    url: `${baseUrl}/${themeId}`,
    method: 'get'
  })
}

themeApi.upload = (formData, uploadProgress, cancelToken) => {
  return service({
    url: `${baseUrl}/upload`,
    timeout: 86400000, // 24 hours
    data: formData, // form data
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'post'
  })
}

themeApi.updateByUpload = (formData, uploadProgress, cancelToken, themeId) => {
  return service({
    url: `${baseUrl}/upload/${themeId}`,
    timeout: 86400000, // 24 hours
    data: formData, // form data
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'put'
  })
}

themeApi.fetching = url => {
  return service({
    url: `${baseUrl}/fetching`,
    timeout: 60000,
    params: {
      uri: url
    },
    method: 'post'
  })
}

themeApi.getContent = path => {
  return service({
    url: `${baseUrl}/files/content`,
    params: {
      path: path
    },
    method: 'get'
  })
}

themeApi.getContent = (themeId, path) => {
  return service({
    url: `${baseUrl}/${themeId}/files/content`,
    params: {
      path: path
    },
    method: 'get'
  })
}

themeApi.saveContent = (path, content) => {
  return service({
    url: `${baseUrl}/files/content`,
    data: {
      path: path,
      content: content
    },
    method: 'put'
  })
}

themeApi.saveContent = (themeId, path, content) => {
  return service({
    url: `${baseUrl}/${themeId}/files/content`,
    data: {
      path: path,
      content: content
    },
    method: 'put'
  })
}

themeApi.reload = () => {
  return service({
    url: `${baseUrl}/reload`,
    method: 'post'
  })
}

themeApi.exists = template => {
  return service({
    url: `${baseUrl}/activation/template/exists`,
    method: 'get',
    params: {
      template: template
    }
  })
}

export default themeApi
