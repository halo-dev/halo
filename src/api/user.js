import service from '@/utils/service'

const baseUrl = '/api/admin/users'

const userApi = {}

userApi.getProfile = () => {
  return service({
    url: `${baseUrl}/profile`,
    method: 'get'
  })
}

userApi.updateProfile = profile => {
  return service({
    url: `${baseUrl}/profile`,
    method: 'put',
    data: profile
  })
}

userApi.updatePassword = (oldPassword, newPassword) => {
  return service({
    url: `${baseUrl}/profile/password`,
    method: 'put',
    data: {
      oldPassword: oldPassword,
      newPassword: newPassword
    }
  })
}

export default userApi
