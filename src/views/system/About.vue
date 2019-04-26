<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :span="24">
        <a-card :bordered="false">
          <!-- 简介 -->
          <h2>简介</h2>
          <p>Halo [ˈheɪloʊ]，意为光环。当然，你也可以当成拼音读(哈喽)。</p>
          <p>轻快，简洁，功能强大，使用 Java 开发的博客系统。</p>
          <h2>环境信息</h2>
          <p>版本：1.0</p>
          <p>数据库：MySql</p>
          <p>开发语言：Java</p>
          <!-- 关于我们 -->
          <h2>开发者</h2>
          <div>
            <a-tooltip
              placement="top"
              v-for="(item,index) in developers"
              :title="item.name"
              :key="index"
            >
              <a-avatar
                :size="60"
                :src="item.avatar"
              />
            </a-tooltip>
          </div>
          <!-- 发展历程 -->
          <h2>时间轴</h2>
          <a-timeline>
            <a-timeline-item>Create a services site 2015-09-01</a-timeline-item>
            <a-timeline-item>Solve initial network problems 2015-09-01</a-timeline-item>
            <a-timeline-item color="red">
              <a-icon
                slot="dot"
                type="clock-circle-o"
                style="fontSize: '16px'"
              />
              Technical testing 2015-09-01
            </a-timeline-item>
            <a-timeline-item>Network problems being solved 2015-09-01</a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
export default {
  data() {
    return {
      developers: [
        {
          name: 'Ryan Wang',
          avatar: 'https://gravatar.loli.net/avatar/7cc7f29278071bd4dce995612d428834?s=256&d=mm'
        },
        {
          name: 'John Niang',
          avatar: 'https://johnniang.me/imgs/avatar.png'
        }
      ]
    }
  }
}
</script>

<style scoped>
</style>
