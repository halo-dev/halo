<template>
  <div>
    <a-row :gutter="12">
      <a-col :span="24">
        <div style="margin-bottom: 16px">
          <a-input
            v-model="sheetToStage.title"
            size="large"
            placeholder="请输入页面标题"
          />
        </div>

        <div id="editor">
          <MarkdownEditor
            :originalContent="sheetToStage.originalContent"
            @onSaveDraft="handleSaveDraft(true)"
            @onContentChange="onContentChange"
          />

          <!-- <RichTextEditor
            v-else
            :originalContent="sheetToStage.originalContent"
            @onContentChange="onContentChange"
          /> -->
        </div>
      </a-col>
    </a-row>

    <SheetSettingDrawer
      :sheet="sheetToStage"
      :metas="selectedMetas"
      :visible="sheetSettingVisible"
      @close="onSheetSettingsClose"
      @onRefreshSheet="onRefreshSheetFromSetting"
      @onRefreshSheetMetas="onRefreshSheetMetasFromSetting"
      @onSaved="onSaved"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />
    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="danger"
        @click="handleSaveDraft(false)"
        :loading="draftSaving"
      >保存草稿</a-button>
      <a-button
        @click="handlePreview"
        style="margin-left: 8px;"
        :loading="previewSaving"
      >预览</a-button>
      <a-button
        type="primary"
        style="margin-left: 8px;"
        @click="handleShowSheetSetting"
      >发布</a-button>
      <a-button
        type="dashed"
        style="margin-left: 8px;"
        @click="attachmentDrawerVisible = true"
      >附件库</a-button>
    </footer-tool-bar>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
// import { mapGetters } from 'vuex'
import moment from 'moment'
import SheetSettingDrawer from './components/SheetSettingDrawer'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import FooterToolBar from '@/components/FooterToolbar'
import MarkdownEditor from '@/components/editor/MarkdownEditor'
// import RichTextEditor from '@/components/editor/RichTextEditor'

import sheetApi from '@/api/sheet'
export default {
  components: {
    FooterToolBar,
    AttachmentDrawer,
    SheetSettingDrawer,
    MarkdownEditor
    // RichTextEditor
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      attachmentDrawerVisible: false,
      sheetSettingVisible: false,
      sheetToStage: {},
      selectedMetas: [],
      isSaved: false,
      contentChanges: 0,
      draftSaving: false,
      previewSaving: false
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
          vm.selectedMetas = sheet.metas
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
    if (window.onbeforeunload) {
      window.onbeforeunload = null
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.sheetSettingVisible) {
      this.sheetSettingVisible = false
    }
    if (this.attachmentDrawerVisible) {
      this.attachmentDrawerVisible = false
    }
    if (this.contentChanges <= 1) {
      next()
    } else if (this.isSaved) {
      next()
    } else {
      this.$confirm({
        title: '当前页面数据未保存，确定要离开吗？',
        content: h => <div style="color:red;">如果离开当面页面，你的数据很可能会丢失！</div>,
        onOk() {
          next()
        },
        onCancel() {
          next(false)
        }
      })
    }
  },
  mounted() {
    window.onbeforeunload = function(e) {
      e = e || window.event
      if (e) {
        e.returnValue = '当前页面数据未保存，确定要离开吗？'
      }
      return '当前页面数据未保存，确定要离开吗？'
    }
    // if (!this.sheetToStage.editorType) {
    //   this.sheetToStage.editorType = this.options.default_editor
    // }
  },
  watch: {
    temporaryContent: function(newValue, oldValue) {
      if (newValue) {
        this.contentChanges++
      }
    }
  },
  computed: {
    temporaryContent() {
      return this.sheetToStage.originalContent
    }
    // ...mapGetters(['options'])
  },
  methods: {
    handleSaveDraft(draftOnly = false) {
      this.$log.debug('Draft only: ' + draftOnly)
      this.sheetToStage.status = 'DRAFT'
      if (!this.sheetToStage.title) {
        this.sheetToStage.title = moment(new Date()).format('YYYY-MM-DD-HH-mm-ss')
      }
      this.draftSaving = true
      if (this.sheetToStage.id) {
        if (draftOnly) {
          sheetApi
            .updateDraft(this.sheetToStage.id, this.sheetToStage.originalContent)
            .then(response => {
              this.$message.success('保存草稿成功！')
            })
            .finally(() => {
              this.draftSaving = false
            })
        } else {
          sheetApi
            .update(this.sheetToStage.id, this.sheetToStage, false)
            .then(response => {
              this.$log.debug('Updated sheet', response.data.data)
              this.$message.success('保存草稿成功！')
              this.sheetToStage = response.data.data
            })
            .finally(() => {
              this.draftSaving = false
            })
        }
      } else {
        sheetApi
          .create(this.sheetToStage, false)
          .then(response => {
            this.$log.debug('Created sheet', response.data.data)
            this.$message.success('保存草稿成功！')
            this.sheetToStage = response.data.data
          })
          .finally(() => {
            this.draftSaving = false
          })
      }
    },
    handleShowSheetSetting() {
      this.sheetSettingVisible = true
    },
    handlePreview() {
      this.sheetToStage.status = 'DRAFT'
      if (!this.sheetToStage.title) {
        this.sheetToStage.title = moment(new Date()).format('YYYY-MM-DD-HH-mm-ss')
      }
      this.previewSaving = true
      if (this.sheetToStage.id) {
        sheetApi.update(this.sheetToStage.id, this.sheetToStage, false).then(response => {
          this.$log.debug('Updated sheet', response.data.data)
          sheetApi
            .preview(this.sheetToStage.id)
            .then(response => {
              window.open(response.data, '_blank')
            })
            .finally(() => {
              this.previewSaving = false
            })
        })
      } else {
        sheetApi.create(this.sheetToStage, false).then(response => {
          this.$log.debug('Created sheet', response.data.data)
          this.sheetToStage = response.data.data
          sheetApi
            .preview(this.sheetToStage.id)
            .then(response => {
              window.open(response.data, '_blank')
            })
            .finally(() => {
              this.previewSaving = false
            })
        })
      }
    },
    onContentChange(val) {
      this.sheetToStage.originalContent = val
    },
    onSheetSettingsClose() {
      this.sheetSettingVisible = false
    },
    onRefreshSheetFromSetting(sheet) {
      this.sheetToStage = sheet
    },
    onRefreshSheetMetasFromSetting(metas) {
      this.selectedMetas = metas
    },
    onSaved(isSaved) {
      this.isSaved = isSaved
    }
  }
}
</script>
