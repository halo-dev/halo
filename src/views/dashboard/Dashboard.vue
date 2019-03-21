<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="12"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
        <a-card
          :loading="postLoading"
          :bordered="false"
          title="最新文章"
          :bodyStyle="{ padding: '0px' }"
        >
          <a-table
            :columns="postColumns"
            :dataSource="postData"
            :pagination="false"
          >
          </a-table>
        </a-card>
      </a-col>
      <a-col
        :xl="12"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
        <a-card
          :loading="commentLoading"
          :bordered="false"
          title="最新评论"
          :bodyStyle="{ padding: '0px' }"
        >
          <a-table
            :columns="commentColumns"
            :dataSource="commentData"
            :pagination="false"
          >
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import postApi from '@/api/post'
import commentApi from '@/api/comment'
import logApi from '@/api/log'
import adminApi from '@/api/admin'

const postColumns = [
  {
    title: '标题',
    dataIndex: 'title',
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '状态',
    className: 'status',
    dataIndex: 'status'
  },
  {
    title: '发布时间',
    dataIndex: 'date'
  }
]

const commentColumns = [
  {
    title: '评论者',
    dataIndex: 'author',
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '状态',
    className: 'status',
    dataIndex: 'status'
  },
  {
    title: '内容',
    className: 'content',
    dataIndex: 'content'
  },
  {
    title: '发布时间',
    dataIndex: 'date'
  }
]

export default {
  name: 'Dashboard',
  components: {},
  data() {
    return {
      postLoading: true,
      commentLoading: true,
      logLoading: true,
      countsLoading: true,
      postColumns,
      postData: [],
      commentColumns,
      commentData: [],
      logData: [],
      countsData: null
    }
  },
  created() {
    this.getCounts()
    this.listLatestPosts()
    this.listLatestComments()
    this.listLatestLogs()
  },
  methods: {
    listLatestPosts() {
      postApi.listLatest().then(response => {
        this.postLoading = false
        this.postData = response.data.data
      })
    },
    listLatestComments() {
      commentApi.listLatest().then(response => {
        this.commentLoading = false
        this.commentData = response.data.data
      })
    },
    listLatestLogs() {
      logApi.listLatest().then(response => {
        this.logLoading = false
        this.logData = response.data.data
      })
    },
    getCounts() {
      adminApi.counts().then(response => {
        this.countsLoading = false
        this.countsData = response.data.data
      })
    }
  }
}
</script>

<style lang="less" scoped>
</style>
