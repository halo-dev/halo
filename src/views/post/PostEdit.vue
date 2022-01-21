<template>
  <page-view :title="postToStage.title ? postToStage.title : '新文章'" affix>
    <template slot="extra">
      <a-space>
        <ReactiveButton
          :errored="draftSaveErrored"
          :loading="draftSaving"
          erroredText="保存失败"
          loadedText="保存成功"
          text="保存草稿"
          type="danger"
          @callback="draftSaveErrored = false"
          @click="handleSaveDraft(false)"
        ></ReactiveButton>
        <a-button :loading="previewSaving" @click="handlePreview">预览</a-button>
        <a-button type="primary" @click="postSettingVisible = true">发布</a-button>
        <a-button type="dashed" @click="attachmentDrawerVisible = true">附件库</a-button>
      </a-space>
    </template>
    <a-row :gutter="12">
      <a-col :span="24">
        <div class="mb-4">
          <a-input v-model="postToStage.title" placeholder="请输入文章标题" size="large" />
        </div>
        <div id="editor" :style="{ height: editorHeight }">
          <MarkdownEditor
            :originalContent="postToStage.originalContent"
            @onContentChange="onContentChange"
            @onSaveDraft="handleSaveDraft(true)"
          />
        </div>
      </a-col>
    </a-row>

    <PostSettingModal
      :post="postToStage"
      :savedCallback="onPostSavedCallback"
      :visible.sync="postSettingVisible"
      @onUpdate="onUpdateFromSetting"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />
  </page-view>
</template>

<script>
import { mixin, mixinDevice, mixinPostEdit } from '@/mixins/mixin.js'
import { datetimeFormat } from '@/utils/datetime'

import PostSettingModal from './components/PostSettingModal'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import MarkdownEditor from '@/components/Editor/MarkdownEditor'
import { PageView } from '@/layouts'
import apiClient from '@/utils/api-client'

export default {
  mixins: [mixin, mixinDevice, mixinPostEdit],
  components: {
    PostSettingModal,
    AttachmentDrawer,
    MarkdownEditor,
    PageView
  },
  data() {
    return {
      attachmentDrawerVisible: false,
      postSettingVisible: false,
      postToStage: {},
      contentChanges: 0,
      draftSaving: false,
      previewSaving: false,
      draftSaveErrored: false
    }
  },
  beforeRouteEnter(to, from, next) {
    // Get post id from query
    const postId = to.query.postId
    next(vm => {
      if (postId) {
        apiClient.post.get(postId).then(response => {
          vm.postToStage = response.data
        })
      }
    })
  },
  destroyed() {
    if (this.attachmentDrawerVisible) {
      this.attachmentDrawerVisible = false
    }
    if (window.onbeforeunload) {
      window.onbeforeunload = null
    }
  },
  beforeRouteLeave(to, from, next) {
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
    window.onbeforeunload = function (e) {
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
      this.postToStage.status = 'DRAFT'
      if (!this.postToStage.title) {
        this.postToStage.title = datetimeFormat(new Date(), 'YYYY-MM-DD-HH-mm-ss')
      }
      this.draftSaving = true
      if (this.postToStage.id) {
        // Update the post
        if (draftOnly) {
          apiClient.post
            .updateDraftById(this.postToStage.id, this.postToStage.originalContent)
            .then(() => {
              this.handleRestoreSavedStatus()
            })
            .catch(() => {
              this.draftSaveErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.draftSaving = false
              }, 400)
            })
        } else {
          apiClient.post
            .update(this.postToStage.id, this.postToStage)
            .then(response => {
              this.postToStage = response.data
              this.handleRestoreSavedStatus()
            })
            .catch(() => {
              this.draftSaveErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.draftSaving = false
              }, 400)
            })
        }
      } else {
        // Create the post
        apiClient.post
          .create(this.postToStage)
          .then(response => {
            this.postToStage = response.data
            this.handleRestoreSavedStatus()
          })
          .catch(() => {
            this.draftSaveErrored = true
          })
          .finally(() => {
            setTimeout(() => {
              this.draftSaving = false
            }, 400)
          })
      }
    },
    handlePreview() {
      this.postToStage.status = 'DRAFT'
      if (!this.postToStage.title) {
        this.postToStage.title = datetimeFormat(new Date(), 'YYYY-MM-DD-HH-mm-ss')
      }
      this.previewSaving = true
      if (this.postToStage.id) {
        // Update the post
        apiClient.post.update(this.postToStage.id, this.postToStage).then(response => {
          this.$log.debug('Updated post', response.data)
          apiClient.post
            .getPreviewLinkById(this.postToStage.id)
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
        // Create the post
        apiClient.post.create(this.postToStage).then(response => {
          this.$log.debug('Created post', response.data)
          this.postToStage = response.data
          apiClient.post
            .getPreviewLinkById(this.postToStage.id)
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
      this.postToStage.originalContent = val
    },
    onPostSavedCallback() {
      this.contentChanges = 0
      this.$router.push({ name: 'PostList' })
    },
    onUpdateFromSetting(post) {
      this.postToStage = post
    }
  }
}
</script>
