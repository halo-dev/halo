<template>
  <div>
    <a-drawer
      title="附件库"
      :width="isMobile()?'100%':'480'"
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
            <a-empty v-if="formattedDatas.length==0" />
            <div
              v-else
              class="attach-item"
              v-for="(item, index) in formattedDatas"
              :key="index"
              @click="handleShowDetailDrawer(item)"
              @contextmenu.prevent="handleContextMenu($event, item)"
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
    handleShowDetailDrawer(attachment) {
      this.selectedAttachment = attachment
      this.$log.debug('Show detail of', attachment)
      this.detailVisible = true
    },
    handleContextMenu(event, item) {
      this.$contextmenu({
        items: [
          {
            label: '复制图片链接',
            onClick: () => {
              const text = `${encodeURI(item.path)}`
              this.$copyText(text)
                .then(message => {
                  this.$log.debug('copy', message)
                  this.$message.success('复制成功！')
                })
                .catch(err => {
                  this.$log.debug('copy.err', err)
                  this.$message.error('复制失败！')
                })
            },
            divided: true
          },
          {
            label: '复制 Markdown 格式链接',
            onClick: () => {
              const text = `![${item.name}](${encodeURI(item.path)})`
              this.$copyText(text)
                .then(message => {
                  this.$log.debug('copy', message)
                  this.$message.success('复制成功！')
                })
                .catch(err => {
                  this.$log.debug('copy.err', err)
                  this.$message.error('复制失败！')
                })
            }
          }
        ],
        event,
        zIndex: 1001,
        minWidth: 210
      })
      return false
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
    handleDelete() {
      this.loadAttachments()
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
