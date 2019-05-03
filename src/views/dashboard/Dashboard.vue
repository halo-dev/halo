<template>
  <page-view>
    <a-row :gutter="24">
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '24px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="文章"
          :number="countsData.postCount"
        >
        </analysis-card>
      </a-col>
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '24px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="评论"
          :number="countsData.commentCount"
        >
        </analysis-card>
      </a-col>
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '24px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="附件"
          :number="countsData.attachmentCount"
        >
        </analysis-card>
      </a-col>
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '24px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="成立天数"
          :number="countsData.establishDays"
        >
        </analysis-card>
      </a-col>
    </a-row>
    <a-row :gutter="12">
      <a-col
        :xl="8"
        :lg="8"
        :md="12"
        :sm="24"
        :xs="24"
      >
        <a-card
          :loading="postLoading"
          :bordered="false"
          title="最新文章"
        >
          最新文章
        </a-card>
      </a-col>
      <a-col
        :xl="8"
        :lg="8"
        :md="12"
        :sm="24"
        :xs="24"
      >
        <a-card
          :loading="commentLoading"
          :bordered="false"
          title="最新评论"
        >
          最新评论
        </a-card>
      </a-col>
      <a-col
        :xl="8"
        :lg="8"
        :md="12"
        :sm="24"
        :xs="24"
      >
        <a-card
          :loading="commentLoading"
          :bordered="false"
          title="最新日志"
        >
          最新日志
        </a-card>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import AnalysisCard from './components/AnalysisCard'

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
    dataIndex: 'status',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '最后编辑时间',
    dataIndex: 'editTime',
    scopedSlots: { customRender: 'editTime' }
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
  components: {
    PageView,
    AnalysisCard
  },
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
  computed: {
    formattedPostData() {
      return Object.assign([], this.postData).map(post => {
        // Format the status
        post.status = postApi.postStatus[post.status]
        return post
      })
    }
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
