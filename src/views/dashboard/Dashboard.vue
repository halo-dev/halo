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
          :number="statisticsData.postCount"
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
          :number="statisticsData.commentCount"
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
          title="总访问"
          :number="statisticsData.visitCount"
        >
          <a-tooltip slot="action">
            <template slot="title">
              文章总访问共
              <countTo
                :startVal="0"
                :endVal="statisticsData.visitCount"
                :duration="3000"
              ></countTo>次
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
          :loading="countsLoading"
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
                <a-list :dataSource="postData">
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
                        v-else
                        href="javascript:void(0);"
                        disabled
                      >
                        {{ text }}
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
                :autosize="{ minRows: 8 }"
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
                @click="handleShowLogDrawer"
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
              <div>{{ item.content }}</div>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <a-drawer
      title="操作日志"
      :width="isMobile()?'100%':'460'"
      closable
      :visible="logDrawerVisible"
      destroyOnClose
      @close="()=>this.logDrawerVisible = false"
    >
      <a-row
        type="flex"
        align="middle"
      >
        <a-col :span="24">
          <a-skeleton
            active
            :loading="logsLoading"
            :paragraph="{rows: 18}"
          >
            <a-list :dataSource="formattedLogsDatas">
              <a-list-item
                slot="renderItem"
                slot-scope="item, index"
                :key="index"
              >
                <a-list-item-meta :description="item.createTime | timeAgo">
                  <span slot="title">{{ item.type }}</span>
                </a-list-item-meta>
                <div>{{ item.content }}</div>
              </a-list-item>
            </a-list>
          </a-skeleton>

          <div class="page-wrapper">
            <a-pagination
              class="pagination"
              :current="logPagination.page"
              :total="logPagination.total"
              :defaultPageSize="logPagination.size"
              :pageSizeOptions="['50', '100','150','200']"
              showSizeChanger
              @showSizeChange="handlePaginationChange"
              @change="handlePaginationChange"
            />
          </div>
        </a-col>
      </a-row>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-popconfirm
          title="你确定要清空所有操作日志？"
          okText="确定"
          @confirm="handleClearLogs"
          cancelText="取消"
        >
          <a-button type="danger">清空操作日志</a-button>
        </a-popconfirm>
      </div>
    </a-drawer>
  </page-view>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { mapGetters } from 'vuex'
import { PageView } from '@/layouts'
import AnalysisCard from './components/AnalysisCard'
import RecentCommentTab from './components/RecentCommentTab'
import countTo from 'vue-count-to'

import postApi from '@/api/post'
import logApi from '@/api/log'
import statisticsApi from '@/api/statistics'
import journalApi from '@/api/journal'
export default {
  name: 'Dashboard',
  mixins: [mixin, mixinDevice],
  components: {
    PageView,
    AnalysisCard,
    RecentCommentTab,
    countTo
  },
  data() {
    return {
      startVal: 0,
      logType: logApi.logType,
      activityLoading: true,
      writeLoading: true,
      logLoading: true,
      logsLoading: true,
      countsLoading: true,
      logDrawerVisible: false,
      postData: [],
      logData: [],
      statisticsData: {},
      journal: {
        content: '',
        photos: []
      },
      logs: [],
      logPagination: {
        page: 1,
        size: 50,
        sort: null
      },
      logQueryParam: {
        page: 0,
        size: 50,
        sort: null
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
    formattedPostData() {
      return Object.assign([], this.postData).map(post => {
        // Format the status
        post.status = postApi.postStatus[post.status]
        return post
      })
    },
    formattedLogDatas() {
      return this.logData.map(log => {
        log.type = this.logType[log.type].text
        return log
      })
    },
    formattedLogsDatas() {
      return this.logs.map(log => {
        log.type = this.logType[log.type].text
        return log
      })
    }
  },
  destroyed: function() {
    if (this.logDrawerVisible) {
      this.logDrawerVisible = false
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
    if (this.logDrawerVisible) {
      this.logDrawerVisible = false
    }
    next()
  },
  methods: {
    listLatestPosts() {
      postApi.listLatest(5).then(response => {
        this.postData = response.data.data
        this.activityLoading = false
      })
    },
    listLatestLogs() {
      logApi.listLatest(5).then(response => {
        this.logData = response.data.data
        this.logLoading = false
        this.writeLoading = false
      })
    },
    getStatistics() {
      statisticsApi.statistics().then(response => {
        this.statisticsData = response.data.data
        this.countsLoading = false
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
    handleShowLogDrawer() {
      this.logDrawerVisible = true
      this.loadLogs()
    },
    loadLogs() {
      this.logsLoading = true
      setTimeout(() => {
        this.logsLoading = false
      }, 500)
      this.logQueryParam.page = this.logPagination.page - 1
      this.logQueryParam.size = this.logPagination.size
      this.logQueryParam.sort = this.logPagination.sort
      logApi.pageBy(this.logQueryParam).then(response => {
        this.logs = response.data.data.content
        this.logPagination.total = response.data.data.total
      })
    },
    handleClearLogs() {
      logApi.clear().then(response => {
        this.$message.success('清除成功！')
        this.loadLogs()
        this.listLatestLogs()
      })
    },
    handlePostPreview(postId) {
      postApi.preview(postId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.logPagination.page = page
      this.logPagination.size = pageSize
      this.loadLogs()
    }
  }
}
</script>
