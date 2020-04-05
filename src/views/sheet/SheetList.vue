<template>
  <div>
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs
            v-model="activeKey"
            type="card"
          >
            <a-tab-pane
              v-for="pane in panes"
              :key="pane.key"
            >
              <span slot="tab">
                <a-icon :type="pane.icon" />{{ pane.title }}
              </span>
              <component :is="pane.component"></component>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import IndependentSheetList from './components/IndependentSheetList'
import CustomSheetList from './components/CustomSheetList'

export default {
  data() {
    const panes = [
      { title: '独立页面', icon: 'paper-clip', component: 'IndependentSheetList', key: 'independent' },
      { title: '自定义页面', icon: 'fork', component: 'CustomSheetList', key: 'custom' }
    ]
    return {
      activeKey: panes[0].key,
      panes
    }
  },
  beforeRouteEnter(to, from, next) {
    // Get post id from query
    const activeKey = to.query.activeKey
    next(vm => {
      if (activeKey) {
        vm.activeKey = activeKey
      }
    })
  },
  watch: {
    activeKey: {
      handler: function(newVal, oldVal) {
        if (newVal) {
          const path = this.$router.history.current.path
          this.$router.push({ path, query: { activeKey: newVal } }).catch(err => err)
        }
      }
    }
  },
  components: {
    IndependentSheetList,
    CustomSheetList
  }
}
</script>
