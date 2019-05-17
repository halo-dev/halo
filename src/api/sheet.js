import service from '@/utils/service'

const baseUrl = '/api/admin/sheets'

const sheetApi = {}

sheetApi.list = () => {
  return service({
    url: baseUrl,
    method: 'get'
  })
}

sheetApi.get = sheetId => {
  return service({
    url: `${baseUrl}/${sheetId}`,
    method: 'get'
  })
}

sheetApi.create = (sheetToCreate, autoSave) => {
  return service({
    url: baseUrl,
    method: 'post',
    data: sheetToCreate,
    params: {
      autoSave: autoSave
    }
  })
}

sheetApi.update = (sheetId, sheetToUpdate, autoSave) => {
  return service({
    url: `${baseUrl}/${sheetId}`,
    method: 'put',
    data: sheetToUpdate,
    params: {
      autoSave: autoSave
    }
  })
}

sheetApi.updateStatus = (sheetId, status) => {
  return service({
    url: `${baseUrl}/${sheetId}/${status}`,
    method: 'put'
  })
}

sheetApi.delete = sheetId => {
  return service({
    url: `${baseUrl}/${sheetId}`,
    method: 'delete'
  })
}

sheetApi.sheetStatus = {
  PUBLISHED: {
    color: 'green',
    status: 'success',
    text: '已发布'
  },
  DRAFT: {
    color: 'yellow',
    status: 'warning',
    text: '草稿'
  },
  RECYCLE: {
    color: 'red',
    status: 'error',
    text: '回收站'
  }
}
export default sheetApi
