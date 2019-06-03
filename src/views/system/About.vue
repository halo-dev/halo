<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :span="24">
        <a-card :bordered="false">

          <a-list itemLayout="horizontal">
            <a-list-item>
              <a-list-item-meta>
                <h3 slot="title">
                  环境信息
                </h3>
                <template slot="description">
                  <ul>
                    <li>版本：{{ environments.version }}</li>
                    <li>数据库：{{ environments.database }}</li>
                    <li>启动时间：{{ environments.startTime | moment }}</li>
                  </ul>
                  <a
                    href="https://github.com/halo-dev"
                    target="_blank"
                  >开源地址
                    <a-icon type="link" /></a>&nbsp;
                  <a
                    href="https://halo.run/docs"
                    target="_blank"
                  >用户文档
                    <a-icon type="link" /></a>&nbsp;
                  <a
                    href="https://bbs.halo.run"
                    target="_blank"
                  >在线社区
                    <a-icon type="link" /></a>&nbsp;
                </template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <h3 slot="title">
                  开发者
                </h3>
                <template slot="description">
                  <a-tooltip
                    placement="top"
                    v-for="(item,index) in developers"
                    :title="item.name"
                    :key="index"
                  >
                    <a-avatar
                      size="large"
                      :src="item.avatar"
                      :style="{ marginRight: '10px' }"
                    />
                  </a-tooltip>
                </template>
              </a-list-item-meta>
            </a-list-item>

            <a-list-item>
              <a-list-item-meta>
                <h3 slot="title">
                  时间轴
                </h3>
                <template slot="description">
                  <a-timeline>
                    <a-timeline-item>...</a-timeline-item>
                    <a-timeline-item
                      v-for="(item, index) in steps"
                      :key="index"
                    >{{ item.date }} {{ item.content }}</a-timeline-item>
                  </a-timeline>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
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
        }
      ],
      steps: [
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
      ]
    }
  },
  created() {
    this.getEnvironments()
  },
  methods: {
    getEnvironments() {
      adminApi.environments().then(response => {
        this.environments = response.data.data
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
</style>
