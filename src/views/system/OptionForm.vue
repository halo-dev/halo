<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane tab="常规设置" key="general">
              <a-form layout="vertical">
                <a-form-item label="博客标题：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="博客地址：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="LOGO：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="Favicon：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="页脚信息： " :wrapper-col="wrapperCol">
                  <a-input type="textarea"/>
                </a-form-item>
                <a-form-item>
                  <a-button type="primary">保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane tab="SEO设置" key="seo">
            </a-tab-pane>
            <a-tab-pane tab="文章设置" key="post">
            </a-tab-pane>
            <a-tab-pane tab="评论设置" key="comment">
            </a-tab-pane>
            <a-tab-pane tab="附件设置" key="attachment">
            </a-tab-pane>
            <a-tab-pane tab="后台设置" key="admin">
            </a-tab-pane>
            <a-tab-pane tab="邮箱设置" key="email">
            </a-tab-pane>
            <a-tab-pane tab="其他设置" key="other">
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script>
export default {
  data() {
    return {
      wrapperCol: {
        xl: { span: 8 },
        sm: { span: 8 },
        xs: { span: 24 }
      }
    }
  },
  methods: {
    callback(key) {
      console.log(key)
    }
  }
}
</script>
<style>
.card-container {
  background: #f5f5f5;
}
.card-container > .ant-tabs-card > .ant-tabs-content {
  margin-top: -16px;
}

.card-container > .ant-tabs-card > .ant-tabs-content > .ant-tabs-tabpane {
  background: #fff;
  padding: 16px;
}

.card-container > .ant-tabs-card > .ant-tabs-bar {
  border-color: #fff;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab {
  border-color: transparent;
  background: transparent;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab-active {
  border-color: #fff;
  background: #fff;
}
</style>
