<template>
  <div :style="!$route.meta.hiddenHeaderContent ? 'margin: -24px -24px 0px;' : null">
    <a-affix v-if="affix">
      <div class="page-header" v-if="!$route.meta.hiddenHeaderContent">
        <div class="page-header-index-wide">
          <a-page-header :title="title" :sub-title="subTitle" :breadcrumb="{ props: { routes: breadList } }">
            <slot name="extra" slot="extra"> </slot>
            <slot name="footer" slot="footer"> </slot>
          </a-page-header>
        </div>
      </div>
    </a-affix>
    <div class="page-header" v-if="!$route.meta.hiddenHeaderContent && !affix">
      <div class="page-header-index-wide">
        <a-page-header :title="title" :sub-title="subTitle" :breadcrumb="{ props: { routes: breadList } }">
          <slot name="extra" slot="extra"> </slot>
          <slot name="footer" slot="footer"> </slot>
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
      default: null
    },
    subTitle: {
      type: String,
      default: null
    },
    affix: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      breadList: []
    }
  },
  created() {
    this.getBreadcrumb()
  },
  watch: {
    $route() {
      this.getBreadcrumb()
    }
  },
  methods: {
    getBreadcrumb() {
      this.breadList = []
      this.$route.matched.forEach(item => {
        item.breadcrumbName = item.meta.title
        this.breadList.push(item)
      })
    }
  }
}
</script>

<style lang="less" scoped>
.page-header {
  background: #fff;
  padding: 0 24px 0;
  border-bottom: 1px solid #e8e8e8;
  .ant-page-header {
    padding: 16px 0px;
  }
}

.mobile .page-header,
.tablet .page-header {
  padding: 0 !important;
  .ant-page-header {
    padding: 16px;
  }
}

.content {
  margin: 24px 24px 0;
  .link {
    margin-top: 16px;
    &:not(:empty) {
      margin-bottom: 16px;
    }
    a {
      margin-right: 32px;
      height: 24px;
      line-height: 24px;
      display: inline-block;
      i {
        font-size: 24px;
        margin-right: 8px;
        vertical-align: middle;
      }
      span {
        height: 24px;
        line-height: 24px;
        display: inline-block;
        vertical-align: middle;
      }
    }
  }
}
</style>
