import '@babel/polyfill'
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/'
import './logger'

import bootstrap from './core/bootstrap'
import './core/lazy_use'
import './permission'
import '@/utils/filter' // global filter
import './components'
import { version } from '../package.json'

Vue.config.productionTip = false
Vue.prototype.VERSION = version

Vue.use(router)

new Vue({
  router,
  store,
  created: bootstrap,
  render: h => h(App)
}).$mount('#app')
