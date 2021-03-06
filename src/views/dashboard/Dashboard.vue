<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12" class="mb-3">
        <analysis-card title="文章" :number="statisticsData.postCount">
          <router-link :to="{ name: 'PostWrite' }" slot="action">
            <a-icon v-if="statisticsLoading" type="loading" />
            <a-icon v-else type="plus" />
          </router-link>
        </analysis-card>
      </a-col>
      <a-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12" class="mb-3">
        <analysis-card title="评论" :number="statisticsData.commentCount">
          <router-link :to="{ name: 'Comments' }" slot="action">
            <a-icon v-if="statisticsLoading" type="loading" />
            <a-icon v-else type="unordered-list" />
          </router-link>
        </analysis-card>
      </a-col>
      <a-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12" class="mb-3">
        <analysis-card title="阅读量" :number="statisticsData.visitCount">
          <a-tooltip slot="action">
            <template slot="title"> 文章阅读共 {{ statisticsData.visitCount }} 次 </template>
            <a href="javascript:void(0);">
              <a-icon v-if="statisticsLoading" type="loading" />
              <a-icon v-else type="info-circle-o" />
            </a>
          </a-tooltip>
        </analysis-card>
      </a-col>
      <a-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12" class="mb-3">
        <analysis-card title="建立天数" :number="statisticsData.establishDays">
          <a-tooltip slot="action">
            <template slot="title">博客建立于 {{ statisticsData.birthday | moment }}</template>
            <a href="javascript:void(0);">
              <a-icon v-if="statisticsLoading" type="loading" />
              <a-icon v-else type="info-circle-o" />
            </a>
          </a-tooltip>
        </analysis-card>
      </a-col>
    </a-row>
    <a-row :gutter="12">
      <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24" class="mb-3">
        <a-card :bordered="false" title="新动态" :bodyStyle="{ padding: 0 }">
          <div class="card-container">
            <a-tabs type="card">
              <a-tab-pane key="1" tab="最近文章">
                <a-list :loading="activityLoading" :dataSource="latestPosts">
                  <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
                    <a-list-item-meta>
                      <a
                        v-if="item.status == 'PUBLISHED' || item.status == 'INTIMATE'"
                        slot="title"
                        :href="item.fullPath"
                        target="_blank"
                        >{{ item.title }}</a
                      >
                      <a
                        v-else-if="item.status == 'DRAFT'"
                        slot="title"
                        href="javascript:void(0)"
                        @click="handlePostPreview(item.id)"
                        >{{ item.title }}</a
                      >
                      <a v-else-if="item.status == 'RECYCLE'" slot="title" href="javascript:void(0);" disabled>
                        {{ item.title }}
                      </a>
                    </a-list-item-meta>
                    <div>{{ item.createTime | timeAgo }}</div>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
              <a-tab-pane key="2" tab="最近评论">
                <div class="custom-tab-wrapper">
                  <a-tabs>
                    <a-tab-pane tab="文章" key="1">
                      <recent-comment-tab type="posts"></recent-comment-tab>
                    </a-tab-pane>
                    <a-tab-pane tab="页面" key="2">
                      <recent-comment-tab type="sheets"></recent-comment-tab>
                    </a-tab-pane>
                    <!-- <a-tab-pane
                      tab="日志"
                      key="3"
                    >
                      <recent-comment-tab type="journals"></recent-comment-tab>
                    </a-tab-pane>-->
                  </a-tabs>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>
      </a-col>
      <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24" class="mb-3">
        <JournalPublishCard />
      </a-col>
      <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24" class="mb-3">
        <a-card :bordered="false" :bodyStyle="{ padding: '16px' }">
          <template slot="title">
            操作记录
            <a-tooltip slot="action" title="更多">
              <a href="javascript:void(0);" @click="logListDrawerVisible = true">
                <a-icon type="ellipsis" />
              </a>
            </a-tooltip>
          </template>
          <a-list :dataSource="formattedLogDatas" :loading="logLoading">
            <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
              <a-list-item-meta :description="item.createTime | timeAgo">
                <span slot="title">{{ item.type }}</span>
              </a-list-item-meta>
              <ellipsis :length="35" tooltip>{{ item.content }}</ellipsis>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <LogListDrawer :visible="logListDrawerVisible" @close="handleLogListClose" />
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import AnalysisCard from './components/AnalysisCard'
import JournalPublishCard from './components/JournalPublishCard'
import RecentCommentTab from './components/RecentCommentTab'
import LogListDrawer from './components/LogListDrawer'

import postApi from '@/api/post'
import logApi from '@/api/log'
import statisticsApi from '@/api/statistics'
export default {
  name: 'Dashboard',
  components: {
    PageView,
    AnalysisCard,
    JournalPublishCard,
    RecentCommentTab,
    LogListDrawer
  },
  data() {
    return {
      logTypes: logApi.logTypes,
      activityLoading: false,
      logLoading: false,
      statisticsLoading: true,
      logListDrawerVisible: false,
      latestPosts: [],
      latestLogs: [],
      statisticsData: {},
      journal: {
        content: ''
      },
      interval: null
    }
  },
  beforeMount() {
    this.handleLoadStatistics()
    this.handleListLatestPosts()
    this.handleListLatestLogs()
  },
  computed: {
    formattedLogDatas() {
      return this.latestLogs.map(log => {
        log.type = this.logTypes[log.type].text
        return log
      })
    }
  },
  destroyed: function() {
    if (this.logListDrawerVisible) {
      this.logListDrawerVisible = false
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.interval = setInterval(() => {
        vm.handleLoadStatistics()
      }, 5000)
    })
  },
  beforeRouteLeave(to, from, next) {
    if (this.interval) {
      clearInterval(this.interval)
      this.interval = null
      this.$log.debug('Cleared interval')
    }
    if (this.logListDrawerVisible) {
      this.logListDrawerVisible = false
    }
    next()
  },
  methods: {
    handleListLatestPosts() {
      this.activityLoading = true
      postApi
        .listLatest(5)
        .then(response => {
          this.latestPosts = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.activityLoading = false
          }, 200)
        })
    },
    handleListLatestLogs() {
      this.logLoading = true
      logApi
        .listLatest(5)
        .then(response => {
          this.latestLogs = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.logLoading = false
          }, 200)
        })
    },
    handleLoadStatistics() {
      statisticsApi
        .statistics()
        .then(response => {
          this.statisticsData = response.data.data
        })
        .catch(() => {
          clearInterval(this.interval)
        })
        .finally(() => {
          setTimeout(() => {
            this.statisticsLoading = false
          }, 200)
        })
    },
    handlePostPreview(postId) {
      postApi.preview(postId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    handleLogListClose() {
      this.logListDrawerVisible = false
      this.handleListLatestLogs()
    }
  }
}
</script>
