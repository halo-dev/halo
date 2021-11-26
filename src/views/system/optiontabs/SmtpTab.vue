<template>
  <div class="custom-tab-wrapper">
    <a-tabs :animated="{ inkBar: true, tabPane: false }">
      <a-tab-pane key="smtpoptions" tab="发信设置">
        <a-form-model ref="smtpOptionsForm" :model="options" :rules="rules" :wrapperCol="wrapperCol" layout="vertical">
          <a-form-model-item label="是否启用：">
            <a-switch v-model="options.email_enabled" />
          </a-form-model-item>
          <a-form-model-item label="SMTP 地址：" prop="email_host">
            <a-input v-model="options.email_host" />
          </a-form-model-item>
          <a-form-model-item label="发送协议：" prop="email_protocol">
            <a-input v-model="options.email_protocol" />
          </a-form-model-item>
          <a-form-model-item label="SSL 端口：" prop="email_ssl_port">
            <a-input v-model="options.email_ssl_port" />
          </a-form-model-item>
          <a-form-model-item label="邮箱账号：" prop="email_username">
            <a-input v-model="options.email_username" />
          </a-form-model-item>
          <a-form-model-item label="邮箱密码：" prop="email_password">
            <a-input-password
              v-model="options.email_password"
              autocomplete="new-password"
              placeholder="部分邮箱可能是授权码"
            />
          </a-form-model-item>
          <a-form-model-item label="发件人：" prop="email_from_name">
            <a-input v-model="options.email_from_name" />
          </a-form-model-item>
          <a-form-model-item>
            <ReactiveButton
              :errored="errored"
              :loading="saving"
              erroredText="保存失败"
              loadedText="保存成功"
              text="保存"
              type="primary"
              @callback="$emit('callback')"
              @click="handleSaveOptions"
            ></ReactiveButton>
          </a-form-model-item>
        </a-form-model>
      </a-tab-pane>
      <a-tab-pane key="smtptest" tab="发送测试">
        <a-form-model
          ref="smtpTestForm"
          :model="mailParam"
          :rules="testRules"
          :wrapperCol="wrapperCol"
          layout="vertical"
        >
          <a-form-model-item label="收件人地址：" prop="to">
            <a-input v-model="mailParam.to" />
          </a-form-model-item>
          <a-form-model-item label="主题：" prop="subject">
            <a-input v-model="mailParam.subject" />
          </a-form-model-item>
          <a-form-model-item label="内容：" prop="content">
            <a-input v-model="mailParam.content" :autoSize="{ minRows: 5 }" type="textarea" />
          </a-form-model-item>
          <a-form-model-item>
            <ReactiveButton
              :errored="sendErrored"
              :loading="sending"
              erroredText="发送失败"
              loadedText="发送成功"
              text="发送"
              type="primary"
              @callback="sendErrored = false"
              @click="handleTestMailClick"
            ></ReactiveButton>
          </a-form-model-item>
        </a-form-model>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'SmtpTab',
  props: {
    options: {
      type: Object,
      required: true
    },
    saving: {
      type: Boolean,
      default: false
    },
    errored: {
      type: Boolean,
      default: false
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
      mailParam: {},
      sending: false,
      sendErrored: false,
      testRules: {
        to: [{ required: true, message: '* 收件人地址不能为空', trigger: ['change'] }],
        subject: [{ required: true, message: '* 主题不能为空', trigger: ['change'] }],
        content: [{ required: true, message: '* 内容不能为空', trigger: ['change'] }]
      }
    }
  },
  watch: {
    options(val) {
      this.$emit('onChange', val)
    }
  },
  computed: {
    rules() {
      const required = this.options.email_enabled
      return {
        email_host: [{ required: required, message: '* SMTP 地址不能为空', trigger: ['change'] }],
        email_protocol: [{ required: required, message: '* 发送协议不能为空', trigger: ['change'] }],
        email_ssl_port: [{ required: required, message: '* SSL 端口不能为空', trigger: ['change'] }],
        email_username: [
          { required: required, message: '* 邮箱账号不能为空', trigger: ['change'] },
          { type: 'email', message: '* 邮箱账号格式不正确', trigger: ['change'] }
        ],
        email_password: [{ required: required, message: '* 邮箱密码不能为空', trigger: ['change'] }],
        email_from_name: [{ required: required, message: '* 发件人不能为空', trigger: ['change'] }]
      }
    }
  },
  methods: {
    handleSaveOptions() {
      const _this = this
      _this.$refs.smtpOptionsForm.validate(valid => {
        if (valid) {
          _this.$emit('onSave')
        }
      })
    },
    handleTestMailClick() {
      const _this = this
      _this.$refs.smtpTestForm.validate(valid => {
        if (valid) {
          this.sending = true
          apiClient.mail
            .testSmtpService(this.mailParam)
            .then(response => {
              this.$message.info(response.data)
            })
            .catch(() => {
              this.sendErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.sending = false
              }, 400)
            })
        }
      })
    }
  }
}
</script>
