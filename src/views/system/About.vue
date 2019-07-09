<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :span="24">
        <a-card :bordered="false">
          <a-card
            :bordered="false"
            class="environment-info"
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
            <a-popconfirm
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
            </a-popconfirm>

            <ul>
              <li>Server 版本：{{ environments.version }}</li>
              <li>Admin 版本：{{ adminVersion }}</li>
              <li>数据库：{{ environments.database }}</li>
              <li>运行模式：{{ environments.mode }}</li>
              <li>启动时间：{{ environments.startTime | moment }}</li>
            </ul>

            <a
              href="https://github.com/halo-dev"
              target="_blank"
            >开源地址
              <a-icon type="link" /></a>
            <a
              href="https://halo.run/guide"
              target="_blank"
            >用户文档
              <a-icon type="link" /></a>
            <a
              href="https://bbs.halo.run"
              target="_blank"
            >在线社区
              <a-icon type="link" /></a>
          </a-card>

          <a-card
            title="开发者"
            :bordered="false"
          >
            <a
              :href="item.github"
              v-for="(item,index) in developers"
              :key="index"
              target="_blank"
            >
              <a-tooltip
                placement="top"
                :title="item.name"
              >
                <a-avatar
                  size="large"
                  :src="item.avatar"
                  :style="{ marginRight: '10px' }"
                />
              </a-tooltip>
            </a>
          </a-card>

          <a-card
            title="时间轴"
            :bordered="false"
          >
            <a-timeline>
              <a-timeline-item>...</a-timeline-item>
              <a-timeline-item
                v-for="(item, index) in steps"
                :key="index"
              >{{ item.date }} {{ item.content }}</a-timeline-item>
            </a-timeline>
          </a-card>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import adminApi from '@/api/admin'
export default {
  data() {
    return {
      adminVersion: this.VERSION,
      environments: {},
      developers: [
        {
          name: 'Ryan Wang',
          avatar: '//cn.gravatar.com/avatar/7cc7f29278071bd4dce995612d428834?s=256&d=mm',
          website: 'https://ryanc.cc',
          github: 'https://github.com/ruibaby'
        },
        {
          name: 'John Niang',
          avatar: '//cn.gravatar.com/avatar/1dcf60ef27363dae539385d5bae9b2bd?s=256&d=mm',
          website: 'https://johnniang.me',
          github: 'https://github.com/johnniang'
        },
        {
          name: 'Aquan',
          avatar: '//cn.gravatar.com/avatar/3958035fa354403fa9ca3fca36b08068?s=256&d=mm',
          website: 'https://blog.eunji.cn',
          github: 'https://github.com/aquanlerou'
        },
        {
          name: 'appdev',
          avatar: '//cn.gravatar.com/avatar/08cf681fb7c6ad1b4fe70a8269c2103c?s=256&d=mm',
          website: 'https://www.apkdv.com',
          github: 'https://github.com/appdev'
        },
        {
          name: 'guqing',
          avatar: '//cn.gravatar.com/avatar/ad062ba572c8b006bfd2cbfc43fdee5e?s=256&d=mm',
          website: 'http://www.guqing.xyz',
          github: 'https://github.com/guqing'
        }
      ],
      steps: [
        {
          date: '2019-07-09',
          content: 'Halo v1.0.3 发布'
        },
        {
          date: '2019-07-08',
          content: 'Star 数达到 6500'
        },
        {
          date: '2019-06-01',
          content: '1.0 正式版发布'
        },
        {
          date: '2019-05-03',
          content: 'Star 数达到 3300'
        },
        {
          date: '2019-01-30',
          content: 'John Niang 加入开发'
        },
        {
          date: '2018-10-18',
          content: '构建镜像到 Docker hub'
        },
        {
          date: '2018-09-22',
          content: 'Star 数达到 800'
        },
        {
          date: '2018-05-02',
          content: '第一条 Issue'
        },
        {
          date: '2018-05-01',
          content: 'Star 数达到 100'
        },
        {
          date: '2018-04-29',
          content: '第一个 Pull request'
        },
        {
          date: '2018-04-28',
          content: '正式开源'
        },
        {
          date: '2018-03-21',
          content: '确定命名为 Halo，并上传到 Github'
        }
      ],
      updating: false
    }
  },
  created() {
    this.getEnvironments()
  },
  computed: {
    updateText() {
      return this.updating ? '更新中...' : '更新'
    }
  },
  methods: {
    getEnvironments() {
      adminApi.environments().then(response => {
        this.environments = response.data.data
      })
    },
    confirmUpdate() {
      this.updating = true
      adminApi
        .updateAdminAssets()
        .then(response => {
          this.$notification.success({
            message: '更新成功',
            description: '请刷新后体验最新版本！'
          })
        })
        .finally(() => {
          this.updating = false
        })
    },
    handleCopyEnvironments() {
      const text = `Server 版本：${this.environments.version}
Admin 版本：${this.adminVersion}
数据库：${this.environments.database}
运行模式：${this.environments.mode}`
      this.$copyText(text)
        .then(message => {
          console.log('copy', message)
          this.$message.success('复制成功！')
        })
        .catch(err => {
          console.log('copy.err', err)
          this.$message.error('复制失败！')
        })
    }
  }
}
</script>
<style lang="less" scope>
ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.environment-info {
  ul {
    margin: 0;
    padding: 0;
    list-style: none;
  }
  a {
    margin-right: 10px;
  }
}
</style>
