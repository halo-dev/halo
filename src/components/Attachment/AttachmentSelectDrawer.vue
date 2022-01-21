<template>
  <div>
    <a-drawer
      :afterVisibleChange="handleAfterVisibleChanged"
      :title="title"
      :visible="visible"
      :width="isMobile() ? '100%' : drawerWidth"
      closable
      destroyOnClose
      @close="onClose"
    >
      <a-row align="middle" type="flex">
        <a-input-search v-model="queryParam.keyword" enterButton placeholder="搜索" @search="handleQuery()" />
      </a-row>
      <a-divider />
      <a-row align="middle" type="flex">
        <a-col :span="24">
          <a-spin :spinning="loading" class="attachments-group">
            <a-empty v-if="attachments.length === 0" />
            <div
              v-for="(item, index) in attachments"
              v-else
              :key="index"
              class="attach-item attachments-group-item"
              @click="handleSelectAttachment(item)"
            >
              <span v-if="!handleJudgeMediaType(item)" class="attachments-group-item-type">{{ item.suffix }}</span>
              <span
                v-else
                :style="`background-image:url(${item.thumbPath})`"
                class="attachments-group-item-img"
                loading="lazy"
              />
            </div>
          </a-spin>
        </a-col>
      </a-row>
      <a-divider />
      <div class="page-wrapper">
        <a-pagination
          :current="pagination.page"
          :defaultPageSize="pagination.size"
          :total="pagination.total"
          showLessItems
          @change="handlePaginationChange"
        ></a-pagination>
      </div>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-space>
          <a-button v-if="isChooseAvatar" type="dashed" @click="handleSelectGravatar">使用 Gravatar</a-button>
          <a-button type="primary" @click="handleShowUploadModal">上传附件</a-button>
        </a-space>
      </div>
    </a-drawer>

    <AttachmentUploadModal :visible.sync="uploadVisible" @close="onUploadClose" />
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'

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
      attachments: []
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
      apiClient.attachment
        .list(this.queryParam)
        .then(response => {
          this.attachments = response.data.content
          this.pagination.total = response.data.total
        })
        .finally(() => {
          this.loading = false
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
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleListAttachments()
      }
    },
    handleJudgeMediaType(attachment) {
      const mediaType = attachment.mediaType
      // 判断文件类型
      if (mediaType) {
        const prefix = mediaType.split('/')[0]

        return prefix === 'image'
      }
      return false
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>
