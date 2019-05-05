<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :span="24">
        <div style="margin-bottom: 16px">
          <a-input
            v-model="sheetToStage.title"
            v-decorator="['title', { rules: [{ required: true, message: '请输入页面标题' }] }]"
            size="large"
            placeholder="请输入页面标题"
          />
        </div>
        <div id="editor">
          <mavon-editor
            v-model="sheetToStage.originalContent"
            :boxShadow="false"
            :toolbars="toolbars"
            :ishljs="true"
            :autofocus="false"
          />
        </div>
      </a-col>

      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
        <a-drawer
          title="页面设置"
          :width="isMobile()?'100%':'460'"
          :closable="true"
          @close="onClose"
          :visible="visible"
        >
          <div class="post-setting-drawer-content">
            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">基本设置</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item
                    label="页面路径："
                    :help="'https://localhost:8090/s/'+ (sheetToStage.url ? sheetToStage.url : '{auto_generate}')"
                  >
                    <a-input v-model="sheetToStage.url" />
                  </a-form-item>
                  <a-form-item label="页面密码：">
                    <a-input
                      type="password"
                      v-model="sheetToStage.password"
                    />
                  </a-form-item>
                  <a-form-item label="开启评论：">
                    <a-radio-group
                      v-model="sheetToStage.disallowComment"
                      :defaultValue="false"
                    >
                      <a-radio :value="false">开启</a-radio>
                      <a-radio :value="true">关闭</a-radio>
                    </a-radio-group>
                  </a-form-item>
                  <a-form-item label="自定义模板：">
                    <a-select v-model="sheetToStage.template">
                      <a-select-option key="" value="">无</a-select-option>
                      <a-select-option
                        v-for="tpl in customTpls"
                        :key="tpl"
                        :value="tpl"
                      >{{ tpl }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-form>
              </div>
            </div>
            <a-divider />

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">缩略图</h3>
              <div class="post-setting-drawer-item">
                <div class="sheet-thum">
                  <img
                    class="img"
                    :src="sheetToStage.thumbnail || '//i.loli.net/2019/05/05/5ccf007c0a01d.png'"
                    @click="handleShowThumbDrawer"
                  >
                  <a-button
                    class="sheet-thum-remove"
                    type="dashed"
                    @click="handlerRemoveThumb"
                  >移除</a-button>
                </div>
              </div>
            </div>
            <a-divider />
          </div>
          <AttachmentSelectDrawer
            v-model="thumDrawerVisible"
            @listenToSelect="handleSelectSheetThumb"
            :drawerWidth="460"
          />
          <div class="bottom-control">
            <a-button
              style="marginRight: 8px"
              @click="handleDraftClick"
            >保存草稿</a-button>
            <a-button
              type="primary"
              @click="handlePublishClick"
            >发布</a-button>
          </div>
        </a-drawer>
      </a-col>
    </a-row>
    <AttachmentDrawer v-model="attachmentDrawerVisible" />
    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="primary"
        @click="handleShowDrawer"
      >发布</a-button>
      <a-button
        type="dashed"
        style="margin-left: 8px;"
        @click="handleShowAttachDrawer"
      >附件库</a-button>
    </footer-tool-bar>
  </div>
</template>

<script>
import { mavonEditor } from 'mavon-editor'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import FooterToolBar from '@/components/FooterToolbar'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { toolbars } from '@/core/const'
import 'mavon-editor/dist/css/index.css'
import sheetApi from '@/api/sheet'
import themeApi from '@/api/theme'
export default {
  components: {
    mavonEditor,
    FooterToolBar,
    AttachmentDrawer,
    AttachmentSelectDrawer
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      toolbars,
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      attachmentDrawerVisible: false,
      thumDrawerVisible: false,
      visible: false,
      customTpls: [],
      sheetToStage: {},
      timer: null
    }
  },
  created() {
    this.loadCustomTpls()
    clearInterval(this.timer)
    this.timer = null
    this.autoSaveTimer()
  },
  destroyed: function() {
    clearInterval(this.timer)
    this.timer = null
  },
  beforeRouteEnter(to, from, next) {
    // Get sheetId id from query
    const sheetId = to.query.sheetId

    next(vm => {
      if (sheetId) {
        sheetApi.get(sheetId).then(response => {
          const sheet = response.data.data
          vm.sheetToStage = sheet
        })
      }
    })
  },
  methods: {
    loadCustomTpls() {
      themeApi.customTpls().then(response => {
        this.customTpls = response.data.data
      })
    },
    handleShowAttachDrawer() {
      this.attachmentDrawerVisible = true
    },
    handleShowThumbDrawer() {
      this.thumDrawerVisible = true
    },
    handleShowDrawer() {
      this.visible = true
    },
    handlePublishClick() {
      this.sheetToStage.status = 'PUBLISHED'
      this.createOrUpdateSheet()
    },
    handleDraftClick() {
      this.sheetToStage.status = 'DRAFT'
      this.createOrUpdateSheet()
    },
    handlerRemoveThumb() {
      this.sheetToStage.thumbnail = null
    },
    createOrUpdateSheet() {
      if (this.sheetToStage.id) {
        sheetApi.update(this.sheetToStage.id, this.sheetToStage).then(response => {
          this.$log.debug('Updated sheet', response.data.data)
          this.$message.success('页面更新成功')
        })
      } else {
        sheetApi.create(this.sheetToStage).then(response => {
          this.$log.debug('Created sheet', response.data.data)
          this.$message.success('页面创建成功')
          this.sheetToStage = response.data.data
        })
      }
    },
    onClose() {
      this.visible = false
    },
    handleSelectSheetThumb(data) {
      this.sheetToStage.thumbnail = data.path
      this.thumDrawerVisible = false
    },
    autoSaveTimer() {
      if (this.timer == null) {
        this.timer = setInterval(() => {
          if (this.sheetToStage.title != null && this.sheetToStage.originalContent != null) {
            this.sheetToStage.categoryIds = this.selectedCategoryIds
            this.sheetToStage.tagIds = this.selectedTagIds

            if (this.sheetToStage.id) {
              sheetApi.update(this.sheetToStage.id, this.sheetToStage).then(response => {
                this.$log.debug('Auto updated sheet', response.data.data)
              })
            } else {
              sheetApi.create(this.sheetToStage).then(response => {
                this.$log.debug('Auto saved sheet', response.data.data)
                this.sheetToStage = response.data.data
              })
            }
          }
        }, 15000)
      }
    }
  }
}
</script>
<style lang="less" scoped>
.v-note-wrapper {
  z-index: 1000;
  min-height: 580px;
}

.sheet-thum {
  .img {
    width: 100%;
    cursor: pointer;
    border-radius: 4px;
  }
  .sheet-thum-remove {
    margin-top: 16px;
  }
}
</style>
