<template>
  <div :style="!$route.meta.hiddenHeaderContent ? 'margin: -24px -24px 0px;' : null">
    <div
      class="page-header"
      v-if="!$route.meta.hiddenHeaderContent"
    >
      <div class="page-header-index-wide">
        <a-page-header
          :title="title"
          :sub-title="subTitle"
          :breadcrumb="{ props: { routes:breadList } }"
        >
          <slot
            name="extra"
            slot="extra"
          >
          </slot>
        </a-page-header>
      </div>
    </div>
    <div class="content">
      <div class="page-header-index-wide">
        <slot>
          <router-view ref="content" />
        </slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PageView',
  props: {
    title: {
      type: String,
      default: null,
    },
    subTitle: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      breadList: [],
    }
  },
  created() {
    this.getBreadcrumb()
  },
  watch: {
    $route() {
      this.getBreadcrumb()
    },
  },
  methods: {
    getBreadcrumb() {
      this.breadList = []
      this.$route.matched.forEach((item) => {
        item.breadcrumbName = item.meta.title
        this.breadList.push(item)
      })
    },
  },
}
</script>

<style lang="less" scoped>
.page-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  .ant-page-header {
    padding: 16px 0px;
  }
  margin: 0 0 24px 0;
}

.mobile .page-header,
.tablet .page-header {
  .ant-page-header {
    padding: 16px;
  }
}
</style>
