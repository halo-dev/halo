export const markdownEditorToolbars = {
  bold: true,
  italic: true,
  header: true,
  underline: true,
  strikethrough: true,
  superscript: true,
  subscript: true,
  quote: true,
  ol: true,
  ul: true,
  link: true,
  imagelink: true,
  code: true,
  table: true,
  undo: true,
  redo: true,
  save: true,
  navigation: true,
  subfield: true,
  fullscreen: true,
  readmodel: true,
  htmlcode: true,
  preview: true
}

export const actionLogTypes = {
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
    text: '登录验证'
  }
}

export const attachmentTypes = {
  LOCAL: {
    type: 'LOCAL',
    text: '本地'
  },
  SMMS: {
    type: 'SMMS',
    text: 'SM.MS'
  },
  UPOSS: {
    type: 'UPOSS',
    text: '又拍云'
  },
  QINIUOSS: {
    type: 'QINIUOSS',
    text: '七牛云'
  },
  ALIOSS: {
    type: 'ALIOSS',
    text: '阿里云'
  },
  BAIDUBOS: {
    type: 'BAIDUBOS',
    text: '百度云'
  },
  TENCENTCOS: {
    type: 'TENCENTCOS',
    text: '腾讯云'
  },
  HUAWEIOBS: {
    type: 'HUAWEIOBS',
    text: '华为云'
  },
  MINIO: {
    type: 'MINIO',
    text: 'MinIO'
  }
}

export const postStatuses = {
  PUBLISHED: {
    value: 'PUBLISHED',
    color: 'green',
    status: 'success',
    text: '已发布'
  },
  DRAFT: {
    value: 'DRAFT',
    color: 'yellow',
    status: 'warning',
    text: '草稿'
  },
  RECYCLE: {
    value: 'RECYCLE',
    color: 'red',
    status: 'error',
    text: '回收站'
  },
  INTIMATE: {
    value: 'INTIMATE',
    color: 'blue',
    status: 'success',
    text: '私密'
  }
}

export const sheetStatuses = {
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
