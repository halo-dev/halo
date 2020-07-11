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
          v-model="queryParam.keyword"
          @search="handleQuery()"
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
            <a-empty v-if="attachments.length==0" />
            <div
              v-else
              class="attach-item"
              v-for="(item, index) in attachments"
              :key="index"
              @click="handleSelectAttachment(item)"
            >
              <span v-show="!handleJudgeMediaType(item)">当前格式不支持预览</span>
              <img
                :src="item.thumbPath"
                v-show="handleJudgeMediaType(item)"
                loading="lazy"
              >
            </div>
          </a-col>
        </a-skeleton>
      </a-row>
      <a-divider />
      <div class="page-wrapper">
        <a-pagination
          :current="pagination.page"
          :total="pagination.total"
          :defaultPageSize="pagination.size"
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
      skeletonLoading: true,
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
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
        this.loadAttachments()
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
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      attachmentApi.query(this.queryParam).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
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
      this.loadAttachments()
    },
    onUploadClose() {
      this.$refs.upload.handleClearFileList()
      this.loadSkeleton()
      this.handlePaginationChange(1, this.pagination.size)
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
