<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
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
                    :src="sheetToStage.thumbnail || 'https://os.alipayobjects.com/rmsportal/mgesTPFxodmIwpi.png'"
                    @click="showThumbDrawer"
                  >
                </div>
              </div>
            </div>
            <a-divider />
          </div>
          <AttachmentSelectDrawer
            v-model="thumDrawerVisible"
            @listenToSelect="selectSheetThumb"
            :drawerWidth="460"
          />
          <div class="sheetControl">
            <a-button
              style="marginRight: 8px"
              @click="handleDraftClick"
            >保存草稿</a-button>
            <a-button
              type="primary"
              @click="handlePublishClick"
            >{{ publishText }}</a-button>
          </div>
        </a-drawer>
      </a-col>
    </a-row>
    <AttachmentDrawer v-model="attachmentDrawerVisible" />
    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="primary"
        @click="showDrawer"
      >发布</a-button>
      <a-button
        type="dashed"
        style="margin-left: 8px;"
        @click="showAttachDrawer"
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
import 'mavon-editor/dist/css/index.css'
import sheetApi from '@/api/sheet'
import themeApi from '@/api/theme'
const toolbars = {
  bold: true, // 粗体
  italic: true, // 斜体
  header: true, // 标题
  underline: true, // 下划线
  strikethrough: true, // 中划线
  quote: true, // 引用
  ol: true, // 有序列表
  ul: true, // 无序列表
  link: true, // 链接
  imagelink: true, // 图片链接
  code: true, // code
  table: true, // 表格
  fullscreen: true, // 全屏编辑
  readmodel: true, // 沉浸式阅读
  htmlcode: true, // 展示html源码
  undo: true, // 上一步
  redo: true, // 下一步
  trash: true, // 清空
  navigation: true, // 导航目录
  subfield: true, // 单双栏模式
  preview: true // 预览
}
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
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      attachmentDrawerVisible: false,
      thumDrawerVisible: false,
      toolbars,
      visible: false,
      sheetUrl: 'hello-world',
      customTpls: [],
      sheetToStage: {}
    }
  },
  computed: {
    publishText() {
      if (this.sheetToStage.id) {
        return '更新并发布'
      }
      return '创建并发布'
    }
  },
  created() {
    this.loadCustomTpls()
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
    showAttachDrawer() {
      this.attachmentDrawerVisible = true
    },
    showThumbDrawer() {
      this.thumDrawerVisible = true
    },
    showDrawer() {
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
    selectSheetThumb(data) {
      this.sheetToStage.thumbnail = data.path
      this.thumDrawerVisible = false
    }
  }
}
</script>
<style scoped>
.v-note-wrapper {
  z-index: 1000;
  min-height: 580px;
}

.sheetControl {
  position: absolute;
  bottom: 0px;
  width: 100%;
  border-top: 1px solid rgb(232, 232, 232);
  padding: 10px 16px;
  text-align: right;
  left: 0px;
  background: rgb(255, 255, 255);
  border-radius: 0px 0px 4px 4px;
}

.sheet-thum .img {
  width: 100%;
}
</style>
