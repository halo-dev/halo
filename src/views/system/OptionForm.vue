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
                    placeholder="支持 HTML 格式的文本"
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
                  <a-input
                    v-model="options.seo_keywords"
                    placeholder="多个关键词以英文状态下的逗号隔开"
                  />
                </a-form-item>
                <a-form-item
                  label="博客描述："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.seo_description"
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
            <a-tab-pane key="post">
              <span slot="tab">
                <a-icon type="form" />文章设置
              </span>
              <a-form layout="vertical">
                <a-form-item
                  label="首页文章排序："
                  :wrapper-col="wrapperCol"
                >
                  <a-select v-model="options.post_index_sort">
                    <a-select-option value="createTime">创建时间</a-select-option>
                    <a-select-option value="editTime">最后编辑时间</a-select-option>
                    <a-select-option value="visits">点击量</a-select-option>
                  </a-select>
                </a-form-item>
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
                  label="评论模块 JS："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 2 }"
                    v-model="options.comment_internal_plugin_js"
                    placeholder="该设置仅对内置的评论模块有效"
                  />
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
                  class="smmsForm"
                  v-show="smmsFormVisible"
                >
                  <a-form-item
                    label="Secret Token："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.smms_api_secret_token"
                      placeholder="需要到 sm.ms 官网注册后获取"
                    />
                  </a-form-item>
                </div>
                <div
                  class="upyunForm"
                  v-show="upyunFormVisible"
                >
                  <a-form-item
                    label="绑定域名："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_upyun_domain"
                      placeholder="需要加上 http:// 或者 https://"
                    />
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
                    label="图片处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_upyun_style_rule"
                      placeholder="间隔标识符+图片处理版本名称"
                    />
                  </a-form-item>
                  <a-form-item
                    label="缩略图处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_upyun_thumbnail_style_rule"
                      placeholder="间隔标识符+图片处理版本名称，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  class="qnyunForm"
                  v-show="qnyunFormVisible"
                >
                  <a-form-item
                    label="绑定域名："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_qiniu_domain"
                      placeholder="需要加上 http:// 或者 https://"
                    />
                  </a-form-item>
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
                    label="Access Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.oss_qiniu_access_key" />
                  </a-form-item>
                  <a-form-item
                    label="Secret Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      type="password"
                      v-model="options.oss_qiniu_secret_key"
                    />
                  </a-form-item>
                  <a-form-item
                    label="Bucket："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_qiniu_bucket"
                      placeholder="存储空间名称"
                    />
                  </a-form-item>
                  <a-form-item
                    label="图片处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_qiniu_style_rule"
                      placeholder="样式分隔符+图片处理样式名称"
                    />
                  </a-form-item>
                  <a-form-item
                    label="缩略图处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_qiniu_thumbnail_style_rule"
                      placeholder="样式分隔符+图片处理样式名称，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  class="aliyunForm"
                  v-show="aliyunFormVisible"
                >
                  <a-form-item
                    label="绑定域名："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_aliyun_domain"
                      placeholder="如不填写，路径根域名将为 Bucket + EndPoint"
                    />
                  </a-form-item>
                  <a-form-item
                    label="Bucket："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_aliyun_bucket_name"
                      placeholder="存储空间名称"
                    />
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
                    <a-input
                      type="password"
                      v-model="options.oss_aliyun_access_secret"
                    />
                  </a-form-item>
                  <a-form-item
                    label="图片处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_aliyun_style_rule"
                      placeholder="请到阿里云控制台的图片处理获取"
                    />
                  </a-form-item>
                  <a-form-item
                    label="缩略图处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.oss_aliyun_thumbnail_style_rule"
                      placeholder="请到阿里云控制台的图片处理获取，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  class="baiduyunForm"
                  v-show="baiduyunFormVisible"
                >
                  <a-form-item
                    label="绑定域名："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.bos_baiduyun_domain"
                      placeholder="如不填写，路径根域名将为 Bucket + EndPoint"
                    />
                  </a-form-item>
                  <a-form-item
                    label="Bucket："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.bos_baiduyun_bucket_name"
                      placeholder="存储空间名称"
                    />
                  </a-form-item>
                  <a-form-item
                    label="EndPoint（地域节点）："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.bos_baiduyun_endpoint" />
                  </a-form-item>
                  <a-form-item
                    label="Access Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.bos_baiduyun_access_key" />
                  </a-form-item>
                  <a-form-item
                    label="Secret Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      type="password"
                      v-model="options.bos_baiduyun_secret_key"
                    />
                  </a-form-item>
                  <a-form-item
                    label="图片处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.bos_baiduyun_style_rule"
                      placeholder="请到百度云控制台的图片处理获取"
                    />
                  </a-form-item>
                  <a-form-item
                    label="缩略图处理策略："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.bos_baiduyun_thumbnail_style_rule"
                      placeholder="请到百度云控制台的图片处理获取，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  class="tencentyunForm"
                  v-show="tencentyunFormVisible"
                >
                  <a-form-item
                    label="绑定域名："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.cos_tencentyun_domain"
                      placeholder="如不填写，路径根域名将为 Bucket + 区域地址"
                    />
                  </a-form-item>
                  <a-form-item
                    label="Bucket："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      v-model="options.cos_tencentyun_bucket_name"
                      placeholder="存储桶名称"
                    />
                  </a-form-item>
                  <a-form-item
                    label="区域："
                    :wrapper-col="wrapperCol"
                  >
                    <a-select v-model="options.cos_tencentyun_region">
                      <a-select-option value="ap-beijing-1">北京一区</a-select-option>
                      <a-select-option value="ap-beijing">北京</a-select-option>
                      <a-select-option value="ap-shanghai">上海（华东）</a-select-option>
                      <a-select-option value="ap-guangzhou">广州（华南）</a-select-option>
                      <a-select-option value="ap-chengdu">成都（西南）</a-select-option>
                      <a-select-option value="ap-chongqing">重庆</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item
                    label="Secret Id："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input v-model="options.cos_tencentyun_secret_id" />
                  </a-form-item>
                  <a-form-item
                    label="Secret Key："
                    :wrapper-col="wrapperCol"
                  >
                    <a-input
                      type="password"
                      v-model="options.cos_tencentyun_secret_key"
                    />
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
                        <a-input
                          v-model="options.email_password"
                          type="password"
                          placeholder="部分邮箱可能是授权码"
                        />
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
                <a-icon type="thunderbolt" />API 设置
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
                  label="CDN 加速域名："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    v-model="options.blog_cdn_domain"
                    placeholder="请确保已经正确配置好了 CDN"
                  />
                </a-form-item>
                <a-form-item
                  label="自定义 head："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_custom_head"
                    placeholder="将放置于每个页面的<head></head>标签中"
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
                    placeholder="第三方网站统计的代码，如：Google Analytics、百度统计、CNZZ 等"
                  />
                </a-form-item>
                <!-- <a-form-item
                  label="黑名单 IP："
                  :wrapper-col="wrapperCol"
                >
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_ip_blacklist"
                    placeholder="多个 IP 地址换行隔开"
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
import { mapActions } from 'vuex'
import optionApi from '@/api/option'
import mailApi from '@/api/mail'
import attachmentApi from '@/api/attachment'

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
      smmsFormVisible: false,
      upyunFormVisible: false,
      qnyunFormVisible: false,
      aliyunFormVisible: false,
      baiduyunFormVisible: false,
      tencentyunFormVisible: false,
      logoDrawerVisible: false,
      faviconDrawerVisible: false,
      options: [],
      mailParam: {}
    }
  },
  mounted() {
    this.loadFormOptions()
  },
  destroyed: function() {
    if (this.faviconDrawerVisible) {
      this.faviconDrawerVisible = false
    }
    if (this.logoDrawerVisible) {
      this.logoDrawerVisible = false
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.faviconDrawerVisible) {
      this.faviconDrawerVisible = false
    }
    if (this.logoDrawerVisible) {
      this.logoDrawerVisible = false
    }
    next()
  },
  methods: {
    ...mapActions(['loadUser', 'loadOptions']),
    loadFormOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
        this.handleAttachChange(this.options['attachment_type'])
      })
    },
    handleSaveOptions() {
      if (!this.options.blog_title) {
        this.$notification['error']({
          message: '提示',
          description: '博客标题不能为空！'
        })
        return
      }

      if (!this.options.blog_url) {
        this.$notification['error']({
          message: '提示',
          description: '博客地址不能为空！'
        })
        return
      }

      // 新评论通知和回复通知验证
      if (this.options.comment_new_notice || this.options.comment_reply_notice) {
        if (!this.options.email_enabled) {
          this.$notification['error']({
            message: '提示',
            description: '新评论通知或回复通知需要打开和配置 SMTP 服务！'
          })
          return
        }
      }

      // 附件配置验证
      switch (this.options.attachment_type) {
        case 'SMMS':
          if (!this.options.smms_api_secret_token) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Token不能为空！'
            })
            return
          }
          break
        case 'UPYUN':
          if (!this.options.oss_upyun_domain) {
            this.$notification['error']({
              message: '提示',
              description: '域名不能为空！'
            })
            return
          }
          if (!this.options.oss_upyun_bucket) {
            this.$notification['error']({
              message: '提示',
              description: '空间名称不能为空！'
            })
            return
          }
          if (!this.options.oss_upyun_operator) {
            this.$notification['error']({
              message: '提示',
              description: '操作员名称不能为空！'
            })
            return
          }
          if (!this.options.oss_upyun_password) {
            this.$notification['error']({
              message: '提示',
              description: '操作员密码不能为空！'
            })
            return
          }
          if (!this.options.oss_upyun_source) {
            this.$notification['error']({
              message: '提示',
              description: '文件目录不能为空！'
            })
            return
          }
          break
        case 'QNYUN':
          if (!this.options.oss_qiniu_domain) {
            this.$notification['error']({
              message: '提示',
              description: '域名不能为空！'
            })
            return
          }
          if (!this.options.oss_qiniu_access_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Key 不能为空！'
            })
            return
          }
          if (!this.options.oss_qiniu_secret_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Key 不能为空！'
            })
            return
          }
          if (!this.options.oss_qiniu_bucket) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          break
        case 'ALIYUN':
          if (!this.options.oss_aliyun_bucket_name) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          if (!this.options.oss_aliyun_endpoint) {
            this.$notification['error']({
              message: '提示',
              description: 'EndPoint（地域节点） 不能为空！'
            })
            return
          }
          if (!this.options.oss_aliyun_access_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Key 不能为空！'
            })
            return
          }
          if (!this.options.oss_aliyun_access_secret) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Secret 不能为空！'
            })
            return
          }
          break
        case 'BAIDUYUN':
          if (!this.options.bos_baiduyun_bucket_name) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          if (!this.options.bos_baiduyun_endpoint) {
            this.$notification['error']({
              message: '提示',
              description: 'EndPoint（地域节点） 不能为空！'
            })
            return
          }
          if (!this.options.bos_baiduyun_access_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Key 不能为空！'
            })
            return
          }
          if (!this.options.bos_baiduyun_secret_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Key 不能为空！'
            })
            return
          }
          break
        case 'TENCENTYUN':
          if (!this.options.cos_tencentyun_bucket_name) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          if (!this.options.cos_tencentyun_region) {
            this.$notification['error']({
              message: '提示',
              description: '区域不能为空！'
            })
            return
          }
          if (!this.options.cos_tencentyun_secret_id) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Id 不能为空！'
            })
            return
          }
          if (!this.options.cos_tencentyun_secret_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Key 不能为空！'
            })
            return
          }
          break
      }

      // SMTP 配置验证
      if (this.options.email_enabled) {
        if (!this.options.email_host) {
          this.$notification['error']({
            message: '提示',
            description: 'SMTP 地址不能为空！'
          })
          return
        }
        if (!this.options.email_protocol) {
          this.$notification['error']({
            message: '提示',
            description: '发送协议不能为空！'
          })
          return
        }
        if (!this.options.email_ssl_port) {
          this.$notification['error']({
            message: '提示',
            description: 'SSL 端口不能为空！'
          })
          return
        }
        if (!this.options.email_username) {
          this.$notification['error']({
            message: '提示',
            description: '邮箱账号不能为空！'
          })
          return
        }
        if (!this.options.email_password) {
          this.$notification['error']({
            message: '提示',
            description: '邮箱密码不能为空！'
          })
          return
        }
        if (!this.options.email_from_name) {
          this.$notification['error']({
            message: '提示',
            description: '发件人不能为空！'
          })
          return
        }
      }

      // API 配置验证
      if (this.options.api_enabled) {
        if (!this.options.api_access_key) {
          this.$notification['error']({
            message: '提示',
            description: 'Access key 不能为空！'
          })
          return
        }
      }

      optionApi.save(this.options).then(response => {
        this.loadFormOptions()
        this.loadOptions()
        this.loadUser()
        this.$message.success('保存成功！')
      })
    },
    handleAttachChange(e) {
      switch (e) {
        case 'LOCAL':
          this.upyunFormVisible = false
          this.qnyunFormVisible = false
          this.aliyunFormVisible = false
          this.baiduyunFormVisible = false
          this.tencentyunFormVisible = false
          this.smmsFormVisible = false
          break
        case 'SMMS':
          this.smmsFormVisible = true
          this.upyunFormVisible = false
          this.qnyunFormVisible = false
          this.aliyunFormVisible = false
          this.baiduyunFormVisible = false
          this.tencentyunFormVisible = false
          break
        case 'UPYUN':
          this.smmsFormVisible = false
          this.upyunFormVisible = true
          this.qnyunFormVisible = false
          this.aliyunFormVisible = false
          this.baiduyunFormVisible = false
          this.tencentyunFormVisible = false
          break
        case 'QNYUN':
          this.smmsFormVisible = false
          this.qnyunFormVisible = true
          this.upyunFormVisible = false
          this.aliyunFormVisible = false
          this.baiduyunFormVisible = false
          this.tencentyunFormVisible = false
          break
        case 'ALIYUN':
          this.smmsFormVisible = false
          this.aliyunFormVisible = true
          this.qnyunFormVisible = false
          this.upyunFormVisible = false
          this.baiduyunFormVisible = false
          this.tencentyunFormVisible = false
          break
        case 'BAIDUYUN':
          this.smmsFormVisible = false
          this.aliyunFormVisible = false
          this.qnyunFormVisible = false
          this.upyunFormVisible = false
          this.baiduyunFormVisible = true
          this.tencentyunFormVisible = false
          break
        case 'TENCENTYUN':
          this.smmsFormVisible = false
          this.aliyunFormVisible = false
          this.qnyunFormVisible = false
          this.upyunFormVisible = false
          this.baiduyunFormVisible = false
          this.tencentyunFormVisible = true
          break
      }
    },
    handleSelectLogo(data) {
      this.options.blog_logo = encodeURI(data.path)
      this.logoDrawerVisible = false
    },
    handleTestMailClick() {
      if (!this.mailParam.to) {
        this.$notification['error']({
          message: '提示',
          description: '收件人不能为空！'
        })
        return
      }
      if (!this.mailParam.subject) {
        this.$notification['error']({
          message: '提示',
          description: '主题不能为空！'
        })
        return
      }
      if (!this.mailParam.content) {
        this.$notification['error']({
          message: '提示',
          description: '内容不能为空！'
        })
        return
      }
      mailApi.testMail(this.mailParam).then(response => {
        this.$message.info(response.data.message)
      })
    },
    handleSelectFavicon(data) {
      this.options.blog_favicon = encodeURI(data.path)
      this.faviconDrawerVisible = false
    }
  }
}
</script>
