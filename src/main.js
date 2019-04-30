import '@babel/polyfill'
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/'
import './logger'

import './core/lazy_use'
import './permission'
import '@/utils/filter' // global filter
import './components'
import animated from 'animate.css'

Vue.config.productionTip = false

Vue.use(router)
Vue.use(animated)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
