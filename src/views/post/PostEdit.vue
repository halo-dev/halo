<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :span="24">
        <div style="margin-bottom: 16px">
          <a-input
            v-model="postToStage.title"
            v-decorator="['title', { rules: [{ required: true, message: '请输入文章标题' }] }]"
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
            @keydown.ctrl.83.native="handleSaveDraft"
            @keydown.meta.83.native="handleSaveDraft"
          />
        </div>
      </a-col>
    </a-row>

    <PostSetting
      :post="postToStage"
      :tagIds="selectedTagIds"
      :categoryIds="selectedCategoryIds"
      :visible="postSettingVisible"
      @close="onPostSettingsClose"
      @onRefreshPost="onRefreshPostFromSetting"
      @onRefreshTagIds="onRefreshTagIdsFromSetting"
      @onRefreshCategoryIds="onRefreshCategoryIdsFromSetting"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />

    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="danger"
        @click="handleSaveDraft"
      >保存草稿</a-button>
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
import moment from 'moment'
import PostSetting from './components/PostSetting'
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
    PostSetting,
    haloEditor,
    FooterToolBar,
    AttachmentDrawer
  },
  data() {
    return {
      toolbars,
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      attachmentDrawerVisible: false,
      postSettingVisible: false,
      postToStage: {},
      selectedTagIds: [],
      selectedCategoryIds: []
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
        })
      }
    })
  },
  methods: {
    handleSaveDraft() {
      this.postToStage.status = 'DRAFT'
      if (!this.postToStage.title) {
        this.postToStage.title = moment(new Date()).format('YYYY-MM-DD-HH-mm-ss')
      }
      if (!this.postToStage.originalContent) {
        this.postToStage.originalContent = '开始编辑...'
      }
      this.createOrUpdatePost(
        () => this.$message.success('保存草稿成功！'),
        () => this.$message.success('保存草稿成功！'),
        false
      )
    },
    createOrUpdatePost(createSuccess, updateSuccess, autoSave) {
      if (this.postToStage.id) {
        // Update the post
        postApi.update(this.postToStage.id, this.postToStage, autoSave).then(response => {
          this.$log.debug('Updated post', response.data.data)
          if (updateSuccess) {
            updateSuccess()
          }
        })
      } else {
        // Create the post
        postApi.create(this.postToStage, autoSave).then(response => {
          this.$log.debug('Created post', response.data.data)
          if (createSuccess) {
            createSuccess()
          }
          this.postToStage = response.data.data
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
    handleShowPostSetting() {
      this.postSettingVisible = true
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
    }
  }
}
</script>

<style lang="less">
</style>
