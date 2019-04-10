<template>
  <page-view>
    <a-row :gutter="12" type="flex" align="middle">
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
      <a-col
        class="attachment-item"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24">
        <a-card :bodyStyle="{ padding: '14px' }">
          <img :src="src" slot="cover">
        </a-card>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
export default {
  components: {
    PageView
  },
  data() {
    return {
      src: 'https://cdn.ryanc.cc/img/blog/thumbnails/c86b59623c4a9bfeac403824dec3a789.jpg'
    }
  }
}
</script>

<style lang="less" scoped>
.attachment-item {
  padding-bottom: 12px;
}
</style>
