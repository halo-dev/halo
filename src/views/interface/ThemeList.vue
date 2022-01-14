<template>
  <page-view :title="activatedTheme ? activatedTheme.name : '无'" affix subTitle="当前启用">
    <template slot="extra">
      <a-button :loading="list.loading" icon="reload" @click="handleRefreshThemesCache">
        刷新
      </a-button>
      <a-button icon="plus" type="primary" @click="installModal.visible = true">
        安装
      </a-button>
    </template>
    <a-row :gutter="12" align="middle" type="flex">
      <a-col :span="24">
        <a-list
          :dataSource="sortedThemes"
          :grid="{ gutter: 12, xs: 1, sm: 1, md: 2, lg: 4, xl: 4, xxl: 4 }"
          :loading="list.loading"
        >
          <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
            <a-card :bodyStyle="{ padding: 0 }" :title="item.name" hoverable>
              <div class="theme-screenshot">
                <img :alt="item.name" :src="item.screenshots || '/images/placeholder.jpg'" loading="lazy" />
              </div>
              <template slot="actions" class="ant-card-actions">
                <div v-if="item.activated">
                  <a-icon style="margin-right:3px" theme="twoTone" type="unlock" />
                  已启用
                </div>
                <div v-else @click="handleActiveTheme(item)">
                  <a-icon style="margin-right:3px" type="lock" />
                  启用
                </div>
                <div @click="handleRouteToThemeSetting(item)">
                  <a-icon style="margin-right:3px" type="setting" />
                  设置
                </div>
                <a-dropdown :trigger="['click']" placement="topCenter">
                  <a class="ant-dropdown-link" href="#">
                    <a-icon style="margin-right:3px" type="ellipsis" />
                    更多
                  </a>
                  <a-menu slot="overlay">
                    <a-menu-item :key="1" :disabled="item.activated" @click="handleOpenThemeDeleteModal(item)">
                      <a-icon style="margin-right:3px" type="delete" />
                      删除
                    </a-menu-item>
                    <a-menu-item v-if="item.repo" :key="2" @click="handleConfirmRemoteUpdate(item)">
                      <a-icon style="margin-right:3px" type="cloud" />
                      在线更新
                    </a-menu-item>
                    <a-menu-item :key="3" @click="handleOpenLocalUpdateModal(item)">
                      <a-icon style="margin-right:3px" type="file" />
                      本地更新
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
      v-model="themeSettingDrawer.visible"
      :theme="themeSettingDrawer.selected"
      @close="onThemeSettingsDrawerClose"
    />

    <a-modal
      v-model="installModal.visible"
      :afterClose="onThemeInstallModalClose"
      :bodyStyle="{ padding: '0 24px 24px' }"
      :footer="null"
      destroyOnClose
      title="安装主题"
    >
      <div class="custom-tab-wrapper">
        <a-tabs :animated="{ inkBar: true, tabPane: false }">
          <a-tab-pane key="1" tab="本地上传">
            <FilePondUpload
              ref="upload"
              :accepts="['application/x-zip', 'application/x-zip-compressed', 'application/zip']"
              :uploadHandler="installModal.local.uploadHandler"
              label="点击选择主题包或将主题包拖拽到此处<br>仅支持 ZIP 格式的文件"
              name="file"
              @success="handleUploadSucceed"
            ></FilePondUpload>
            <a-alert closable type="info">
              <template slot="message">
                更多主题请访问：
                <a href="https://halo.run/themes.html" target="_blank">https://halo.run/themes</a>
              </template>
            </a-alert>
          </a-tab-pane>
          <a-tab-pane key="2" tab="远程下载">
            <a-form-model
              ref="remoteInstallForm"
              :model="installModal.remote"
              :rules="installModal.remote.rules"
              layout="vertical"
            >
              <a-form-model-item help="* 支持 Git 仓库地址，ZIP 链接。" label="远程地址：" prop="url">
                <a-input v-model="installModal.remote.url" />
              </a-form-model-item>
              <a-form-model-item>
                <ReactiveButton
                  :errored="installModal.remote.fetchErrored"
                  :loading="installModal.remote.fetching"
                  erroredText="下载失败"
                  loadedText="下载成功"
                  text="下载"
                  type="primary"
                  @callback="handleRemoteFetchCallback"
                  @click="handleRemoteFetching"
                ></ReactiveButton>
              </a-form-model-item>
            </a-form-model>
            <a-alert closable type="info">
              <template slot="message">
                目前仅支持远程 Git 仓库和 ZIP 下载链接。更多主题请访问：
                <a href="https://halo.run/themes.html" target="_blank">https://halo.run/themes</a>
              </template>
            </a-alert>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>

    <ThemeDeleteConfirmModal
      :theme="themeDeleteModal.selected"
      :visible.sync="themeDeleteModal.visible"
      @onAfterClose="themeDeleteModal.selected = {}"
      @success="handleListThemes"
    />

    <ThemeLocalUpgradeModal
      :theme="localUpgradeModel.selected"
      :visible.sync="localUpgradeModel.visible"
      @onAfterClose="localUpgradeModel.selected = {}"
      @success="handleListThemes"
    />
  </page-view>
</template>

<script>
import ThemeSettingDrawer from './components/ThemeSettingDrawer'
import ThemeDeleteConfirmModal from './components/ThemeDeleteConfirmModal'
import ThemeLocalUpgradeModal from './components/ThemeLocalUpgradeModal'
import { PageView } from '@/layouts'
import apiClient from '@/utils/api-client'

export default {
  components: {
    PageView,
    ThemeSettingDrawer,
    ThemeDeleteConfirmModal,
    ThemeLocalUpgradeModal
  },
  data() {
    return {
      list: {
        loading: false,
        data: []
      },

      installModal: {
        visible: false,
        local: {
          uploadHandler: (file, options) => apiClient.theme.upload(file, options)
        },

        remote: {
          url: null,

          fetching: false,
          fetchErrored: false,

          rules: {
            url: [{ required: true, message: '* 远程地址不能为空', trigger: ['change'] }]
          }
        }
      },

      localUpgradeModel: {
        visible: false,
        selected: {}
      },

      themeDeleteModal: {
        visible: false,
        selected: {}
      },

      themeSettingDrawer: {
        visible: false,
        selected: {}
      }
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
    }
  },
  beforeMount() {
    this.handleListThemes()
  },
  methods: {
    handleListThemes() {
      this.list.loading = true
      apiClient.theme
        .list()
        .then(response => {
          this.list.data = response.data
        })
        .finally(() => {
          this.list.loading = false
        })
    },
    handleRefreshThemesCache() {
      apiClient.theme.reload().finally(() => {
        this.handleListThemes()
      })
    },
    handleActiveTheme(theme) {
      apiClient.theme.active(theme.id).finally(() => {
        this.handleListThemes()
      })
    },
    handleUploadSucceed() {
      this.installModal.visible = false
      this.handleListThemes()
    },
    handleRemoteFetching() {
      this.$refs.remoteInstallForm.validate(valid => {
        if (valid) {
          this.installModal.remote.fetching = true
          apiClient.theme
            .fetchTheme(this.installModal.remote.url)
            .catch(() => {
              this.installModal.remote.fetchErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.installModal.remote.fetching = false
              }, 400)
            })
        }
      })
    },
    handleRemoteFetchCallback() {
      if (this.installModal.remote.fetchErrored) {
        this.installModal.remote.fetchErrored = false
      } else {
        this.installModal.visible = false
        this.handleListThemes()
      }
    },
    handleOpenLocalUpdateModal(item) {
      this.localUpgradeModel.selected = item
      this.localUpgradeModel.visible = true
    },
    handleRouteToThemeSetting(theme) {
      this.$router.push({ name: 'ThemeSetting', query: { themeId: theme.id } })
    },
    handleOpenThemeDeleteModal(item) {
      this.themeDeleteModal.visible = true
      this.themeDeleteModal.selected = item
    },
    handleConfirmRemoteUpdate(item) {
      const _this = this
      _this.$confirm({
        title: '提示',
        maskClosable: true,
        content: '确定更新【' + item.name + '】主题？',
        onOk() {
          const hide = _this.$message.loading('更新中...', 0)
          apiClient.theme
            .updateThemeByFetching(item.id)
            .then(() => {
              _this.$message.success('更新成功！')
            })
            .finally(() => {
              hide()
              _this.handleListThemes()
            })
        }
      })
    },
    onThemeInstallModalClose() {
      if (this.$refs.upload) {
        this.$refs.upload.handleClearFileList()
      }
      this.installModal.remote.url = null
      this.handleListThemes()
    },
    onThemeSettingsDrawerClose() {
      this.themeSettingDrawer.visible = false
      this.themeSettingDrawer.selected = {}
    }
  }
}
</script>
