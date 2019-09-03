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
            @imgAdd="handleAttachmentUpload"
            @keydown.ctrl.83.native="handleSaveDraft"
            @keydown.meta.83.native="handleSaveDraft"
          />
        </div>
      </a-col>
    </a-row>

    <SheetSetting
      :sheet="sheetToStage"
      :visible="sheetSettingVisible"
      @close="onSheetSettingsClose"
      @onRefreshSheet="onRefreshSheetFromSetting"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />
    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="danger"
        @click="handleSaveDraft"
      >保存草稿</a-button>
      <a-button
        type="primary"
        style="margin-left: 8px;"
        @click="handleShowSheetSetting"
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
import { mixin, mixinDevice } from '@/utils/mixin.js'
import moment from 'moment'
import { toolbars } from '@/core/const'
import SheetSetting from './components/SheetSetting'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import FooterToolBar from '@/components/FooterToolbar'
import { haloEditor } from 'halo-editor'
import 'halo-editor/dist/css/index.css'
import sheetApi from '@/api/sheet'
import attachmentApi from '@/api/attachment'
export default {
  components: {
    haloEditor,
    FooterToolBar,
    AttachmentDrawer,
    SheetSetting
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
      sheetSettingVisible: false,
      sheetToStage: {}
    }
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
  destroyed: function() {
    if (this.sheetSettingVisible) {
      this.sheetSettingVisible = false
    }
    if (this.attachmentDrawerVisible) {
      this.attachmentDrawerVisible = false
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.sheetSettingVisible) {
      this.sheetSettingVisible = false
    }
    if (this.attachmentDrawerVisible) {
      this.attachmentDrawerVisible = false
    }
    next()
  },
  methods: {
    handleSaveDraft() {
      this.sheetToStage.status = 'DRAFT'
      if (!this.sheetToStage.title) {
        this.sheetToStage.title = moment(new Date()).format('YYYY-MM-DD-HH-mm-ss')
      }
      if (!this.sheetToStage.originalContent) {
        this.sheetToStage.originalContent = '开始编辑...'
      }
      this.createOrUpdateSheet(
        () => this.$message.success('保存草稿成功！'),
        () => this.$message.success('保存草稿成功！'),
        false
      )
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
    handleAttachmentUpload(pos, $file) {
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
    handleShowSheetSetting() {
      this.sheetSettingVisible = true
    },
    onSheetSettingsClose() {
      this.sheetSettingVisible = false
    },
    onRefreshSheetFromSetting(sheet) {
      this.sheetToStage = sheet
    }
  }
}
</script>
