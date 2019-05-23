import Vue from 'vue'
import router from './router'
import store from './store'
import { setDocumentTitle, domTitle } from '@/utils/domUtil'

import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['Login', 'Install', 'NotFound'] // no redirect whitelist

router.beforeEach((to, from, next) => {
  NProgress.start()
  to.meta && (typeof to.meta.title !== 'undefined' && setDocumentTitle(`${to.meta.title} - ${domTitle}`))
  Vue.$log.debug('Token', store.getters.token)
  if (store.getters.token) {
    if (to.name === 'Login') {
      next({ name: 'Dashboard' })
      NProgress.done()
      return
    }
    // TODO Get installation status

    next()
    NProgress.done()
    return
  }

  // Not login
  // Check whitelist
  if (whiteList.includes(to.name)) {
    next()
    NProgress.done()
    return
  }

  next({ name: 'Login', query: { redirect: to.fullPath } })
  NProgress.done()
})
