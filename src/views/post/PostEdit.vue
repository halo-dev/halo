<template>
  <page-view affix :title="postToStage.title ? postToStage.title : '新文章'">
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
        <a-button type="primary" @click="postSettingVisible = true">发布</a-button>
        <a-button type="dashed" @click="attachmentDrawerVisible = true">附件库</a-button>
      </a-space>
    </template>
    <a-row :gutter="12">
      <a-col :span="24">
        <div class="mb-4">
          <a-input v-model="postToStage.title" size="large" placeholder="请输入文章标题" />
        </div>

        <div id="editor">
          <MarkdownEditor
            :originalContent="postToStage.originalContent"
            @onSaveDraft="handleSaveDraft(true)"
            @onContentChange="onContentChange"
          />
        </div>
      </a-col>
    </a-row>

    <PostSettingDrawer
      :post="postToStage"
      :tagIds="selectedTagIds"
      :categoryIds="selectedCategoryIds"
      :metas="selectedMetas"
      :visible="postSettingVisible"
      @close="postSettingVisible = false"
      @onRefreshPost="onRefreshPostFromSetting"
      @onRefreshTagIds="onRefreshTagIdsFromSetting"
      @onRefreshCategoryIds="onRefreshCategoryIdsFromSetting"
      @onRefreshPostMetas="onRefreshPostMetasFromSetting"
      @onSaved="handleRestoreSavedStatus"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />
  </page-view>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { datetimeFormat } from '@/utils/datetime'

import PostSettingDrawer from './components/PostSettingDrawer'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import MarkdownEditor from '@/components/Editor/MarkdownEditor'
import { PageView } from '@/layouts'

import postApi from '@/api/post'
export default {
  mixins: [mixin, mixinDevice],
  components: {
    PostSettingDrawer,
    AttachmentDrawer,
    MarkdownEditor,
    PageView
  },
  data() {
    return {
      attachmentDrawerVisible: false,
      postSettingVisible: false,
      postToStage: {},
      selectedTagIds: [],
      selectedCategoryIds: [],
      selectedMetas: [],
      contentChanges: 0,
      draftSaving: false,
      previewSaving: false,
      draftSavederrored: false
    }
  },
  beforeRouteEnter(to, from, next) {
    // Get post id from query
    const postId = to.query.postId
    next(vm => {
      if (postId) {
        postApi.get(postId).then(response => {
          const post = response.data.data
          vm.postToStage = post
          vm.selectedTagIds = post.tagIds
          vm.selectedCategoryIds = post.categoryIds
          vm.selectedMetas = post.metas
        })
      }
    })
  },
  destroyed: function() {
    if (this.postSettingVisible) {
      this.postSettingVisible = false
    }
    if (this.attachmentDrawerVisible) {
      this.attachmentDrawerVisible = false
    }
    if (window.onbeforeunload) {
      window.onbeforeunload = null
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.postSettingVisible) {
      this.postSettingVisible = false
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
      this.postToStage.status = 'DRAFT'
      if (!this.postToStage.title) {
        this.postToStage.title = datetimeFormat(new Date(), 'YYYY-MM-DD-HH-mm-ss')
      }
      this.draftSaving = true
      if (this.postToStage.id) {
        // Update the post
        if (draftOnly) {
          postApi
            .updateDraft(this.postToStage.id, this.postToStage.originalContent)
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
          postApi
            .update(this.postToStage.id, this.postToStage, false)
            .then(response => {
              this.postToStage = response.data.data
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
        // Create the post
        postApi
          .create(this.postToStage, false)
          .then(response => {
            this.postToStage = response.data.data
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
      this.postToStage.status = 'DRAFT'
      if (!this.postToStage.title) {
        this.postToStage.title = datetimeFormat(new Date(), 'YYYY-MM-DD-HH-mm-ss')
      }
      this.previewSaving = true
      if (this.postToStage.id) {
        // Update the post
        postApi.update(this.postToStage.id, this.postToStage, false).then(response => {
          this.$log.debug('Updated post', response.data.data)
          postApi
            .preview(this.postToStage.id)
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
        postApi.create(this.postToStage, false).then(response => {
          this.$log.debug('Created post', response.data.data)
          this.postToStage = response.data.data
          postApi
            .preview(this.postToStage.id)
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
    onRefreshPostFromSetting(post) {
      this.postToStage = post
    },
    onRefreshTagIdsFromSetting(tagIds) {
      this.selectedTagIds = tagIds
    },
    onRefreshCategoryIdsFromSetting(categoryIds) {
      this.selectedCategoryIds = categoryIds
    },
    onRefreshPostMetasFromSetting(metas) {
      this.selectedMetas = metas
    }
  }
}
</script>
