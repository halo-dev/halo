<template>
  <page-view>
    <a-row :gutter="12">
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '12px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="文章"
          :number="countsData.postCount"
        >
          <router-link
            :to="{ name:'PostList' }"
            slot="action"
          >
            <a-icon type="link" />
          </router-link>
        </analysis-card>
      </a-col>
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '12px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="评论"
          :number="countsData.commentCount"
        >
          <router-link
            :to="{ name:'Comments' }"
            slot="action"
          >
            <a-icon type="link" />
          </router-link>
        </analysis-card>
      </a-col>
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '12px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="附件"
          :number="countsData.attachmentCount"
        >
          <router-link
            :to="{ name:'Attachments' }"
            slot="action"
          >
            <a-icon type="link" />
          </router-link>
        </analysis-card>
      </a-col>
      <a-col
        :xl="6"
        :lg="6"
        :md="12"
        :sm="12"
        :xs="12"
        :style="{ marginBottom: '12px' }"
      >
        <analysis-card
          :loading="countsLoading"
          title="建立天数"
          :number="countsData.establishDays"
        >
          <a-tooltip
            slot="action"
          >
            <template slot="title">
              博客建立于 {{ countsData.birthday | moment }}
            </template>
            <a href="javascript:void(0);">
              <a-icon type="info-circle-o" /></a>
          </a-tooltip>
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
        :style="{ marginBottom: '12px' }"
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
        :style="{ marginBottom: '12px' }"
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
        :style="{ marginBottom: '12px' }"
      >
        <a-card
          :loading="logLoading"
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
      countsData: {}
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
        this.countsData = response.data.data
        this.countsLoading = false
      })
    }
  }
}
</script>

<style lang="less" scoped>
</style>
