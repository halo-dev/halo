<template>
  <a-layout :class="['layout', device]">
    <!-- SideMenu -->
    <a-drawer
      v-if="isMobile()"
      :closable="false"
      :visible="collapsed"
      :wrapClassName="`drawer-sider ${navTheme}`"
      placement="left"
      @close="drawerClose"
    >
      <side-menu
        :collapsed="false"
        :collapsible="true"
        :menus="menus"
        :theme="navTheme"
        mode="inline"
        @menuSelect="menuSelect"
      ></side-menu>
    </a-drawer>

    <side-menu
      v-else-if="isSideMenu()"
      :collapsed="collapsed"
      :collapsible="true"
      :menus="menus"
      :theme="navTheme"
      mode="inline"
    ></side-menu>

    <a-layout
      :class="[layoutMode, `content-width-${contentWidth}`]"
      :style="{ paddingLeft: contentPaddingLeft, minHeight: '100vh' }"
    >
      <!-- layout header -->
      <global-header
        :collapsed="collapsed"
        :device="device"
        :menus="menus"
        :mode="layoutMode"
        :theme="navTheme"
        @toggle="toggle"
      />

      <!-- layout content -->
      <a-layout-content :style="{ height: '100%', margin: '24px 24px 0', paddingTop: fixedHeader ? '64px' : '0' }">
        <transition name="page-transition">
          <route-view />
        </transition>
      </a-layout-content>

      <!-- layout footer -->
      <a-layout-footer>
        <global-footer />
      </a-layout-footer>
    </a-layout>

    <setting-drawer ref="drawer"></setting-drawer>

    <LoginModal @success="onLoginSucceed" />
  </a-layout>
</template>

<script>
import { triggerWindowResizeEvent } from '@/utils/util'
import { mapActions } from 'vuex'
import { mixin, mixinDevice } from '@/mixins/mixin'
import config from '@/config/defaultSettings'
import { asyncRouterMap } from '@/config/router.config.js'

import RouteView from './RouteView'
import SideMenu from '@/components/Menu/SideMenu'
import GlobalHeader from '@/components/GlobalHeader'
import GlobalFooter from '@/components/GlobalFooter'
import SettingDrawer from '@/components/SettingDrawer/SettingDrawer'
import LoginModal from '@/components/Login/LoginModal'

export default {
  name: 'BasicLayout',
  mixins: [mixin, mixinDevice],
  components: {
    RouteView,
    SideMenu,
    GlobalHeader,
    GlobalFooter,
    SettingDrawer,
    LoginModal
  },
  data() {
    return {
      production: config.production,
      collapsed: false,
      menus: []
    }
  },
  computed: {
    contentPaddingLeft() {
      if (!this.fixedSidebar || this.isMobile()) {
        return '0'
      }
      if (this.sidebarOpened) {
        return '256px'
      }
      return '80px'
    }
  },
  watch: {
    sidebarOpened(val) {
      this.collapsed = !val
    }
  },
  created() {
    this.menus = asyncRouterMap.find(item => item.path === '/').children
    this.collapsed = !this.sidebarOpened
  },
  mounted() {
    const userAgent = navigator.userAgent
    if (userAgent.indexOf('Edge') > -1) {
      this.$nextTick(() => {
        this.collapsed = !this.collapsed
        setTimeout(() => {
          this.collapsed = !this.collapsed
        }, 16)
      })
    }
  },
  methods: {
    ...mapActions(['setSidebar', 'ToggleLoginModal']),
    toggle() {
      this.collapsed = !this.collapsed
      this.setSidebar(!this.collapsed)
      triggerWindowResizeEvent()
    },
    paddingCalc() {
      let left = ''
      if (this.sidebarOpened) {
        left = this.isDesktop() ? '256px' : '80px'
      } else {
        left = (this.isMobile() && '0') || (this.fixedSidebar && '80px') || '0'
      }
      return left
    },
    menuSelect() {
      if (!this.isDesktop()) {
        this.collapsed = false
      }
    },
    drawerClose() {
      this.collapsed = false
    },
    onLoginSucceed() {
      this.ToggleLoginModal(false)
    }
  }
}
</script>

<style lang="less">
@import url('../styles/global.less');

.page-transition-enter {
  opacity: 0;
}

.page-transition-leave-active {
  opacity: 0;
}

.page-transition-enter .page-transition-container,
.page-transition-leave-active .page-transition-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}
</style>
