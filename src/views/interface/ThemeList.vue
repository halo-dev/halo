<template>
  <div>
    <a-row
      :gutter="12"
      type="flex"
      align="middle"
    >
      <a-col :span="24">
        <a-list
          :grid="{ gutter: 12, xs: 1, sm: 1, md: 2, lg: 4, xl: 4, xxl: 4 }"
          :dataSource="sortedThemes"
          :loading="themeLoading"
        >
          <a-list-item
            slot="renderItem"
            slot-scope="item, index"
            :key="index"
          >
            <a-card
              hoverable
              :title="item.name"
              :bodyStyle="{ padding: 0 }"
            >
              <div class="theme-thumb">
                <img
                  :alt="item.name"
                  :src="item.screenshots || '/images/placeholder.jpg'"
                  loading="lazy"
                >
              </div>
              <template
                class="ant-card-actions"
                slot="actions"
              >
                <div v-if="item.activated">
                  <a-icon
                    type="unlock"
                    theme="twoTone"
                    style="margin-right:3px"
                  />已启用
                </div>
                <div
                  v-else
                  @click="handleActivateClick(item)"
                >
                  <a-icon
                    type="lock"
                    style="margin-right:3px"
                  />启用
                </div>
                <div @click="handleShowThemeSetting(item)">
                  <a-icon
                    type="setting"
                    style="margin-right:3px"
                  />设置
                </div>
                <a-dropdown
                  placement="topCenter"
                  :trigger="['click']"
                >
                  <a
                    class="ant-dropdown-link"
                    href="#"
                  >
                    <a-icon
                      type="ellipsis"
                      style="margin-right:3px"
                    />更多
                  </a>
                  <a-menu slot="overlay">
                    <a-menu-item
                      :key="1"
                      :disabled="item.activated"
                      @click="handleConfirmDelete(item)"
                    >
                      <a-icon
                        type="delete"
                        style="margin-right:3px"
                      />删除
                    </a-menu-item>
                    <a-menu-item
                      :key="2"
                      v-if="item.repo"
                      @click="handleConfirmUpdate(item)"
                    >
                      <a-icon
                        type="cloud"
                        style="margin-right:3px"
                      />在线更新
                    </a-menu-item>
                    <a-menu-item
                      :key="3"
                      @click="handleShowUpdateNewThemeModal(item)"
                    >
                      <a-icon
                        type="file"
                        style="margin-right:3px"
                      />从主题包更新
                    </a-menu-item>
                  </a-menu>
                </a-dropdown>
              </template>
            </a-card>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>

    <ThemeSetting
      :theme="selectedTheme"
      v-if="themeSettingVisible"
      @close="onThemeSettingsClose"
    />

    <div class="upload-button">
      <a-dropdown
        placement="topLeft"
        :trigger="['click']"
      >
        <a-button
          type="primary"
          shape="circle"
          icon="plus"
          size="large"
        ></a-button>
        <a-menu slot="overlay">
          <a-menu-item>
            <a
              rel="noopener noreferrer"
              href="javascript:void(0);"
              @click="uploadThemeVisible = true"
            >安装主题</a>
          </a-menu-item>
          <a-menu-item>
            <a
              rel="noopener noreferrer"
              href="javascript:void(0);"
              @click="handleReload"
            >刷新列表</a>
          </a-menu-item>
        </a-menu>
      </a-dropdown>
    </div>
    <a-modal
      title="安装主题"
      v-model="uploadThemeVisible"
      destroyOnClose
      :footer="null"
      :bodyStyle="{ padding: '0 24px 24px' }"
      :afterClose="onThemeUploadClose"
    >
      <div class="custom-tab-wrapper">
        <a-tabs>
          <a-tab-pane
            tab="本地上传"
            key="1"
          >
            <FilePondUpload
              ref="upload"
              name="file"
              accept="application/zip"
              label="点击选择主题包或将主题包拖拽到此处<br>仅支持 ZIP 格式的文件"
              :uploadHandler="uploadHandler"
              @success="handleUploadSuccess"
            >
            </FilePondUpload>
            <a-alert
              type="info"
              closable
            >
              <template slot="message">
                更多主题请访问：
                <a
                  target="_blank"
                  href="https://halo.run/p/themes"
                >https://halo.run/p/themes</a>
              </template>
            </a-alert>
          </a-tab-pane>
          <a-tab-pane
            tab="远程拉取"
            key="2"
          >
            <a-form layout="vertical">
              <a-form-item label="远程地址：">
                <a-input v-model="fetchingUrl" />
              </a-form-item>
              <a-form-item>
                <a-button
                  type="primary"
                  @click="handleFetching"
                  :loading="fetchButtonLoading"
                >下载</a-button>
              </a-form-item>
            </a-form>
            <a-alert
              type="info"
              closable
            >
              <template slot="message">
                远程地址即主题仓库地址，使用这种方式安装的一般为开发版本，请谨慎使用。
                <br>更多主题请访问：
                <a
                  target="_blank"
                  href="https://halo.run/p/themes"
                >https://halo.run/p/themes</a>
              </template>
            </a-alert>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
    <a-modal
      title="更新主题"
      v-model="uploadNewThemeVisible"
      :footer="null"
      destroyOnClose
      :afterClose="onThemeUploadClose"
    >
      <FilePondUpload
        ref="updateByupload"
        name="file"
        accept="application/zip"
        label="点击选择主题更新包或将主题更新包拖拽到此处<br>仅支持 ZIP 格式的文件"
        :uploadHandler="updateByUploadHandler"
        :filed="prepareUpdateTheme.id"
        :multiple="false"
        @success="handleUploadSuccess"
      >
      </FilePondUpload>
    </a-modal>
  </div>
</template>

<script>
import ThemeSetting from './components/ThemeSetting'
import themeApi from '@/api/theme'
export default {
  components: {
    ThemeSetting
  },
  data() {
    return {
      themeLoading: false,
      uploadThemeVisible: false,
      uploadNewThemeVisible: false,
      fetchButtonLoading: false,
      themes: [],
      themeSettingVisible: false,
      selectedTheme: {},
      fetchingUrl: null,
      uploadHandler: themeApi.upload,
      updateByUploadHandler: themeApi.updateByUpload,
      prepareUpdateTheme: {}
    }
  },
  computed: {
    sortedThemes() {
      const data = this.themes.slice(0)
      return data.sort(function(a, b) {
        return b.activated - a.activated
      })
    }
  },
  created() {
    this.loadThemes()
  },
  destroyed: function() {
    if (this.themeSettingVisible) {
      this.themeSettingVisible = false
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.themeSettingVisible) {
      this.themeSettingVisible = false
    }
    next()
  },
  methods: {
    loadThemes() {
      this.themeLoading = true
      themeApi.listAll().then(response => {
        this.themes = response.data.data
        this.themeLoading = false
      })
    },

    activeTheme(themeId) {
      themeApi.active(themeId).then(response => {
        this.$message.success('设置成功！')
        this.loadThemes()
      })
    },
    handleUpdateTheme(themeId) {
      const hide = this.$message.loading('更新中...', 0)
      themeApi
        .update(themeId)
        .then(response => {
          this.$message.success('更新成功！')
          this.loadThemes()
        })
        .finally(() => {
          hide()
        })
    },
    handleDeleteTheme(themeId) {
      themeApi.delete(themeId).then(response => {
        this.$message.success('删除成功！')
        this.loadThemes()
      })
    },
    handleUploadSuccess() {
      if (this.uploadThemeVisible) {
        this.uploadThemeVisible = false
      }
      if (this.uploadNewThemeVisible) {
        this.uploadNewThemeVisible = false
      }
      this.loadThemes()
    },
    handleEditClick(theme) {
      this.settingDrawer(theme)
    },
    handleActivateClick(theme) {
      this.activeTheme(theme.id)
    },
    handleFetching() {
      if (!this.fetchingUrl) {
        this.$notification['error']({
          message: '提示',
          description: '远程地址不能为空！'
        })
        return
      }
      this.fetchButtonLoading = true
      themeApi
        .fetching(this.fetchingUrl)
        .then(response => {
          this.$message.success('拉取成功！')
          this.uploadThemeVisible = false
          this.loadThemes()
        })
        .finally(() => {
          this.fetchButtonLoading = false
        })
    },
    handleReload() {
      themeApi.reload().then(response => {
        this.loadThemes()
        this.$message.success('刷新成功！')
      })
    },
    handleShowUpdateNewThemeModal(item) {
      this.prepareUpdateTheme = item
      this.uploadNewThemeVisible = true
    },
    handleShowThemeSetting(theme) {
      this.selectedTheme = theme
      this.themeSettingVisible = true
    },
    handleConfirmDelete(item) {
      const that = this
      this.$confirm({
        title: '提示',
        maskClosable: true,
        content: '确定删除【' + item.name + '】主题？',
        onOk() {
          that.handleDeleteTheme(item.id)
        },
        onCancel() {}
      })
    },
    handleConfirmUpdate(item) {
      const that = this
      this.$confirm({
        title: '提示',
        maskClosable: true,
        content: '确定更新【' + item.name + '】主题？',
        onOk() {
          that.handleUpdateTheme(item.id)
        },
        onCancel() {}
      })
    },
    onThemeUploadClose() {
      if (this.uploadThemeVisible) {
        this.$refs.upload.handleClearFileList()
      }
      if (this.uploadNewThemeVisible) {
        this.$refs.updateByupload.handleClearFileList()
      }
      this.loadThemes()
    },
    onThemeSettingsClose() {
      this.themeSettingVisible = false
      this.selectedTheme = {}
    }
  }
}
</script>

<style lang="less">
@keyframes scaleDraw {
  0% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.3);
  }
  50% {
    transform: scale(1);
  }
  75% {
    transform: scale(1.3);
  }
}

.upload-button {
  -webkit-animation: scaleDraw 4s ease-in-out infinite;
  position: fixed;
  bottom: 30px;
  right: 30px;
}
.theme-thumb {
  width: 100%;
  margin: 0 auto;
  position: relative;
  padding-bottom: 56%;
  overflow: hidden;
  img {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
  }
}
</style>
