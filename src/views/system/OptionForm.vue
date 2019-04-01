<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane
              tab="常规设置"
              key="general"
            >
              <a-form layout="vertical">
                <a-form-item
                  label="博客标题："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.blog_title" />
                </a-form-item>
                <a-form-item
                  label="博客地址："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.blog_url" />
                </a-form-item>
                <a-form-item
                  label="LOGO："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.blog_logo" />
                </a-form-item>
                <a-form-item
                  label="Favicon："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.blog_favicon" />
                </a-form-item>
                <a-form-item
                  label="页脚信息："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_footer_info"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="saveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane
              tab="SEO设置"
              key="seo"
            >
              <a-form layout="vertical">
                <a-form-item
                  label="关键词： "
                  :wrapper-col="wrapperCol"
                >
                  <a-tooltip
                    :trigger="['focus']"
                    placement="right"
                    title="多个关键词以英文逗号隔开"
                  >
                    <a-input v-model="options.seo_keywords" />
                  </a-tooltip>
                </a-form-item>
                <a-form-item
                  label="博客描述："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.seo_description" />
                </a-form-item>
                <a-form-item
                  label="百度推送 Token： "
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.seo_baidu_token" />
                </a-form-item>
                <a-form-item
                  label="百度站点验证："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.seo_verification_baidu" />
                </a-form-item>
                <a-form-item
                  label="Google 站点验证："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.seo_verification_google" />
                </a-form-item>
                <a-form-item
                  label="Bing 站点验证："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.seo_verification_bing" />
                </a-form-item>
                <a-form-item
                  label="360 站点验证："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.seo_verification_qihu" />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="saveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane
              tab="文章设置"
              key="post"
            >
              <a-form layout="vertical">
                <a-form-item
                  label="首页显示条数："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    defaultValue="10"
                    v-model="options.post_index_page_size"
                  />
                </a-form-item>
                <a-form-item
                  label="RSS 显示条数："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    defaultValue="10"
                    v-model="options.rss_page_size"
                  />
                </a-form-item>
                <a-form-item
                  label="文章摘要字数："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    defaultValue="200"
                    v-model="options.post_summary_length"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="saveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane
              tab="评论设置"
              key="comment"
            >
              <a-form layout="vertical">
                <a-form-item
                  label="评论者头像："
                  :wrapper-col="wrapperCol"
                >
                  <a-select
                    defaultValue="mm"
                    v-model="options.comment_gavatar_default"
                  >
                    <a-select-option value="mm">默认</a-select-option>
                    <a-select-option value="identicon">抽象几何图形</a-select-option>
                    <a-select-option value="monsterid">小怪物</a-select-option>
                    <a-select-option value="wavatar">Wavatar</a-select-option>
                    <a-select-option value="retro">复古</a-select-option>
                    <a-select-option value="robohash">机器人</a-select-option>
                    <a-select-option value="blank">不显示头像</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item
                  label="评论审核后才显示："
                  :wrapper-col="wrapperCol"
                >
                  <a-radio-group
                    v-decorator="['radio-group']"
                    defaultValue="true"
                    v-model="options.comment_new_need_check"
                  >
                    <a-radio value="true">启用</a-radio>
                    <a-radio value="false">禁用</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item
                  label="新评论通知："
                  :wrapper-col="wrapperCol"
                >
                  <a-radio-group
                    v-decorator="['radio-group']"
                    defaultValue="true"
                    v-model="options.comment_new_notice"
                  >
                    <a-radio value="true">启用</a-radio>
                    <a-radio value="false">禁用</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item
                  label="评论审核通过通知对方："
                  :wrapper-col="wrapperCol"
                >
                  <a-radio-group
                    v-decorator="['radio-group']"
                    defaultValue="false"
                    v-model="options.comment_pass_notice"
                  >
                    <a-radio value="true">启用</a-radio>
                    <a-radio value="false">禁用</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item
                  label="评论回复通知对方："
                  :wrapper-col="wrapperCol"
                >
                  <a-radio-group
                    v-decorator="['radio-group']"
                    defaultValue="false"
                    v-model="options.comment_reply_notice"
                  >
                    <a-radio value="true">启用</a-radio>
                    <a-radio value="false">禁用</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item
                  label="API 评论开关："
                  :wrapper-col="wrapperCol"
                >
                  <a-radio-group
                    v-decorator="['radio-group']"
                    defaultValue="false"
                    v-model="options.comment_api_enabled"
                  >
                    <a-radio value="true">启用</a-radio>
                    <a-radio value="false">禁用</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item
                  label="每页显示条数： "
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    defaultValue="10"
                    v-model="options.comment_page_size"
                  />
                </a-form-item>
                <a-form-item
                  label="占位提示："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.comment_content_placeholder" />
                </a-form-item>
                <a-form-item
                  label="自定义样式："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.comment_custom_style"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="saveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane
              tab="附件设置"
              key="attachment"
            >
              <a-form layout="vertical">
                <a-form-item
                  label="存储位置："
                  :wrapper-col="wrapperCol"
                >
                  <a-select
                    defaultValue="local"
                    @change="handleAttachChange"
                    v-model="options.attachment_type"
                  >
                    <a-select-option value="local">本地</a-select-option>
                    <a-select-option value="ypyun">又拍云</a-select-option>
                    <a-select-option value="qnyun">七牛云</a-select-option>
                    <a-select-option value="smms">SM.MS</a-select-option>
                  </a-select>
                </a-form-item>
                <div
                  class="upyunForm"
                  v-show="upyunFormHidden"
                >
                  <a-form-item
                    label="域名："
                    :wrapper-col="wrapperCol"
                  >
                    <a-tooltip
                      :trigger="['focus']"
                      placement="right"
                      title="需要加上 http:// 或者 https://"
                    >
                      <a-input v-model="options.oss_upyun_domain" />
                    </a-tooltip>
                  </a-form-item>
                  <a-form-item
                    label="空间名称："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_upyun_bucket" />
                  </a-form-item>
                  <a-form-item
                    label="操作员名称："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_upyun_operator" />
                  </a-form-item>
                  <a-form-item
                    label="操作员密码："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_upyun_password" />
                  </a-form-item>
                  <a-form-item
                    label="文件目录："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_upyun_source" />
                  </a-form-item>
                  <a-form-item
                    label="处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_upyun_small_url" />
                  </a-form-item>
                </div>
                <div
                  class="qiniuForm"
                  v-show="qiniuFormHidden"
                >
                  <a-form-item
                    label="区域："
                    :wrapper-col="wrapperCol"
                  >
                    <a-select
                      defaultValue="auto"
                      v-model="options.oss_qiniu_zone"
                    >
                      <a-select-option value="auto">自动选择</a-select-option>
                      <a-select-option value="z0">华东</a-select-option>
                      <a-select-option value="z1">华北</a-select-option>
                      <a-select-option value="z2">华南</a-select-option>
                      <a-select-option value="na0">北美</a-select-option>
                      <a-select-option value="as0">东南亚</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item
                    label="域名："
                    :wrapper-col="wrapperCol"
                  >
                    <a-tooltip
                      :trigger="['focus']"
                      placement="right"
                      title="需要加上 http:// 或者 https://"
                    >
                      <a-input v-model="options.oss_qiniu_domain" />
                    </a-tooltip>
                  </a-form-item>
                  <a-form-item
                    label="Access Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_qiniu_access_key" />
                  </a-form-item>
                  <a-form-item
                    label="Secret Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_qiniu_secret_key" />
                  </a-form-item>
                  <a-form-item
                    label="Bucket："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_qiniu_bucket" />
                  </a-form-item>
                  <a-form-item
                    label="处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_qiniu_small_url" />
                  </a-form-item>
                </div>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="saveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane
              tab="SMTP 服务"
              key="smtp"
            >
              <a-tabs defaultActiveKey="1">
                <a-tab-pane
                  tab="发信设置"
                  key="1"
                >
                  <a-form layout="vertical">
                    <a-form-item
                      label="是否启用："
                      :wrapper-col="wrapperCol"
                    >
                      <a-radio-group
                        v-decorator="['radio-group']"
                        defaultValue="false"
                        v-model="options.email_enabled"
                      >
                        <a-radio value="true">启用</a-radio>
                        <a-radio value="false">禁用</a-radio>
                      </a-radio-group>
                    </a-form-item>
                    <a-form-item
                      label="SMTP 地址："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input v-model="options.email_host" />
                    </a-form-item>
                    <a-form-item
                      label="发送协议："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input v-model="options.email_protocol" />
                    </a-form-item>
                    <a-form-item
                      label="SSL 端口："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input v-model="options.email_ssl_port" />
                    </a-form-item>
                    <a-form-item
                      label="邮箱账号："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input v-model="options.email_username" />
                    </a-form-item>
                    <a-form-item
                      label="邮箱密码："
                      :wrapper-col="wrapperCol"
                    >
                      <a-tooltip
                        :trigger="['focus']"
                        placement="right"
                        title="部分邮箱可能是授权码"
                      >
                        <a-input v-model="options.email_password" />
                      </a-tooltip>
                    </a-form-item>
                    <a-form-item
                      label="发件人："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input v-model="options.email_from_name" />
                    </a-form-item>
                    <a-form-item>
                      <a-button
                        type="primary"
                        @click="saveOptions"
                      >保存</a-button>
                    </a-form-item>
                  </a-form>
                </a-tab-pane>
                <a-tab-pane
                  tab="发送测试"
                  key="2"
                >
                  <a-form layout="vertical">
                    <a-form-item
                      label="收件人："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input />
                    </a-form-item>
                    <a-form-item
                      label="主题："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input />
                    </a-form-item>
                    <a-form-item
                      label="内容："
                      :wrapper-col="wrapperCol"
                    >
                      <a-input
                        type="textarea"
                        :autosize="{ minRows: 5 }"
                      />
                    </a-form-item>
                    <a-form-item>
                      <a-button type="primary">发送</a-button>
                    </a-form-item>
                  </a-form>
                </a-tab-pane>
              </a-tabs>
            </a-tab-pane>
            <a-tab-pane
              tab="其他设置"
              key="other"
            >
              <a-form layout="vertical">
                <a-form-item
                  label="API服务："
                  :wrapper-col="wrapperCol"
                >
                  <a-radio-group
                    v-decorator="['radio-group']"
                    defaultValue="false"
                    v-model="options.api_enabled"
                  >
                    <a-radio value="true">启用</a-radio>
                    <a-radio value="false">禁用</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item
                  label="Api Token："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.blog_api_token" />
                </a-form-item>
                <a-form-item
                  label="统计代码："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_statistics_code"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="saveOptions"
                  >保存</a-button>
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
import optionApi from '@/api/option'
export default {
  data() {
    return {
      wrapperCol: {
        xl: { span: 8 },
        sm: { span: 8 },
        xs: { span: 24 }
      },
      upyunFormHidden: false,
      qiniuFormHidden: false,
      options: []
    }
  },
  mounted() {
    this.loadOptions()
  },
  methods: {
    loadOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
      })
    },
    saveOptions() {
      optionApi.save(this.options).then(response => {
        this.loadOptions()
        this.$message.success('保存成功！')
      })
    },
    handleAttachChange(e) {
      switch (e) {
        case '0':
        case '3':
          this.upyunFormHidden = false
          this.qiniuFormHidden = false
          break
        case '1':
          this.upyunFormHidden = true
          this.qiniuFormHidden = false
          break
        case '2':
          this.qiniuFormHidden = true
          this.upyunFormHidden = false
          break
      }
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
