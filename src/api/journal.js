import service from '@/utils/service'

const baseUrl = '/api/admin/journals'

const journalApi = {}

journalApi.query = params => {
  return service({
    url: baseUrl,
    params: params,
    method: 'get'
  })
}

journalApi.create = (journal) => {
  return service({
    url: baseUrl,
    data: journal,
    method: 'post'
  })
}

journalApi.update = (journalId, journal) => {
  return service({
    url: `${baseUrl}/${journalId}`,
    data: journal,
    method: 'put'
  })
}

export default journalApi
