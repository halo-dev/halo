<template>
  <page-view>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col
              :md="6"
              :sm="24"
            >
              <a-form-item label="关键词">
                <a-input v-model="queryParam.keyword" />
              </a-form-item>
            </a-col>
            <a-col
              :md="6"
              :sm="24"
            >
              <a-form-item label="评论状态">
                <a-select
                  v-model="queryParam.status"
                  placeholder="请选择评论状态"
                >
                  <a-select-option
                    v-for="status in Object.keys(commentStatus)"
                    :key="status"
                    :value="status"
                  >{{ commentStatus[status].text }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col
              :md="12"
              :sm="24"
            >
              <span class="table-page-search-submitButtons">
                <a-button
                  type="primary"
                  @click="loadComments"
                >查询</a-button>
                <a-button
                  style="margin-left: 8px;"
                  @click="handleResetParam"
                >重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <a-dropdown>
          <a-menu slot="overlay">
            <a-menu-item key="1">
              <a-icon type="delete" />回收站
            </a-menu-item>
          </a-menu>
          <a-button>
            批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
        <a-button
          type="primary"
          icon="message"
          v-if="tableListMode"
          @click="toggleTableMode"
        >会话模式</a-button>
        <a-button
          type="primary"
          icon="ordered-list"
          v-else
          @click="toggleTableMode"
        >列表模式</a-button>
      </div>
      <div style="margin-top:15px">
        <a-table
          :rowKey="comment => comment.id"
          :columns="columns"
          :dataSource="formattedComments"
          :loading="commentsLoading"
          :pagination="false"
        >
          <span
            slot="status"
            slot-scope="statusProperty"
          >
            <a-badge :status="statusProperty.status" />
            {{ statusProperty.text }}
          </span>
          <a
            slot="post"
            slot-scope="post"
            :href="post.url"
            target="_blank"
          >{{ post.title }}</a>
          <span
            slot="createTime"
            slot-scope="createTime"
          >{{ createTime | timeAgo }}</span>
          <span
            slot="action"
            slot-scope="text, record"
          >
            <a
              href="javascript:;"
              @click="handleEditComment(record.id)"
            >通过</a>
            <a-divider type="vertical" />
            <a
              href="javascript:;"
              @click="handleDeleteComment(record.id)"
            >删除</a>
          </span>
        </a-table>
        <div class="page-wrapper">
          <a-pagination
            class="pagination"
            :total="pagination.total"
            :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
            showSizeChanger
            @showSizeChange="handlePaginationChange"
            @change="handlePaginationChange"
          />
        </div>
      </div>
    </a-card>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import commentApi from '@/api/comment'
const columns = [
  {
    title: '昵称',
    dataIndex: 'author'
  },
  {
    title: '内容',
    dataIndex: 'content'
  },
  {
    title: '状态',
    className: 'status',
    dataIndex: 'statusProperty',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '评论页面',
    dataIndex: 'post',
    scopedSlots: { customRender: 'post' }
  },
  {
    title: '日期',
    dataIndex: 'createTime',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  name: 'CommentList',
  components: {
    PageView
  },
  data() {
    return {
      columns,
      pagination: {
        current: 1,
        pageSize: 10,
        sort: null
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
      commentsLoading: false,
      tableListMode: true,
      commentStatus: commentApi.commentStatus
    }
  },
  computed: {
    formattedComments() {
      return this.comments.map(comment => {
        comment.statusProperty = this.commentStatus[comment.status]
        return comment
      })
    }
  },
  created() {
    this.loadComments()
  },
  methods: {
    loadComments() {
      this.commentsLoading = true
      // Set from pagination
      this.queryParam.page = this.pagination.current - 1
      this.queryParam.size = this.pagination.pageSize
      this.queryParam.sort = this.pagination.sort
      if (this.tableListMode) {
        commentApi.query(this.queryParam).then(response => {
          this.comments = response.data.data.content
          this.pagination.total = response.data.data.total
          this.commentsLoading = false
        })
      }else{
        // TODO tree view
      }
    },
    handleEditComment(id) {
      this.$message.success('编辑')
    },
    handleDeleteComment(id) {
      this.$message.success('删除')
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.current = page
      this.pagination.pageSize = pageSize
      this.loadComments()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.status = null
      this.loadComments()
    },
    toggleTableMode() {
      this.tableListMode = !this.tableListMode
      this.loadComments()
    }
  }
}
</script>
<style scoped>
.pagination {
  margin-top: 1rem;
}
</style>
