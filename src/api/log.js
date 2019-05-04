import service from '@/utils/service'

const baseUrl = '/api/admin/logs'

const logApi = {}

logApi.listLatest = (top) => {
  return service({
    url: `${baseUrl}/latest`,
    params: {
      top: top
    },
    method: 'get'
  })
}

logApi.logType = {
  BLOG_INITIALIZED: {
    value: 0,
    text: '博客初始化'
  },
  POST_PUBLISHED: {
    value: 5,
    text: '文章发布'
  },
  POST_EDITED: {
    value: 15,
    text: '文章修改'
  },
  POST_DELETED: {
    value: 20,
    text: '文章删除'
  },
  LOGGED_IN: {
    value: 25,
    text: '用户登陆'
  },
  LOGGED_OUT: {
    value: 30,
    text: '注销登陆'
  },
  LOGIN_FAILED: {
    value: 35,
    text: '登陆失败'
  },
  PASSWORD_UPDATED: {
    value: 40,
    text: '修改密码'
  },
  PROFILE_UPDATED: {
    value: 45,
    text: '资料修改'
  }
}

export default logApi
