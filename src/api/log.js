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

logApi.pageBy = logPagination => {
  return service({
    url: baseUrl,
    params: logPagination,
    method: 'get'
  })
}

logApi.clear = () => {
  return service({
    url: `${baseUrl}/clear`,
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
    text: '用户登录'
  },
  LOGGED_OUT: {
    value: 30,
    text: '注销登录'
  },
  LOGIN_FAILED: {
    value: 35,
    text: '登录失败'
  },
  PASSWORD_UPDATED: {
    value: 40,
    text: '修改密码'
  },
  PROFILE_UPDATED: {
    value: 45,
    text: '资料修改'
  },
  SHEET_PUBLISHED: {
    value: 50,
    text: '页面发布'
  },
  SHEET_EDITED: {
    value: 55,
    text: '页面修改'
  },
  SHEET_DELETED: {
    value: 60,
    text: '页面删除'
  },
  MFA_UPDATED: {
    value: 65,
    text: '两步验证'
  },
  LOGGED_PRE_CHECK: {
    value: 70,
    text: '登陆验证'
  }
}

export default logApi
