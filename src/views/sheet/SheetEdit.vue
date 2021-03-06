<template>
  <page-view affix :title="sheetToStage.title ? sheetToStage.title : '新页面'">
    <template slot="extra">
      <a-space>
        <ReactiveButton
          type="danger"
          @click="handleSaveDraft(false)"
          @callback="draftSavederrored = false"
          :loading="draftSaving"
          :errored="draftSavederrored"
          text="保存草稿"
          loadedText="保存成功"
          erroredText="保存失败"
        ></ReactiveButton>
        <a-button @click="handlePreview" :loading="previewSaving">预览</a-button>
        <a-button type="primary" @click="sheetSettingVisible = true">发布</a-button>
        <a-button type="dashed" @click="attachmentDrawerVisible = true">附件库</a-button>
      </a-space>
    </template>
    <a-row :gutter="12">
      <a-col :span="24">
        <div class="mb-4">
          <a-input v-model="sheetToStage.title" size="large" placeholder="请输入页面标题" />
        </div>

        <div id="editor">
          <MarkdownEditor
            :originalContent="sheetToStage.originalContent"
            @onSaveDraft="handleSaveDraft(true)"
            @onContentChange="onContentChange"
          />
        </div>
      </a-col>
    </a-row>

    <SheetSettingDrawer
      :sheet="sheetToStage"
      :metas="selectedMetas"
      :visible="sheetSettingVisible"
      @close="sheetSettingVisible = false"
      @onRefreshSheet="onRefreshSheetFromSetting"
      @onRefreshSheetMetas="onRefreshSheetMetasFromSetting"
      @onSaved="handleRestoreSavedStatus"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />
  </page-view>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { datetimeFormat } from '@/utils/datetime'
import { PageView } from '@/layouts'
import SheetSettingDrawer from './components/SheetSettingDrawer'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import MarkdownEditor from '@/components/Editor/MarkdownEditor'

import sheetApi from '@/api/sheet'
export default {
  components: {
    PageView,
    AttachmentDrawer,
    SheetSettingDrawer,
    MarkdownEditor
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      attachmentDrawerVisible: false,
      sheetSettingVisible: false,
      sheetToStage: {},
      selectedMetas: [],
      contentChanges: 0,
      draftSaving: false,
      draftSavederrored: false,
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
    } else {
      this.$confirm({
        title: '当前页面数据未保存，确定要离开吗？',
        content: () => <div style="color:red;">如果离开当面页面，你的数据很可能会丢失！</div>,
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
  },
  methods: {
    handleSaveDraft(draftOnly = false) {
      this.$log.debug('Draft only: ' + draftOnly)
      this.sheetToStage.status = 'DRAFT'
      if (!this.sheetToStage.title) {
        this.sheetToStage.title = datetimeFormat(new Date(), 'YYYY-MM-DD-HH-mm-ss')
      }
      this.draftSaving = true
      if (this.sheetToStage.id) {
        if (draftOnly) {
          sheetApi
            .updateDraft(this.sheetToStage.id, this.sheetToStage.originalContent)
            .then(() => {
              this.handleRestoreSavedStatus()
            })
            .catch(() => {
              this.draftSavederrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.draftSaving = false
              }, 400)
            })
        } else {
          sheetApi
            .update(this.sheetToStage.id, this.sheetToStage, false)
            .then(response => {
              this.sheetToStage = response.data.data
              this.handleRestoreSavedStatus()
            })
            .catch(() => {
              this.draftSavederrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.draftSaving = false
              }, 400)
            })
        }
      } else {
        sheetApi
          .create(this.sheetToStage, false)
          .then(response => {
            this.sheetToStage = response.data.data
            this.handleRestoreSavedStatus()
          })
          .catch(() => {
            this.draftSavederrored = true
          })
          .finally(() => {
            setTimeout(() => {
              this.draftSaving = false
            }, 400)
          })
      }
    },
    handlePreview() {
      this.sheetToStage.status = 'DRAFT'
      if (!this.sheetToStage.title) {
        this.sheetToStage.title = datetimeFormat(new Date(), 'YYYY-MM-DD-HH-mm-ss')
      }
      this.previewSaving = true
      if (this.sheetToStage.id) {
        sheetApi.update(this.sheetToStage.id, this.sheetToStage, false).then(response => {
          this.$log.debug('Updated sheet', response.data.data)
          sheetApi
            .preview(this.sheetToStage.id)
            .then(response => {
              window.open(response.data, '_blank')
              this.handleRestoreSavedStatus()
            })
            .finally(() => {
              setTimeout(() => {
                this.previewSaving = false
              }, 400)
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
              this.handleRestoreSavedStatus()
            })
            .finally(() => {
              setTimeout(() => {
                this.previewSaving = false
              }, 400)
            })
        })
      }
    },
    handleRestoreSavedStatus() {
      this.contentChanges = 0
    },
    onContentChange(val) {
      this.contentChanges++
      this.sheetToStage.originalContent = val
    },
    onRefreshSheetFromSetting(sheet) {
      this.sheetToStage = sheet
    },
    onRefreshSheetMetasFromSetting(metas) {
      this.selectedMetas = metas
    }
  }
}
</script>
