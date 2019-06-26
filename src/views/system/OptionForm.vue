<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane key="general">
              <span slot="tab">
                <a-icon type="tool" />常规设置
              </span>
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
                  <a-input
                    v-model="options.blog_url"
                    placeholder="如：https://halo.run"
                  />
                </a-form-item>
                <a-form-item
                  label="Logo："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.blog_logo">
                    <a
                      href="javascript:void(0);"
                      slot="addonAfter"
                      @click="()=>this.logoDrawerVisible = true"
                    >
                      <a-icon type="picture" />
                    </a>
                  </a-input>
                </a-form-item>
                <a-form-item
                  label="Favicon："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.blog_favicon">
                    <a
                      href="javascript:void(0);"
                      slot="addonAfter"
                      @click="()=>this.faviconDrawerVisible = true"
                    >
                      <a-icon type="picture" />
                    </a>
                  </a-input>
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
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="seo">
              <span slot="tab">
                <a-icon type="global" />SEO 设置
              </span>
              <a-form layout="vertical">
                <a-form-item
                  label="屏蔽搜索引擎："
                  :wrapper-col="wrapperCol"
                >
                  <a-switch v-model="options.seo_spider_disabled" />
                </a-form-item>
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
                <!-- <a-form-item
                  label="百度推送 Token： "
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.seo_baidu_token" />
                </a-form-item> -->
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="post">
              <span slot="tab">
                <a-icon type="form" />文章设置
              </span>
              <a-form layout="vertical">
                <a-form-item
                  label="首页显示条数："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    v-model="options.post_index_page_size"
                  />
                </a-form-item>
                <a-form-item
                  label="RSS 显示条数："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    v-model="options.rss_page_size"
                  />
                </a-form-item>
                <a-form-item
                  label="文章摘要字数："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    v-model="options.post_summary_length"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="comment">
              <span slot="tab">
                <a-icon type="message" />评论设置
              </span>
              <a-form layout="vertical">
                <a-form-item
                  label="评论者头像："
                  :wrapper-col="wrapperCol"
                >
                  <a-select v-model="options.comment_gravatar_default">
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
                  <a-switch v-model="options.comment_new_need_check" />
                </a-form-item>
                <a-form-item
                  label="新评论通知："
                  :wrapper-col="wrapperCol"
                >
                  <a-switch v-model="options.comment_new_notice" />
                </a-form-item>
                <a-form-item
                  label="评论回复通知对方："
                  :wrapper-col="wrapperCol"
                >
                  <a-switch v-model="options.comment_reply_notice" />
                </a-form-item>
                <a-form-item
                  label="API 评论开关："
                  :wrapper-col="wrapperCol"
                >
                  <a-switch v-model="options.comment_api_enabled" />
                </a-form-item>
                <a-form-item
                  label="每页显示条数： "
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="number"
                    v-model="options.comment_page_size"
                  />
                </a-form-item>
                <a-form-item
                  label="占位提示："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.comment_content_placeholder" />
                </a-form-item>
                <!-- <a-form-item
                  label="自定义样式："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.comment_custom_style"
                  />
                </a-form-item> -->
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="attachment">
              <span slot="tab">
                <a-icon type="picture" />附件设置
              </span>
              <a-form layout="vertical">
                <a-form-item
                  label="存储位置："
                  :wrapper-col="wrapperCol"
                >
                  <a-select
                    @change="handleAttachChange"
                    v-model="options.attachment_type"
                  >
                    <a-select-option
                      v-for="item in Object.keys(attachmentType)"
                      :key="item"
                      :value="item"
                    >{{ attachmentType[item].text }}</a-select-option>
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
                    <a-input
                      type="password"
                      v-model="options.oss_upyun_password"
                    />
                  </a-form-item>
                  <a-form-item
                    label="文件目录："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_upyun_source" />
                  </a-form-item>
                  <a-form-item
                    label="缩略图处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_upyun_style_rule" />
                  </a-form-item>
                </div>
                <div
                  class="qnyunForm"
                  v-show="qnyunFormHidden"
                >
                  <a-form-item
                    label="区域："
                    :wrapper-col="wrapperCol"
                  >
                    <a-select v-model="options.oss_qiniu_zone">
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
                    label="缩略图处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_qiniu_style_rule" />
                  </a-form-item>
                </div>
                <div
                  class="aliyunForm"
                  v-show="aliyunFormHidden"
                >
                  <a-form-item
                    label="Bucket："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_aliyun_bucket_name" />
                  </a-form-item>
                  <a-form-item
                    label="EndPoint（地域节点）："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_aliyun_endpoint" />
                  </a-form-item>
                  <a-form-item
                    label="Access Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_aliyun_access_key" />
                  </a-form-item>
                  <a-form-item
                    label="Access Secret："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_aliyun_access_secret" />
                  </a-form-item>
                  <a-form-item
                    label="缩略图处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_aliyun_style_rule" />
                  </a-form-item>
                </div>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="smtp">
              <span slot="tab">
                <a-icon type="mail" />SMTP 服务
              </span>
              <div class="custom-tab-wrapper">
                <a-tabs>
                  <a-tab-pane
                    tab="发信设置"
                    key="1"
                  >
                    <a-form layout="vertical">
                      <a-form-item
                        label="是否启用："
                        :wrapper-col="wrapperCol"
                      >
                        <a-switch v-model="options.email_enabled" />
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
                          @click="handleSaveOptions"
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
                        <a-input v-model="mailParam.to" />
                      </a-form-item>
                      <a-form-item
                        label="主题："
                        :wrapper-col="wrapperCol"
                      >
                        <a-input v-model="mailParam.subject" />
                      </a-form-item>
                      <a-form-item
                        label="内容："
                        :wrapper-col="wrapperCol"
                      >
                        <a-input
                          type="textarea"
                          :autosize="{ minRows: 5 }"
                          v-model="mailParam.content"
                        />
                      </a-form-item>
                      <a-form-item>
                        <a-button
                          type="primary"
                          @click="handleTestMailClick"
                        >发送</a-button>
                      </a-form-item>
                    </a-form>
                  </a-tab-pane>
                </a-tabs>
              </div>
            </a-tab-pane>
            <a-tab-pane key="api">
              <span slot="tab">
                <a-icon type="align-left" />API 设置
              </span>
              <a-form layout="vertical">
                <a-form-item
                  label="API 服务："
                  :wrapper-col="wrapperCol"
                >
                  <a-switch v-model="options.api_enabled" />
                </a-form-item>
                <a-form-item
                  label="Access key："
                  :wrapper-col="wrapperCol"
                >
                  <a-input v-model="options.api_access_key" />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="other">
              <span slot="tab">
                <a-icon type="align-left" />其他设置
              </span>
              <a-form layout="vertical">
                <a-form-item
                  label="自定义 head："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_custom_head"
                  />
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
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="logoDrawerVisible"
      @listenToSelect="handleSelectLogo"
      title="选择 Logo"
    />
    <AttachmentSelectDrawer
      v-model="faviconDrawerVisible"
      @listenToSelect="handleSelectFavicon"
      title="选择 Favicon"
    />
  </div>
</template>
<script>
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import optionApi from '@/api/option'
import mailApi from '@/api/mail'
import attachmentApi from '@/api/attachment'
import { mapActions } from 'vuex'

export default {
  components: {
    AttachmentSelectDrawer
  },
  data() {
    return {
      attachmentType: attachmentApi.type,
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      },
      upyunFormHidden: false,
      qnyunFormHidden: false,
      aliyunFormHidden: false,
      logoDrawerVisible: false,
      faviconDrawerVisible: false,
      options: [],
      mailParam: {}
    }
  },
  mounted() {
    this.loadOptions()
  },
  methods: {
    ...mapActions(['loadUser']),
    loadOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
        this.handleAttachChange(this.options['attachment_type'])
      })
    },
    handleSaveOptions() {
      optionApi.save(this.options).then(response => {
        this.loadOptions()
        this.loadUser()
        this.$message.success('保存成功！')
      })
    },
    handleAttachChange(e) {
      switch (e) {
        case 'LOCAL':
        case 'SMMS':
          this.upyunFormHidden = false
          this.qnyunFormHidden = false
          this.aliyunFormHidden = false
          break
        case 'UPYUN':
          this.upyunFormHidden = true
          this.qnyunFormHidden = false
          this.aliyunFormHidden = false
          break
        case 'QNYUN':
          this.qnyunFormHidden = true
          this.upyunFormHidden = false
          this.aliyunFormHidden = false
          break
        case 'ALIYUN':
          this.aliyunFormHidden = true
          this.qnyunFormHidden = false
          this.upyunFormHidden = false
          break
      }
    },
    handleSelectLogo(data) {
      this.options.blog_logo = data.path
      this.logoDrawerVisible = false
    },
    handleTestMailClick() {
      mailApi.testMail(this.mailParam).then(response => {
        this.$message.info(response.data.message)
      })
    },
    handleSelectFavicon(data) {
      this.options.blog_favicon = data.path
      this.faviconDrawerVisible = false
    }
  }
}
</script>
