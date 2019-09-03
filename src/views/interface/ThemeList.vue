<template>
  <div class="page-header-index-wide">
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
                  :src="item.screenshots"
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
                    >
                      <a-popconfirm
                        v-if="!item.activated"
                        :title="'确定删除【' + item.name + '】主题？'"
                        @confirm="handleDeleteTheme(item.id)"
                        okText="确定"
                        cancelText="取消"
                      >
                        <a-icon
                          type="delete"
                          style="margin-right:3px"
                        />删除
                      </a-popconfirm>
                      <span v-else>
                        <a-icon
                          type="delete"
                          style="margin-right:3px"
                        />删除
                      </span>
                    </a-menu-item>
                    <a-menu-item
                      :key="2"
                      v-if="item.repo"
                    >
                      <a-popconfirm
                        :title="'确定更新【' + item.name + '】主题？'"
                        @confirm="handleUpdateTheme(item.id)"
                        okText="确定"
                        cancelText="取消"
                      >
                        <a-icon
                          type="cloud"
                          style="margin-right:3px"
                        />在线更新
                      </a-popconfirm>
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
              @click="()=>this.uploadThemeVisible = true"
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
      :footer="null"
      :bodyStyle="{ padding: '0 24px 24px' }"
    >
      <div class="custom-tab-wrapper">
        <a-tabs>
          <a-tab-pane
            tab="远程拉取"
            key="1"
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
                远程地址即主题仓库地址，如：https://github.com/halo-dev/halo-theme-quick-starter。
                <br>更多主题请访问：
                <a
                  target="_blank"
                  href="https://halo.run/theme"
                >https://halo.run/theme</a>
              </template>
            </a-alert>
          </a-tab-pane>
          <a-tab-pane
            tab="本地上传"
            key="2"
          >
            <upload
              name="file"
              multiple
              accept="application/zip"
              :uploadHandler="uploadHandler"
              @change="handleUploadChange"
              @success="handleUploadSuccess"
            >
              <p class="ant-upload-drag-icon">
                <a-icon type="inbox" />
              </p>
              <p class="ant-upload-text">点击选择主题包或将主题包拖拽到此处</p>
              <p class="ant-upload-hint">支持单个或批量上传，仅支持 ZIP 格式的文件</p>
            </upload>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
    <a-modal
      title="更新主题"
      v-model="uploadNewThemeVisible"
      :footer="null"
    >
      <UpdateTheme
        name="file"
        :themeId="prepareUpdateTheme.id"
        accept="application/zip"
        :uploadHandler="updateByUploadHandler"
        @change="handleNewThemeUploadChange"
        @success="handleUploadSuccess"
      >
        <p class="ant-upload-drag-icon">
          <a-icon type="inbox" />
        </p>
        <p class="ant-upload-text">点击选择主题包或将主题包拖拽到此处</p>
        <p class="ant-upload-hint">请选择最新的主题包，仅支持 ZIP 格式的文件</p>
      </UpdateTheme>
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
      themeApi.update(themeId).then(response => {
        this.$message.success('更新成功！')
        this.loadThemes()
      })
    },
    handleDeleteTheme(key) {
      themeApi.delete(key).then(response => {
        this.$message.success('删除成功！')
        this.loadThemes()
      })
    },
    onThemeSettingsClose() {
      this.themeSettingVisible = false
      this.selectedTheme = {}
    },
    handleUploadChange(info) {
      const status = info.file.status
      if (status === 'done') {
        this.$message.success(`${info.file.name} 主题上传成功！`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 主题上传失败！`)
      }
    },
    handleNewThemeUploadChange(info) {
      const status = info.file.status
      if (status === 'done') {
        this.$message.success(`${info.file.name} 主题更新成功！`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 主题更新失败！`)
      }
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
      console.log(item)
      this.prepareUpdateTheme = item
      this.uploadNewThemeVisible = true
    },
    handleShowThemeSetting(theme) {
      this.selectedTheme = theme
      this.themeSettingVisible = true
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
