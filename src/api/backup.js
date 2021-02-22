import service from '@/utils/service'

const baseUrl = '/api/admin/backups'

const backupApi = {}

backupApi.importMarkdown = (formData, uploadProgress, cancelToken) => {
  return service({
    url: `${baseUrl}/markdown/import`,
    timeout: 8640000, // 24 hours
    data: formData, // form data
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'post'
  })
}

backupApi.backupWorkDir = () => {
  return service({
    url: `${baseUrl}/work-dir`,
    method: 'post',
    timeout: 8640000 // 24 hours
  })
}

backupApi.listWorkDirBackups = () => {
  return service({
    url: `${baseUrl}/work-dir`,
    method: 'get'
  })
}

backupApi.fetchWorkDir = filename => {
  return service({
    url: `${baseUrl}/work-dir/fetch?filename=${filename}`,
    method: 'get'
  })
}

backupApi.deleteWorkDirBackup = filename => {
  return service({
    url: `${baseUrl}/work-dir`,
    params: {
      filename: filename
    },
    method: 'delete'
  })
}

backupApi.exportData = () => {
  return service({
    url: `${baseUrl}/data`,
    method: 'post',
    timeout: 8640000 // 24 hours
  })
}

backupApi.listExportedData = () => {
  return service({
    url: `${baseUrl}/data`,
    method: 'get'
  })
}

backupApi.fetchData = filename => {
  return service({
    url: `${baseUrl}/data/fetch?filename=${filename}`,
    method: 'get'
  })
}

backupApi.deleteExportedData = filename => {
  return service({
    url: `${baseUrl}/data`,
    params: {
      filename: filename
    },
    method: 'delete'
  })
}

backupApi.exportMarkdowns = needFrontMatter => {
  return service({
    url: `${baseUrl}/markdown/export`,
    method: 'post',
    data: {
      needFrontMatter: needFrontMatter
    },
    timeout: 8640000 // 24 hours
  })
}

backupApi.listExportedMarkdowns = () => {
  return service({
    url: `${baseUrl}/markdown/export`,
    method: 'get'
  })
}

backupApi.fetchMarkdown = filename => {
  return service({
    url: `${baseUrl}/markdown/fetch?filename=${filename}`,
    method: 'get'
  })
}

backupApi.deleteExportedMarkdown = filename => {
  return service({
    url: `${baseUrl}/markdown/export`,
    params: {
      filename: filename
    },
    method: 'delete'
  })
}

export default backupApi
