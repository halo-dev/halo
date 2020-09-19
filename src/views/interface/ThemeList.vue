<template>
  <page-view
    :title="activatedTheme?activatedTheme.name:'无'"
    subTitle="当前启用"
  >
    <template slot="extra">
      <a-button
        key="2"
        icon="reload"
        :loading="themeLoading"
        @click="handleReload"
      >
        刷新
      </a-button>
      <a-button
        key="1"
        type="primary"
        icon="plus"
        @click="uploadThemeVisible = true"
      >
        安装
      </a-button>
    </template>
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
              <div class="theme-screenshot">
                <img
                  :alt="item.name"
                  :src="item.screenshots || '/images/placeholder.jpg'"
                  loading="lazy"
                />
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
                  @click="handleActiveTheme(item)"
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

    <ThemeSettingDrawer
      :theme="selectedTheme"
      v-model="themeSettingVisible"
      @close="onThemeSettingsClose"
    />

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
            ></FilePondUpload>
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
            <a-form
              v-if="!fetchBranches"
              layout="vertical"
            >
              <a-form-item label="远程地址：">
                <a-input v-model="fetchingUrl" />
              </a-form-item>
              <a-form-item>
                <a-button
                  type="primary"
                  @click="handleFetching"
                  :loading="fetchButtonLoading"
                >获取</a-button>
              </a-form-item>
            </a-form>
            <a-tabs v-else>
              <a-tab-pane
                tab="稳定版"
                key="1"
              >
                <a-form layout="vertical">
                  <a-form-item>
                    <a-select
                      style="width: 120px"
                      @change="onSelectChange"
                    >
                      <a-select-option
                        v-for="(item, index) in releases"
                        :key="index"
                        :value="index"
                      >{{ item.branch }}</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item>
                    <a-button
                      type="primary"
                      @click="handleReleaseFetching"
                    >下载</a-button>
                  </a-form-item>
                </a-form>
              </a-tab-pane>
              <a-tab-pane
                tab="开发版"
                key="2"
              >
                <a-form layout="vertical">
                  <a-form-item>
                    <a-select
                      style="width: 120px"
                      @change="onSelectChange"
                    >
                      <a-select-option
                        v-for="(item, index) in branches"
                        :key="index"
                        :value="index"
                      >{{ item.branch }}</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item>
                    <a-button
                      type="primary"
                      @click="handleBranchFetching"
                    >下载</a-button>
                  </a-form-item>
                </a-form>
              </a-tab-pane>
            </a-tabs>

            <a-alert
              type="info"
              closable
            >
              <template slot="message">
                远程地址即主题仓库地址，使用这种方式安装的一般为开发版本，请谨慎使用。
                <br />更多主题请访问：
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
      ></FilePondUpload>
    </a-modal>
  </page-view>
</template>

<script>
import ThemeSettingDrawer from './components/ThemeSettingDrawer'
import { PageView } from '@/layouts'
import themeApi from '@/api/theme'

export default {
  components: {
    PageView,
    ThemeSettingDrawer,
  },
  data() {
    return {
      themeLoading: false,
      uploadThemeVisible: false,
      uploadNewThemeVisible: false,
      fetchButtonLoading: false,
      fetchBranches: false,
      themes: [],
      branches: [],
      releases: [],
      themeSettingVisible: false,
      selectedTheme: {},
      selectedBranch: null,
      fetchingUrl: null,
      uploadHandler: themeApi.upload,
      updateByUploadHandler: themeApi.updateByUpload,
      prepareUpdateTheme: {},
      activatedTheme: null,
    }
  },
  computed: {
    sortedThemes() {
      const data = this.themes.slice(0)
      return data.sort((a, b) => {
        return b.activated - a.activated
      })
    },
  },
  created() {
    this.handleListThemes()
    this.handleGetActivatedTheme()
  },
  destroyed: function() {
    this.$log.debug('Theme list destroyed.')
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
    handleGetActivatedTheme() {
      themeApi.getActivatedTheme().then((response) => {
        this.activatedTheme = response.data.data
      })
    },
    handleListThemes() {
      this.themeLoading = true
      themeApi
        .listAll()
        .then((response) => {
          this.themes = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.themeLoading = false
          }, 200)
        })
    },
    handleActiveTheme(theme) {
      themeApi
        .active(theme.id)
        .finally(() => {
          this.handleListThemes()
        })
        .finally(() => {
          this.handleGetActivatedTheme()
        })
    },
    handleUpdateTheme(themeId) {
      const hide = this.$message.loading('更新中...', 0)
      themeApi
        .update(themeId)
        .then((response) => {
          this.$message.success('更新成功！')
        })
        .finally(() => {
          hide()
          this.handleListThemes()
        })
    },
    handleDeleteTheme(themeId) {
      themeApi
        .delete(themeId)
        .then((response) => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListThemes()
        })
    },
    handleUploadSuccess() {
      if (this.uploadThemeVisible) {
        this.uploadThemeVisible = false
      }
      if (this.uploadNewThemeVisible) {
        this.uploadNewThemeVisible = false
      }
      if (this.fetchBranches) {
        this.fetchBranches = false
      }
      this.handleListThemes()
    },
    handleEditClick(theme) {
      this.settingDrawer(theme)
    },
    handleFetching() {
      if (!this.fetchingUrl) {
        this.$notification['error']({
          message: '提示',
          description: '远程地址不能为空！',
        })
        return
      }
      this.fetchButtonLoading = true
      themeApi.fetchingBranches(this.fetchingUrl).then((response) => {
        this.branches = response.data.data
        this.fetchBranches = true
      })
      themeApi
        .fetchingReleases(this.fetchingUrl)
        .then((response) => {
          this.releases = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.fetchButtonLoading = false
          }, 400)
        })
    },
    handleBranchFetching() {
      themeApi
        .fetchingBranch(this.fetchingUrl, this.branches[this.selectedBranch].branch)
        .then((response) => {
          this.$message.success('拉取成功')
          this.uploadThemeVisible = false
        })
        .finally(() => {
          this.handleListThemes()
        })
    },
    handleReleaseFetching() {
      themeApi
        .fetchingRelease(this.fetchingUrl, this.releases[this.selectedBranch].branch)
        .then((response) => {
          this.$message.success('拉取成功')
          this.uploadThemeVisible = false
        })
        .finally(() => {
          this.handleListThemes()
        })
    },
    handleReload() {
      themeApi.reload().finally(() => {
        this.handleListThemes()
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
      const _this = this
      this.$confirm({
        title: '提示',
        maskClosable: true,
        content: '确定删除【' + item.name + '】主题？',
        onOk() {
          _this.handleDeleteTheme(item.id)
        },
        onCancel() {},
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
        onCancel() {},
      })
    },
    onSelectChange(value) {
      this.selectedBranch = value
    },
    onThemeUploadClose() {
      if (this.uploadThemeVisible) {
        this.$refs.upload.handleClearFileList()
      }
      if (this.uploadNewThemeVisible) {
        this.$refs.updateByupload.handleClearFileList()
      }
      if (this.fetchBranches) {
        this.fetchBranches = false
      }
      if (this.selectedBranch) {
        this.selectedBranch = null
      }
      this.handleListThemes()
    },
    onThemeSettingsClose() {
      this.themeSettingVisible = false
      this.selectedTheme = {}
    },
  },
}
</script>
