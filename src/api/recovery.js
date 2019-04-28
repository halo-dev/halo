import service from '@/utils/service'

const baseUrl = '/api/admin/recoveries'

const recoveryApi = {}

recoveryApi.migrate = formData => {
  return service({
    url: `${baseUrl}/migrations/v0_4_3`,
    data: formData,
    method: 'post'
  })
}

export default recoveryApi
