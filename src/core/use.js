import Vue from 'vue'
import VueStorage from 'vue-ls'
import config from '@/config/defaultSettings'

// base library
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/antd.less'

import VueClipboard from 'vue-clipboard2'

Vue.use(Antd)

Vue.use(VueStorage, config.storageOptions)
Vue.use(VueClipboard)
