import service from '@/utils/service'

const baseUrl = '/api/admin/backups'

const backupApi = {}

backupApi.importMarkdown = (formData, uploadProgress, cancelToken) => {
  return service({
    url: `${baseUrl}/import/markdowns`,
    timeout: 8640000, // 24 hours
    data: formData, // form data
    onUploadProgress: uploadProgress,
    cancelToken: cancelToken,
    method: 'post'
  })
}

export default backupApi
