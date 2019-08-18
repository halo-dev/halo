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
          <halo-editor
            ref="md"
            v-model="sheetToStage.originalContent"
            :boxShadow="false"
            :toolbars="toolbars"
            :ishljs="true"
            :autofocus="false"
            @imgAdd="pictureUploadHandle"
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
          @close="()=>this.sheetSettingVisible = false"
          :visible="sheetSettingVisible"
        >
          <div class="post-setting-drawer-content">
            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">基本设置</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item
                    label="页面路径："
                    :help="options.blog_url+'/s/'+ (sheetToStage.url ? sheetToStage.url : '{auto_generate}')"
                  >
                    <a-input v-model="sheetToStage.url" />
                  </a-form-item>
                  <a-form-item label="发表时间：">
                    <a-date-picker
                      showTime
                      :defaultValue="pickerDefaultValue"
                      format="YYYY-MM-DD HH:mm:ss"
                      placeholder="Select Publish Time"
                      @change="onChange"
                      @ok="onOk"
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
                        key=""
                        value=""
                      >无</a-select-option>
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
                    @click="()=>this.thumDrawerVisible = true"
                  >
                  <a-button
                    class="sheet-thum-remove"
                    type="dashed"
                    @click="handlerRemoveThumb"
                  >移除</a-button>
                </div>
              </div>
            </div>
            <a-divider class="divider-transparent" />
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
        @click="()=>this.sheetSettingVisible = true"
      >发布</a-button>
      <a-button
        type="dashed"
        style="margin-left: 8px;"
        @click="()=>this.attachmentDrawerVisible = true"
      >附件库</a-button>
    </footer-tool-bar>
  </div>
</template>

<script>
import { haloEditor } from 'halo-editor'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import FooterToolBar from '@/components/FooterToolbar'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { toolbars } from '@/core/const'
import 'halo-editor/dist/css/index.css'
import sheetApi from '@/api/sheet'
import themeApi from '@/api/theme'
import optionApi from '@/api/option'
import attachmentApi from '@/api/attachment'
import moment from 'moment'
export default {
  components: {
    haloEditor,
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
      sheetSettingVisible: false,
      customTpls: [],
      sheetToStage: {},
      timer: null,
      options: [],
      keys: ['blog_url']
    }
  },
  created() {
    this.loadCustomTpls()
    this.loadOptions()
    clearInterval(this.timer)
    this.timer = null
    this.autoSaveTimer()
  },
  destroyed: function() {
    clearInterval(this.timer)
    this.timer = null
  },
  beforeRouteLeave(to, from, next) {
    if (this.timer !== null) {
      clearInterval(this.timer)
    }
    // Auto save the sheet
    this.autoSaveSheet()
    next()
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
  computed: {
    pickerDefaultValue() {
      if (this.sheetToStage.createTime) {
        var date = new Date(this.sheetToStage.createTime)
        return moment(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return moment(new Date(), 'YYYY-MM-DD HH:mm:ss')
    }
  },
  methods: {
    loadCustomTpls() {
      themeApi.customTpls().then(response => {
        this.customTpls = response.data.data
      })
    },
    loadOptions() {
      optionApi.listAll(this.keys).then(response => {
        this.options = response.data.data
      })
    },
    handlePublishClick() {
      this.sheetToStage.status = 'PUBLISHED'
      this.saveSheet()
    },
    handleDraftClick() {
      this.sheetToStage.status = 'DRAFT'
      this.saveSheet()
    },
    handlerRemoveThumb() {
      this.sheetToStage.thumbnail = null
    },
    createOrUpdateSheet(createSuccess, updateSuccess, autoSave) {
      if (this.sheetToStage.id) {
        sheetApi.update(this.sheetToStage.id, this.sheetToStage, autoSave).then(response => {
          this.$log.debug('Updated sheet', response.data.data)
          if (updateSuccess) {
            updateSuccess()
          }
        })
      } else {
        sheetApi.create(this.sheetToStage, autoSave).then(response => {
          this.$log.debug('Created sheet', response.data.data)
          if (createSuccess) {
            createSuccess()
          }
          this.sheetToStage = response.data.data
        })
      }
    },
    saveSheet() {
      this.createOrUpdateSheet(
        () => this.$message.success('页面创建成功！'),
        () => this.$message.success('页面更新成功！'),
        false
      )
    },
    autoSaveSheet() {
      if (this.sheetToStage.title != null && this.sheetToStage.originalContent != null) {
        this.createOrUpdateSheet(null, null, true)
      }
    },
    handleSelectSheetThumb(data) {
      this.sheetToStage.thumbnail = encodeURI(data.path)
      this.thumDrawerVisible = false
    },
    autoSaveTimer() {
      if (this.timer == null) {
        this.timer = setInterval(() => {
          this.autoSaveSheet()
        }, 15000)
      }
    },
    pictureUploadHandle(pos, $file) {
      var formdata = new FormData()
      formdata.append('file', $file)
      attachmentApi.upload(formdata).then(response => {
        var responseObject = response.data

        if (responseObject.status === 200) {
          var HaloEditor = this.$refs.md
          HaloEditor.$img2Url(pos, encodeURI(responseObject.data.path))
          this.$message.success('图片上传成功')
        } else {
          this.$message.error('图片上传失败：' + responseObject.message)
        }
      })
    },
    onChange(value, dateString) {
      this.sheetToStage.createTime = value.valueOf()
    },
    onOk(value) {
      this.sheetToStage.createTime = value.valueOf()
    }
  }
}
</script>
<style lang="less" scoped>
.v-note-wrapper {
  min-height: 580px;
}
</style>
