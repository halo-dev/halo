import '@babel/polyfill'
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/'
import './logger'

import './core/use'
import bootstrap from './core/bootstrap'
// import '@/permission' // permission control
import '@/utils/filter' // global filter
import animated from 'animate.css'

Vue.config.productionTip = false

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
