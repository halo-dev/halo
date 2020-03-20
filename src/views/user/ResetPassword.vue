<template>
  <div class="container-wrapper">
    <div class="halo-logo animated fadeInUp">
      <span>Halo<small>重置密码</small></span>
    </div>
    <div class="animated">
      <a-form layout="vertical">
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.1s'}"
        >
          <a-input
            placeholder="用户名"
            v-model="resetParam.username"
          >
            <a-icon
              slot="prefix"
              type="user"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.2s'}"
        >
          <a-input
            placeholder="邮箱"
            v-model="resetParam.email"
          >
            <a-icon
              slot="prefix"
              type="mail"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>

        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.3s'}"
        >
          <a-input
            v-model="resetParam.code"
            type="password"
            placeholder="验证码"
          >
            <a-icon
              slot="prefix"
              type="safety-certificate"
              style="color: rgba(0,0,0,.25)"
            />
            <a
              href="javascript:void(0);"
              slot="addonAfter"
              @click="handleSendCode"
            >
              获取
            </a>
          </a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.4s'}"
        >
          <a-input
            v-model="resetParam.password"
            type="password"
            placeholder="新密码"
            autocomplete="new-password"
          >
            <a-icon
              slot="prefix"
              type="lock"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.5s'}"
        >
          <a-input
            v-model="resetParam.confirmPassword"
            type="password"
            placeholder="确认密码"
            autocomplete="new-password"
          >
            <a-icon
              slot="prefix"
              type="lock"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.6s'}"
        >
          <a-button
            type="primary"
            :block="true"
            @click="handleResetPassword"
          >重置密码</a-button>
        </a-form-item>

        <a-row>
          <router-link :to="{ name:'Login' }">
            <a
              class="tip animated fadeInUp"
              :style="{'animation-delay': '0.7s'}"
            >
              返回登录
            </a>
          </router-link>
        </a-row>
      </a-form>
    </div>
  </div>
</template>

<script>
import adminApi from '@/api/admin'

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
      adminApi
        .sendResetCode(this.resetParam)
        .then(response => {
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
      adminApi.resetPassword(this.resetParam).then(response => {
        this.$message.success('密码重置成功！')
        this.$router.push({ name: 'Login' })
      })
    }
  }
}
</script>
