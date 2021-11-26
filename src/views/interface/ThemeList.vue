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
                <div @click="handleOpenThemeSettingDrawer(item)">
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
                      从主题包更新
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
    <a-modal
      v-model="localUpdateModel.visible"
      :afterClose="onThemeInstallModalClose"
      :footer="null"
      destroyOnClose
      title="更新主题"
    >
      <FilePondUpload
        ref="updateByFile"
        :accepts="['application/x-zip', 'application/x-zip-compressed', 'application/zip']"
        :field="localUpdateModel.selected.id"
        :multiple="false"
        :uploadHandler="localUpdateModel.uploadHandler"
        label="点击选择主题更新包或将主题更新包拖拽到此处<br>仅支持 ZIP 格式的文件"
        name="file"
        @success="handleUploadSucceed"
      ></FilePondUpload>
    </a-modal>
    <a-modal
      v-model="themeDeleteModal.visible"
      :afterClose="onThemeDeleteModalClose"
      :closable="false"
      :width="416"
      destroyOnClose
      title="提示"
    >
      <template slot="footer">
        <a-button @click="themeDeleteModal.visible = false">
          取消
        </a-button>
        <ReactiveButton
          :errored="themeDeleteModal.deleteErrored"
          :loading="themeDeleteModal.deleting"
          erroredText="删除失败"
          loadedText="删除成功"
          text="确定"
          @callback="handleDeleteThemeCallback"
          @click="handleDeleteTheme(themeDeleteModal.selected.id, themeDeleteModal.deleteSettings)"
        ></ReactiveButton>
      </template>
      <p>确定删除【{{ themeDeleteModal.selected.name }}】主题？</p>
      <a-checkbox v-model="themeDeleteModal.deleteSettings">
        同时删除主题配置
      </a-checkbox>
    </a-modal>
  </page-view>
</template>

<script>
import ThemeSettingDrawer from './components/ThemeSettingDrawer'
import { PageView } from '@/layouts'
import apiClient from '@/utils/api-client'

export default {
  components: {
    PageView,
    ThemeSettingDrawer
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

      localUpdateModel: {
        visible: false,
        uploadHandler: (file, options, field) => apiClient.theme.updateByUpload(file, options, field),
        selected: {}
      },

      themeDeleteModal: {
        visible: false,
        deleteSettings: false,
        selected: {},
        deleting: false,
        deleteErrored: false
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
  destroyed() {
    this.$log.debug('Theme list destroyed.')
    this.themeSettingDrawer.visible = false
    this.installModal.visible = false
    this.localUpdateModel.visible = false
    this.themeDeleteModal.visible = false
  },
  beforeRouteLeave(to, from, next) {
    this.themeSettingDrawer.visible = false
    this.installModal.visible = false
    this.localUpdateModel.visible = false
    this.themeDeleteModal.visible = false
    next()
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
    handleDeleteTheme(themeId, deleteSettings) {
      this.themeDeleteModal.deleting = true
      apiClient.theme
        .delete(themeId, deleteSettings)
        .catch(() => {
          this.themeDeleteModal.deleteErrored = false
        })
        .finally(() => {
          setTimeout(() => {
            this.themeDeleteModal.deleting = false
          }, 400)
        })
    },
    handleDeleteThemeCallback() {
      if (this.themeDeleteModal.deleteErrored) {
        this.themeDeleteModal.deleteErrored = false
      } else {
        this.themeDeleteModal.visible = false
        this.handleListThemes()
      }
    },
    handleUploadSucceed() {
      this.installModal.visible = false
      this.localUpdateModel.visible = false
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
      this.localUpdateModel.selected = item
      this.localUpdateModel.visible = true
    },
    handleOpenThemeSettingDrawer(theme) {
      this.themeSettingDrawer.selected = theme
      this.themeSettingDrawer.visible = true
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
      if (this.$refs.updateByFile) {
        this.$refs.updateByFile.handleClearFileList()
      }
      this.installModal.remote.url = null
      this.handleListThemes()
    },
    onThemeSettingsDrawerClose() {
      this.themeSettingDrawer.visible = false
      this.themeSettingDrawer.selected = {}
    },
    onThemeDeleteModalClose() {
      this.themeDeleteModal.visible = false
      this.themeDeleteModal.deleteSettings = false
      this.themeDeleteModal.selected = {}
    }
  }
}
</script>
