<template>
  <page-view>
    <a-row>
      <a-col :span="24">
        <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
          <a-card :bodyStyle="{ padding: '16px' }" :bordered="false" class="environment-info">
            <template slot="title">
              环境信息
              <a href="javascript:void(0);" @click="handleCopyEnvironments">
                <a-icon type="copy" />
              </a>
            </template>
            <a-popover slot="extra" :title="isLatest ? '当前为最新版本' : '有新版本'" placement="left">
              <template slot="content">
                <p>{{ versionMessage }}</p>
                <a-button type="dashed" @click="handleShowVersionContent">查看详情</a-button>
              </template>
              <a-button
                :icon="isLatest ? 'check-circle' : 'exclamation-circle'"
                :loading="checking"
                shape="circle"
                type="dashed"
              ></a-button>
            </a-popover>

            <ul class="p-0 m-0 list-none">
              <li>版本：{{ environments.version }}</li>
              <li>数据库：{{ environments.database }}</li>
              <li>运行模式：{{ environments.mode }}</li>
              <li>启用主题：{{ activatedTheme.name }}</li>
              <li>启动时间：{{ environments.startTime | moment }}</li>
            </ul>
            <a class="mr-3" href="https://halo.run" target="_blank"
              >官网
              <a-icon type="link" />
            </a>
            <a class="mr-3" href="https://docs.halo.run" target="_blank"
              >文档
              <a-icon type="link" />
            </a>
            <a class="mr-3" href="https://github.com/halo-dev" target="_blank"
              >开源组织
              <a-icon type="link" />
            </a>
            <a class="mr-3" href="https://bbs.halo.run" target="_blank"
              >在线社区
              <a-icon type="link" />
            </a>
          </a-card>

          <a-card :bodyStyle="{ padding: '16px' }" :bordered="false" :loading="contributorsLoading" title="开发者">
            <a v-for="(item, index) in contributors" :key="index" :href="item.html_url" target="_blank">
              <a-tooltip :title="item.login" placement="top">
                <a-avatar :src="item.avatar_url" :style="{ marginRight: '10px', marginBottom: '10px' }" size="large" />
              </a-tooltip>
            </a>
          </a-card>
        </a-card>
      </a-col>

      <a-col :span="24"></a-col>
    </a-row>

    <a-modal
      :title="versionContentModalTitle"
      :visible="versionContentVisible"
      :width="620"
      ok-text="查看更多"
      @cancel="versionContentVisible = false"
      @ok="handleOpenVersionUrl"
    >
      <div v-html="versionContent"></div>
    </a-modal>
  </page-view>
</template>

<script>
import apiClient from '@/utils/api-client'
import axios from 'axios'
import { marked } from 'marked'
import { PageView } from '@/layouts'

const axiosInstance = axios.create({
  baseURL: 'https://api.github.com',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.github.v3+json'
  },
  withCredentials: false
})

export default {
  components: {
    PageView
  },
  data() {
    return {
      environments: {},
      contributors: [
        {
          login: '',
          id: 0,
          node_id: '',
          avatar_url: '',
          gravatar_id: '',
          url: '',
          html_url: '',
          followers_url: '',
          following_url: '',
          gists_url: '',
          starred_url: '',
          subscriptions_url: '',
          organizations_url: '',
          repos_url: '',
          events_url: '',
          received_events_url: '',
          type: '',
          site_admin: false,
          contributions: 0
        }
      ],
      contributorsLoading: true,
      checking: false,
      isLatest: false,
      latestData: {},
      versionContentVisible: false,
      activatedTheme: {}
    }
  },
  computed: {
    versionMessage() {
      return `当前版本：${this.environments.version}，${
        this.isLatest ? '已经是最新版本。' : `新版本：${this.latestData.name}，你可以点击下方按钮查看详情。`
      }`
    },
    versionContent() {
      if (this.latestData && this.latestData.body) {
        return marked.parse(this.latestData.body)
      } else {
        return '暂无内容'
      }
    },
    versionContentModalTitle() {
      return `${this.latestData.name} 更新内容`
    }
  },
  created() {
    this.getEnvironments()
    this.handleGetActivatedTheme()
    this.fetchContributors()
  },
  methods: {
    async getEnvironments() {
      const { data } = await apiClient.getEnvironment()
      this.environments = data
      this.checkServerUpdate()
    },
    async handleGetActivatedTheme() {
      const { data } = await apiClient.theme.getActivatedTheme()
      this.activatedTheme = data
    },
    handleCopyEnvironments() {
      const text = `版本：${this.environments.version}
数据库：${this.environments.database}
运行模式：${this.environments.mode}
启用主题：${this.activatedTheme.name}
User Agent：${navigator.userAgent}`
      this.$copyText(text)
        .then(message => {
          this.$log.debug('copy', message)
          this.$message.success('复制成功！')
        })
        .catch(err => {
          this.$log.debug('copy.err', err)
          this.$message.error('复制失败！')
        })
    },
    fetchContributors() {
      const _this = this
      _this.contributorsLoading = true
      axiosInstance
        .get('/repos/halo-dev/halo/contributors?per_page=100')
        .then(response => {
          _this.contributors = response.data
        })
        .catch(function(error) {
          _this.$log.error('Fetch contributors error', error)
        })
        .finally(() => {
          _this.contributorsLoading = false
        })
    },
    checkServerUpdate() {
      const _this = this
      _this.checking = true
      axiosInstance
        .get('/repos/halo-dev/halo/releases/latest')
        .then(response => {
          const data = response.data
          _this.latestData = data
          if (data.draft || data.prerelease) {
            return
          }
          const current = _this.calculateIntValue(_this.environments.version)
          const latest = _this.calculateIntValue(data.name)
          if (current >= latest) {
            _this.isLatest = true
            return
          }
          const title = '新版本提醒'
          const content = '检测到 Halo 新版本：' + data.name + '，点击下方按钮查看最新版本。'
          _this.$notification.open({
            message: title,
            description: content,
            icon: <a-icon type="smile" style="color: #108ee9" />,
            btn: h => {
              return h(
                'a-button',
                {
                  props: {
                    type: 'primary',
                    size: 'small'
                  },
                  on: {
                    click: () => this.handleShowVersionContent()
                  }
                },
                '去看看'
              )
            }
          })
        })
        .catch(function(error) {
          this.$log.error('Check update fail', error)
        })
        .finally(() => {
          this.checking = false
        })
    },
    handleShowVersionContent() {
      this.versionContentVisible = true
    },
    handleOpenVersionUrl() {
      window.open(this.latestData.html_url, '_blank')
    },
    calculateIntValue(version) {
      version = version.replace(/v/g, '')
      const ss = version.split('.')
      if (ss == null || ss.length !== 3) {
        return -1
      }
      const major = parseInt(ss[0])
      const minor = parseInt(ss[1])
      const micro = parseInt(ss[2])
      if (isNaN(major) || isNaN(minor) || isNaN(micro)) {
        return -1
      }
      return major * 1000000 + minor * 1000 + micro
    }
  }
}
</script>
