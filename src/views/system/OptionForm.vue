<template>
  <div>
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane key="general">
              <span slot="tab">
                <a-icon type="tool" />常规设置
              </span>
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="博客标题：">
                  <a-input v-model="options.blog_title" />
                </a-form-item>
                <a-form-item label="博客地址：">
                  <a-input
                    v-model="options.blog_url"
                    placeholder="如：https://halo.run"
                  />
                </a-form-item>
                <a-form-item label="Logo：">
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
                <a-form-item label="Favicon：">
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
                <a-form-item label="页脚信息：">
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
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="屏蔽搜索引擎：">
                  <a-switch v-model="options.seo_spider_disabled" />
                </a-form-item>
                <a-form-item label="关键词：">
                  <a-input
                    v-model="options.seo_keywords"
                    placeholder="多个关键词以英文状态下的逗号隔开"
                  />
                </a-form-item>
                <a-form-item label="博客描述：">
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
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="首页文章排序：">
                  <a-select v-model="options.post_index_sort">
                    <a-select-option value="createTime">创建时间</a-select-option>
                    <a-select-option value="editTime">最后编辑时间</a-select-option>
                    <a-select-option value="visits">点击量</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="首页显示条数：">
                  <a-input
                    type="number"
                    v-model="options.post_index_page_size"
                  />
                </a-form-item>
                <a-form-item label="RSS 内容类型：">
                  <a-select v-model="options.rss_content_type">
                    <a-select-option value="full">全文</a-select-option>
                    <a-select-option value="summary">摘要</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="RSS 内容条数：">
                  <a-input
                    type="number"
                    v-model="options.rss_page_size"
                  />
                </a-form-item>
                <a-form-item label="文章摘要字数：">
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
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="评论者头像：">
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
                <a-form-item label="评论审核后才显示：">
                  <a-switch v-model="options.comment_new_need_check" />
                </a-form-item>
                <a-form-item label="新评论通知：">
                  <a-switch v-model="options.comment_new_notice" />
                </a-form-item>
                <a-form-item label="评论回复通知对方：">
                  <a-switch v-model="options.comment_reply_notice" />
                </a-form-item>
                <a-form-item
                  label="API 评论开关："
                  help="* 关闭之后将无法进行评论"
                >
                  <a-switch v-model="options.comment_api_enabled" />
                </a-form-item>
                <a-form-item label="评论模块 JS：">
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 2 }"
                    v-model="options.comment_internal_plugin_js"
                    placeholder="该设置仅对内置的评论模块有效"
                  />
                </a-form-item>
                <a-form-item label="每页显示条数： ">
                  <a-input
                    type="number"
                    v-model="options.comment_page_size"
                  />
                </a-form-item>
                <a-form-item label="占位提示：">
                  <a-input v-model="options.comment_content_placeholder" />
                </a-form-item>
                <!-- <a-form-item
                  label="自定义样式："

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
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="上传图片时预览：">
                  <a-switch v-model="options.attachment_upload_image_preview_enable" />
                </a-form-item>
                <a-form-item label="最大上传文件数：">
                  <a-input
                    type="number"
                    v-model="options.attachment_upload_max_files"
                  />
                </a-form-item>
                <a-form-item label="同时上传文件数：">
                  <a-input
                    type="number"
                    v-model="options.attachment_upload_max_parallel_uploads"
                  />
                </a-form-item>
                <a-form-item label="存储位置：">
                  <a-select v-model="options.attachment_type">
                    <a-select-option
                      v-for="item in Object.keys(attachmentType)"
                      :key="item"
                      :value="item"
                    >{{ attachmentType[item].text }}</a-select-option>
                  </a-select>
                </a-form-item>
                <div
                  id="smmsForm"
                  v-show="options.attachment_type === 'SMMS'"
                >
                  <a-form-item label="Secret Token：">
                    <a-input-password
                      v-model="options.smms_api_secret_token"
                      placeholder="需要到 sm.ms 官网注册后获取"
                    />
                  </a-form-item>
                </div>
                <div
                  id="upOssForm"
                  v-show="options.attachment_type === 'UPOSS'"
                >
                  <a-form-item label="绑定域名协议：">
                    <a-select v-model="options.oss_upyun_domain_protocol">
                      <a-select-option value="https://">HTTPS</a-select-option>
                      <a-select-option value="http://">HTTP</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="绑定域名：">
                    <a-input
                      v-model="options.oss_upyun_domain"
                      placeholder="无需再加上 http:// 或者 https://"
                    />
                  </a-form-item>
                  <a-form-item label="空间名称：">
                    <a-input v-model="options.oss_upyun_bucket" />
                  </a-form-item>
                  <a-form-item label="操作员名称：">
                    <a-input v-model="options.oss_upyun_operator" />
                  </a-form-item>
                  <a-form-item label="操作员密码：">
                    <a-input-password v-model="options.oss_upyun_password" />
                  </a-form-item>
                  <a-form-item label="文件目录：">
                    <a-input v-model="options.oss_upyun_source" />
                  </a-form-item>
                  <a-form-item label="图片处理策略：">
                    <a-input
                      v-model="options.oss_upyun_style_rule"
                      placeholder="间隔标识符+图片处理版本名称"
                    />
                  </a-form-item>
                  <a-form-item label="缩略图处理策略：">
                    <a-input
                      v-model="options.oss_upyun_thumbnail_style_rule"
                      placeholder="间隔标识符+图片处理版本名称，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  id="qiniuOssForm"
                  v-show="options.attachment_type === 'QINIUOSS'"
                >
                  <a-form-item label="绑定域名协议：">
                    <a-select v-model="options.oss_qiniu_domain_protocol">
                      <a-select-option value="https://">HTTPS</a-select-option>
                      <a-select-option value="http://">HTTP</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="绑定域名：">
                    <a-input
                      v-model="options.oss_qiniu_domain"
                      placeholder="无需再加上 http:// 或者 https://"
                    />
                  </a-form-item>
                  <a-form-item label="区域：">
                    <a-auto-complete
                      :dataSource="qiniuOssZones"
                      v-model="options.oss_qiniu_zone"
                      allowClear
                    />
                  </a-form-item>
                  <a-form-item label="Access Key：">
                    <a-input-password v-model="options.oss_qiniu_access_key" />
                  </a-form-item>
                  <a-form-item label="Secret Key：">
                    <a-input-password v-model="options.oss_qiniu_secret_key" />
                  </a-form-item>
                  <a-form-item label="文件目录：">
                    <a-input
                      v-model="options.oss_qiniu_source"
                      placeholder="不填写则上传到根目录"
                    />
                  </a-form-item>
                  <a-form-item label="Bucket：">
                    <a-input
                      v-model="options.oss_qiniu_bucket"
                      placeholder="存储空间名称"
                    />
                  </a-form-item>
                  <a-form-item label="图片处理策略：">
                    <a-input
                      v-model="options.oss_qiniu_style_rule"
                      placeholder="样式分隔符+图片处理样式名称"
                    />
                  </a-form-item>
                  <a-form-item label="缩略图处理策略：">
                    <a-input
                      v-model="options.oss_qiniu_thumbnail_style_rule"
                      placeholder="样式分隔符+图片处理样式名称，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  id="aliOssForm"
                  v-show="options.attachment_type === 'ALIOSS'"
                >
                  <a-form-item label="绑定域名协议：">
                    <a-select v-model="options.oss_ali_domain_protocol">
                      <a-select-option value="https://">HTTPS</a-select-option>
                      <a-select-option value="http://">HTTP</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="绑定域名：">
                    <a-input
                      v-model="options.oss_ali_domain"
                      placeholder="如不填写，路径根域名将为 Bucket + EndPoint"
                    />
                  </a-form-item>
                  <a-form-item label="Bucket：">
                    <a-input
                      v-model="options.oss_ali_bucket_name"
                      placeholder="存储空间名称"
                    />
                  </a-form-item>
                  <a-form-item label="EndPoint（地域节点）：">
                    <a-input v-model="options.oss_ali_endpoint" />
                  </a-form-item>
                  <a-form-item label="Access Key：">
                    <a-input-password v-model="options.oss_ali_access_key" />
                  </a-form-item>
                  <a-form-item label="Access Secret：">
                    <a-input-password v-model="options.oss_ali_access_secret" />
                  </a-form-item>
                  <a-form-item label="文件目录：">
                    <a-input
                      v-model="options.oss_ali_source"
                      placeholder="不填写则上传到根目录"
                    />
                  </a-form-item>
                  <a-form-item label="图片处理策略：">
                    <a-input
                      v-model="options.oss_ali_style_rule"
                      placeholder="请到阿里云控制台的图片处理获取"
                    />
                  </a-form-item>
                  <a-form-item label="缩略图处理策略：">
                    <a-input
                      v-model="options.oss_ali_thumbnail_style_rule"
                      placeholder="请到阿里云控制台的图片处理获取，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  id="baiduBosForm"
                  v-show="options.attachment_type === 'BAIDUBOS'"
                >
                  <a-form-item label="绑定域名协议：">
                    <a-select v-model="options.bos_baidu_domain_protocol">
                      <a-select-option value="https://">HTTPS</a-select-option>
                      <a-select-option value="http://">HTTP</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="绑定域名：">
                    <a-input
                      v-model="options.bos_baidu_domain"
                      placeholder="如不填写，路径根域名将为 Bucket + EndPoint"
                    />
                  </a-form-item>
                  <a-form-item label="Bucket：">
                    <a-input
                      v-model="options.bos_baidu_bucket_name"
                      placeholder="存储空间名称"
                    />
                  </a-form-item>
                  <a-form-item label="EndPoint（地域节点）：">
                    <a-input v-model="options.bos_baidu_endpoint" />
                  </a-form-item>
                  <a-form-item label="Access Key：">
                    <a-input-password v-model="options.bos_baidu_access_key" />
                  </a-form-item>
                  <a-form-item label="Secret Key：">
                    <a-input-password v-model="options.bos_baidu_secret_key" />
                  </a-form-item>
                  <a-form-item label="图片处理策略：">
                    <a-input
                      v-model="options.bos_baidu_style_rule"
                      placeholder="请到百度云控制台的图片处理获取"
                    />
                  </a-form-item>
                  <a-form-item label="缩略图处理策略：">
                    <a-input
                      v-model="options.bos_baidu_thumbnail_style_rule"
                      placeholder="请到百度云控制台的图片处理获取，一般为后台展示所用"
                    />
                  </a-form-item>
                </div>
                <div
                  id="tencentCosForm"
                  v-show="options.attachment_type === 'TENCENTCOS'"
                >
                  <a-form-item label="绑定域名协议：">
                    <a-select v-model="options.cos_tencent_domain_protocol">
                      <a-select-option value="https://">HTTPS</a-select-option>
                      <a-select-option value="http://">HTTP</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="绑定域名：">
                    <a-input
                      v-model="options.cos_tencent_domain"
                      placeholder="如不填写，路径根域名将为 Bucket + 区域地址"
                    />
                  </a-form-item>
                  <a-form-item label="Bucket：">
                    <a-input
                      v-model="options.cos_tencent_bucket_name"
                      placeholder="存储桶名称"
                    />
                  </a-form-item>
                  <a-form-item label="区域：">
                    <a-auto-complete
                      :dataSource="tencentCosRegions"
                      v-model="options.cos_tencent_region"
                      allowClear
                    />
                  </a-form-item>
                  <a-form-item label="Secret Id：">
                    <a-input-password v-model="options.cos_tencent_secret_id" />
                  </a-form-item>
                  <a-form-item label="Secret Key：">
                    <a-input-password v-model="options.cos_tencent_secret_key" />
                  </a-form-item>
                  <a-form-item label="文件目录：">
                    <a-input
                      v-model="options.cos_tencent_source"
                      placeholder="不填写则上传到根目录"
                    />
                  </a-form-item>
                  <a-form-item label="图片处理策略：">
                    <a-input
                      v-model="options.cos_tencent_style_rule"
                      placeholder="请到腾讯云控制台的图片处理获取"
                    />
                  </a-form-item>
                  <a-form-item label="缩略图处理策略：">
                    <a-input
                      v-model="options.cos_tencent_thumbnail_style_rule"
                      placeholder="请到腾讯云控制台的图片处理获取，一般为后台展示所用"
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
                    key="smtpoptions"
                  >
                    <a-form
                      layout="vertical"
                      :wrapperCol="wrapperCol"
                    >
                      <a-form-item label="是否启用：">
                        <a-switch v-model="options.email_enabled" />
                      </a-form-item>
                      <a-form-item label="SMTP 地址：">
                        <a-input v-model="options.email_host" />
                      </a-form-item>
                      <a-form-item label="发送协议：">
                        <a-input v-model="options.email_protocol" />
                      </a-form-item>
                      <a-form-item label="SSL 端口：">
                        <a-input v-model="options.email_ssl_port" />
                      </a-form-item>
                      <a-form-item label="邮箱账号：">
                        <a-input v-model="options.email_username" />
                      </a-form-item>
                      <a-form-item label="邮箱密码：">
                        <a-input-password
                          v-model="options.email_password"
                          placeholder="部分邮箱可能是授权码"
                        />
                      </a-form-item>
                      <a-form-item label="发件人：">
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
                    key="smtptest"
                  >
                    <a-form
                      layout="vertical"
                      :wrapperCol="wrapperCol"
                    >
                      <a-form-item label="收件人：">
                        <a-input v-model="mailParam.to" />
                      </a-form-item>
                      <a-form-item label="主题：">
                        <a-input v-model="mailParam.subject" />
                      </a-form-item>
                      <a-form-item label="内容：">
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
            <a-tab-pane key="permalink">
              <span slot="tab">
                <a-icon type="link" />固定链接
              </span>
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="文章固定链接类型：">
                  <template slot="help">
                    <span v-if="options.post_permalink_type === 'DEFAULT'">{{ options.blog_url }}/{{ options.archives_prefix }}/${url}{{ options.path_suffix }}</span>
                    <span v-else-if="options.post_permalink_type === 'DATE'">{{ options.blog_url }}/1970/01/${url}{{ options.path_suffix }}</span>
                    <span v-else-if="options.post_permalink_type === 'DAY'">{{ options.blog_url }}/1970/01/01/${url}{{ options.path_suffix }}</span>
                    <span v-else-if="options.post_permalink_type === 'ID'">{{ options.blog_url }}/?p=${id}</span>
                  </template>
                  <a-select v-model="options.post_permalink_type">
                    <a-select-option
                      v-for="item in Object.keys(permalinkType)"
                      :key="item"
                      :value="item"
                    >{{ permalinkType[item].text }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="自定义页面前缀：">
                  <template slot="help">
                    <span>{{ options.blog_url }}/{{ options.sheet_prefix }}/${url}{{ options.path_suffix }}</span>
                  </template>
                  <a-input v-model="options.sheet_prefix" />
                </a-form-item>
                <a-form-item label="归档前缀：">
                  <template slot="help">
                    <span>{{ options.blog_url }}/{{ options.archives_prefix }}{{ options.path_suffix }}</span>
                  </template>
                  <a-input v-model="options.archives_prefix" />
                </a-form-item>
                <a-form-item label="分类前缀：">
                  <template slot="help">
                    <span>{{ options.blog_url }}/{{ options.categories_prefix }}/${slugName}{{ options.path_suffix }}</span>
                  </template>
                  <a-input v-model="options.categories_prefix" />
                </a-form-item>
                <a-form-item label="标签前缀：">
                  <template slot="help">
                    <span>{{ options.blog_url }}/{{ options.tags_prefix }}/${slugName}{{ options.path_suffix }}</span>
                  </template>
                  <a-input v-model="options.tags_prefix" />
                </a-form-item>
                <a-form-item label="路径后缀：">
                  <template slot="help">
                    <span>仅对部分路径有效</span>
                  </template>
                  <a-input v-model="options.path_suffix" />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="handleSaveOptions"
                  >保存</a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="api">
              <span slot="tab">
                <a-icon type="thunderbolt" />API 设置
              </span>
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="API 服务：">
                  <a-switch v-model="options.api_enabled" />
                </a-form-item>
                <a-form-item label="Access key：">
                  <a-input-password v-model="options.api_access_key" />
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
              <a-form
                layout="vertical"
                :wrapperCol="wrapperCol"
              >
                <a-form-item label="自定义全局 head：">
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_custom_head"
                    placeholder="放置于每个页面的 <head></head> 标签中"
                  />
                </a-form-item>
                <a-form-item label="自定义内容页 head：">
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_custom_content_head"
                    placeholder="仅放置于内容页面的 <head></head> 标签中"
                  />
                </a-form-item>
                <a-form-item label="统计代码：">
                  <a-input
                    type="textarea"
                    :autosize="{ minRows: 5 }"
                    v-model="options.blog_statistics_code"
                    placeholder="第三方网站统计的代码，如：Google Analytics、百度统计、CNZZ 等"
                  />
                </a-form-item>
                <!-- <a-form-item
                  label="黑名单 IP："

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
import postApi from '@/api/post'

export default {
  components: {
    AttachmentSelectDrawer
  },
  data() {
    return {
      attachmentType: attachmentApi.type,
      permalinkType: postApi.permalinkType,
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      },
      logoDrawerVisible: false,
      faviconDrawerVisible: false,
      options: [],
      mailParam: {},
      tencentCosRegions: [
        {
          text: '北京一区',
          value: 'ap-beijing-1'
        },
        {
          text: '北京',
          value: 'ap-beijing'
        },
        {
          text: '上海（华东）',
          value: 'ap-shanghai'
        },
        {
          text: '广州（华南）',
          value: 'ap-guangzhou'
        },
        {
          text: '成都（西南）',
          value: 'ap-chengdu'
        },
        {
          text: '重庆',
          value: 'ap-chongqing'
        }
      ],
      qiniuOssZones: [
        {
          text: '自动选择',
          value: 'auto'
        },
        {
          text: '华东',
          value: 'z0'
        },
        {
          text: '华北',
          value: 'z1'
        },
        {
          text: '华南',
          value: 'z2'
        },
        {
          text: '北美',
          value: 'na0'
        },
        {
          text: '东南亚',
          value: 'as0'
        }
      ]
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
        case 'UPOSS':
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
        case 'QINIUOSS':
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
        case 'ALIOSS':
          if (!this.options.oss_ali_bucket_name) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          if (!this.options.oss_ali_endpoint) {
            this.$notification['error']({
              message: '提示',
              description: 'EndPoint（地域节点） 不能为空！'
            })
            return
          }
          if (!this.options.oss_ali_access_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Key 不能为空！'
            })
            return
          }
          if (!this.options.oss_ali_access_secret) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Secret 不能为空！'
            })
            return
          }
          break
        case 'BAIDUBOS':
          if (!this.options.bos_baidu_bucket_name) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          if (!this.options.bos_baidu_endpoint) {
            this.$notification['error']({
              message: '提示',
              description: 'EndPoint（地域节点） 不能为空！'
            })
            return
          }
          if (!this.options.bos_baidu_access_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Key 不能为空！'
            })
            return
          }
          if (!this.options.bos_baidu_secret_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Key 不能为空！'
            })
            return
          }
          break
        case 'TENCENTCOS':
          if (!this.options.cos_tencent_bucket_name) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          if (!this.options.cos_tencent_region) {
            this.$notification['error']({
              message: '提示',
              description: '区域不能为空！'
            })
            return
          }
          if (!this.options.cos_tencent_secret_id) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Id 不能为空！'
            })
            return
          }
          if (!this.options.cos_tencent_secret_key) {
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
    handleSelectLogo(data) {
      this.$set(this.options, 'blog_logo', encodeURI(data.path))
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
