<template>
  <page-view affix :title="activatedTheme ? activatedTheme.name : '无'" subTitle="当前启用">
    <template slot="extra">
      <a-button icon="reload" :loading="list.loading" @click="handleRefreshThemesCache">
        刷新
      </a-button>
      <a-button type="primary" icon="plus" @click="installModal.visible = true">
        安装
      </a-button>
    </template>
    <a-row :gutter="12" type="flex" align="middle">
      <a-col :span="24">
        <a-list
          :grid="{ gutter: 12, xs: 1, sm: 1, md: 2, lg: 4, xl: 4, xxl: 4 }"
          :dataSource="sortedThemes"
          :loading="list.loading"
        >
          <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
            <a-card hoverable :title="item.name" :bodyStyle="{ padding: 0 }">
              <div class="theme-screenshot">
                <img :alt="item.name" :src="item.screenshots || '/images/placeholder.jpg'" loading="lazy" />
              </div>
              <template class="ant-card-actions" slot="actions">
                <div v-if="item.activated"><a-icon type="unlock" theme="twoTone" style="margin-right:3px" />已启用</div>
                <div v-else @click="handleActiveTheme(item)"><a-icon type="lock" style="margin-right:3px" />启用</div>
                <div @click="handleOpenThemeSettingDrawer(item)">
                  <a-icon type="setting" style="margin-right:3px" />设置
                </div>
                <a-dropdown placement="topCenter" :trigger="['click']">
                  <a class="ant-dropdown-link" href="#"> <a-icon type="ellipsis" style="margin-right:3px" />更多 </a>
                  <a-menu slot="overlay">
                    <a-menu-item :key="1" :disabled="item.activated" @click="handleOpenThemeDeleteModal(item)">
                      <a-icon type="delete" style="margin-right:3px" />删除
                    </a-menu-item>
                    <a-menu-item :key="2" v-if="item.repo" @click="handleConfirmRemoteUpdate(item)">
                      <a-icon type="cloud" style="margin-right:3px" />在线更新
                    </a-menu-item>
                    <a-menu-item :key="3" @click="handleOpenLocalUpdateModal(item)">
                      <a-icon type="file" style="margin-right:3px" />从主题包更新
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
          <a-tab-pane tab="本地上传" key="1">
            <FilePondUpload
              ref="upload"
              name="file"
              :accepts="['application/x-zip', 'application/x-zip-compressed', 'application/zip']"
              label="点击选择主题包或将主题包拖拽到此处<br>仅支持 ZIP 格式的文件"
              :uploadHandler="installModal.local.uploadHandler"
              @success="handleUploadSucceed"
            ></FilePondUpload>
            <a-alert type="info" closable>
              <template slot="message">
                更多主题请访问：
                <a target="_blank" href="https://halo.run/themes.html">https://halo.run/themes</a>
              </template>
            </a-alert>
          </a-tab-pane>
          <a-tab-pane tab="远程下载" key="2">
            <a-form-model
              ref="remoteInstallForm"
              :model="installModal.remote"
              :rules="installModal.remote.rules"
              layout="vertical"
            >
              <a-form-model-item prop="url" label="远程地址：" help="* 支持 Git 仓库地址，ZIP 链接。">
                <a-input v-model="installModal.remote.url" />
              </a-form-model-item>
              <a-form-model-item>
                <ReactiveButton
                  type="primary"
                  @click="handleRemoteFetching"
                  @callback="handleRemoteFetchCallback"
                  :loading="installModal.remote.fetching"
                  :errored="installModal.remote.fetchErrored"
                  text="下载"
                  loadedText="下载成功"
                  erroredText="下载失败"
                ></ReactiveButton>
              </a-form-model-item>
            </a-form-model>
            <a-alert type="info" closable>
              <template slot="message">
                目前仅支持远程 Git 仓库和 ZIP 下载链接。更多主题请访问：
                <a target="_blank" href="https://halo.run/themes.html">https://halo.run/themes</a>
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
        :accepts="['application/x-zip', 'application/x-zip-compressed', 'application/zip']"
        label="点击选择主题更新包或将主题更新包拖拽到此处<br>仅支持 ZIP 格式的文件"
        :uploadHandler="localUpdateModel.uploadHandler"
        :filed="localUpdateModel.selected.id"
        :multiple="false"
        @success="handleUploadSucceed"
      ></FilePondUpload>
    </a-modal>
    <a-modal
      title="提示"
      v-model="themeDeleteModal.visible"
      :width="416"
      :closable="false"
      destroyOnClose
      :afterClose="onThemeDeleteModalClose"
    >
      <template slot="footer">
        <a-button @click="themeDeleteModal.visible = false">
          取消
        </a-button>
        <ReactiveButton
          @click="handleDeleteTheme(themeDeleteModal.selected.id, themeDeleteModal.deleteSettings)"
          @callback="handleDeleteThemeCallback"
          :loading="themeDeleteModal.deleting"
          :errored="themeDeleteModal.deleteErrored"
          text="确定"
          loadedText="删除成功"
          erroredText="删除失败"
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
import themeApi from '@/api/theme'

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
          uploadHandler: themeApi.upload
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
        uploadHandler: themeApi.updateByUpload,
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
      themeApi
        .list()
        .then(response => {
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
    handleDeleteTheme(themeId, deleteSettings) {
      this.themeDeleteModal.deleting = true
      themeApi
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
          themeApi
            .fetching(this.installModal.remote.url)
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
          themeApi
            .update(item.id)
            .then(() => {
              _this.$message.success('更新成功！')
            })
            .finally(() => {
              hide()
              _this.handleListThemes()
            })
        },
        onCancel() {}
      })
    },
    onThemeInstallModalClose() {
      if (this.$refs.upload) {
        this.$refs.upload.handleClearFileList()
      }
      if (this.$refs.updateByupload) {
        this.$refs.updateByupload.handleClearFileList()
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
