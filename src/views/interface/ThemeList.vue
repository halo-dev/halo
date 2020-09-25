<template>
  <page-view
    :title="activatedTheme?activatedTheme.name:'无'"
    subTitle="当前启用"
  >
    <template slot="extra">
      <a-button
        icon="reload"
        :loading="list.loading"
        @click="handleRefreshThemesCache"
      >
        刷新
      </a-button>
      <a-button
        type="primary"
        icon="plus"
        @click="installModal.visible = true"
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
          :loading="list.loading"
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
                <div @click="handleOpenThemeSettingDrawer(item)">
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
                      @click="handleConfirmRemoteUpdate(item)"
                    >
                      <a-icon
                        type="cloud"
                        style="margin-right:3px"
                      />在线更新
                    </a-menu-item>
                    <a-menu-item
                      :key="3"
                      @click="handleOpenLocalUpdateModal(item)"
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
      :theme="themeSettingDrawer.selected"
      v-model="themeSettingDrawer.visible"
      @close="onThemeSettingsDrawerClose"
    />

    <a-modal
      title="安装主题"
      v-model="installModal.visible"
      destroyOnClose
      :footer="null"
      :bodyStyle="{ padding: '0 24px 24px' }"
      :afterClose="onThemeInstallModalClose"
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
              :uploadHandler="installModal.local.uploadHandler"
              @success="handleUploadSucceed"
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
            <a-form-model
              ref="remoteInstallForm"
              :model="installModal.remote"
              :rules="installModal.remote.rules"
              layout="vertical"
            >
              <a-form-model-item
                prop="url"
                label="Github 仓库地址："
              >
                <a-input-search
                  v-model="installModal.remote.url"
                  enter-button="获取版本"
                  @search="handleFetching"
                  :loading="installModal.remote.repoFetching"
                />
              </a-form-model-item>
              <a-form-model-item label="版本类型：">
                <a-select v-model="installModal.remote.byBranchOrRelease">
                  <a-select-option value="release">发行版</a-select-option>
                  <a-select-option value="branch">开发版</a-select-option>
                </a-select>
              </a-form-model-item>
              <a-form-model-item
                label="版本："
                v-show="installModal.remote.byBranchOrRelease ==='release'"
              >
                <a-select
                  v-model="installModal.remote.selectedRelease"
                  :loading="installModal.remote.repoFetching"
                >
                  <a-select-option
                    v-for="(item, index) in installModal.remote.releases"
                    :key="index"
                    :value="item.branch"
                  >{{ item.branch }}</a-select-option>
                </a-select>
              </a-form-model-item>
              <a-form-model-item
                label="分支："
                v-show="installModal.remote.byBranchOrRelease ==='branch'"
              >
                <a-select
                  v-model="installModal.remote.selectedBranch"
                  :loading="installModal.remote.repoFetching"
                >
                  <a-select-option
                    v-for="(item, index) in installModal.remote.branches"
                    :key="index"
                    :value="item.branch"
                  >{{ item.branch }}</a-select-option>
                </a-select>
              </a-form-model-item>
              <a-form-model-item v-show="installModal.remote.byBranchOrRelease ==='release'">
                <ReactiveButton
                  :disabled="!installModal.remote.selectedRelease"
                  type="primary"
                  @click="handleReleaseDownloading"
                  @callback="handleReleaseDownloadedCallback"
                  :loading="installModal.remote.releaseDownloading"
                  :errored="installModal.remote.releaseDownloadErrored"
                  text="下载"
                  loadedText="下载成功"
                  erroredText="下载失败"
                ></ReactiveButton>
              </a-form-model-item>
              <a-form-model-item v-show="installModal.remote.byBranchOrRelease ==='branch'">
                <ReactiveButton
                  :disabled="!installModal.remote.selectedBranch"
                  type="primary"
                  @click="handleBranchPulling"
                  @callback="handleBranchPulledCallback"
                  :loading="installModal.remote.branchPulling"
                  :errored="installModal.remote.branchPullErrored"
                  text="下载"
                  loadedText="下载成功"
                  erroredText="下载失败"
                ></ReactiveButton>
              </a-form-model-item>
            </a-form-model>
            <a-alert
              type="info"
              closable
            >
              <template slot="message">
                远程地址即主题仓库地址，建议使用发行版本。更多主题请访问：
                <a
                  target="_blank"
                  href="https://halo.run/p/themes.html"
                >https://halo.run/p/themes</a>
              </template>
            </a-alert>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
    <a-modal
      title="更新主题"
      v-model="localUpdateModel.visible"
      :footer="null"
      destroyOnClose
      :afterClose="onThemeInstallModalClose"
    >
      <FilePondUpload
        ref="updateByupload"
        name="file"
        accept="application/zip"
        label="点击选择主题更新包或将主题更新包拖拽到此处<br>仅支持 ZIP 格式的文件"
        :uploadHandler="localUpdateModel.uploadHandler"
        :filed="localUpdateModel.selected.id"
        :multiple="false"
        @success="handleUploadSucceed"
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
      list: {
        loading: false,
        data: [],
      },

      installModal: {
        visible: false,
        local: {
          uploadHandler: themeApi.upload,
        },

        remote: {
          url: null,

          repoFetching: false,
          repoFetchErrored: false,

          branches: [],
          selectedBranch: null,
          branchPulling: false,
          branchPullErrored: false,

          releases: [],
          selectedRelease: null,
          releaseDownloading: false,
          releaseDownloadErrored: false,

          byBranchOrRelease: 'release', // release or branch, default is release

          rules: {
            url: [{ required: true, message: '* Github 仓库地址不能为空', trigger: ['change'] }],
          },
        },
      },

      localUpdateModel: {
        visible: false,
        uploadHandler: themeApi.updateByUpload,
        selected: {},
      },

      themeSettingDrawer: {
        visible: false,
        selected: {},
      },
    }
  },
  computed: {
    sortedThemes() {
      const data = this.list.data.slice(0)
      return data.sort((a, b) => {
        return b.activated - a.activated
      })
    },
    activatedTheme() {
      if (this.sortedThemes.length > 0) {
        return this.sortedThemes[0]
      }
      return null
    },
  },
  beforeMount() {
    this.handleListThemes()
  },
  destroyed() {
    this.$log.debug('Theme list destroyed.')
    this.themeSettingDrawer.visible = false
    this.installModal.visible = false
    this.localUpdateModel.visible = false
  },
  beforeRouteLeave(to, from, next) {
    this.themeSettingDrawer.visible = false
    this.installModal.visible = false
    this.localUpdateModel.visible = false
    next()
  },
  methods: {
    handleListThemes() {
      this.list.loading = true
      themeApi
        .listAll()
        .then((response) => {
          this.list.data = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.list.loading = false
          }, 200)
        })
    },
    handleRefreshThemesCache() {
      themeApi.reload().finally(() => {
        this.handleListThemes()
      })
    },
    handleActiveTheme(theme) {
      themeApi.active(theme.id).finally(() => {
        this.handleListThemes()
      })
    },
    handleDeleteTheme(themeId) {
      themeApi.delete(themeId).finally(() => {
        this.handleListThemes()
      })
    },
    handleUploadSucceed() {
      this.installModal.visible = false
      this.localUpdateModel.visible = false
      this.handleListThemes()
    },
    handleFetching() {
      const _this = this
      _this.$refs.remoteInstallForm.validate((valid) => {
        if (valid) {
          _this.installModal.remote.repoFetching = true
          themeApi.fetchingBranches(_this.installModal.remote.url).then((response) => {
            const branches = response.data.data
            _this.installModal.remote.branches = branches
            if (branches && branches.length > 0) {
              _this.installModal.remote.selectedBranch = branches[0].branch
            }
          })
          themeApi
            .fetchingReleases(_this.installModal.remote.url)
            .then((response) => {
              const releases = response.data.data
              _this.installModal.remote.releases = releases
              if (releases && releases.length > 0) {
                _this.installModal.remote.selectedRelease = releases[0].branch
              }
            })
            .finally(() => {
              setTimeout(() => {
                _this.installModal.remote.repoFetching = false
              }, 400)
            })
        }
      })
    },
    handleBranchPulling() {
      this.installModal.remote.branchPulling = true
      themeApi
        .fetchingBranch(this.installModal.remote.url, this.installModal.remote.selectedBranch)
        .catch(() => {
          this.installModal.remote.branchPullErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.installModal.remote.branchPulling = false
          }, 400)
        })
    },
    handleBranchPulledCallback() {
      if (this.installModal.remote.branchPullErrored) {
        this.installModal.remote.branchPullErrored = false
      } else {
        this.installModal.visible = false
        this.handleListThemes()
      }
    },
    handleReleaseDownloading() {
      this.installModal.remote.releaseDownloading = true
      themeApi
        .fetchingRelease(this.installModal.remote.url, this.installModal.remote.selectedRelease)
        .catch(() => {
          this.installModal.remote.branchPullErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.installModal.remote.releaseDownloading = false
          }, 400)
        })
    },
    handleReleaseDownloadedCallback() {
      if (this.installModal.remote.releaseDownloadErrored) {
        this.installModal.remote.releaseDownloadErrored = false
      } else {
        this.installModal.visible = false
        this.handleListThemes()
      }
    },
    handleOpenLocalUpdateModal(item) {
      this.localUpdateModel.selected = item
      this.localUpdateModel.visible = true
    },
    handleOpenThemeSettingDrawer(theme) {
      this.themeSettingDrawer.selected = theme
      this.themeSettingDrawer.visible = true
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
    handleConfirmRemoteUpdate(item) {
      const _this = this
      _this.$confirm({
        title: '提示',
        maskClosable: true,
        content: '确定更新【' + item.name + '】主题？',
        onOk() {
          const hide = _this.$message.loading('更新中...', 0)
          themeApi
            .update(item.id)
            .then((response) => {
              _this.$message.success('更新成功！')
            })
            .finally(() => {
              hide()
              _this.handleListThemes()
            })
        },
        onCancel() {},
      })
    },
    onThemeInstallModalClose() {
      if (this.$refs.upload) {
        this.$refs.upload.handleClearFileList()
      }
      if (this.$refs.updateByupload) {
        this.$refs.updateByupload.handleClearFileList()
      }
      this.installModal.remote.branches = []
      this.installModal.remote.selectedBranch = null
      this.installModal.remote.releases = []
      this.installModal.remote.selectedRelease = null
      this.installModal.remote.url = null
      this.installModal.remote.byBranchOrRelease = 'release'
      this.handleListThemes()
    },
    onThemeSettingsDrawerClose() {
      this.themeSettingDrawer.visible = false
      this.themeSettingDrawer.selected = {}
    },
  },
}
</script>
