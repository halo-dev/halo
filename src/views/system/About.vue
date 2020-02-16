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
            <!-- <a-popconfirm
              slot="extra"
              placement="left"
              okText="确定"
              cancelText="取消"
              @confirm="confirmUpdate"
            >
              <template slot="title">
                <p>确定更新 <b>Halo admin</b> 吗？</p>
              </template>
              <a-icon
                type="cloud-download"
                slot="icon"
              ></a-icon>
              <a-button
                :loading="updating"
                type="dashed"
                shape="circle"
                icon="cloud-download"
              >
              </a-button>
            </a-popconfirm> -->

            <ul style="margin: 0;padding: 0;list-style: none;">
              <li>Server 版本：{{ environments.version }}</li>
              <li>Admin 版本：{{ adminVersion }}</li>
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
    </a-row>
  </div>
</template>

<script>
import adminApi from '@/api/admin'
import axios from 'axios'
export default {
  data() {
    return {
      adminVersion: this.VERSION,
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
      contributorsLoading: true
    }
  },
  created() {
    this.getEnvironments()
    this.fetchContributors()
    this.checkServerUpdate()
    // this.checkAdminUpdate()
  },
  methods: {
    getEnvironments() {
      adminApi.environments().then(response => {
        this.environments = response.data.data
      })
    },
    // confirmUpdate() {
    //   this.updating = true
    //   adminApi
    //     .updateAdminAssets()
    //     .then(response => {
    //       this.$notification.success({
    //         message: '更新成功',
    //         description: '请刷新后体验最新版本！'
    //       })
    //     })
    //     .finally(() => {
    //       this.updating = false
    //     })
    // },
    handleCopyEnvironments() {
      const text = `Server 版本：${this.environments.version}
Admin 版本：${this.adminVersion}
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
    async fetchContributors() {
      this.contributorsLoading = true
      const _this = this
      axios
        .get('https://api.github.com/repos/halo-dev/halo/contributors')
        .then(response => {
          _this.contributors = response.data
          this.contributorsLoading = false
        })
        .catch(function(error) {
          console.error('Fetch contributors error', error)
        })
    },
    async checkServerUpdate() {
      const _this = this

      axios
        .get('https://api.github.com/repos/halo-dev/halo/releases/latest')
        .then(response => {
          const data = response.data
          if (data.draft || data.prerelease) {
            return
          }
          const current = _this.calculateIntValue(_this.environments.version)
          const latest = _this.calculateIntValue(data.name)
          if (current >= latest) {
            return
          }
          const title = '新版本提醒'
          const content = '检测到 Server 新版本：' + data.name + '，点击下方按钮查看最新版本。'
          const url = data.html_url
          this.$notification.open({
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
                    click: () => window.open(url, '_blank')
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
    },
    // async checkAdminUpdate() {
    //   const _this = this

    //   axios
    //     .get('https://api.github.com/repos/halo-dev/halo-admin/releases/latest')
    //     .then(response => {
    //       const data = response.data
    //       if (data.draft || data.prerelease) {
    //         return
    //       }
    //       const current = _this.calculateIntValue(_this.adminVersion)
    //       const latest = _this.calculateIntValue(data.name)
    //       if (current >= latest) {
    //         return
    //       }
    //       const title = '新版本提醒'
    //       const content = '检测到 Admin 新版本：' + data.name + '，点击下方按钮可直接更新为最新版本。'
    //       this.$notification.open({
    //         message: title,
    //         description: content,
    //         icon: <a-icon type="smile" style="color: #108ee9" />,
    //         btn: h => {
    //           return h(
    //             'a-button',
    //             {
    //               props: {
    //                 type: 'primary',
    //                 size: 'small'
    //               },
    //               on: {
    //                 click: () => _this.confirmUpdate()
    //               }
    //             },
    //             '点击更新'
    //           )
    //         }
    //       })
    //     })
    //     .catch(function(error) {
    //       console.error('Check update fail', error)
    //     })
    // },
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
