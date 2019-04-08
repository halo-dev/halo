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
            <span class="theme-title">{{ theme.properties.name }}</span>
            <a-button-group class="theme-button">
              <a-button type="primary" v-if="activatedTheme == theme.key" disabled>已启用</a-button>
              <a-button type="primary" @click="activeTheme(theme.key)" v-else>启用</a-button>
              <a-button @click="optionDrawer(theme.key)" v-if="activatedTheme == theme.key && theme.hasOptions">设置</a-button>
              <a-popconfirm
                :title="'确定删除【' + theme.properties.name + '】主题？'"
                @confirm="deleteTheme(theme.key)"
                okText="确定"
                cancelText="取消"
                v-else-if="activatedTheme != theme.key"
              >
                <a-button type="dashed">删除</a-button>
              </a-popconfirm>
            </a-button-group>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <a-drawer :title="optionTheme + ' 主题设置'" width="100%" :closable="true" @close="onClose" :visible="visible">
      <a-row :gutter="12" type="flex">
        <a-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <img
            :alt="activatedTheme"
            :src="'http://localhost:8090/' + activatedTheme + '/screenshot.png'"
            width="100%"
          />
        </a-col>
        <a-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <a-tabs>
            <a-tab-pane v-for="(group, index) in themeConfiguration" :key="index" :tab="group.label">
              <a-form layout="vertical">
                <a-form-item
                  v-for="(item, index1) in group.items"
                  :label="item.label + '：'"
                  :key="index1"
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="themeOptions[item.name]" v-if="item.type == 'text'" />
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="themeOptions[item.name]"
                    v-else-if="item.type == 'textarea'"
                  />
                  <a-radio-group
                    v-decorator="['radio-group']"
                    defaultValue="false"
                    v-model="themeOptions[item.name]"
                    v-else-if="item.type == 'radio'"
                  >
                    <a-radio v-for="(option, index2) in item.options" :key="index2" :value="option.value">{{
                      option.label
                    }}</a-radio>
                  </a-radio-group>
                  <a-select v-model="themeOptions[item.name]" v-else-if="item.type == 'select'">
                    <a-select-option v-for="(option, index3) in item.options" :key="index3" :value="option.value">{{
                      option.label
                    }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" @click="saveOptions">保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
          </a-tabs>
        </a-col>
      </a-row>
    </a-drawer>
  </div>
</template>

<script>
import themeApi from '@/api/theme'
import optionApi from '@/api/option'
export default {
  data() {
    return {
      wrapperCol: {
        xl: { span: 12 },
        lg: { span: 12 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      themes: [],
      visible: false,
      optionTheme: '',
      optionUrl: 'https://ryanc.cc',
      // TODO 从api获取当前使用的主题
      activatedTheme: 'anatole',
      themeConfiguration: [],
      options: {},
      themeOptions: {}
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
    optionDrawer(theme) {
      themeApi.listOptions(theme).then(response => {
        this.visible = true
        this.themeConfiguration = response.data.data
        this.optionTheme = theme
        this.loadOptions()
      })
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
    },
    saveOptions() {
      optionApi.save(this.options).then(response => {
        this.loadOptions()
        this.$message.success('保存成功！')
      })
    },
    loadOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
      })
    },
    onClose() {
      this.visible = false
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
