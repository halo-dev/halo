import Vue from 'vue'
import App from './App.vue'
import router from './router'
import Contextmenu from 'vue-contextmenujs'
import store from './store/'
import './logger'

import '@/styles/tailwind.css'
import './core/lazy_use'
import '@/router/guard/'
import '@/filters/filter' // global filter
import './components'
import { version } from '../package.json'

Vue.config.productionTip = false
Vue.prototype.VERSION = version

Vue.use(router)
Vue.use(Contextmenu)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
