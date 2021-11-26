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
            <a href="javascript:void(0);">
              <a-icon v-if="statisticsLoading" type="loading" />
              <a-icon v-else type="info-circle-o" />
            </a>
          </a-tooltip>
        </analysis-card>
      </a-col>
      <a-col :lg="6" :md="12" :sm="12" :xl="6" :xs="12" class="mb-3">
        <analysis-card :number="statisticsData.establishDays" title="建立天数">
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
      <a-col :lg="8" :md="12" :sm="24" :xl="8" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: 0 }" :bordered="false" title="新动态">
          <div class="card-container">
            <a-tabs type="card">
              <a-tab-pane key="1" tab="最近文章">
                <a-list :dataSource="latestPosts" :loading="activityLoading">
                  <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
                    <a-list-item-meta>
                      <a
                        v-if="['PUBLISHED', 'INTIMATE'].includes(item.status)"
                        slot="title"
                        :href="item.fullPath"
                        target="_blank"
                      >
                        {{ item.title }}
                      </a>
                      <a
                        v-else-if="item.status === 'DRAFT'"
                        slot="title"
                        href="javascript:void(0)"
                        @click="handlePostPreview(item.id)"
                      >
                        {{ item.title }}
                      </a>
                      <a v-else-if="item.status === 'RECYCLE'" slot="title" disabled href="javascript:void(0);">
                        {{ item.title }}
                      </a>
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
            操作记录
            <a-tooltip slot="action" title="更多">
              <a href="javascript:void(0);" @click="logListDrawerVisible = true">
                <a-icon type="ellipsis" />
              </a>
            </a-tooltip>
          </template>
          <a-list :dataSource="formattedLogDatas" :loading="logLoading">
            <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
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

import apiClient from '@/utils/api-client'

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
      logTypes: {
        BLOG_INITIALIZED: {
          value: 0,
          text: '博客初始化'
        },
        POST_PUBLISHED: {
          value: 5,
          text: '文章发布'
        },
        POST_EDITED: {
          value: 15,
          text: '文章修改'
        },
        POST_DELETED: {
          value: 20,
          text: '文章删除'
        },
        LOGGED_IN: {
          value: 25,
          text: '用户登录'
        },
        LOGGED_OUT: {
          value: 30,
          text: '注销登录'
        },
        LOGIN_FAILED: {
          value: 35,
          text: '登录失败'
        },
        PASSWORD_UPDATED: {
          value: 40,
          text: '修改密码'
        },
        PROFILE_UPDATED: {
          value: 45,
          text: '资料修改'
        },
        SHEET_PUBLISHED: {
          value: 50,
          text: '页面发布'
        },
        SHEET_EDITED: {
          value: 55,
          text: '页面修改'
        },
        SHEET_DELETED: {
          value: 60,
          text: '页面删除'
        },
        MFA_UPDATED: {
          value: 65,
          text: '两步验证'
        },
        LOGGED_PRE_CHECK: {
          value: 70,
          text: '登录验证'
        }
      },
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
