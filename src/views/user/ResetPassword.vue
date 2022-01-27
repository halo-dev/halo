<template>
  <div class="container-wrapper animated fadeIn">
    <div class="halo-logo">
      <img alt="Halo Logo" src="/images/logo.svg" />
      <span>重置密码</span>
    </div>
    <div>
      <a-form-model ref="sendCodeForm" :model="form.model" :rules="form.sendCodeRules" layout="vertical">
        <a-form-model-item prop="username">
          <a-input v-model="form.model.username" placeholder="用户名">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="user" />
          </a-input>
        </a-form-model-item>
        <a-form-model-item prop="email">
          <a-input v-model="form.model.email" placeholder="邮箱">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="mail" />
          </a-input>
        </a-form-model-item>
      </a-form-model>
      <a-form-model ref="passwordForm" :model="form.model" :rules="form.rules" layout="vertical">
        <a-form-model-item prop="code">
          <a-input v-model="form.model.code" placeholder="验证码" type="password">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="safety-certificate" />
            <template #addonAfter>
              <a-button class="!p-0 !h-auto" type="link" @click="handleSendCode">获取</a-button>
            </template>
          </a-input>
        </a-form-model-item>
        <a-form-model-item prop="password">
          <a-input v-model="form.model.password" autocomplete="new-password" placeholder="新密码" type="password">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="lock" />
          </a-input>
        </a-form-model-item>
        <a-form-model-item prop="confirmPassword">
          <a-input
            v-model="form.model.confirmPassword"
            autocomplete="new-password"
            placeholder="确认密码"
            type="password"
          >
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="lock" />
          </a-input>
        </a-form-model-item>
        <a-form-model-item>
          <a-button :block="true" type="primary" @click="handleResetPassword">重置密码</a-button>
        </a-form-model-item>
      </a-form-model>
      <router-link :to="{ name: 'Login' }" class="tip"> 返回登录</router-link>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/api-client'

export default {
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value && this.form.model.password !== value) {
        callback(new Error('确认密码与新密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {
        model: {},
        sendCodeRules: {
          username: [{ required: true, message: '* 用户名不能为空', trigger: ['change'] }],
          email: [{ required: true, message: '* 电子邮箱地址不能为空', trigger: ['change'] }]
        },
        rules: {
          code: [{ required: true, message: '* 验证码不能为空', trigger: ['change'] }],
          password: [
            { required: true, message: '* 新密码不能为空', trigger: ['change'] },
            { max: 100, min: 8, message: '* 密码的字符长度必须在 8 - 100 之间', trigger: ['change'] }
          ],
          confirmPassword: [
            { required: true, message: '* 确认密码不能为空', trigger: ['change'] },
            { validator: validateConfirmPassword, trigger: ['change'] }
          ]
        }
      }
    }
  },
  methods: {
    handleSendCode() {
      this.$refs.sendCodeForm.validate(async valid => {
        if (valid) {
          const hideLoading = this.$message.loading('发送中...', 0)
          try {
            await apiClient.sendResetPasswordCode(this.form.model)
            this.$message.success('邮件发送成功，五分钟内有效')
          } catch (e) {
            this.$log.error('Failed send code: ', e)
          } finally {
            hideLoading()
          }
        }
      })
    },
    handleResetPassword() {
      this.$refs.sendCodeForm.validate(sendCodeValid => {
        if (!sendCodeValid) {
          return
        }
        this.$refs.passwordForm.validate(async valid => {
          if (valid) {
            try {
              await apiClient.resetPassword(this.form.model)
              await this.$router.push({ name: 'Login' })
              this.$message.success('密码重置成功！')
            } catch (e) {
              this.$log.error('Failed reset password: ', e)
            }
          }
        })
      })
    }
  }
}
</script>
