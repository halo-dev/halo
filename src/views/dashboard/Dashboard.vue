<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :lg="6" :md="12" :sm="12" :xl="6" :xs="12" class="mb-3">
        <analysis-card :number="statisticsData.postCount" title="文章">
          <router-link slot="action" :to="{ name: 'PostWrite' }">
            <a-icon v-if="statisticsLoading" type="loading" />
            <a-icon v-else type="plus" />
          </router-link>
        </analysis-card>
      </a-col>
      <a-col :lg="6" :md="12" :sm="12" :xl="6" :xs="12" class="mb-3">
        <analysis-card :number="statisticsData.commentCount" title="评论">
          <router-link slot="action" :to="{ name: 'Comments' }">
            <a-icon v-if="statisticsLoading" type="loading" />
            <a-icon v-else type="unordered-list" />
          </router-link>
        </analysis-card>
      </a-col>
      <a-col :lg="6" :md="12" :sm="12" :xl="6" :xs="12" class="mb-3">
        <analysis-card :number="statisticsData.visitCount" title="阅读量">
          <a-tooltip slot="action">
            <template slot="title"> 文章阅读共 {{ statisticsData.visitCount }} 次</template>
            <a-button class="!p-0" type="link">
              <a-icon v-if="statisticsLoading" type="loading" />
              <a-icon v-else type="info-circle-o" />
            </a-button>
          </a-tooltip>
        </analysis-card>
      </a-col>
      <a-col :lg="6" :md="12" :sm="12" :xl="6" :xs="12" class="mb-3">
        <analysis-card :number="statisticsData.establishDays" title="建立天数">
          <a-tooltip slot="action">
            <template slot="title">博客建立于 {{ statisticsData.birthday | moment }}</template>
            <a-button class="!p-0" type="link">
              <a-icon v-if="statisticsLoading" type="loading" />
              <a-icon v-else type="info-circle-o" />
            </a-button>
          </a-tooltip>
        </analysis-card>
      </a-col>
    </a-row>
    <a-row :gutter="12">
      <a-col :lg="8" :md="12" :sm="24" :xl="8" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: 0 }" :bordered="false" title="新动态">
          <div class="card-container">
            <a-tabs type="card">
              <a-tab-pane key="1" tab="最近文章">
                <a-list :dataSource="latestPosts" :loading="activityLoading">
                  <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
                    <a-list-item-meta>
                      <template #title>
                        <a v-if="['PUBLISHED', 'INTIMATE'].includes(item.status)" :href="item.fullPath" target="_blank">
                          {{ item.title }}
                        </a>

                        <a-button
                          v-else-if="item.status === 'DRAFT'"
                          class="!p-0"
                          type="link"
                          @click="handlePostPreview(item.id)"
                        >
                          {{ item.title }}
                        </a-button>

                        <a-button v-else-if="item.status === 'RECYCLE'" class="!p-0" disabled type="link">
                          {{ item.title }}
                        </a-button>
                      </template>
                    </a-list-item-meta>
                    <div>{{ item.createTime | timeAgo }}</div>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
              <a-tab-pane key="2" tab="最近评论">
                <div class="custom-tab-wrapper">
                  <a-tabs :animated="{ inkBar: true, tabPane: false }">
                    <a-tab-pane key="1" tab="文章">
                      <recent-comment-tab type="posts"></recent-comment-tab>
                    </a-tab-pane>
                    <a-tab-pane key="2" tab="页面">
                      <recent-comment-tab type="sheets"></recent-comment-tab>
                    </a-tab-pane>
                  </a-tabs>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>
      </a-col>
      <a-col :lg="8" :md="12" :sm="24" :xl="8" :xs="24" class="mb-3">
        <JournalPublishCard />
      </a-col>
      <a-col :lg="8" :md="12" :sm="24" :xl="8" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
          <template slot="title">
            操作日志
            <a-tooltip slot="action" title="更多">
              <router-link :to="{ name: 'SystemActionLogs' }">
                <a-icon type="ellipsis" />
              </router-link>
            </a-tooltip>
          </template>
          <a-list :dataSource="latestLogs" :loading="logLoading">
            <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
              <a-list-item-meta :description="item.createTime | timeAgo">
                <span slot="title">{{ item.type | typeConvert }}</span>
              </a-list-item-meta>
              <ellipsis :length="35" tooltip>{{ item.content }}</ellipsis>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import AnalysisCard from './components/AnalysisCard'
import JournalPublishCard from './components/JournalPublishCard'
import RecentCommentTab from './components/RecentCommentTab'

import apiClient from '@/utils/api-client'
import { actionLogTypes } from '@/core/constant'

export default {
  name: 'Dashboard',
  components: {
    PageView,
    AnalysisCard,
    JournalPublishCard,
    RecentCommentTab
  },
  data() {
    return {
      activityLoading: false,
      logLoading: false,
      statisticsLoading: true,
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
    next()
  },
  methods: {
    handleListLatestPosts() {
      this.activityLoading = true
      apiClient.post
        .latest(5)
        .then(response => {
          this.latestPosts = response.data
        })
        .finally(() => {
          this.activityLoading = false
        })
    },
    handleListLatestLogs() {
      this.logLoading = true
      apiClient.log
        .latest(5)
        .then(response => {
          this.latestLogs = response.data
        })
        .finally(() => {
          this.logLoading = false
        })
    },
    handleLoadStatistics() {
      apiClient.statistic
        .statistics()
        .then(response => {
          this.statisticsData = response.data
        })
        .catch(() => {
          clearInterval(this.interval)
        })
        .finally(() => {
          this.statisticsLoading = false
        })
    },
    handlePostPreview(postId) {
      apiClient.post.getPreviewLinkById(postId).then(response => {
        window.open(response, '_blank')
      })
    }
  },
  filters: {
    typeConvert(key) {
      const type = actionLogTypes[key]
      return type ? type.text : key
    }
  }
}
</script>
