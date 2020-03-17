import service from '@/utils/service'

const baseUrl = '/api/admin/migrations'

const migrateApi = {}

migrateApi.migrate = formData => {
  return service({
    url: `${baseUrl}/halo`,
    data: formData,
    method: 'post'
  })
}

export default migrateApi
