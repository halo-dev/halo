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
                <div @click="handleEditClick(item)">
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
                    <a-menu-item :key="2">
                      <a-popconfirm
                        :title="'确定更新【' + item.name + '】主题？'"
                        @confirm="handleUpdateTheme(item.id)"
                        okText="确定"
                        cancelText="取消"
                      >
                        <a-icon
                          type="download"
                          style="margin-right:3px"
                        />更新
                      </a-popconfirm>
                    </a-menu-item>
                  </a-menu>
                </a-dropdown>
              </template>
            </a-card>
          </a-list-item>
        </a-list>
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
            <a-card :bordered="false">
              <img
                :alt="themeProperty.name"
                :src="themeProperty.screenshots"
                slot="cover"
              >
              <a-card-meta :description="themeProperty.description">
                <template slot="title">
                  <a
                    :href="themeProperty.author.website"
                    target="_blank"
                  >{{ themeProperty.author.name }}</a>
                </template>
                <a-avatar
                  v-if="themeProperty.logo"
                  :src="themeProperty.logo"
                  size="large"
                  slot="avatar"
                />
                <a-avatar
                  v-else
                  size="large"
                  slot="avatar"
                >{{ themeProperty.author.name }}</a-avatar>
              </a-card-meta>
            </a-card>
          </a-skeleton>
        </a-col>
        <a-col
          :xl="12"
          :lg="12"
          :md="12"
          :sm="24"
          :xs="24"
          style="padding-bottom: 50px;"
        >
          <a-skeleton
            active
            :loading="optionLoading"
            :paragraph="{rows: 20}"
          >
            <div class="card-container">
              <a-tabs
                type="card"
                defaultActiveKey="0"
                v-if="themeConfiguration.length>0"
              >
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
                        :placeholder="item.placeholder"
                        v-if="item.type == 'TEXT'"
                      />
                      <a-input
                        type="textarea"
                        :autosize="{ minRows: 5 }"
                        v-model="themeSettings[item.name]"
                        :placeholder="item.placeholder"
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
                  </a-form>
                </a-tab-pane>
              </a-tabs>
              <a-alert
                message="当前主题暂无设置选项"
                banner
                v-else
              />
            </div>
          </a-skeleton>
        </a-col>
      </a-row>

      <footer-tool-bar
        v-if="themeConfiguration.length>0"
        :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}"
      >
        <a-button
          type="primary"
          @click="handleSaveSettings"
        >保存</a-button>
        <a-button
          type="dashed"
          @click="()=>this.attachmentDrawerVisible = true"
          style="margin-left: 8px;"
        >附件库</a-button>
      </footer-tool-bar>

      <AttachmentDrawer v-model="attachmentDrawerVisible" />
    </a-drawer>
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
              @click="()=>this.uploadVisible = true"
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
      v-model="uploadVisible"
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
              @change="handleChange"
              @success="handleUploadSuccess"
            >
              <p class="ant-upload-drag-icon">
                <a-icon type="inbox" />
              </p>
              <p class="ant-upload-text">点击选择主题或将主题拖拽到此处</p>
              <p class="ant-upload-hint">支持单个或批量上传，仅支持 ZIP 格式的文件</p>
            </upload>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<script>
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import FooterToolBar from '@/components/FooterToolbar'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import themeApi from '@/api/theme'

export default {
  components: {
    AttachmentDrawer,
    FooterToolBar
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      themeLoading: false,
      optionLoading: true,
      uploadVisible: false,
      fetchButtonLoading: false,
      wrapperCol: {
        xl: { span: 12 },
        lg: { span: 12 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      attachmentDrawerVisible: false,
      themes: [],
      visible: false,
      themeConfiguration: [],
      themeSettings: [],
      themeProperty: null,
      fetchingUrl: null,
      uploadHandler: themeApi.upload
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
    if (this.visible) {
      this.visible = false
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.visible) {
      this.visible = false
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
    settingDrawer(theme) {
      this.visible = true
      this.optionLoading = true
      this.themeProperty = theme

      themeApi.fetchConfiguration(theme.id).then(response => {
        this.themeConfiguration = response.data.data
        themeApi.fetchSettings(theme.id).then(response => {
          this.themeSettings = response.data.data

          setTimeout(() => {
            this.visible = true
            this.optionLoading = false
          }, 300)
        })
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
    handleSaveSettings() {
      themeApi.saveSettings(this.themeProperty.id, this.themeSettings).then(response => {
        this.$message.success('保存成功！')
      })
    },
    onClose() {
      this.visible = false
      this.optionLoading = false
      this.themeConfiguration = []
      this.themeProperty = null
    },
    handleChange(info) {
      const status = info.file.status
      if (status === 'done') {
        this.$message.success(`${info.file.name} 主题上传成功！`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 主题上传失败！`)
      }
    },
    handleUploadSuccess() {
      this.uploadVisible = false
      this.loadThemes()
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
    handleFetching() {
      this.fetchButtonLoading = true
      themeApi
        .fetching(this.fetchingUrl)
        .then(response => {
          this.$message.success('拉取成功！')
          this.uploadVisible = false
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
    }
  }
}
</script>

<style lang="less" scoped>
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
