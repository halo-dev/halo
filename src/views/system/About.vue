<template>
  <div class="page-header-index-wide" style="padding:10px">
    <div align="center">
      <img src="../../assets/halo-logo.png" width="300px">
    </div>
    <!-- 简介 -->
    <h2>简介</h2>
    <p>Halo [ˈheɪloʊ]，意为光环。当然，你也可以当成拼音读(哈喽)。</p>
    <p>轻快，简洁，功能强大，使用 Java 开发的博客系统。</p>
    <h2>环境信息</h2>
    <p>版本：1.0</p>
    <p>数据库：MySql</p>
    <p>开发语言：Java</p>
    <!-- 关于我们 -->
    <div class="aboutTitle">关于我们</div>
    <div>
      <a-row>
        <a-col
          :xs="8"
          :md="6"
          :lg="4"
          :xl="4"
          v-for="(dev,index) in devs"
          :key="index">
          <div class="head-Box">
            <a-avatar :size="60" :src="dev.src"/>
            <p>{{ dev.Name }}</p>
          </div>
        </a-col>
      </a-row>
    </div>
    <!-- 发展历程 -->
    <div>
      <h2>Halo的发展历程：</h2>
      <a-timeline mode="alternate">
        <a-timeline-item>Halo 0.1版本诞生啦</a-timeline-item>
        <a-timeline-item color="green">啦啦啦啦</a-timeline-item>
        <a-timeline-item>
          <a-icon slot="dot" type="clock-circle-o" style="font-size: 16px;"/>测试测手册
        </a-timeline-item>
        <a-timeline-item color="red">首席测试官测出bug了 2015-09-01</a-timeline-item>
        <a-timeline-item>又出bug了</a-timeline-item>
        <a-timeline-item>
          <a-icon slot="dot" type="clock-circle-o" style="font-size: 16px;"/>Halo 1.0版本 2019-05-01
        </a-timeline-item>
      </a-timeline>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      devs: [
        {
          Name: '首席测试工程师',
          src: 'https://q1.qlogo.cn/g?b=qq&nk=952396285&s=5'
        }
      ]
    }
  }
}
</script>

<style scoped>
.aboutTitle {
  margin: 0px auto;
  text-align: center;
  font-size: 24px;
}
.head-Box {
  text-align: center;
  padding: 10px;
  display: inline-block;
}
</style>
