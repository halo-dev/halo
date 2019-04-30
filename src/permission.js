import Vue from 'vue'
import router from './router'
import store from './store'

import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['Login', 'Install', 'NotFound'] // no redirect whitelist

router.beforeEach((to, from, next) => {
  NProgress.start()
  Vue.$log.debug('Token', store.getters.token)
  if (store.getters.token) {
    if (to.name === 'Login') {
      next({ name: 'Dashboard' })
      NProgress.done()
      return
    }
    // TODO Get installation status

    next()
    return
  }

  // Not login
  // Check whitelist
  if (whiteList.includes(to.name)) {
    next()
    return
  }

  next({ name: 'Login', query: { redirect: to.fullPath } })
})

router.afterEach(() => {
  NProgress.done()
})
