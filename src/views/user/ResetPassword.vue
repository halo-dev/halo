<template>
  <div class="container-wrapper animated fadeIn">
    <div class="halo-logo">
      <img alt="Halo Logo" src="/images/logo.svg" />
      <span>重置密码</span>
    </div>
    <div>
      <a-form layout="vertical">
        <a-form-item>
          <a-input v-model="resetParam.username" placeholder="用户名">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="user" />
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-input v-model="resetParam.email" placeholder="邮箱">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="mail" />
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-input v-model="resetParam.code" placeholder="验证码" type="password">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="safety-certificate" />
            <a slot="addonAfter" href="javascript:void(0);" @click="handleSendCode"> 获取 </a>
          </a-input>
        </a-form-item>

        <a-form-item>
          <a-input v-model="resetParam.password" autocomplete="new-password" placeholder="新密码" type="password">
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="lock" />
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-input
            v-model="resetParam.confirmPassword"
            autocomplete="new-password"
            placeholder="确认密码"
            type="password"
          >
            <a-icon slot="prefix" style="color: rgba(0, 0, 0, 0.25)" type="lock" />
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-button :block="true" type="primary" @click="handleResetPassword">重置密码</a-button>
        </a-form-item>
      </a-form>
      <router-link :to="{ name: 'Login' }" class="tip">
        返回登录
      </router-link>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/api-client'

export default {
  data() {
    return {
      resetParam: {
        username: '',
        email: '',
        code: '',
        password: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    handleSendCode() {
      if (!this.resetParam.username) {
        this.$notification['error']({
          message: '提示',
          description: '用户名不能为空！'
        })
        return
      }
      if (!this.resetParam.email) {
        this.$notification['error']({
          message: '提示',
          description: '邮箱不能为空！'
        })
        return
      }
      const hide = this.$message.loading('发送中...', 0)
      apiClient
        .sendResetPasswordCode(this.resetParam)
        .then(() => {
          this.$message.success('邮件发送成功，五分钟内有效')
        })
        .finally(() => {
          hide()
        })
    },
    handleResetPassword() {
      if (!this.resetParam.username) {
        this.$notification['error']({
          message: '提示',
          description: '用户名不能为空！'
        })
        return
      }
      if (!this.resetParam.email) {
        this.$notification['error']({
          message: '提示',
          description: '邮箱不能为空！'
        })
        return
      }
      if (!this.resetParam.code) {
        this.$notification['error']({
          message: '提示',
          description: '验证码不能为空！'
        })
        return
      }
      if (!this.resetParam.password) {
        this.$notification['error']({
          message: '提示',
          description: '新密码不能为空！'
        })
        return
      }
      if (!this.resetParam.confirmPassword) {
        this.$notification['error']({
          message: '提示',
          description: '确认密码不能为空！'
        })
        return
      }
      if (this.resetParam.confirmPassword !== this.resetParam.password) {
        this.$notification['error']({
          message: '提示',
          description: '确认密码和新密码不匹配！'
        })
        return
      }
      apiClient.resetPassword(this.resetParam).then(() => {
        this.$message.success('密码重置成功！')
        this.$router.push({ name: 'Login' })
      })
    }
  }
}
</script>
