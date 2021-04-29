<template>
  <div class="comment-tab-wrapper">
    <a-card :bordered="false" :bodyStyle="{ padding: 0 }">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="关键词：">
                <a-input v-model="queryParam.keyword" @keyup.enter="handleQuery()" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="评论状态：">
                <a-select v-model="queryParam.status" placeholder="请选择评论状态" @change="handleQuery()" allowClear>
                  <a-select-option v-for="status in Object.keys(commentStatus)" :key="status" :value="status">{{
                    commentStatus[status].text
                  }}</a-select-option>
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
        <a-dropdown v-show="queryParam.status != null && queryParam.status != '' && !isMobile()">
          <a-menu slot="overlay">
            <a-menu-item key="1" v-if="queryParam.status === 'AUDITING'">
              <a href="javascript:void(0);" @click="handleEditStatusMore(commentStatus.PUBLISHED.value)">
                通过
              </a>
            </a-menu-item>
            <a-menu-item key="2" v-if="queryParam.status === 'PUBLISHED' || queryParam.status === 'AUDITING'">
              <a href="javascript:void(0);" @click="handleEditStatusMore(commentStatus.RECYCLE.value)">
                移到回收站
              </a>
            </a-menu-item>
            <a-menu-item key="3" v-if="queryParam.status === 'RECYCLE'">
              <a href="javascript:void(0);" @click="handleDeleteMore">
                永久删除
              </a>
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
          itemLayout="vertical"
          size="large"
          :pagination="false"
          :dataSource="formattedComments"
          :loading="loading"
        >
          <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
            <template slot="actions">
              <a-dropdown placement="topLeft" :trigger="['click']">
                <span>
                  <a-icon type="bars" />
                </span>
                <a-menu slot="overlay">
                  <a-menu-item v-if="item.status === 'AUDITING'">
                    <a href="javascript:;" @click="handleEditStatusClick(item.id, 'PUBLISHED')">通过</a>
                  </a-menu-item>
                  <a-menu-item v-if="item.status === 'AUDITING'">
                    <a href="javascript:;" @click="handleReplyAndPassClick(item)">通过并回复</a>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'PUBLISHED'">
                    <a href="javascript:;" @click="handleReplyClick(item)">回复</a>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'RECYCLE'">
                    <a-popconfirm
                      :title="'你确定要还原该评论？'"
                      @confirm="handleEditStatusClick(item.id, 'PUBLISHED')"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:;">还原</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'AUDITING'">
                    <a-popconfirm
                      :title="'你确定要将该评论移到回收站？'"
                      @confirm="handleEditStatusClick(item.id, 'RECYCLE')"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:;">回收站</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'RECYCLE'">
                    <a-popconfirm
                      :title="'你确定要永久删除该评论？'"
                      @confirm="handleDeleteClick(item.id)"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:;">删除</a>
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
              <a-avatar slot="avatar" size="large" :src="item.avatar" />
              <span
                slot="title"
                style="max-width: 300px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
                v-if="item.authorUrl"
              >
                <a-icon type="user" v-if="item.isAdmin" style="margin-right: 3px;" />&nbsp;
                <a :href="item.authorUrl" target="_blank">{{ item.author }}</a>
                &nbsp;<small style="color:rgba(0, 0, 0, 0.45)">{{ item.createTime | timeAgo }}</small>
              </span>
              <span
                slot="title"
                style="max-width: 300px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
                v-else
              >
                <a-icon type="user" v-if="item.isAdmin" style="margin-right: 3px;" />&nbsp;{{ item.author }}&nbsp;<small
                  style="color:rgba(0, 0, 0, 0.45)"
                  >{{ item.createTime | timeAgo }}</small
                >
              </span>
            </a-list-item-meta>
            <p v-html="item.content"></p>
          </a-list-item>
        </a-list>
        <!-- Desktop -->
        <a-table
          v-else
          :rowKey="comment => comment.id"
          :rowSelection="{
            selectedRowKeys: selectedRowKeys,
            onChange: onSelectionChange,
            getCheckboxProps: getCheckboxProps
          }"
          :columns="columns"
          :dataSource="formattedComments"
          :loading="loading"
          :pagination="false"
          scrollToFirstRowOnChange
        >
          <template slot="author" slot-scope="text, record">
            <a-icon type="user" v-if="record.isAdmin" style="margin-right: 3px;" />
            <a :href="record.authorUrl" target="_blank" v-if="record.authorUrl">{{ text }}</a>
            <span v-else>{{ text }}</span>
          </template>
          <p class="comment-content-wrapper" slot="content" slot-scope="content" v-html="content"></p>
          <span slot="status" slot-scope="statusProperty">
            <a-badge :status="statusProperty.status" :text="statusProperty.text" />
          </span>
          <a v-if="type === 'posts'" slot="post" slot-scope="post" :href="post.fullPath" target="_blank">{{
            post.title
          }}</a>
          <a v-if="type === 'sheets'" slot="sheet" slot-scope="sheet" :href="sheet.fullPath" target="_blank">{{
            sheet.title
          }}</a>
          <span slot="createTime" slot-scope="createTime">
            <a-tooltip placement="top">
              <template slot="title">
                {{ createTime | moment }}
              </template>
              {{ createTime | timeAgo }}
            </a-tooltip>
          </span>
          <span slot="action" slot-scope="text, record">
            <a-dropdown :trigger="['click']" v-if="record.status === 'AUDITING'">
              <a href="javascript:void(0);" class="ant-dropdown-link">通过</a>
              <a-menu slot="overlay">
                <a-menu-item key="1">
                  <a href="javascript:void(0);" @click="handleEditStatusClick(record.id, 'PUBLISHED')">通过</a>
                </a-menu-item>
                <a-menu-item key="2">
                  <a href="javascript:void(0);" @click="handleReplyAndPassClick(record)">通过并回复</a>
                </a-menu-item>
              </a-menu>
            </a-dropdown>

            <a href="javascript:void(0);" v-else-if="record.status === 'PUBLISHED'" @click="handleReplyClick(record)"
              >回复</a
            >

            <a-popconfirm
              :title="'你确定要还原该评论？'"
              @confirm="handleEditStatusClick(record.id, 'PUBLISHED')"
              okText="确定"
              cancelText="取消"
              v-else-if="record.status === 'RECYCLE'"
            >
              <a href="javascript:;">还原</a>
            </a-popconfirm>

            <a-divider type="vertical" />

            <a-popconfirm
              :title="'你确定要将该评论移到回收站？'"
              @confirm="handleEditStatusClick(record.id, 'RECYCLE')"
              okText="确定"
              cancelText="取消"
              v-if="record.status === 'PUBLISHED' || record.status === 'AUDITING'"
            >
              <a href="javascript:;">回收站</a>
            </a-popconfirm>

            <a-popconfirm
              :title="'你确定要永久删除该评论？'"
              @confirm="handleDeleteClick(record.id)"
              okText="确定"
              cancelText="取消"
              v-else-if="record.status === 'RECYCLE'"
            >
              <a href="javascript:;">删除</a>
            </a-popconfirm>
          </span>
        </a-table>
        <div class="page-wrapper">
          <a-pagination
            class="pagination"
            :current="pagination.page"
            :total="pagination.total"
            :defaultPageSize="pagination.size"
            :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
            showSizeChanger
            @showSizeChange="handlePaginationChange"
            @change="handlePaginationChange"
            showLessItems
          />
        </div>
      </div>
    </a-card>

    <a-modal
      v-if="selectedComment"
      :title="'回复给：' + selectedComment.author"
      v-model="replyCommentVisible"
      @close="onReplyClose"
      destroyOnClose
    >
      <template slot="footer">
        <ReactiveButton
          type="primary"
          @click="handleCreateClick"
          @callback="handleRepliedCallback"
          :loading="replying"
          :errored="replyErrored"
          text="回复"
          loadedText="回复成功"
          erroredText="回复失败"
        ></ReactiveButton>
      </template>
      <a-form-model ref="replyCommentForm" :model="replyComment" :rules="replyCommentRules" layout="vertical">
        <a-form-model-item prop="content">
          <a-input ref="contentInput" type="textarea" :autoSize="{ minRows: 8 }" v-model="replyComment.content" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import marked from 'marked'
import commentApi from '@/api/comment'

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
export default {
  name: 'CommentTab',
  mixins: [mixin, mixinDevice],
  props: {
    type: {
      type: String,
      required: false,
      default: 'posts',
      validator: function(value) {
        return ['posts', 'sheets', 'journals'].indexOf(value) !== -1
      }
    }
  },
  data() {
    return {
      columns: this.type === 'posts' ? postColumns : sheetColumns,
      replyCommentVisible: false,
      pagination: {
        page: 1,
        size: 10,
        sort: null,
        total: 1
      },
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null,
        status: null
      },
      selectedRowKeys: [],
      selectedRows: [],
      comments: [],
      selectedComment: {},
      replyComment: {},
      replyCommentRules: {
        content: [{ required: true, message: '* 内容不能为空', trigger: ['change'] }]
      },
      loading: false,
      commentStatus: commentApi.commentStatus,
      replying: false,
      replyErrored: false
    }
  },
  created() {
    this.handleListComments()
  },
  computed: {
    formattedComments() {
      return this.comments.map(comment => {
        comment.statusProperty = this.commentStatus[comment.status]
        comment.content = marked(comment.content)
        return comment
      })
    }
  },
  methods: {
    handleListComments() {
      this.loading = true
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      commentApi
        .queryComment(this.type, this.queryParam)
        .then(response => {
          this.comments = response.data.data.content
          this.pagination.total = response.data.data.total
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 200)
        })
    },
    handleQuery() {
      this.handleClearRowKeys()
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleEditStatusClick(commentId, status) {
      commentApi
        .updateStatus(this.type, commentId, status)
        .then(() => {
          this.$message.success('操作成功！')
        })
        .finally(() => {
          this.handleListComments()
        })
    },
    handleDeleteClick(commentId) {
      commentApi
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
          commentApi
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
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.handleListComments()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.status = null
      this.handleClearRowKeys()
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleEditStatusMore(status) {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.info('请至少选择一项！')
        return
      }
      commentApi
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
      commentApi
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
          disabled: this.queryParam.status == null || this.queryParam.status === '',
          name: comment.author
        }
      }
    }
  }
}
</script>
