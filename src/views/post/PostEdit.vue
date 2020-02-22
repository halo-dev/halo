<template>
  <div>
    <a-row :gutter="12">
      <a-col :span="24">
        <div style="margin-bottom: 16px">
          <a-input
            v-model="postToStage.title"
            size="large"
            placeholder="请输入文章标题"
          />
        </div>

        <div id="editor">
          <halo-editor
            ref="md"
            v-model="postToStage.originalContent"
            :boxShadow="false"
            :toolbars="toolbars"
            :ishljs="true"
            :autofocus="false"
            @imgAdd="handleAttachmentUpload"
            @save="handleSaveDraft(true)"
          />
        </div>
      </a-col>
    </a-row>

    <PostSettingDrawer
      :post="postToStage"
      :tagIds="selectedTagIds"
      :categoryIds="selectedCategoryIds"
      :postMetas="selectedPostMetas"
      :visible="postSettingVisible"
      @close="onPostSettingsClose"
      @onRefreshPost="onRefreshPostFromSetting"
      @onRefreshTagIds="onRefreshTagIdsFromSetting"
      @onRefreshCategoryIds="onRefreshCategoryIdsFromSetting"
      @onRefreshPostMetas="onRefreshPostMetasFromSetting"
      @onSaved="onSaved"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />

    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="danger"
        @click="handleSaveDraft(false)"
        :disabled="saving"
      >保存草稿</a-button>
      <a-button
        @click="handlePreview"
        style="margin-left: 8px;"
        :disabled="saving"
      >预览</a-button>
      <a-button
        type="primary"
        @click="handleShowPostSetting"
        style="margin-left: 8px;"
      >发布</a-button>
      <a-button
        type="dashed"
        @click="()=>this.attachmentDrawerVisible = true"
        style="margin-left: 8px;"
      >附件库</a-button>
    </footer-tool-bar>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { mapGetters } from 'vuex'
import moment from 'moment'
import PostSettingDrawer from './components/PostSettingDrawer'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import FooterToolBar from '@/components/FooterToolbar'
import { toolbars } from '@/core/const'
import { haloEditor } from 'halo-editor'
import 'halo-editor/dist/css/index.css'

import postApi from '@/api/post'
import attachmentApi from '@/api/attachment'
export default {
  mixins: [mixin, mixinDevice],
  components: {
    PostSettingDrawer,
    haloEditor,
    FooterToolBar,
    AttachmentDrawer
  },
  data() {
    return {
      toolbars,
      attachmentDrawerVisible: false,
      postSettingVisible: false,
      postToStage: {},
      selectedTagIds: [],
      selectedCategoryIds: [],
      selectedPostMetas: [],
      isSaved: false,
      contentChanges: 0,
      saving: false
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
          vm.selectedPostMetas = post.postMetas
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
      return this.postToStage.originalContent
    },
    ...mapGetters(['options'])
  },
  methods: {
    handleSaveDraft(draftOnly = false) {
      this.$log.debug('Draft only: ' + draftOnly)
      this.postToStage.status = 'DRAFT'
      if (!this.postToStage.title) {
        this.postToStage.title = moment(new Date()).format('YYYY-MM-DD-HH-mm-ss')
      }
      this.saving = true
      if (this.postToStage.id) {
        // Update the post
        if (draftOnly) {
          postApi
            .updateDraft(this.postToStage.id, this.postToStage.originalContent)
            .then(response => {
              this.$message.success('保存草稿成功！')
            })
            .finally(() => {
              this.saving = false
            })
        } else {
          postApi
            .update(this.postToStage.id, this.postToStage, false)
            .then(response => {
              this.$log.debug('Updated post', response.data.data)
              this.$message.success('保存草稿成功！')
            })
            .finally(() => {
              this.saving = false
            })
        }
      } else {
        // Create the post
        postApi
          .create(this.postToStage, false)
          .then(response => {
            this.$log.debug('Created post', response.data.data)
            this.$message.success('保存草稿成功！')
            this.postToStage = response.data.data
          })
          .finally(() => {
            this.saving = false
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
          this.$message.success('图片上传成功！')
        } else {
          this.$message.error('图片上传失败：' + responseObject.message)
        }
      })
    },
    handleShowPostSetting() {
      this.postSettingVisible = true
    },
    handlePreview() {
      this.postToStage.status = 'DRAFT'
      if (!this.postToStage.title) {
        this.postToStage.title = moment(new Date()).format('YYYY-MM-DD-HH-mm-ss')
      }
      this.saving = true
      if (this.postToStage.id) {
        // Update the post
        postApi.update(this.postToStage.id, this.postToStage, false).then(response => {
          this.$log.debug('Updated post', response.data.data)
          postApi
            .preview(this.postToStage.id)
            .then(response => {
              window.open(response.data, '_blank')
            })
            .finally(() => {
              this.saving = false
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
            })
            .finally(() => {
              this.saving = false
            })
        })
      }
    },
    // 关闭文章设置抽屉
    onPostSettingsClose() {
      this.postSettingVisible = false
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
    onRefreshPostMetasFromSetting(postMetas) {
      this.selectedPostMetas = postMetas
    },
    onSaved(isSaved) {
      this.isSaved = isSaved
    }
  }
}
</script>
