<template>
  <page-view>
    <a-row :gutter="12" align="middle" type="flex">
      <a-col :span="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col :md="6" :sm="24">
                  <a-form-item label="关键词：">
                    <a-input v-model="list.params.keyword" @keyup.enter="handleQuery()" />
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="存储位置：">
                    <a-select
                      v-model="list.params.attachmentType"
                      :loading="types.loading"
                      allowClear
                      @change="handleQuery()"
                    >
                      <a-select-option v-for="item in types.data" :key="item" :value="item">
                        {{ item | typeText }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="文件类型：">
                    <a-select
                      v-model="list.params.mediaType"
                      :loading="mediaTypes.loading"
                      allowClear
                      @change="handleQuery()"
                    >
                      <a-select-option v-for="(item, index) in mediaTypes.data" :key="index" :value="item"
                        >{{ item }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <span class="table-page-search-submitButtons">
                    <a-space>
                      <a-button type="primary" @click="handleQuery()">查询</a-button>
                      <a-button @click="handleResetParam()">重置</a-button>
                    </a-space>
                  </span>
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div class="mb-0 table-operator">
            <a-button icon="cloud-upload" type="primary" @click="upload.visible = true">上传</a-button>
            <a-button v-show="!supportMultipleSelection" icon="select" @click="handleMultipleSelection">
              批量操作
            </a-button>
            <a-button
              v-show="supportMultipleSelection"
              icon="delete"
              type="danger"
              @click="handleDeleteAttachmentInBatch"
            >
              删除
            </a-button>
            <a-button v-show="supportMultipleSelection" icon="close" @click="handleCancelMultipleSelection">
              取消
            </a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-list
          :dataSource="list.data"
          :grid="{ gutter: 12, xs: 2, sm: 2, md: 4, lg: 6, xl: 6, xxl: 6 }"
          :loading="list.loading"
          class="attachments-group"
        >
          <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
            <a-card
              :bodyStyle="{ padding: 0 }"
              hoverable
              @click="handleOpenDetail(item)"
              @contextmenu.prevent="handleContextMenu($event, item)"
            >
              <div class="attach-thumb attachments-group-item">
                <span v-if="!isImage(item)" class="attachments-group-item-type">{{ item.suffix }}</span>
                <span
                  v-else
                  :style="`background-image:url(${item.thumbPath})`"
                  class="attachments-group-item-img"
                  loading="lazy"
                />
              </div>
              <a-card-meta class="p-2">
                <template #description>
                  <a-tooltip :title="item.name">
                    <div class="truncate">{{ item.name }}</div>
                  </a-tooltip>
                </template>
              </a-card-meta>
              <a-checkbox
                v-show="supportMultipleSelection"
                :checked="getCheckStatus(item.id)"
                :style="getCheckStatus(item.id) ? selectedAttachmentStyle : ''"
                class="select-attachment-checkbox"
                @click="handleAttachmentSelectionChanged($event, item)"
              ></a-checkbox>
            </a-card>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
    <div class="page-wrapper">
      <a-pagination
        :current="pagination.page"
        :defaultPageSize="pagination.size"
        :pageSizeOptions="['18', '36', '54', '72', '90', '108']"
        :total="pagination.total"
        class="pagination"
        showLessItems
        showSizeChanger
        @change="handlePageChange"
        @showSizeChange="handlePageSizeChange"
      />
    </div>

    <AttachmentUploadModal :visible.sync="upload.visible" @close="onUploadClose" />

    <AttachmentDetailModal
      :addToPhoto="true"
      :attachment="list.selected"
      :visible.sync="detailVisible"
      @delete="handleListAttachments()"
    >
      <template #extraFooter>
        <a-button :disabled="selectPreviousButtonDisabled" @click="handleSelectPrevious">上一项</a-button>
        <a-button :disabled="selectNextButtonDisabled" @click="handleSelectNext">下一项</a-button>
      </template>
    </AttachmentDetailModal>
  </page-view>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { PageView } from '@/layouts'
import apiClient from '@/utils/api-client'
import { mapGetters } from 'vuex'
import { attachmentTypes } from '@/core/constant'

export default {
  components: {
    PageView
  },
  mixins: [mixin, mixinDevice],
  filters: {
    typeText(type) {
      return attachmentTypes[type].text
    }
  },
  data() {
    return {
      list: {
        data: [],
        loading: false,
        total: 0,
        hasNext: false,
        hasPrevious: false,
        selected: {},
        params: {
          page: 0,
          size: 18,
          keyword: undefined,
          mediaType: undefined,
          attachmentType: undefined
        }
      },

      mediaTypes: {
        data: [],
        loading: false
      },

      types: {
        data: [],
        loading: false
      },

      upload: {
        visible: false
      },

      detailVisible: false,

      supportMultipleSelection: false,
      selectedAttachmentCheckbox: {},
      batchSelectedAttachments: []
    }
  },
  computed: {
    selectedAttachmentStyle() {
      return {
        border: `2px solid ${this.color()}`
      }
    },
    isImage() {
      return function (attachment) {
        if (!attachment || !attachment.mediaType) {
          return false
        }
        return attachment.mediaType.startsWith('image')
      }
    },
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    },
    selectPreviousButtonDisabled() {
      const index = this.list.data.findIndex(attachment => attachment.id === this.list.selected.id)
      return index === 0 && !this.list.hasPrevious
    },
    selectNextButtonDisabled() {
      const index = this.list.data.findIndex(attachment => attachment.id === this.list.selected.id)
      return index === this.list.data.length - 1 && !this.list.hasNext
    }
  },
  created() {
    this.handleListAttachments()
    this.handleListMediaTypes()
    this.handleListTypes()
  },
  methods: {
    ...mapGetters(['color']),

    /**
     * List attachments
     */
    async handleListAttachments() {
      try {
        this.list.loading = true

        const response = await apiClient.attachment.list(this.list.params)

        this.list.data = response.data.content
        this.list.total = response.data.total
        this.list.hasNext = response.data.hasNext
        this.list.hasPrevious = response.data.hasPrevious
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.list.loading = false
      }
    },

    /**
     * List attachment media types
     */
    async handleListMediaTypes() {
      try {
        this.mediaTypes.loading = true

        const response = await apiClient.attachment.listMediaTypes()

        this.mediaTypes.data = response.data
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.mediaTypes.loading = false
      }
    },

    /**
     * List attachment upload types
     */
    async handleListTypes() {
      try {
        this.types.loading = true

        const response = await apiClient.attachment.listTypes()

        this.types.data = response.data
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.types.loading = false
      }
    },

    /**
     * Handle open attachment detail modal event
     */
    handleOpenDetail(attachment) {
      this.list.selected = attachment
      this.detailVisible = !this.supportMultipleSelection
    },

    /**
     * Show context menu
     */
    handleContextMenu(event, item) {
      this.$contextmenu({
        items: [
          {
            label: `复制${this.isImage(item) ? '图片' : '文件'}链接`,
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
            disabled: !this.isImage(item),
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
            },
            divided: true
          },
          {
            label: '删除',
            onClick: () => {
              this.$confirm({
                title: '提示',
                content: '确定删除该附件？',
                okText: '确定',
                cancelText: '取消',
                onOk: async () => {
                  await apiClient.attachment.delete(item.id)
                  await this.handleListAttachments()
                }
              })
            }
          }
        ],
        event,
        minWidth: 210
      })
      return false
    },

    /**
     * Handle page change
     */
    handlePageChange(page = 1) {
      this.list.params.page = page - 1
      this.handleListAttachments()
    },

    /**
     * Handle page size change
     */
    handlePageSizeChange(current, size) {
      this.$log.debug(`Current: ${current}, PageSize: ${size}`)
      this.list.params.page = 0
      this.list.params.size = size
      this.handleListAttachments()
    },

    /**
     * Reset query params
     */
    handleResetParam() {
      this.list.params.keyword = undefined
      this.list.params.mediaType = undefined
      this.list.params.attachmentType = undefined
      this.handlePageChange()
      this.handleListMediaTypes()
      this.handleListTypes()
    },

    /**
     * Search attachments
     */
    handleQuery() {
      this.handlePageChange()
    },
    onUploadClose() {
      this.handlePageChange()
      this.handleListMediaTypes()
      this.handleListTypes()
    },
    getCheckStatus(key) {
      return this.selectedAttachmentCheckbox[key] || false
    },
    handleMultipleSelection() {
      this.supportMultipleSelection = true
      // 不允许附件详情抽屉显示
      this.detailVisible = false
      this.list.data.forEach(item => {
        this.$set(this.selectedAttachmentCheckbox, item.id, false)
      })
    },
    handleCancelMultipleSelection() {
      this.supportMultipleSelection = false
      this.detailVisible = false
      this.batchSelectedAttachments = []
      for (const key in this.selectedCheckbox) {
        this.$set(this.selectedAttachmentCheckbox, key, false)
      }
    },
    handleAttachmentSelectionChanged(e, item) {
      const isChecked = e.target.checked || false
      if (isChecked) {
        this.$set(this.selectedAttachmentCheckbox, item.id, true)
        this.batchSelectedAttachments.push(item.id)
      } else {
        this.$set(this.selectedAttachmentCheckbox, item.id, false)
        // 从选中id集合中删除id
        const index = this.batchSelectedAttachments.indexOf(item.id)
        this.batchSelectedAttachments.splice(index, 1)
      }
    },

    /**
     * Deletes selected attachments
     */
    handleDeleteAttachmentInBatch() {
      const that = this
      if (this.batchSelectedAttachments.length <= 0) {
        this.$message.warn('你还未选择任何附件，请至少选择一个！')
        return
      }
      this.$confirm({
        title: '确定要批量删除选中的附件吗?',
        content: '一旦删除不可恢复，请谨慎操作',
        onOk() {
          apiClient.attachment
            .deleteInBatch(that.batchSelectedAttachments)
            .then(() => {
              that.handleCancelMultipleSelection()
              that.$message.success('删除成功')
            })
            .finally(() => {
              that.handleListAttachments()
            })
        },
        onCancel() {}
      })
    },

    /**
     * Select previous attachment
     */
    async handleSelectPrevious() {
      const index = this.list.data.findIndex(item => item.id === this.list.selected.id)
      if (index > 0) {
        this.list.selected = this.list.data[index - 1]
        return
      }
      if (index === 0 && this.list.hasPrevious) {
        this.list.params.page--
        await this.handleListAttachments()

        this.list.selected = this.list.data[this.list.data.length - 1]
      }
    },

    /**
     * Select next attachment
     */
    async handleSelectNext() {
      const index = this.list.data.findIndex(item => item.id === this.list.selected.id)
      if (index < this.list.data.length - 1) {
        this.list.selected = this.list.data[index + 1]
        return
      }
      if (index === this.list.data.length - 1 && this.list.hasNext) {
        this.list.params.page++
        await this.handleListAttachments()

        this.list.selected = this.list.data[0]
      }
    }
  }
}
</script>
