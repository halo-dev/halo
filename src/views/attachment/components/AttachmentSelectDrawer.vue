<template>
  <div>
    <a-drawer
      :title="title"
      :width="isMobile()?'100%':drawerWidth"
      closable
      :visible="visible"
      destroyOnClose
      @close="onClose"
    >
      <a-row
        type="flex"
        align="middle"
      >
        <a-input-search
          placeholder="搜索附件"
          enterButton
        />
      </a-row>
      <a-divider />
      <a-row
        type="flex"
        align="middle"
      >
        <a-skeleton
          active
          :loading="skeletonLoading"
          :paragraph="{ rows: 18 }"
        >
          <a-col :span="24">
            <div
              class="attach-item"
              v-for="(item, index) in attachments"
              :key="index"
              @click="handleSelectAttachment(item)"
            >
              <img :src="item.thumbPath">
            </div>
          </a-col>
        </a-skeleton>
      </a-row>
      <a-divider />
      <div class="page-wrapper">
        <a-pagination
          :defaultPageSize="pagination.size"
          :total="pagination.total"
          @change="handlePaginationChange"
        ></a-pagination>
      </div>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-button
          type="dashed"
          style="marginRight: 8px"
          v-if="isChooseAvatar"
          @click="handleSelectGravatar"
        >使用 Gravatar</a-button>
        <a-button
          @click="handleShowUploadModal"
          type="primary"
        >上传附件</a-button>
      </div>
    </a-drawer>

    <a-modal
      title="上传附件"
      v-model="uploadVisible"
      :footer="null"
      :afterClose="onUploadClose"
    >
      <upload
        name="file"
        multiple
        :uploadHandler="attachmentUploadHandler"
      >
        <p class="ant-upload-drag-icon">
          <a-icon type="inbox" />
        </p>
        <p class="ant-upload-text">点击选择文件或将文件拖拽到此处</p>
        <p class="ant-upload-hint">支持单个或批量上传</p>
      </upload>
    </a-modal>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
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
      default: 460
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
      skeletonLoading: true,
      pagination: {
        page: 1,
        size: 12,
        sort: ''
      },
      attachments: [],
      attachmentUploadHandler: attachmentApi.upload
    }
  },
  created() {
    this.loadSkeleton()
    this.loadAttachments()
  },
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
      }
    }
  },
  methods: {
    loadSkeleton() {
      this.skeletonLoading = true
      setTimeout(() => {
        this.skeletonLoading = false
      }, 500)
    },
    handleShowUploadModal() {
      this.uploadVisible = true
    },
    loadAttachments() {
      const pagination = Object.assign({}, this.pagination)
      pagination.page--
      attachmentApi.query(pagination).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
      })
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
      this.loadAttachments()
    },
    handleAttachmentUploadSuccess() {
      this.$message.success('上传成功！')
      this.loadAttachments()
    },
    onUploadClose() {
      this.loadSkeleton()
      this.loadAttachments()
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>

<style lang="less" scope>
.attach-item {
  width: 50%;
  margin: 0 auto;
  position: relative;
  padding-bottom: 28%;
  overflow: hidden;
  float: left;
  cursor: pointer;
  img {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
  }
}
</style>
