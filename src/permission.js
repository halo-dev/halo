import Vue from 'vue'
import router from './router'
import store from './store'
import {
  setDocumentTitle,
  domTitle
} from '@/utils/domUtil'

const whiteList = ['Login', 'Install', 'NotFound', 'ResetPassword'] // no redirect whitelist

router.beforeEach((to, from, next) => {
  to.meta && (typeof to.meta.title !== 'undefined' && setDocumentTitle(`${to.meta.title} - ${domTitle}`))
  Vue.$log.debug('Token', store.getters.token)
  if (store.getters.token) {
    if (to.name === 'Login') {
      next({
        name: 'Dashboard'
      })
      return
    }
    // TODO Get installation status

    if (!store.getters.options) {
      store.dispatch('loadOptions').then()
    }

    next()
    return
  }

  // Not login
  // Check whitelist
  if (whiteList.includes(to.name)) {
    next()
    return
  }

  next({
    name: 'Login',
    query: {
      redirect: to.fullPath
    }
  })
})
