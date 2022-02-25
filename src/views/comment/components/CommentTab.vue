<template>
  <div class="comment-tab-wrapper">
    <a-card :bodyStyle="{ padding: 0 }" :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="关键词：">
                <a-input v-model="list.params.keyword" @keyup.enter="handleQuery()" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="评论状态：">
                <a-select v-model="list.params.status" allowClear placeholder="请选择评论状态" @change="handleQuery()">
                  <a-select-option v-for="status in Object.keys(commentStatuses)" :key="status" :value="status">
                    {{ commentStatuses[status].text }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="12" :sm="24">
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

      <div class="table-operator">
        <a-dropdown v-show="list.params.status != null && list.params.status !== '' && !isMobile()">
          <template #overlay>
            <a-menu>
              <a-menu-item
                v-if="list.params.status === commentStatuses.AUDITING.value"
                key="1"
                @click="handleChangeStatusInBatch(commentStatuses.PUBLISHED.value)"
              >
                通过
              </a-menu-item>
              <a-menu-item
                v-if="[commentStatuses.PUBLISHED.value, commentStatuses.AUDITING.value].includes(list.params.status)"
                key="2"
                @click="handleChangeStatusInBatch(commentStatuses.RECYCLE.value)"
              >
                移到回收站
              </a-menu-item>
              <a-menu-item
                v-if="list.params.status === commentStatuses.RECYCLE.value"
                key="3"
                @click="handleDeleteInBatch"
              >
                永久删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>

      <div class="mt-4">
        <!-- Mobile -->
        <a-list
          v-if="isMobile()"
          :dataSource="list.data"
          :loading="list.loading"
          :pagination="false"
          itemLayout="vertical"
          size="large"
        >
          <template #renderItem="item, index">
            <a-list-item :key="index">
              <template #actions>
                <a-dropdown :trigger="['click']" placement="topLeft">
                  <span>
                    <a-icon type="bars" />
                  </span>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item
                        v-if="item.status === commentStatuses.AUDITING.value"
                        @click="handleChangeStatus(item.id, commentStatuses.PUBLISHED.value)"
                      >
                        通过
                      </a-menu-item>
                      <a-menu-item
                        v-if="item.status === commentStatuses.AUDITING.value"
                        @click="handlePublishAndReply(item)"
                      >
                        通过并回复
                      </a-menu-item>
                      <a-menu-item
                        v-else-if="item.status === commentStatuses.PUBLISHED.value"
                        @click="handleOpenReplyModal(item)"
                      >
                        回复
                      </a-menu-item>
                      <a-menu-item v-else-if="item.status === commentStatuses.RECYCLE.value">
                        <a-popconfirm
                          :title="'你确定要还原该评论？'"
                          cancelText="取消"
                          okText="确定"
                          @confirm="handleChangeStatus(item.id, commentStatuses.PUBLISHED.value)"
                        >
                          还原
                        </a-popconfirm>
                      </a-menu-item>
                      <a-menu-item
                        v-if="[commentStatuses.PUBLISHED.value, commentStatuses.AUDITING.value].includes(item.status)"
                      >
                        <a-popconfirm
                          :title="'你确定要将该评论移到回收站？'"
                          cancelText="取消"
                          okText="确定"
                          @confirm="handleChangeStatus(item.id, commentStatuses.RECYCLE.value)"
                        >
                          回收站
                        </a-popconfirm>
                      </a-menu-item>
                      <a-menu-item v-else-if="item.status === commentStatuses.RECYCLE.value">
                        <a-popconfirm
                          :title="'你确定要永久删除该评论？'"
                          cancelText="取消"
                          okText="确定"
                          @confirm="handleDelete(item.id)"
                        >
                          删除
                        </a-popconfirm>
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </template>
              <template #extra>
                <a-badge :status="commentStatuses[item.status].status" :text="item.status | statusText" />
              </template>
              <a-list-item-meta>
                <template #description>
                  发表在
                  <a v-if="targetName === 'posts'" :href="item.post.fullPath" target="_blank"
                    >《{{ item.post.title }}》</a
                  >
                  <a v-if="targetName === 'sheets'" :href="item.sheet.fullPath" target="_blank"
                    >《{{ item.sheet.title }}》</a
                  >
                </template>

                <template #avatar>
                  <a-avatar :src="item.avatar" size="large" />
                </template>

                <template #title>
                  <div class="truncate">
                    <a-icon v-if="item.isAdmin" class="mr-2" type="user" />

                    <a v-if="item.authorUrl" :href="item.authorUrl" class="mr-1" target="_blank">{{ item.author }}</a>
                    <span v-else class="mr-1">{{ item.author }}</span>

                    <small style="color: rgba(0, 0, 0, 0.45)">
                      {{ item.createTime | timeAgo }}
                    </small>
                  </div>
                </template>
              </a-list-item-meta>
              <p v-html="$options.filters.markdownRender(item.content)"></p>
            </a-list-item>
          </template>
        </a-list>
        <!-- Desktop -->
        <a-table
          v-else
          :columns="columns"
          :dataSource="list.data"
          :loading="list.loading"
          :pagination="false"
          :rowKey="comment => comment.id"
          :rowSelection="{
            selectedRowKeys: selectedRowKeys,
            onChange: onSelectionChange,
            getCheckboxProps: getCheckboxProps
          }"
          scrollToFirstRowOnChange
        >
          <template #author="text, record">
            <a-icon v-if="record.isAdmin" class="mr-2" type="user" />
            <a v-if="record.authorUrl" :href="record.authorUrl" target="_blank">{{ text }}</a>
            <span v-else>{{ text }}</span>
          </template>

          <template #content="content">
            <p class="comment-content-wrapper" v-html="$options.filters.markdownRender(content)"></p>
          </template>

          <template #status="status">
            <a-badge :status="commentStatuses[status].status" :text="status | statusText" />
          </template>

          <template v-if="targetName === 'posts'" #post="post">
            <a :href="post.fullPath" target="_blank">
              {{ post.title }}
            </a>
          </template>

          <template v-if="targetName === 'sheets'" #sheet="sheet">
            <a :href="sheet.fullPath" target="_blank">
              {{ sheet.title }}
            </a>
          </template>

          <template #createTime="createTime">
            <a-tooltip placement="top">
              <template #title>
                {{ createTime | moment }}
              </template>
              {{ createTime | timeAgo }}
            </a-tooltip>
          </template>

          <template #action="text, record">
            <a-dropdown v-if="record.status === commentStatuses.AUDITING.value" :trigger="['click']">
              <a-button class="!p-0" type="link">通过</a-button>

              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="handleChangeStatus(record.id, commentStatuses.PUBLISHED.value)">
                    通过
                  </a-menu-item>
                  <a-menu-item key="2" @click="handlePublishAndReply(record)"> 通过并回复</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>

            <a-button
              v-else-if="record.status === commentStatuses.PUBLISHED.value"
              class="!p-0"
              type="link"
              @click="handleOpenReplyModal(record)"
            >
              回复
            </a-button>

            <a-popconfirm
              v-else-if="record.status === commentStatuses.RECYCLE.value"
              :title="'你确定要还原该评论？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleChangeStatus(record.id, commentStatuses.PUBLISHED.value)"
            >
              <a-button class="!p-0" type="link">还原</a-button>
            </a-popconfirm>

            <a-divider type="vertical" />

            <a-popconfirm
              v-if="[commentStatuses.PUBLISHED.value, commentStatuses.AUDITING.value].includes(record.status)"
              :title="'你确定要将该评论移到回收站？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleChangeStatus(record.id, commentStatuses.RECYCLE.value)"
            >
              <a-button class="!p-0" type="link">回收站</a-button>
            </a-popconfirm>

            <a-popconfirm
              v-else-if="record.status === commentStatuses.RECYCLE.value"
              :title="'你确定要永久删除该评论？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleDelete(record.id)"
            >
              <a-button class="!p-0" type="link">删除</a-button>
            </a-popconfirm>
          </template>
        </a-table>
        <div class="page-wrapper">
          <a-pagination
            :current="pagination.page"
            :defaultPageSize="pagination.size"
            :pageSizeOptions="['10', '20', '50', '100']"
            :total="pagination.total"
            class="pagination"
            showLessItems
            showSizeChanger
            @change="handlePageChange"
            @showSizeChange="handlePageSizeChange"
          />
        </div>
      </div>
    </a-card>

    <CommentReplyModal
      :comment="selectedComment"
      :target="target"
      :target-id="targetId"
      :visible.sync="replyModalVisible"
      @succeed="onReplyModalClose"
    />
  </div>
</template>
<script>
// components
import CommentReplyModal from '@/components/Comment/CommentReplyModal'

import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'
import { commentStatuses } from '@/core/constant'

const postColumns = [
  {
    title: '昵称',
    dataIndex: 'author',
    width: '150px',
    ellipsis: true,
    scopedSlots: { customRender: 'author' }
  },
  {
    title: '内容',
    dataIndex: 'content',
    scopedSlots: { customRender: 'content' }
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: '100px',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '评论文章',
    dataIndex: 'post',
    width: '200px',
    ellipsis: true,
    scopedSlots: { customRender: 'post' }
  },
  {
    title: '日期',
    dataIndex: 'createTime',
    width: '170px',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '180px',
    scopedSlots: { customRender: 'action' }
  }
]
const sheetColumns = [
  {
    title: '昵称',
    dataIndex: 'author',
    width: '150px',
    ellipsis: true,
    scopedSlots: { customRender: 'author' }
  },
  {
    title: '内容',
    dataIndex: 'content',
    scopedSlots: { customRender: 'content' }
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: '100px',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '评论页面',
    dataIndex: 'sheet',
    width: '200px',
    ellipsis: true,
    scopedSlots: { customRender: 'sheet' }
  },
  {
    title: '日期',
    dataIndex: 'createTime',
    width: '170px',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '180px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'CommentTab',
  components: { CommentReplyModal },
  mixins: [mixin, mixinDevice],
  props: {
    target: {
      type: String,
      required: false,
      default: 'post',
      validator: function (value) {
        return ['post', 'sheet', 'journal'].indexOf(value) !== -1
      }
    }
  },
  data() {
    return {
      commentStatuses,

      list: {
        data: [],
        loading: false,
        total: 0,
        hasPrevious: false,
        hasNext: false,
        params: {
          page: 0,
          size: 10,
          keyword: null,
          status: null
        }
      },

      selectedRowKeys: [],
      selectedRows: [],
      selectedComment: {},
      replyModalVisible: false
    }
  },
  created() {
    this.handleListComments()
  },
  computed: {
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    },
    columns() {
      return this.targetName === 'posts' ? postColumns : sheetColumns
    },
    targetName() {
      return `${this.target}s`
    },
    targetId() {
      if (Object.keys(this.selectedComment).length === 0) {
        return 0
      }
      if (this.targetName === 'posts') {
        return this.selectedComment.post.id
      }
      if (this.targetName === 'sheets') {
        return this.selectedComment.sheet.id
      }
      return 0
    }
  },
  methods: {
    async handleListComments() {
      try {
        this.list.loading = true

        const response = await apiClient.comment.list(this.targetName, this.list.params)

        this.list.data = response.data.content
        this.list.total = response.data.total
        this.list.hasPrevious = response.data.hasPrevious
        this.list.hasNext = response.data.hasNext
      } catch (e) {
        this.$log.error(e)
      } finally {
        this.list.loading = false
      }
    },

    handleQuery() {
      this.handleClearRowKeys()
      this.handlePageChange(1)
    },

    async handleChangeStatus(commentId, status) {
      try {
        await apiClient.comment.updateStatusById(this.targetName, commentId, status)
        this.$message.success('操作成功！')
      } catch (e) {
        this.$log.error('Failed to change comment status', e)
      } finally {
        await this.handleListComments()
      }
    },

    async handleChangeStatusInBatch(status) {
      if (!this.selectedRowKeys.length) {
        this.$message.info('请至少选择一项！')
        return
      }

      try {
        this.$log.debug(`commentIds: ${this.selectedRowKeys}, status: ${status}`)
        await apiClient.comment.updateStatusInBatch(this.targetName, this.selectedRowKeys, status)
        this.selectedRowKeys = []
      } catch (e) {
        this.$log.error('Failed to change comment status in batch', e)
      } finally {
        await this.handleListComments()
      }
    },

    async handleDelete(commentId) {
      try {
        await apiClient.comment.delete(this.targetName, commentId)
        this.$message.success('删除成功！')
      } catch (e) {
        this.$log.error('Failed to delete comment', e)
      } finally {
        await this.handleListComments()
      }
    },

    async handleDeleteInBatch() {
      if (!this.selectedRowKeys.length) {
        this.$message.info('请至少选择一项！')
        return
      }

      try {
        this.$log.debug(`delete: ${this.selectedRowKeys}`)
        await apiClient.comment.deleteInBatch(this.targetName, this.selectedRowKeys)
        this.selectedRowKeys = []
      } catch (e) {
        this.$log.error('Failed to delete comments in batch', e)
      } finally {
        await this.handleListComments()
      }
    },

    async handlePublishAndReply(comment) {
      await this.handleChangeStatus(comment.id, this.commentStatuses.PUBLISHED.value)
      this.handleOpenReplyModal(comment)
    },

    handleOpenReplyModal(comment) {
      this.selectedComment = comment
      this.replyModalVisible = true
    },

    /**
     * Handle page change
     */
    handlePageChange(page = 1) {
      this.list.params.page = page - 1
      this.handleListComments()
    },

    /**
     * Handle page size change
     */
    handlePageSizeChange(current, size) {
      this.$log.debug(`Current: ${current}, PageSize: ${size}`)
      this.list.params.page = 0
      this.list.params.size = size
      this.handleListComments()
    },

    handleResetParam() {
      this.list.params.keyword = null
      this.list.params.status = null
      this.handleClearRowKeys()
      this.handlePageChange(1)
    },

    handleClearRowKeys() {
      this.selectedRowKeys = []
    },

    onReplyModalClose() {
      this.selectedComment = {}
      this.replyModalVisible = false
      this.handleListComments()
    },

    onSelectionChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
      this.$log.debug(`SelectedRowKeys: ${selectedRowKeys}`)
    },

    getCheckboxProps(comment) {
      return {
        props: {
          disabled: this.list.params.status == null || this.list.params.status === '',
          name: comment.author
        }
      }
    }
  },
  filters: {
    statusText(type) {
      return type ? commentStatuses[type].text : ''
    }
  }
}
</script>
