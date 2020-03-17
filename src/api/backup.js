import service from '@/utils/service'

const baseUrl = '/api/admin/backups'

const backupApi = {}

backupApi.importMarkdown = (formData, uploadProgress, cancelToken) => {
  return service({
    url: `${baseUrl}/markdown`,
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

backupApi.deleteExportedData = filename => {
  return service({
    url: `${baseUrl}/data`,
    params: {
      filename: filename
    },
    method: 'delete'
  })
}

export default backupApi
