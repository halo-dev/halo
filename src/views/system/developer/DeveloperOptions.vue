<template>
  <page-view>
    <a-row>
      <a-col :span="24">
        <div v-if="options.developer_mode" class="card-container">
          <a-tabs v-model="activeKey" type="card">
            <a-tab-pane v-for="pane in panes" :key="pane.key">
              <span slot="tab"> <a-icon :type="pane.icon" />{{ pane.title }} </span>
              <component :is="pane.component"></component>
            </a-tab-pane>
          </a-tabs>
        </div>
        <a-alert
          v-else
          description="当前没有启用开发者选项，请启用之后再访问该页面！"
          message="提示"
          showIcon
          type="error"
        />
      </a-col>
    </a-row>
  </page-view>
</template>
<script>
import { mapGetters } from 'vuex'
import { PageView } from '@/layouts'

export default {
  components: {
    PageView
  },
  data() {
    const panes = [
      {
        title: '运行环境',
        icon: 'safety',
        component: () => import('./tabs/Environment'),
        key: 'environment'
      },
      {
        title: '实时日志',
        icon: 'code',
        component: () => import('./tabs/RuntimeLogs'),
        key: 'runtimeLogs'
      },
      {
        title: '系统变量',
        icon: 'table',
        component: () => import('./tabs/OptionsList'),
        key: 'optionsList'
      },
      {
        title: '静态存储',
        icon: 'cloud',
        component: () => import('./tabs/StaticStorage'),
        key: 'staticStorage'
      },
      {
        title: '设置',
        icon: 'setting',
        component: () => import('./tabs/SettingsForm'),
        key: 'settings'
      }
    ]
    return {
      activeKey: panes[0].key,
      panes
    }
  },
  computed: {
    ...mapGetters(['options'])
  },
  beforeRouteEnter(to, from, next) {
    const activeKey = to.query.activeKey
    next(vm => {
      if (activeKey) {
        vm.activeKey = activeKey
      }
    })
  },
  watch: {
    activeKey(newVal) {
      if (newVal) {
        const path = this.$router.history.current.path
        this.$router.push({ path, query: { activeKey: newVal } }).catch(err => err)
      }
    }
  }
}
</script>
