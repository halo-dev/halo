<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12" type="flex" align="middle">
      <a-col
        class="theme-item"
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="24"
        v-for="(theme, index) in themes"
        :key="index"
      >
        <a-card :bodyStyle="{ padding: '14px' }">
          <img
            :alt="theme.properties.name"
            :src="'http://localhost:8090/' + theme.key + '/screenshot.png'"
            slot="cover"
          />
          <a-divider></a-divider>
          <div class="theme-control">
            <span class="theme-title">
              {{ theme.properties.name }}
            </span>
            <a-button-group class="theme-button">
              <a-button type="primary" v-if="activatedTheme == theme.key" disabled>已启用</a-button>
              <a-button type="primary" @click="activeTheme(theme.key)" v-else>启用</a-button>
              <a-button @click="optionModal(theme.key)" v-if="activatedTheme == theme.key">设置</a-button>
              <a-popconfirm
                :title="'确定删除【' + theme.properties.name + '】主题？'"
                @confirm="deleteTheme(theme.key)"
                okText="确定"
                cancelText="取消"
                v-else
              >
                <a-button type="dashed">删除</a-button>
              </a-popconfirm>
            </a-button-group>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <a-modal :title="optionTheme + ' 主题设置'" width="90%" v-model="visible">
      <iframe :src="optionUrl" height="560" width="100%" frameborder="0" scrolling="auto"></iframe>
    </a-modal>
  </div>
</template>

<script>
import themeApi from '@/api/theme'
export default {
  data() {
    return {
      themes: [],
      visible: false,
      optionTheme: '',
      optionUrl: 'https://ryanc.cc',
      // TODO 从api获取当前使用的主题
      activatedTheme: 'anatole'
    }
  },
  created() {
    this.loadThemes()
  },
  methods: {
    loadThemes() {
      themeApi.listAll().then(response => {
        this.themes = response.data.data
      })
    },
    optionModal(theme) {
      this.optionTheme = theme
      this.visible = true
    },
    activeTheme(theme) {
      themeApi.active(theme).then(response => {
        this.activatedTheme = theme
        this.$message.success('设置成功！')
        this.loadThemes()
      })
    },
    deleteTheme(key) {
      themeApi.delete(key).then(response => {
        this.$message.success('删除成功！')
        this.loadThemes()
      })
    }
  }
}
</script>

<style scoped>
.ant-divider-horizontal {
  margin: 14px 0;
}

.theme-item {
  padding-bottom: 12px;
}

.theme-item .theme-control .theme-title {
  font-size: 18px;
}

.theme-item .theme-control .theme-button {
  float: right;
}
</style>
