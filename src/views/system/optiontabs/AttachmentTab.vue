<template>
  <div>
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
            autocomplete="new-password"
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
          <a-input-password
            v-model="options.oss_upyun_password"
            autocomplete="new-password"
          />
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
          <a-input-password
            v-model="options.oss_qiniu_access_key"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="Secret Key：">
          <a-input-password
            v-model="options.oss_qiniu_secret_key"
            autocomplete="new-password"
          />
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
          <a-input-password
            v-model="options.oss_ali_access_key"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="Access Secret：">
          <a-input-password
            v-model="options.oss_ali_access_secret"
            autocomplete="new-password"
          />
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
          <a-input-password
            v-model="options.bos_baidu_access_key"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="Secret Key：">
          <a-input-password
            v-model="options.bos_baidu_secret_key"
            autocomplete="new-password"
          />
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
          <a-input-password
            v-model="options.cos_tencent_secret_id"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="Secret Key：">
          <a-input-password
            v-model="options.cos_tencent_secret_key"
            autocomplete="new-password"
          />
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
      <div
        id="huaweiObsForm"
        v-show="options.attachment_type === 'HUAWEIOBS'"
      >
        <a-form-item label="绑定域名协议：">
          <a-select v-model="options.obs_huawei_domain_protocol">
            <a-select-option value="https://">HTTPS</a-select-option>
            <a-select-option value="http://">HTTP</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="绑定域名：">
          <a-input
            v-model="options.obs_huawei_domain"
            placeholder="如不填写，路径根域名将为 Bucket + EndPoint"
          />
        </a-form-item>
        <a-form-item label="Bucket（桶名称）：">
          <a-input
            v-model="options.obs_huawei_bucket_name"
            placeholder="桶名称"
          />
        </a-form-item>
        <a-form-item label="EndPoint（终端节点）：">
          <a-input
            v-model="options.obs_huawei_endpoint"
            placeholder="Endpoint"
          />
        </a-form-item>
        <a-form-item label="Access Key：">
          <a-input-password
            v-model="options.obs_huawei_access_key"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="Access Secret：">
          <a-input-password
            v-model="options.obs_huawei_access_secret"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="文件目录：">
          <a-input
            v-model="options.obs_huawei_source"
            placeholder="不填写则上传到根目录"
          />
        </a-form-item>
        <a-form-item label="图片处理策略：">
          <a-input
            v-model="options.obs_huawei_style_rule"
            placeholder="请到华为云控制台的图片处理创建"
          />
        </a-form-item>
        <a-form-item label="缩略图处理策略：">
          <a-input
            v-model="options.obs_huawei_thumbnail_style_rule"
            placeholder="请到华为云控制台的图片处理获取，一般为后台展示所用"
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
  </div>
</template>
<script>
import attachmentApi from '@/api/attachment'
const tencentCosRegions = [
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
]
const qiniuOssZones = [
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
export default {
  name: 'AttachmentTab',
  props: {
    options: {
      type: Object,
      required: true
    }
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
      tencentCosRegions,
      qiniuOssZones
    }
  },
  watch: {
    options(val) {
      this.$emit('onChange', val)
    }
  },
  methods: {
    handleSaveOptions() {
      // 附件配置验证
      switch (this.options.attachment_type) {
        case 'SMMS':
          if (!this.options.smms_api_secret_token) {
            this.$notification['error']({
              message: '提示',
              description: 'Secret Token 不能为空！'
            })
            return
          }
          break
        case 'UPOSS':
          if (!this.options.oss_upyun_domain) {
            this.$notification['error']({
              message: '提示',
              description: '绑定域名不能为空！'
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
              description: '绑定域名不能为空！'
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
        case 'HUAWEIOBS':
          if (!this.options.obs_huawei_bucket_name) {
            this.$notification['error']({
              message: '提示',
              description: 'Bucket 不能为空！'
            })
            return
          }
          if (!this.options.obs_huawei_endpoint) {
            this.$notification['error']({
              message: '提示',
              description: 'EndPoint（终端节点） 不能为空！'
            })
            return
          }
          if (!this.options.obs_huawei_access_key) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Key 不能为空！'
            })
            return
          }
          if (!this.options.obs_huawei_access_secret) {
            this.$notification['error']({
              message: '提示',
              description: 'Access Secret 不能为空！'
            })
            return
          }
          break
      }
      this.$emit('onSave')
    }
  }
}
</script>
