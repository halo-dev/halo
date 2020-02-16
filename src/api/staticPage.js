import service from '@/utils/service'

const baseUrl = '/api/admin/static_page'

const staticPageApi = {}

staticPageApi.list = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

staticPageApi.generate = () => {
  return service({
    url: `${baseUrl}/generate`,
    method: 'get'
  })
}

staticPageApi.deploy = () => {
  return service({
    url: `${baseUrl}/deploy`,
    method: 'get'
  })
}

staticPageApi.deployType = {
  GIT: {
    type: 'GIT',
    text: 'Git'
  },
  NETLIFY: {
    type: 'NETLIFY',
    text: 'Netlify'
  }
}

export default staticPageApi
