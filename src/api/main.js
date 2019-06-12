import service from '@/utils/service'

const mainApi = {}

mainApi.version = () => {
  return service({
    url: 'version',
    method: 'get'
  })
}

export default mainApi
