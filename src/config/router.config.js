// eslint-disable-next-line
import { UserLayout, BasicLayout, RouteView, BlankLayout, PageView } from '@/components/layouts'

export const asyncRouterMap = [

  {
    path: '/',
    name: 'index',
    component: BasicLayout,
    meta: { title: '首页' },
    redirect: '/dashboard',
    children: [

      // dashboard
      {
        path: '/dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/Dashboard'),
        meta: { title: '仪表盘', icon: 'dashboard', hiddenHeaderContent: true }
      },

      // posts
      {
        path: '/posts',
        name: 'posts',
        redirect: '/posts/list',
        component: RouteView,
        meta: { title: '文章', icon: 'form', permission: [ 'form' ] },
        children: [
          {
            path: '/posts/list',
            name: 'PostList',
            component: () => import('@/views/post/PostList'),
            meta: { title: '所有文章', hiddenHeaderContent: true, permission: [ 'form' ] }
          },
          {
            path: '/posts/write',
            name: 'PostEdit',
            component: () => import('@/views/post/PostEdit'),
            meta: { title: '写文章', hiddenHeaderContent: true, permission: [ 'form' ] }
          },
          {
            path: '/categories',
            name: 'CategoryList',
            component: () => import('@/views/post/CategoryList'),
            meta: { title: '分类目录', hiddenHeaderContent: true, permission: [ 'form' ] }
          },
          {
            path: '/tags',
            name: 'TagList',
            component: () => import('@/views/post/TagList'),
            meta: { title: '标签', hiddenHeaderContent: true, permission: [ 'form' ] }
          }
        ]
      },

      // pages
      {
        path: '/pages',
        name: 'pages',
        component: RouteView,
        redirect: '/pages/list',
        meta: { title: '页面', icon: 'read', permission: [ 'table' ] },
        children: [
          {
            path: '/pages/list',
            name: 'PageList',
            component: () => import('@/views/page/PageList'),
            meta: { title: '所有页面', hiddenHeaderContent: true, permission: [ 'table' ] }
          },
          {
            path: '/pages/write',
            name: 'PageEdit',
            component: () => import('@/views/page/PageEdit'),
            meta: { title: '新建页面', hiddenHeaderContent: true, permission: [ 'table' ] }
          }
        ]
      },

      // attachments
      {
        path: '/attachments',
        name: 'attachments',
        component: () => import('@/views/attachment/AttachmentList'),
        meta: { title: '附件', icon: 'picture', hiddenHeaderContent: true, permission: [ 'profile' ] }
      },

      // comments
      {
        path: '/comments',
        name: 'comments',
        component: () => import('@/views/comment/CommentList'),
        meta: { title: '评论', icon: 'message', hiddenHeaderContent: true, permission: [ 'profile' ] }
      },

      // interface
      {
        path: '/interface',
        name: 'interface',
        component: RouteView,
        redirect: '/interface/themes',
        meta: { title: '外观', icon: 'skin', permission: [ 'result' ] },
        children: [
          {
            path: '/interface/themes',
            name: 'ThemeList',
            component: () => import('@/views/interface/ThemeList'),
            meta: { title: '主题', hiddenHeaderContent: true, permission: [ 'result' ] }
          },
          {
            path: '/interface/menus',
            name: 'MenuList',
            component: () => import('@/views/interface/MenuList'),
            meta: { title: '菜单', hiddenHeaderContent: true, permission: [ 'result' ] }
          },
          {
            path: '/interface/themes/edit',
            name: 'ThemeEdit',
            component: () => import('@/views/interface/ThemeEdit'),
            meta: { title: '主题编辑', hiddenHeaderContent: true, permission: [ 'result' ] }
          }
        ]
      },

      // user
      {
        path: '/user',
        name: 'user',
        component: RouteView,
        redirect: '/user/profile',
        meta: { title: '用户', icon: 'user', permission: [ 'exception' ] },
        children: [
          {
            path: '/user/profile',
            name: 'Profile',
            component: () => import('@/views/user/Profile'),
            meta: { title: '个人资料', hiddenHeaderContent: true, permission: [ 'exception' ] }
          }
        ]
      },

      // system
      {
        path: '/system',
        name: 'options',
        component: RouteView,
        redirect: '/system/options',
        meta: { title: '系统', icon: 'setting', permission: [ 'user' ] },
        children: [
          {
            path: '/system/options',
            name: 'OptionForm',
            component: () => import('@/views/system/OptionForm'),
            meta: { title: '博客设置', hiddenHeaderContent: true, permission: [ 'user' ] }
          },
          {
            path: '/system/backup',
            name: 'BackupList',
            component: () => import('@/views/system/BackupList'),
            meta: { title: '博客备份', hiddenHeaderContent: true, permission: [ 'user' ] }
          },
          {
            path: '/system/tools',
            name: 'ToolList',
            component: () => import('@/views/system/ToolList'),
            meta: { title: '小工具', hiddenHeaderContent: true, permission: [ 'user' ] }
          }
        ]
      }
    ]
  },
  {
    path: '*', redirect: '/404', hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      }
    ]
  },

  {
    path: '/test',
    component: BlankLayout,
    redirect: '/test/home',
    children: [
      {
        path: 'home',
        name: 'TestHome',
        component: () => import('@/views/Home')
      }
    ]
  },

  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }

]
