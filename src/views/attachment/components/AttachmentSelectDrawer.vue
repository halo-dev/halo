<template>
  <div>
    <a-drawer
      title="选择附件"
      :width="isMobile()?'100%':drawerWidth"
      closable
      :visible="visiable"
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
              @click="selectAttachment(item)"
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
      <a-divider />
      <div class="bottom-control">
        <a-button
          @click="showUploadModal"
          type="primary"
        >上传附件</a-button>
      </div>
    </a-drawer>

    <a-modal
      title="上传附件"
      v-model="uploadVisible"
      :footer="null"
    >
      <upload
        name="file"
        multiple
        accept="image/*"
        :uploadHandler="attachmentUploadHandler"
        @success="handleAttachmentUploadSuccess"
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
    prop: 'visiable',
    event: 'close'
  },
  props: {
    visiable: {
      type: Boolean,
      required: false,
      default: false
    },
    drawerWidth: {
      type: Number,
      required: false,
      default: 580
    }
  },
  data() {
    return {
      uploadVisible: false,
      skeletonLoading: true,
      pagination: {
        page: 1,
        size: 10,
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
    visiable: function(newValue, oldValue) {
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
    showUploadModal() {
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
    selectAttachment(item) {
      this.$emit('listenToSelect', item)
    },
    handlePaginationChange(page, pageSize) {
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadAttachments()
    },
    handleAttachmentUploadSuccess() {
      this.$message.success('上传成功')
      this.loadAttachments()
    },
    handleDelete() {
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
