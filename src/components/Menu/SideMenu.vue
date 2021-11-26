<template>
  <a-layout-sider
    v-model="collapsed"
    :class="['sider', isDesktop() ? null : 'shadow', theme, fixedSidebar ? 'ant-fixed-sidemenu' : null]"
    :collapsible="collapsible"
    :trigger="null"
    width="256px"
  >
    <logo />
    <s-menu
      :collapsed="collapsed"
      :menu="menus"
      :mode="mode"
      :theme="theme"
      style="padding: 16px 0px;"
      @select="onSelect"
    ></s-menu>
  </a-layout-sider>
</template>

<script>
import Logo from '@/components/Tools/Logo'
import SMenu from './index'
import { mixin, mixinDevice } from '@/mixins/mixin'

export default {
  name: 'SideMenu',
  components: { Logo, SMenu },
  mixins: [mixin, mixinDevice],
  props: {
    mode: {
      type: String,
      required: false,
      default: 'inline'
    },
    theme: {
      type: String,
      required: false,
      default: 'dark'
    },
    collapsible: {
      type: Boolean,
      required: false,
      default: false
    },
    collapsed: {
      type: Boolean,
      required: false,
      default: false
    },
    menus: {
      type: Array,
      required: true
    }
  },
  methods: {
    onSelect(obj) {
      this.$emit('menuSelect', obj)
    }
  }
}
</script>
