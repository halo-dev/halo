<template>
<div class="page-header-index-wide">
  <a-row
    :gutter="12"
    type="flex"
    align="middle"
  >
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
          :alt="theme.name"
          :src="theme.screenshots"
          slot="cover"
        >
        <a-divider></a-divider>
        <div class="theme-control">
          <span class="theme-title">{{ theme.name }}</span>
          <a-button-group class="theme-button">
            <a-button
              type="primary"
              v-if="theme.activated"
              disabled
            >已启用</a-button>
            <a-button
              type="primary"
              @click="activeTheme(theme.id)"
              v-else
            >启用</a-button>
            <a-button
              @click="settingDrawer(theme)"
              v-if="theme.hasOptions"
            >设置</a-button>
          </a-button-group>
        </div>
      </a-card>
    </a-col>
  </a-row>
  <a-drawer
    v-if="themeProperty"
    :title="themeProperty.name + ' 主题设置'"
    width="100%"
    closable
    @close="onClose"
    :visible="visible"
    destroyOnClose
  >
    <a-row
      :gutter="12"
      type="flex"
    >
      <a-col
        :xl="12"
        :lg="12"
        :md="12"
        :sm="24"
        :xs="24"
      >
        <a-skeleton
          active
          :loading="optionLoading"
          :paragraph="{rows: 10}"
        >
          <img
            v-if="themeProperty"
            :alt="themeProperty.name"
            :src="themeProperty.screenshots"
            width="100%"
          >
        </a-skeleton>
      </a-col>
      <a-col
        :xl="12"
        :lg="12"
        :md="12"
        :sm="24"
        :xs="24"
      >
        <a-skeleton
          active
          :loading="optionLoading"
          :paragraph="{rows: 20}"
        >
          <a-tabs defaultActiveKey="0">
            <a-tab-pane
              v-for="(group, index) in themeConfiguration"
              :key="index.toString()"
              :tab="group.label"
            >
              <a-form layout="vertical">
                <a-form-item
                  v-for="(item, index1) in group.items"
                  :label="item.label + '：'"
                  :key="index1"
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    v-model="themeSettings[item.name]"
                    :defaultValue="item.defaultValue"
                    v-if="item.type == 'TEXT'"
                  />
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="themeSettings[item.name]"
                    v-else-if="item.type == 'TEXTAREA'"
                  />
                  <a-radio-group
                    v-decorator="['radio-group']"
                    :defaultValue="item.defaultValue"
                    v-model="themeSettings[item.name]"
                    v-else-if="item.type == 'RADIO'"
                  >
                    <a-radio
                      v-for="(option, index2) in item.options"
                      :key="index2"
                      :value="option.value"
                    >{{ option.label }}</a-radio>
                  </a-radio-group>
                  <a-select
                    v-model="themeSettings[item.name]"
                    :defaultValue="item.defaultValue"
                    v-else-if="item.type == 'SELECT'"
                  >
                    <a-select-option
                      v-for="option in item.options"
                      :key="option.value"
                      :value="option.value"
                    >{{ option.label }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="saveSettings"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane
              key="about"
              tab="关于"
            >
              <a-form-item>
                <a-popconfirm
                  :title="'确定删除【' + themeProperty.name + '】主题？'"
                  @confirm="deleteTheme(themeProperty.id)"
                  okText="确定"
                  cancelText="取消"
                >
                  <a-button type="danger">删除该主题</a-button>
                </a-popconfirm>
              </a-form-item>
            </a-tab-pane>
          </a-tabs>
        </a-skeleton>
      </a-col>
    </a-row>
  </a-drawer>
</div>
</template>

<script>
import themeApi from '@/api/theme'

export default {
  data() {
    return {
      optionLoading: true,
      wrapperCol: {
        xl: { span: 12 },
        lg: { span: 12 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      themes: [],
      visible: false,
      themeConfiguration: null,
      themeSettings: [],
      themeProperty: null
    }
  },
  computed: {
    activatedTheme() {
      return this.themes.find(theme => theme.activated)
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
    settingDrawer(theme) {
      this.visible = true
      this.optionLoading = true
      this.themeProperty = theme

      setTimeout(() => {
        themeApi.fetchConfiguration(theme.id).then(response => {
          this.themeConfiguration = response.data.data
          themeApi.fetchSettings().then(response => {
            this.themeSettings = response.data.data
            this.visible = true
            this.optionLoading = false
          })
        })
      }, 300)
    },
    activeTheme(theme) {
      themeApi.active(theme).then(response => {
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
    saveSettings() {
      themeApi.saveSettings(this.themeSettings).then(response => {
        this.$message.success('保存成功！')
      })
    },
    getThemeProperty(themeId) {
      themeApi.getProperty(themeId).then(response => {
        this.themeProperty = response.data.data
      })
    },
    onClose() {
      this.visible = false
      this.optionLoading = false
      this.themeConfiguration = null
      this.themeProperty = null
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
