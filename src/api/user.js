import service from '@/utils/service'

const baseUrl = '/api/admin/users'

const userApi = {}

userApi.getProfile = () => {
  return service({
    url: `${baseUrl}/profiles`,
    method: 'get'
  })
}

userApi.updateProfile = profile => {
  return service({
    url: `${baseUrl}/profiles`,
    method: 'put',
    data: profile
  })
}

userApi.updatePassword = (oldPassword, newPassword) => {
  return service({
    url: `${baseUrl}/profiles/password`,
    method: 'put',
    data: {
      oldPassword: oldPassword,
      newPassword: newPassword
    }
  })
}

userApi.mfaGenerate = (mfaType) => {
  return service({
    url: `${baseUrl}/mfa/generate`,
    method: 'put',
    data: {
      mfaType: mfaType
    }
  })
}

userApi.mfaUpdate = (mfaType, mfaKey, authcode) => {
  return service({
    url: `${baseUrl}/mfa/update`,
    method: 'put',
    data: {
      mfaType: mfaType,
      mfaKey: mfaKey,
      authcode: authcode
    }
  })
}

userApi.mfaCheck = (authcode) => {
  return service({
    url: `${baseUrl}/mfa/check`,
    method: 'put',
    data: {
      authcode: authcode
    }
  })
}

export default userApi
