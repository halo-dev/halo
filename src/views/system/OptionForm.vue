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
                <a-form-item label="页脚信息：" :wrapper-col="wrapperCol">
                  <a-input type="textarea" :autosize="{ minRows: 5 }" />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary">保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane tab="SEO设置" key="seo">
              <a-form layout="vertical">
                <a-form-item label="关键词： " :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="博客描述：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="百度推送 token： " :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="Google 站点验证：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="Bing 站点验证：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="Baidu 站点验证：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="360 站点验证：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary">保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane tab="文章设置" key="post">
              <a-form layout="vertical">
                <a-form-item label="首页显示条数：" :wrapper-col="wrapperCol">
                  <a-input type="number" />
                </a-form-item>
                <a-form-item label="RSS 显示条数：" :wrapper-col="wrapperCol">
                  <a-input type="number" />
                </a-form-item>
                <a-form-item label="文章摘要字数：" :wrapper-col="wrapperCol">
                  <a-input type="number" />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary">保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane tab="评论设置" key="comment">
              <a-form layout="vertical">
                <a-form-item label="评论者头像：" :wrapper-col="wrapperCol">
                  <a-select defaultValue="mm">
                    <a-select-option value="mm">
                      默认
                    </a-select-option>
                    <a-select-option value="identicon">
                      抽象几何图形
                    </a-select-option>
                    <a-select-option value="monsterid">
                      小怪物
                    </a-select-option>
                    <a-select-option value="wavatar">
                      Wavatar
                    </a-select-option>
                    <a-select-option value="retro">
                      复古
                    </a-select-option>
                    <a-select-option value="robohash">
                      机器人
                    </a-select-option>
                    <a-select-option value="blank">
                      不显示头像
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="评论审核后才显示：" :wrapper-col="wrapperCol">
                  <a-radio-group v-decorator="['radio-group']" defaultValue="true">
                    <a-radio value="true">
                      启用
                    </a-radio>
                    <a-radio value="false">
                      禁用
                    </a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item label="新评论通知：" :wrapper-col="wrapperCol">
                  <a-radio-group v-decorator="['radio-group']" defaultValue="true">
                    <a-radio value="true">
                      启用
                    </a-radio>
                    <a-radio value="false">
                      禁用
                    </a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item label="评论审核通过通知对方：" :wrapper-col="wrapperCol">
                  <a-radio-group v-decorator="['radio-group']" defaultValue="true">
                    <a-radio value="true">
                      启用
                    </a-radio>
                    <a-radio value="false">
                      禁用
                    </a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item label="评论回复通知对方：" :wrapper-col="wrapperCol">
                  <a-radio-group v-decorator="['radio-group']" defaultValue="true">
                    <a-radio value="true">
                      启用
                    </a-radio>
                    <a-radio value="false">
                      禁用
                    </a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item label="API 评论开关：" :wrapper-col="wrapperCol">
                  <a-radio-group v-decorator="['radio-group']" defaultValue="true">
                    <a-radio value="true">
                      启用
                    </a-radio>
                    <a-radio value="false">
                      禁用
                    </a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item label="每页显示条数： " :wrapper-col="wrapperCol">
                  <a-input type="number" />
                </a-form-item>
                <a-form-item label="占位提示：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="自定义样式：" :wrapper-col="wrapperCol">
                  <a-input type="textarea" :autosize="{ minRows: 5 }" />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary">保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane tab="附件设置" key="attachment"> </a-tab-pane>
            <a-tab-pane tab="后台设置" key="admin"> </a-tab-pane>
            <a-tab-pane tab="邮箱设置" key="email"> </a-tab-pane>
            <a-tab-pane tab="其他设置" key="other">
              <a-form layout="vertical">
                <a-form-item label="API服务：" :wrapper-col="wrapperCol">
                  <a-radio-group v-decorator="['radio-group']" defaultValue="true">
                    <a-radio value="true">
                      启用
                    </a-radio>
                    <a-radio value="false">
                      禁用
                    </a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item label="Api Token：" :wrapper-col="wrapperCol">
                  <a-input />
                </a-form-item>
                <a-form-item label="统计代码：" :wrapper-col="wrapperCol">
                  <a-input type="textarea" :autosize="{ minRows: 5 }" />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary">保存</a-button>
                </a-form-item>
              </a-form>
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
.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}
</style>
