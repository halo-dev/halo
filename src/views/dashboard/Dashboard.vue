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
        <!-- <analysis-card :loading="countsLoading" title="总访问" :number="countsData.visitCount">
          <a-tooltip slot="action">
        <template slot="title">文章总访问共 {{ countsData.visitCount }} 次</template>-->
        <analysis-card
          :loading="countsLoading"
          title="总访问"
          :number="countsData.visitCount"
        >
          <a-tooltip slot="action">
            <template slot="title">
              文章总访问共
              <countTo
                :startVal="0"
                :endVal="countsData.visitCount"
                :duration="3000"
              ></countTo>次
            </template>
            <!-- <countTo :startVal="0" :endVal="countsData.visitCount" :duration="3000"></countTo> -->
            <!-- <template>
              <countTo :startVal="0" :endVal="countsData.visitCount" :duration="3000"></countTo>
            </template>-->
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
          :number="countsData.establishDays"
        >
          <a-tooltip slot="action">
            <template slot="title">博客建立于 {{ countsData.birthday | moment }}</template>
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
                        slot="title"
                        :href="options.blog_url+'/archives/'+item.url"
                        target="_blank"
                      >{{ item.title }}</a>
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
                v-model="journal.content"
                placeholder="写点什么吧..."
              />
            </a-form-item>

            <!-- 日志图片上传 -->
            <!-- <a-form-item v-show="showMoreOptions">
              <UploadPhoto
                @success="handlerPhotoUploadSuccess"
                :photoList="photoList"
              ></UploadPhoto>
            </a-form-item> -->

            <a-form-item>
              <a-button
                type="primary"
                @click="handleCreateJournalClick"
              >保存</a-button>
              <!-- <a
                href="javascript:;"
                class="more-options-btn"
                type="default"
                @click="handleUploadPhotoWallClick"
              >更多选项<a-icon type="down" /></a> -->
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
      <a-skeleton
        active
        :loading="logsLoading"
        :paragraph="{rows: 18}"
      >
        <a-row
          type="flex"
          align="middle"
        >
          <a-col :span="24">
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

              <div class="page-wrapper">
                <a-pagination
                  class="pagination"
                  :total="logPagination.total"
                  :defaultPageSize="logPagination.size"
                  :pageSizeOptions="['50', '100','150','200']"
                  showSizeChanger
                  @showSizeChange="onPaginationChange"
                  @change="onPaginationChange"
                />
              </div>
            </a-list>
          </a-col>
        </a-row>
      </a-skeleton>
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
import { PageView } from '@/layouts'
import AnalysisCard from './components/AnalysisCard'
import RecentCommentTab from './components/RecentCommentTab'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import optionApi from '@/api/option'

import postApi from '@/api/post'
import logApi from '@/api/log'
import adminApi from '@/api/admin'
import journalApi from '@/api/journal'
import countTo from 'vue-count-to'
import UploadPhoto from '../../components/Upload/UploadPhoto.vue'
export default {
  name: 'Dashboard',
  mixins: [mixin, mixinDevice],
  components: {
    PageView,
    AnalysisCard,
    RecentCommentTab,
    countTo,
    UploadPhoto
  },
  data() {
    return {
      photoList: [],
      showMoreOptions: false,
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
      countsData: {},
      journal: {
        content: '',
        photos: []
      },
      journalPhotos: [], // 日志图片集合最多九张
      logs: [],
      options: [],
      keys: ['blog_url'],
      logPagination: {
        page: 1,
        size: 50,
        sort: null
      },
      interval: null
    }
  },
  created() {
    this.getCounts()
    this.listLatestPosts()
    this.listLatestLogs()
    this.loadOptions()

    // this.interval = setInterval(() => {
    //   this.getCounts()
    // }, 5000)
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
  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.interval = setInterval(() => {
        vm.getCounts()
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
    handlerPhotoUploadSuccess(response, file) {
      var callData = response.data.data
      var photo = {
        name: callData.name,
        url: callData.path,
        thumbnail: callData.thumbPath,
        suffix: callData.suffix,
        width: callData.width,
        height: callData.height
      }
      this.journalPhotos.push(photo)
    },
    loadOptions() {
      optionApi.listAll(this.keys).then(response => {
        this.options = response.data.data
      })
    },
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
      // 给属性填充数据
      // this.journal.photos = this.journalPhotos
      if (!this.journal.content) {
        this.$notification['error']({
          message: '提示',
          description: '内容不能为空！'
        })
        return
      }
      journalApi.create(this.journal).then(response => {
        this.$message.success('发表成功！')
        this.journal = {}
        // this.photoList = []
        this.showMoreOptions = false
      })
    },
    handleUploadPhotoWallClick() {
      // 是否显示上传照片墙组件
      this.showMoreOptions = !this.showMoreOptions
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
      this.logPagination.page = this.logPagination.page - 1
      logApi.pageBy(this.logPagination).then(response => {
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
    onPaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.logPagination.page = page
      this.logPagination.size = pageSize
      this.loadLogs()
    }
  }
}
</script>

<style scoped="scoped">
.more-options-btn {
  margin-left: 15px;
  text-decoration: none;
}
a {
  text-decoration: none;
}
</style>
