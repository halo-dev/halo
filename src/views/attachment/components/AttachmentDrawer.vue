<template>
  <div>
    <a-drawer
      title="附件库"
      :width="isMobile()?'100%':'460'"
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
          v-model="queryParam.keyword"
          @search="loadAttachments(true)"
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
              v-for="(item, index) in formattedDatas"
              :key="index"
              @click="handleShowDetailDrawer(item)"
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

      <AttachmentDetailDrawer
        v-model="detailVisiable"
        v-if="selectedAttachment"
        :attachment="selectedAttachment"
        @delete="handleDelete"
      />
      <a-divider class="divider-transparent"/>
      <div class="bottom-control">
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
import AttachmentDetailDrawer from './AttachmentDetailDrawer'

export default {
  name: 'AttachmentDrawer',
  mixins: [mixin, mixinDevice],
  components: {
    AttachmentDetailDrawer
  },
  model: {
    prop: 'visiable',
    event: 'close'
  },
  props: {
    visiable: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      attachmentType: attachmentApi.type,
      detailVisiable: false,
      attachmentDrawerVisible: false,
      uploadVisible: false,
      skeletonLoading: true,
      pagination: {
        page: 1,
        size: 12,
        sort: ''
      },
      queryParam: {
        page: 0,
        size: 18,
        sort: null,
        keyword: null
      },
      attachments: [],
      selectedAttachment: {},
      attachmentUploadHandler: attachmentApi.upload
    }
  },
  computed: {
    formattedDatas() {
      return this.attachments.map(attachment => {
        attachment.typeProperty = this.attachmentType[attachment.type]
        return attachment
      })
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
    handleShowUploadModal() {
      this.uploadVisible = true
    },
    handleShowDetailDrawer(attachment) {
      this.selectedAttachment = attachment
      this.$log.debug('Show detail of', attachment)
      this.detailVisiable = true
    },
    loadAttachments(isSearch) {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      if (isSearch) {
        this.queryParam.page = 0
      }
      attachmentApi.query(this.queryParam).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
      })
    },
    handlePaginationChange(page, pageSize) {
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadAttachments()
    },
    onUploadClose() {
      this.loadSkeleton()
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
