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
          title="总访问"
          :number="countsData.visitCount"
        >
          <a-tooltip slot="action">
            <template slot="title">
              文章总访问共 {{ countsData.visitCount }} 次
            </template>
            <a href="javascript:void(0);">
              <a-icon type="info-circle-o" /></a>
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
          :number="countsData.establishDays"
        >
          <a-tooltip slot="action">
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
          :loading="activityLoading"
          :bordered="false"
          title="新动态"
          :bodyStyle="{ padding: 0 }"
        >
          <div class="card-container">
            <a-tabs
              defaultActiveKey="1"
              type="card"
            >
              <a-tab-pane key="1">
                <span slot="tab">
                  最近文章
                </span>
                <a-list :dataSource="postData">
                  <a-list-item
                    slot="renderItem"
                    slot-scope="item, index"
                    :key="index"
                  >
                    <a-list-item-meta>
                      <a
                        slot="title"
                        href="javascript:void(0);"
                        @click="handleEditPostClick(item)"
                      >{{ item.title }}</a>
                    </a-list-item-meta>
                    <div>{{ item.createTime | timeAgo }}</div>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
              <a-tab-pane
                key="2"
                forceRender
              >
                <span slot="tab">
                  最近评论
                </span>
                <a-list
                  itemLayout="horizontal"
                  :dataSource="commentData"
                >
                  <a-list-item
                    slot="renderItem"
                    slot-scope="item, index"
                    :key="index"
                  >
                    <a-comment :avatar="'//gravatar.loli.net/avatar/'+item.gavatarMd5+'/?s=256&d=mp'">
                      <template slot="author">
                        {{ item.author }} 发表在 《<a
                          href="javascript:void(0);"
                          target="_blank"
                        >{{ item.post.title }}</a>》
                      </template>
                      <template slot="actions">
                        <span>回复</span>
                      </template>
                      <p slot="content">{{ item.content }}</p>
                      <a-tooltip
                        slot="datetime"
                        :title="item.createTime | moment"
                      >
                        <span>{{ item.createTime | timeAgo }}</span>
                      </a-tooltip>
                    </a-comment>
                  </a-list-item>
                </a-list>
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
          :bodyStyle="{ padding: '16px' }"
        >
          <template slot="title">
            速记 <a-tooltip
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
                v-model="journal.content"
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
          title="日志记录"
          :bodyStyle="{ padding: '16px' }"
        >
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
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import AnalysisCard from './components/AnalysisCard'

import postApi from '@/api/post'
import commentApi from '@/api/comment'
import logApi from '@/api/log'
import adminApi from '@/api/admin'
import journalApi from '@/api/journal'
export default {
  name: 'Dashboard',
  components: {
    PageView,
    AnalysisCard
  },
  data() {
    return {
      logType: logApi.logType,
      activityLoading: true,
      logLoading: true,
      countsLoading: true,
      postData: [],
      commentData: [],
      logData: [],
      countsData: {},
      journal: {}
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
    },
    formattedLogDatas() {
      return this.logData.map(log => {
        log.type = this.logType[log.type].text
        return log
      })
    }
  },
  methods: {
    listLatestPosts() {
      postApi.listLatest(5).then(response => {
        this.postData = response.data.data
        this.activityLoading = false
      })
    },
    listLatestComments() {
      commentApi.listLatest(5, 'PUBLISHED').then(response => {
        this.commentData = response.data.data
        this.activityLoading = false
      })
    },
    listLatestLogs() {
      logApi.listLatest(5).then(response => {
        this.logData = response.data.data
        this.logLoading = false
      })
    },
    getCounts() {
      adminApi.counts().then(response => {
        this.countsData = response.data.data
        this.countsLoading = false
      })
    },
    handleEditPostClick(post) {
      this.$router.push({ name: 'PostEdit', query: { postId: post.id } })
    },
    handleCreateJournalClick() {
      journalApi.create(this.journal).then(response => {
        this.$message.success('发表成功！')
        this.journal = {}
      })
    }
  }
}
</script>
