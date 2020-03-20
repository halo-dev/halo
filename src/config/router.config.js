// eslint-disable-next-line
import { BasicLayout, PageView } from '@/layouts'

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
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard'),
        meta: { title: '仪表盘', icon: 'dashboard', hiddenHeaderContent: false, keepAlive: false }
      },

      // posts
      {
        path: '/posts',
        name: 'Posts',
        redirect: '/posts/list',
        component: PageView,
        meta: { title: '文章', icon: 'form' },
        children: [
          {
            path: '/posts/list',
            name: 'PostList',
            component: () => import('@/views/post/PostList'),
            meta: { title: '所有文章', hiddenHeaderContent: false }
          },
          {
            path: '/posts/write',
            name: 'PostEdit',
            component: () => import('@/views/post/PostEdit'),
            meta: { title: '写文章', hiddenHeaderContent: false, keepAlive: false }
          },
          {
            path: '/categories',
            name: 'CategoryList',
            component: () => import('@/views/post/CategoryList'),
            meta: { title: '分类目录', hiddenHeaderContent: false }
          },
          {
            path: '/tags',
            name: 'TagList',
            component: () => import('@/views/post/TagList'),
            meta: { title: '标签', hiddenHeaderContent: false }
          }
        ]
      },

      // sheets
      {
        path: '/sheets',
        name: 'Sheets',
        component: PageView,
        redirect: '/sheets/list',
        meta: { title: '页面', icon: 'read' },
        children: [
          {
            path: '/sheets/list',
            name: 'SheetList',
            component: () => import('@/views/sheet/SheetList'),
            meta: { title: '所有页面', hiddenHeaderContent: false }
          },
          {
            path: '/sheets/write',
            name: 'SheetEdit',
            component: () => import('@/views/sheet/SheetEdit'),
            meta: { title: '新建页面', hiddenHeaderContent: false, keepAlive: false }
          },
          {
            path: '/sheets/links',
            name: 'LinkList',
            hidden: true,
            component: () => import('@/views/sheet/independent/LinkList'),
            meta: { title: '友情链接', hiddenHeaderContent: false }
          },
          {
            path: '/sheets/photos',
            name: 'PhotoList',
            hidden: true,
            component: () => import('@/views/sheet/independent/PhotoList'),
            meta: { title: '图库', hiddenHeaderContent: false }
          },
          {
            path: '/sheets/journals',
            name: 'JournalList',
            hidden: true,
            component: () => import('@/views/sheet/independent/JournalList'),
            meta: { title: '日志', hiddenHeaderContent: false }
          }
        ]
      },

      // attachments
      {
        path: '/attachments',
        name: 'Attachments',
        component: () => import('@/views/attachment/AttachmentList'),
        meta: { title: '附件', icon: 'picture', hiddenHeaderContent: false }
      },

      // comments
      {
        path: '/comments',
        name: 'Comments',
        component: () => import('@/views/comment/CommentList'),
        meta: { title: '评论', icon: 'message', hiddenHeaderContent: false }
      },

      // interface
      {
        path: '/interface',
        name: 'Interface',
        component: PageView,
        redirect: '/interface/themes',
        meta: { title: '外观', icon: 'skin' },
        children: [
          {
            path: '/interface/themes',
            name: 'ThemeList',
            component: () => import('@/views/interface/ThemeList'),
            meta: { title: '主题', hiddenHeaderContent: false }
          },
          {
            path: '/interface/menus',
            name: 'MenuList',
            component: () => import('@/views/interface/MenuList'),
            meta: { title: '菜单', hiddenHeaderContent: false }
          },
          {
            path: '/interface/themes/edit',
            name: 'ThemeEdit',
            component: () => import('@/views/interface/ThemeEdit'),
            meta: { title: '主题编辑', hiddenHeaderContent: false }
          }
        ]
      },

      // user
      {
        path: '/user',
        name: 'User',
        component: PageView,
        redirect: '/user/profile',
        meta: { title: '用户', icon: 'user' },
        children: [
          {
            path: '/user/profile',
            name: 'Profile',
            component: () => import('@/views/user/Profile'),
            meta: { title: '个人资料', hiddenHeaderContent: false }
          }
        ]
      },

      // system
      {
        path: '/system',
        name: 'System',
        component: PageView,
        redirect: '/system/options',
        meta: { title: '系统', icon: 'setting' },
        children: [
          {
            path: '/system/developer/options',
            name: 'DeveloperOptions',
            hidden: true,
            component: () => import('@/views/system/developer/DeveloperOptions'),
            meta: { title: '开发者选项', hiddenHeaderContent: false }
          },
          {
            path: '/system/options',
            name: 'SystemOptions',
            component: () => import('@/views/system/SystemOptions'),
            meta: { title: '博客设置', hiddenHeaderContent: false }
          },
          {
            path: '/system/tools',
            name: 'ToolList',
            component: () => import('@/views/system/ToolList'),
            meta: { title: '小工具', hiddenHeaderContent: false }
          },
          // {
          //   path: '/system/tools/staticpages',
          //   name: 'StaticPagesManage',
          //   hidden: true,
          //   component: () => import('@/views/system/staticpages/StaticPagesManage'),
          //   meta: { title: '静态部署', hiddenHeaderContent: false }
          // },
          {
            path: '/system/about',
            name: 'About',
            component: () => import('@/views/system/About'),
            meta: { title: '关于', hiddenHeaderContent: false }
          }
        ]
      }
    ]
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

export const constantRouterMap = [
  {
    path: '/login',
    name: 'Login',
    meta: { title: '登录' },
    component: () => import('@/views/user/Login')
  },
  {
    path: '/install',
    name: 'Install',
    meta: { title: '安装向导' },
    component: () => import('@/views/system/Installation')
  },
  {
    path: '/password/reset',
    name: 'ResetPassword',
    meta: { title: '重置密码' },
    component: () => import('@/views/user/ResetPassword')
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/exception/404')
  }
]
