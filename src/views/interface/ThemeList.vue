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
        <a-card hoverable :title="theme.name">
          <img :alt="theme.name" :src="theme.screenshots" slot="cover">
          <template class="ant-card-actions" slot="actions">
            <div v-if="theme.activated">
              <a-icon type="unlock" theme="twoTone"/>已启用
            </div>
            <div v-else @click="handleActivateClick(theme)">
              <a-icon type="lock"/>启用
            </div>
            <div @click="handleEditClick(theme)">
              <a-icon type="setting"/>设置
            </div>
            <a-dropdown placement="topCenter">
              <a class="ant-dropdown-link" href="#">
                <a-icon type="ellipsis"/>更多
              </a>
              <a-menu slot="overlay">
                <a-menu-item :key="1" :disabled="theme.activated">
                  <a-popconfirm
                    v-if="!theme.activated"
                    :title="'确定删除【' + theme.name + '】主题？'"
                    @confirm="deleteTheme(theme.id)"
                    okText="确定"
                    cancelText="取消"
                  >
                    <a-icon type="delete"/>删除
                  </a-popconfirm>
                  <span v-else>
                    <a-icon type="delete"/>删除
                  </span>
                </a-menu-item>
              </a-menu>
            </a-dropdown>
          </template>
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
      <a-row :gutter="12" type="flex">
        <a-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <a-skeleton active :loading="optionLoading" :paragraph="{rows: 10}">
            <a-card :bordered="false">
              <img :alt="themeProperty.name" :src="themeProperty.screenshots" slot="cover">
              <a-card-meta
                :title="themeProperty.author.name"
                :description="themeProperty.description"
              >
                <a-avatar
                  v-if="themeProperty.logo"
                  :src="themeProperty.logo"
                  size="large"
                  slot="avatar"
                />
                <a-avatar v-else size="large" slot="avatar">{{ themeProperty.author.name }}</a-avatar>
              </a-card-meta>
            </a-card>
          </a-skeleton>
        </a-col>
        <a-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <a-skeleton active :loading="optionLoading" :paragraph="{rows: 20}">
            <div class="card-container">
              <a-tabs type="card" defaultActiveKey="0">
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
                      <a-button type="primary" @click="saveSettings">保存</a-button>
                    </a-form-item>
                  </a-form>
                </a-tab-pane>
              </a-tabs>
            </div>
          </a-skeleton>
        </a-col>
      </a-row>
    </a-drawer>
    <div class="upload-button">
      <a-button type="primary" shape="circle" icon="plus" size="large" @click="showUploadModal"></a-button>
    </div>
    <a-modal
      title="安装主题"
      v-model="uploadVisible"
      :footer="null"
      :bodyStyle="{ padding: '0 24px 24px' }"
    >
      <a-tabs defaultActiveKey="1">
        <a-tab-pane tab="本地上传" key="1">
          <upload />
          <a-upload-dragger
            name="file"
            :multiple="true"
            accept="application/zip"
            :customRequest="handleUpload"
            @change="handleChange"
          >
            <p class="ant-upload-drag-icon">
              <a-icon type="inbox"/>
            </p>
            <p class="ant-upload-text">点击选择主题或将主题拖拽到此处</p>
            <p class="ant-upload-hint">支持单个或批量上传，仅支持 ZIP 格式的文件</p>
          </a-upload-dragger>
        </a-tab-pane>
        <a-tab-pane tab="远程拉取" key="2">
          <a-form layout="vertical">
            <a-form-item label="远程地址：">
              <a-input v-model="fetchingUrl"/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleFetching">确定</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-modal>
  </div>
</template>

<script>
import themeApi from '@/api/theme'

export default {
  data() {
    return {
      optionLoading: true,
      uploadVisible: false,
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
      themeProperty: null,
      fetchingUrl: null
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
    activeTheme(themeId) {
      themeApi.active(themeId).then(response => {
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
    },
    showUploadModal() {
      this.uploadVisible = true
    },
    handleChange(info) {
      const status = info.file.status
      if (status === 'done') {
        this.$message.success(`${info.file.name} 主题上传成功`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 主题上传失败`)
      }
    },
    handleEllipsisClick(theme) {
      this.$log.debug('Ellipsis clicked', theme)
    },
    handleEditClick(theme) {
      this.settingDrawer(theme)
    },
    handleActivateClick(theme) {
      this.activeTheme(theme.id)
    },
    handleUpload(option) {
      this.$log.debug('Uploading option', option)
      const CancelToken = themeApi.CancelToken
      const source = CancelToken.source()

      const data = new FormData()
      data.append('file', option.file)
      themeApi
        .upload(
          data,
          progressEvent => {
            if (progressEvent.total > 0) {
              progressEvent.percent = (progressEvent.loaded / progressEvent.total) * 100
            }
            this.$log.debug('Uploading percent: ', progressEvent.percent)
            option.onProgress(progressEvent)
          },
          source.token
        )
        .then(response => {
          option.onSuccess(response, option.file)
          this.loadThemes()
        })
        .catch(error => {
          option.onError(error, error.response)
        })

      return {
        abort: () => {
          source.cancel('Upload operation canceled by the user.')
        }
      }
    },
    handleFetching() {
      themeApi.fetching(this.fetchingUrl).then(response => {
        this.$message.success('上传成功')
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

.upload-button {
  position: fixed;
  bottom: 80px;
  right: 20px;
}

.card-container {
  background: #f5f5f5;
}
.card-container > .ant-tabs-card > .ant-tabs-content {
  margin-top: -16px;
}

.card-container > .ant-tabs-card > .ant-tabs-content > .ant-tabs-tabpane {
  background: #fff;
  padding: 16px;
}

.card-container > .ant-tabs-card > .ant-tabs-bar {
  border-color: #fff;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab {
  border-color: transparent;
  background: transparent;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab-active {
  border-color: #fff;
  background: #fff;
}
.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}
</style>
