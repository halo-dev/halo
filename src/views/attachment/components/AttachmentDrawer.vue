<template>
  <div>
    <a-drawer
      title="附件库"
      :width="isMobile()?'100%':'460'"
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
        v-model="detailVisible"
        v-if="selectedAttachment"
        :attachment="selectedAttachment"
        @delete="handleDelete"
      />
      <a-divider class="divider-transparent" />
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
      destroyOnClose
    >
      <FilePondUpload
        ref="upload"
        :uploadHandler="uploadHandler"
      ></FilePondUpload>
    </a-modal>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import AttachmentDetailDrawer from './AttachmentDetailDrawer'
import attachmentApi from '@/api/attachment'

export default {
  name: 'AttachmentDrawer',
  mixins: [mixin, mixinDevice],
  components: {
    AttachmentDetailDrawer
  },
  model: {
    prop: 'visible',
    event: 'close'
  },
  props: {
    visible: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      attachmentType: attachmentApi.type,
      detailVisible: false,
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
      uploadHandler: attachmentApi.upload
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
    handleShowDetailDrawer(attachment) {
      this.selectedAttachment = attachment
      this.$log.debug('Show detail of', attachment)
      this.detailVisible = true
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
      this.$refs.upload.handleClearFileList()
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
