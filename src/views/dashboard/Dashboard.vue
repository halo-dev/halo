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
          :loading="statisticsLoading"
          title="文章"
          :number="statisticsData.postCount"
        >
          <router-link
            :to="{ name:'PostEdit' }"
            slot="action"
          >
            <a-icon type="plus" />
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
          :loading="statisticsLoading"
          title="评论"
          :number="statisticsData.commentCount"
        >
          <router-link
            :to="{ name:'Comments' }"
            slot="action"
          >
            <a-icon type="unordered-list" />
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
          :loading="statisticsLoading"
          title="阅读量"
          :number="statisticsData.visitCount"
        >
          <a-tooltip slot="action">
            <template slot="title">
              文章阅读共 {{ statisticsData.visitCount }} 次
            </template>
            <a href="javascript:void(0);">
              <a-icon type="info-circle-o" />
            </a>
          </a-tooltip>
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
          :loading="statisticsLoading"
          title="建立天数"
          :number="statisticsData.establishDays"
        >
          <a-tooltip slot="action">
            <template slot="title">博客建立于 {{ statisticsData.birthday | moment }}</template>
            <a href="javascript:void(0);">
              <a-icon type="info-circle-o" />
            </a>
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
          :loading="activityLoading"
          :bordered="false"
          title="新动态"
          :bodyStyle="{ padding: 0 }"
        >
          <div class="card-container">
            <a-tabs type="card">
              <a-tab-pane
                key="1"
                tab="最近文章"
              >
                <a-list :dataSource="latestPosts">
                  <a-list-item
                    slot="renderItem"
                    slot-scope="item, index"
                    :key="index"
                  >
                    <a-list-item-meta>
                      <a
                        v-if="item.status=='PUBLISHED' || item.status == 'INTIMATE'"
                        slot="title"
                        :href="item.fullPath"
                        target="_blank"
                      >{{ item.title }}</a>
                      <a
                        v-else-if="item.status=='DRAFT'"
                        slot="title"
                        href="javascript:void(0)"
                        @click="handlePostPreview(item.id)"
                      >{{ item.title }}</a>
                      <a
                        v-else-if="item.status=='RECYCLE'"
                        slot="title"
                        href="javascript:void(0);"
                        disabled
                      >
                        {{ item.title }}
                      </a>
                    </a-list-item-meta>
                    <div>{{ item.createTime | timeAgo }}</div>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
              <a-tab-pane
                key="2"
                tab="最近评论"
              >
                <div class="custom-tab-wrapper">
                  <a-tabs>
                    <a-tab-pane
                      tab="文章"
                      key="1"
                    >
                      <recent-comment-tab type="posts"></recent-comment-tab>
                    </a-tab-pane>
                    <a-tab-pane
                      tab="页面"
                      key="2"
                    >
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
      <a-col
        :xl="8"
        :lg="8"
        :md="12"
        :sm="24"
        :xs="24"
        :style="{ marginBottom: '12px' }"
      >
        <a-card
          :bordered="false"
          :loading="writeLoading"
          :bodyStyle="{ padding: '16px' }"
        >
          <template slot="title">
            速记
            <a-tooltip
              slot="action"
              title="内容将保存到页面/所有页面/日志页面"
            >
              <a-icon type="info-circle-o" />
            </a-tooltip>
          </template>
          <a-form layout="vertical">
            <a-form-item>
              <a-input
                type="textarea"
                :autoSize="{ minRows: 8 }"
                v-model="journal.sourceContent"
                placeholder="写点什么吧..."
              />
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                @click="handleCreateJournalClick"
              >保存</a-button>
            </a-form-item>
          </a-form>
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
          :bodyStyle="{ padding: '16px' }"
        >
          <template slot="title">
            操作记录
            <a-tooltip
              slot="action"
              title="更多"
            >
              <a
                href="javascript:void(0);"
                @click="logListDrawerVisible = true"
              >
                <a-icon type="ellipsis" />
              </a>
            </a-tooltip>
          </template>
          <a-list :dataSource="formattedLogDatas">
            <a-list-item
              slot="renderItem"
              slot-scope="item, index"
              :key="index"
            >
              <a-list-item-meta :description="item.createTime | timeAgo">
                <span slot="title">{{ item.type }}</span>
              </a-list-item-meta>
              <ellipsis
                :length="35"
                tooltip
              >{{ item.content }}</ellipsis>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <LogListDrawer
      :visible="logListDrawerVisible"
      @close="handleLogListClose"
    />
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import AnalysisCard from './components/AnalysisCard'
import RecentCommentTab from './components/RecentCommentTab'
import LogListDrawer from './components/LogListDrawer'
import countTo from 'vue-count-to'

import postApi from '@/api/post'
import logApi from '@/api/log'
import statisticsApi from '@/api/statistics'
import journalApi from '@/api/journal'
export default {
  name: 'Dashboard',
  components: {
    PageView,
    AnalysisCard,
    RecentCommentTab,
    countTo,
    LogListDrawer
  },
  data() {
    return {
      logType: logApi.logType,
      activityLoading: true,
      writeLoading: true,
      logLoading: true,
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
  created() {
    this.getStatistics()
    this.listLatestPosts()
    this.listLatestLogs()
  },
  computed: {
    formattedLogDatas() {
      return this.latestLogs.map(log => {
        log.type = this.logType[log.type].text
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
        vm.getStatistics()
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
    async listLatestPosts() {
      postApi.listLatest(5).then(response => {
        this.latestPosts = response.data.data
        this.activityLoading = false
      })
    },
    async listLatestLogs() {
      logApi.listLatest(5).then(response => {
        this.latestLogs = response.data.data
        this.logLoading = false
        this.writeLoading = false
      })
    },
    async getStatistics() {
      statisticsApi.statistics().then(response => {
        this.statisticsData = response.data.data
        this.statisticsLoading = false
      })
    },
    handleEditPostClick(post) {
      this.$router.push({ name: 'PostEdit', query: { postId: post.id } })
    },
    handleCreateJournalClick() {
      if (!this.journal.sourceContent) {
        this.$notification['error']({
          message: '提示',
          description: '内容不能为空！'
        })
        return
      }
      journalApi.create(this.journal).then(response => {
        this.$message.success('发表成功！')
        this.journal = {}
      })
    },
    handlePostPreview(postId) {
      postApi.preview(postId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    handleLogListClose() {
      this.logListDrawerVisible = false
    }
  }
}
</script>
