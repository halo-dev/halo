import service from '@/utils/service'

const baseUrl = '/admin/api/users'

const userApi = {}

userApi.getProfile = () => {
  return service({
    url: `${baseUrl}/profile`,
    method: 'get'
  })
}

userApi.updateProfile = updatedUserProfile => {
  return service({
    url: `${baseUrl}/profile`,
    method: 'put',
    data: updatedUserProfile
  })
}

export default userApi
