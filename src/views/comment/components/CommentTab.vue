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
                  <a-select-option v-for="status in Object.keys(commentStatus)" :key="status" :value="status">
                    {{ commentStatus[status].text }}
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
          <a-menu slot="overlay">
            <a-menu-item v-if="list.params.status === 'AUDITING'" key="1">
              <a href="javascript:void(0);" @click="handleEditStatusMore(commentStatus.PUBLISHED.value)"> 通过 </a>
            </a-menu-item>
            <a-menu-item v-if="list.params.status === 'PUBLISHED' || list.params.status === 'AUDITING'" key="2">
              <a href="javascript:void(0);" @click="handleEditStatusMore(commentStatus.RECYCLE.value)"> 移到回收站 </a>
            </a-menu-item>
            <a-menu-item v-if="list.params.status === 'RECYCLE'" key="3">
              <a href="javascript:void(0);" @click="handleDeleteMore"> 永久删除 </a>
            </a-menu-item>
          </a-menu>
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
          :dataSource="formattedComments"
          :loading="list.loading"
          :pagination="false"
          itemLayout="vertical"
          size="large"
        >
          <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
            <template slot="actions">
              <a-dropdown :trigger="['click']" placement="topLeft">
                <span>
                  <a-icon type="bars" />
                </span>
                <a-menu slot="overlay">
                  <a-menu-item v-if="item.status === 'AUDITING'">
                    <a href="javascript:void(0);" @click="handleEditStatusClick(item.id, 'PUBLISHED')">通过</a>
                  </a-menu-item>
                  <a-menu-item v-if="item.status === 'AUDITING'">
                    <a href="javascript:void(0);" @click="handleReplyAndPassClick(item)">通过并回复</a>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'PUBLISHED'">
                    <a href="javascript:void(0);" @click="handleReplyClick(item)">回复</a>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'RECYCLE'">
                    <a-popconfirm
                      :title="'你确定要还原该评论？'"
                      cancelText="取消"
                      okText="确定"
                      @confirm="handleEditStatusClick(item.id, 'PUBLISHED')"
                    >
                      <a href="javascript:void(0);">还原</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'AUDITING'">
                    <a-popconfirm
                      :title="'你确定要将该评论移到回收站？'"
                      cancelText="取消"
                      okText="确定"
                      @confirm="handleEditStatusClick(item.id, 'RECYCLE')"
                    >
                      <a href="javascript:void(0);">回收站</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'RECYCLE'">
                    <a-popconfirm
                      :title="'你确定要永久删除该评论？'"
                      cancelText="取消"
                      okText="确定"
                      @confirm="handleDeleteClick(item.id)"
                    >
                      <a href="javascript:void(0);">删除</a>
                    </a-popconfirm>
                  </a-menu-item>
                </a-menu>
              </a-dropdown>
            </template>
            <template slot="extra">
              <span>
                <a-badge :status="item.statusProperty.status" :text="item.statusProperty.text" />
              </span>
            </template>
            <a-list-item-meta>
              <template slot="description">
                发表在
                <a v-if="type === 'posts'" :href="item.post.fullPath" target="_blank">《{{ item.post.title }}》</a>
                <a v-if="type === 'sheets'" :href="item.sheet.fullPath" target="_blank">《{{ item.sheet.title }}》</a>
              </template>
              <a-avatar slot="avatar" :src="item.avatar" size="large" />
              <span
                v-if="item.authorUrl"
                slot="title"
                style="max-width: 300px; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis"
              >
                <a-icon v-if="item.isAdmin" style="margin-right: 3px" type="user" />&nbsp;
                <a :href="item.authorUrl" target="_blank">{{ item.author }}</a>
                &nbsp;<small style="color: rgba(0, 0, 0, 0.45)">{{ item.createTime | timeAgo }}</small>
              </span>
              <span
                v-else
                slot="title"
                style="max-width: 300px; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis"
              >
                <a-icon v-if="item.isAdmin" style="margin-right: 3px" type="user" />&nbsp;{{ item.author }}&nbsp;<small
                  style="color: rgba(0, 0, 0, 0.45)"
                >
                  {{ item.createTime | timeAgo }}
                </small>
              </span>
            </a-list-item-meta>
            <p v-html="item.content"></p>
          </a-list-item>
        </a-list>
        <!-- Desktop -->
        <a-table
          v-else
          :columns="columns"
          :dataSource="formattedComments"
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
          <template slot="author" slot-scope="text, record">
            <a-icon v-if="record.isAdmin" style="margin-right: 3px" type="user" />
            <a v-if="record.authorUrl" :href="record.authorUrl" target="_blank">{{ text }}</a>
            <span v-else>{{ text }}</span>
          </template>
          <p slot="content" slot-scope="content" class="comment-content-wrapper" v-html="content"></p>
          <span slot="status" slot-scope="statusProperty">
            <a-badge :status="statusProperty.status" :text="statusProperty.text" />
          </span>
          <a v-if="type === 'posts'" slot="post" slot-scope="post" :href="post.fullPath" target="_blank">
            {{ post.title }}
          </a>
          <a v-if="type === 'sheets'" slot="sheet" slot-scope="sheet" :href="sheet.fullPath" target="_blank">
            {{ sheet.title }}
          </a>
          <span slot="createTime" slot-scope="createTime">
            <a-tooltip placement="top">
              <template slot="title">
                {{ createTime | moment }}
              </template>
              {{ createTime | timeAgo }}
            </a-tooltip>
          </span>
          <span slot="action" slot-scope="text, record">
            <a-dropdown v-if="record.status === 'AUDITING'" :trigger="['click']">
              <a class="ant-dropdown-link" href="javascript:void(0);">通过</a>
              <a-menu slot="overlay">
                <a-menu-item key="1">
                  <a href="javascript:void(0);" @click="handleEditStatusClick(record.id, 'PUBLISHED')">通过</a>
                </a-menu-item>
                <a-menu-item key="2">
                  <a href="javascript:void(0);" @click="handleReplyAndPassClick(record)">通过并回复</a>
                </a-menu-item>
              </a-menu>
            </a-dropdown>

            <a v-else-if="record.status === 'PUBLISHED'" href="javascript:void(0);" @click="handleReplyClick(record)">
              回复
            </a>

            <a-popconfirm
              v-else-if="record.status === 'RECYCLE'"
              :title="'你确定要还原该评论？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleEditStatusClick(record.id, 'PUBLISHED')"
            >
              <a href="javascript:void(0);">还原</a>
            </a-popconfirm>

            <a-divider type="vertical" />

            <a-popconfirm
              v-if="record.status === 'PUBLISHED' || record.status === 'AUDITING'"
              :title="'你确定要将该评论移到回收站？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleEditStatusClick(record.id, 'RECYCLE')"
            >
              <a href="javascript:void(0);">回收站</a>
            </a-popconfirm>

            <a-popconfirm
              v-else-if="record.status === 'RECYCLE'"
              :title="'你确定要永久删除该评论？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleDeleteClick(record.id)"
            >
              <a href="javascript:void(0);">删除</a>
            </a-popconfirm>
          </span>
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

    <a-modal
      v-if="selectedComment"
      v-model="replyCommentVisible"
      :title="'回复给：' + selectedComment.author"
      destroyOnClose
      @close="onReplyClose"
    >
      <template slot="footer">
        <ReactiveButton
          :errored="replyErrored"
          :loading="replying"
          erroredText="回复失败"
          loadedText="回复成功"
          text="回复"
          type="primary"
          @callback="handleRepliedCallback"
          @click="handleCreateClick"
        ></ReactiveButton>
      </template>
      <a-form-model ref="replyCommentForm" :model="replyComment" :rules="replyCommentRules" layout="vertical">
        <a-form-model-item prop="content">
          <a-input ref="contentInput" v-model="replyComment.content" :autoSize="{ minRows: 8 }" type="textarea" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { marked } from 'marked'
import apiClient from '@/utils/api-client'

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
    className: 'status',
    dataIndex: 'statusProperty',
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
    className: 'status',
    dataIndex: 'statusProperty',
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

const commentStatus = {
  PUBLISHED: {
    value: 'PUBLISHED',
    color: 'green',
    status: 'success',
    text: '已发布'
  },
  AUDITING: {
    value: 'AUDITING',
    color: 'yellow',
    status: 'warning',
    text: '待审核'
  },
  RECYCLE: {
    value: 'RECYCLE',
    color: 'red',
    status: 'error',
    text: '回收站'
  }
}
export default {
  name: 'CommentTab',
  mixins: [mixin, mixinDevice],
  props: {
    type: {
      type: String,
      required: false,
      default: 'posts',
      validator: function (value) {
        return ['posts', 'sheets', 'journals'].indexOf(value) !== -1
      }
    }
  },
  data() {
    return {
      commentStatus,
      replyCommentVisible: false,

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
      replyComment: {},
      replyCommentRules: {
        content: [{ required: true, message: '* 内容不能为空', trigger: ['change'] }]
      },
      replying: false,
      replyErrored: false
    }
  },
  created() {
    this.handleListComments()
  },
  computed: {
    formattedComments() {
      return this.list.data.map(comment => {
        comment.statusProperty = this.commentStatus[comment.status]
        comment.content = marked.parse(comment.content)
        return comment
      })
    },
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    },
    columns() {
      return this.type === 'posts' ? postColumns : sheetColumns
    }
  },
  methods: {
    async handleListComments() {
      try {
        this.list.loading = true

        const response = await apiClient.comment.list(this.type, this.list.params)

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
    handleEditStatusClick(commentId, status) {
      apiClient.comment
        .updateStatusById(this.type, commentId, status)
        .then(() => {
          this.$message.success('操作成功！')
        })
        .finally(() => {
          this.handleListComments()
        })
    },
    handleDeleteClick(commentId) {
      apiClient.comment
        .delete(this.type, commentId)
        .then(() => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListComments()
        })
    },
    handleReplyAndPassClick(comment) {
      this.handleReplyClick(comment)
      this.handleEditStatusClick(comment.id, 'PUBLISHED')
    },
    handleReplyClick(comment) {
      this.selectedComment = comment
      this.replyCommentVisible = true
      this.replyComment.parentId = comment.id
      if (this.type === 'posts') {
        this.replyComment.postId = comment.post.id
      } else {
        this.replyComment.postId = comment.sheet.id
      }
      this.$nextTick(() => {
        this.$refs.contentInput.focus()
      })
    },
    handleCreateClick() {
      const _this = this
      _this.$refs.replyCommentForm.validate(valid => {
        if (valid) {
          _this.replying = true
          apiClient.comment
            .create(_this.type, _this.replyComment)
            .catch(() => {
              _this.replyErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                _this.replying = false
              }, 400)
            })
        }
      })
    },
    handleRepliedCallback() {
      if (this.replyErrored) {
        this.replyErrored = false
      } else {
        this.replyComment = {}
        this.selectedComment = {}
        this.replyCommentVisible = false
        this.handleListComments()
      }
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
    handleEditStatusMore(status) {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.info('请至少选择一项！')
        return
      }
      apiClient.comment
        .updateStatusInBatch(this.type, this.selectedRowKeys, status)
        .then(() => {
          this.$log.debug(`commentIds: ${this.selectedRowKeys}, status: ${status}`)
          this.selectedRowKeys = []
        })
        .finally(() => {
          this.handleListComments()
        })
    },
    handleDeleteMore() {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.info('请至少选择一项！')
        return
      }
      apiClient.comment
        .deleteInBatch(this.type, this.selectedRowKeys)
        .then(() => {
          this.$log.debug(`delete: ${this.selectedRowKeys}`)
          this.selectedRowKeys = []
        })
        .finally(() => {
          this.handleListComments()
        })
    },
    handleClearRowKeys() {
      this.selectedRowKeys = []
    },
    onReplyClose() {
      this.replyComment = {}
      this.selectedComment = {}
      this.replyCommentVisible = false
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
  }
}
</script>
