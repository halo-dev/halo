<template>
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
              autocomplete="new-password"
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
              :autoSize="{ minRows: 5 }"
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
</template>
<script>
import mailApi from '@/api/mail'
export default {
  name: 'SmtpTab',
  props: {
    options: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      },
      mailParam: {}
    }
  },
  watch: {
    options(val) {
      this.$emit('onChange', val)
    }
  },
  methods: {
    handleSaveOptions() {
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
      this.$emit('onSave')
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
    }
  }
}
</script>
