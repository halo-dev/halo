import service from '@/utils/service'

const baseUrl = '/api/admin/backups'

const backupApi = {}

backupApi.importMarkdown = (formData, uploadProgress, cancelToken) => {
  return service({
    url: `${baseUrl}/import/markdown`,
    timeout: 8640000, // 24 hours
    data: formData, // form data
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'post'
  })
}

backupApi.backupHalo = () => {
  return service({
    url: `${baseUrl}/halo`,
    method: 'post',
    timeout: 8640000 // 24 hours
  })
}

backupApi.listHaloBackups = () => {
  return service({
    url: `${baseUrl}/halo`,
    method: 'get'
  })
}

backupApi.deleteHaloBackup = filename => {
  return service({
    url: `${baseUrl}/halo`,
    params: {
      filename: filename
    },
    method: 'delete'
  })
}

export default backupApi
