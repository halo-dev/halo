import Vue from 'vue'
import Router from 'vue-router'
import {
  constantRouterMap,
  asyncRouterMap
} from '@/config/router.config'

Vue.use(Router)

export default new Router({
  mode: 'hash',
  base: process.env.BASE_URL,
  scrollBehavior: () => ({
    y: 0
  }),
  routes: constantRouterMap.concat(asyncRouterMap)
})
