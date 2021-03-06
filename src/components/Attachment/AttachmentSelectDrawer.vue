<template>
  <div>
    <a-drawer
      :title="title"
      :width="isMobile() ? '100%' : drawerWidth"
      closable
      :visible="visible"
      destroyOnClose
      @close="onClose"
      :afterVisibleChange="handleAfterVisibleChanged"
    >
      <a-row type="flex" align="middle">
        <a-input-search placeholder="搜索" v-model="queryParam.keyword" @search="handleQuery()" enterButton />
      </a-row>
      <a-divider />
      <a-row type="flex" align="middle">
        <a-col :span="24">
          <a-spin :spinning="loading">
            <a-empty v-if="attachments.length == 0" />
            <div
              v-else
              class="attach-item"
              v-for="(item, index) in attachments"
              :key="index"
              @click="handleSelectAttachment(item)"
            >
              <span v-show="!handleJudgeMediaType(item)">当前格式不支持预览</span>
              <img :src="item.thumbPath" v-show="handleJudgeMediaType(item)" loading="lazy" />
            </div>
          </a-spin>
        </a-col>
      </a-row>
      <a-divider />
      <div class="page-wrapper">
        <a-pagination
          :current="pagination.page"
          :total="pagination.total"
          :defaultPageSize="pagination.size"
          @change="handlePaginationChange"
          showLessItems
        ></a-pagination>
      </div>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-space>
          <a-button type="dashed" v-if="isChooseAvatar" @click="handleSelectGravatar">使用 Gravatar</a-button>
          <a-button @click="handleShowUploadModal" type="primary">上传附件</a-button>
        </a-space>
      </div>
    </a-drawer>

    <a-modal title="上传附件" v-model="uploadVisible" :footer="null" :afterClose="onUploadClose" destroyOnClose>
      <FilePondUpload ref="upload" :uploadHandler="uploadHandler"></FilePondUpload>
    </a-modal>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import attachmentApi from '@/api/attachment'

export default {
  name: 'AttachmentSelectDrawer',
  mixins: [mixin, mixinDevice],
  model: {
    prop: 'visible',
    event: 'close'
  },
  props: {
    visible: {
      type: Boolean,
      required: false,
      default: false
    },
    drawerWidth: {
      type: Number,
      required: false,
      default: 480
    },
    title: {
      type: String,
      required: false,
      default: '选择附件'
    },
    isChooseAvatar: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      uploadVisible: false,
      loading: true,
      pagination: {
        page: 1,
        size: 12,
        sort: null,
        total: 1
      },
      queryParam: {
        page: 0,
        size: 12,
        sort: null,
        keyword: null
      },
      attachments: [],
      uploadHandler: attachmentApi.upload
    }
  },
  methods: {
    handleShowUploadModal() {
      this.uploadVisible = true
    },
    handleListAttachments() {
      this.loading = true
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      attachmentApi
        .query(this.queryParam)
        .then(response => {
          this.attachments = response.data.data.content
          this.pagination.total = response.data.data.total
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 200)
        })
    },
    handleQuery() {
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleSelectAttachment(item) {
      this.$emit('listenToSelect', item)
    },
    handleSelectGravatar() {
      this.$emit('listenToSelectGravatar')
    },
    handlePaginationChange(page, pageSize) {
      this.pagination.page = page
      this.pagination.size = pageSize
      this.handleListAttachments()
    },
    onUploadClose() {
      this.$refs.upload.handleClearFileList()
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleListAttachments()
      }
    },
    handleJudgeMediaType(attachment) {
      var mediaType = attachment.mediaType
      // 判断文件类型
      if (mediaType) {
        var prefix = mediaType.split('/')[0]

        if (prefix === 'image') {
          // 是图片
          return true
        } else {
          // 非图片
          return false
        }
      }
      // 没有获取到文件返回false
      return false
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>
