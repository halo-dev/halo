import '@babel/polyfill'
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/'
import './logger'

import './core/lazy_use'
import bootstrap from './core/bootstrap'
import '@/utils/filter' // global filter
import './components'
import animated from 'animate.css'

Vue.config.productionTip = false

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' | Halo Dashboard'
  }

  if (to.name !== 'Login' && !store.getters.token) {
    Vue.$log.debug('Redirectint to Login page')
    next({ name: 'Login' })
    return
  }

  next()
})

Vue.use(router)
Vue.use(animated)

new Vue({
  router,
  store,
  created() {
    bootstrap()
  },
  render: h => h(App)
}).$mount('#app')
