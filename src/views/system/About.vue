<template>
  <div>
    <a-row>
      <a-col :span="24">
        <a-card
          :bordered="false"
          :bodyStyle="{ padding: '16px' }"
        >
          <a-card
            :bordered="false"
            class="environment-info"
            :bodyStyle="{ padding: '16px' }"
          >
            <template slot="title">
              环境信息
              <a
                href="javascript:void(0);"
                @click="handleCopyEnvironments"
              >
                <a-icon type="copy" />
              </a>
            </template>
            <a-popover
              slot="extra"
              placement="left"
              :title="isLatest?'当前为最新版本':'有新版本'"
            >
              <template slot="content">
                <p>{{ versionMessage }}</p>
                <a-button
                  type="dashed"
                  @click="handleShowVersionContent"
                >查看详情</a-button>
              </template>
              <a-button
                :loading="checking"
                type="dashed"
                shape="circle"
                :icon="isLatest?'check-circle':'exclamation-circle'"
              ></a-button>
            </a-popover>

            <ul style="margin: 0;padding: 0;list-style: none;">
              <li>版本：{{ environments.version }}</li>
              <li>数据库：{{ environments.database }}</li>
              <li>运行模式：{{ environments.mode }}</li>
              <li>启动时间：{{ environments.startTime | moment }}</li>
            </ul>

            <a
              href="https://github.com/halo-dev"
              target="_blank"
              style="margin-right: 10px;"
            >开源组织
              <a-icon type="link" /></a>
            <a
              href="https://halo.run"
              target="_blank"
              style="margin-right: 10px;"
            >用户文档
              <a-icon type="link" /></a>
            <a
              href="https://bbs.halo.run"
              target="_blank"
              style="margin-right: 10px;"
            >在线社区
              <a-icon type="link" /></a>
          </a-card>

          <a-card
            title="开发者"
            :bordered="false"
            :bodyStyle="{ padding: '16px' }"
            :loading="contributorsLoading"
          >
            <a
              :href="item.html_url"
              v-for="(item,index) in contributors"
              :key="index"
              target="_blank"
            >
              <a-tooltip
                placement="top"
                :title="item.login"
              >
                <a-avatar
                  size="large"
                  :src="item.avatar_url"
                  :style="{ marginRight: '10px',marginBottom: '10px'}"
                />
              </a-tooltip>
            </a>
          </a-card>
        </a-card>
      </a-col>

      <a-col :span="24">
      </a-col>
    </a-row>

    <a-modal
      :title="versionContentModalTitle"
      :visible="versionContentVisible"
      ok-text="查看更多"
      @cancel="versionContentVisible=false"
      @ok="handleOpenVersionUrl"
    >
      <div v-html="versionContent"></div>
    </a-modal>
  </div>
</template>

<script>
import adminApi from '@/api/admin'
import axios from 'axios'
import marked from 'marked'
export default {
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
      versionContentVisible: false
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
        return marked(this.latestData.body)
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
    this.fetchContributors()
  },
  methods: {
    async getEnvironments() {
      await adminApi.environments().then(response => {
        this.environments = response.data.data
      })
      this.checkServerUpdate()
    },
    handleCopyEnvironments() {
      const text = `版本：${this.environments.version}
数据库：${this.environments.database}
运行模式：${this.environments.mode}
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
      axios
        .get('https://api.github.com/repos/halo-dev/halo/contributors')
        .then(response => {
          _this.contributors = response.data
        })
        .catch(function(error) {
          console.error('Fetch contributors error', error)
        })
        .finally(() => {
          setTimeout(() => {
            _this.contributorsLoading = false
          }, 200)
        })
    },
    checkServerUpdate() {
      const _this = this
      _this.checking = true
      axios
        .get('https://api.github.com/repos/halo-dev/halo/releases/latest')
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
          console.error('Check update fail', error)
        })
        .finally(() => {
          setTimeout(() => {
            this.checking = false
          }, 200)
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
