import service from '@/utils/service'

const baseUrl = '/api/admin/mails'

const mailApi = {}

mailApi.test = mailData => {
  return service({
    url: `${baseUrl}/test`,
    method: 'post',
    data: mailData
  })
}

export default mailApi
