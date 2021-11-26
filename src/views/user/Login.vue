<template>
  <div class="container-wrapper animated fadeIn">
    <div class="halo-logo">
      <img alt="Halo Logo" src="/images/logo.svg" />
    </div>
    <div class="login-form">
      <LoginForm @success="onLoginSucceed" />
      <router-link v-if="resetPasswordButtonVisible" :to="{ name: 'ResetPassword' }" class="tip">
        找回密码
      </router-link>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

import LoginForm from '@/components/Login/LoginForm'
import apiClient from '@/utils/api-client'

export default {
  components: {
    LoginForm
  },
  data() {
    return {
      resetPasswordButtonVisible: false
    }
  },
  beforeMount() {
    this.handleVerifyIsInstall()
    document.addEventListener('keydown', this.onRegisterResetPasswordKeydown)
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.onRegisterResetPasswordKeydown)
  },
  methods: {
    ...mapActions(['refreshUserCache', 'refreshOptionsCache']),
    onRegisterResetPasswordKeydown(e) {
      // Windows / Linux: Shift + Alt + h
      // maxOS: Shift + Command + h
      if (e.keyCode === 72 && (e.altKey || e.metaKey) && e.shiftKey) {
        e.preventDefault()
        this.resetPasswordButtonVisible = !this.resetPasswordButtonVisible
      }
    },
    async handleVerifyIsInstall() {
      const response = await apiClient.isInstalled()
      if (!response.data) {
        await this.$router.push({ name: 'Install' })
      }
    },
    onLoginSucceed() {
      // Refresh the user info
      this.refreshUserCache()
      this.refreshOptionsCache()
      if (this.$route.query.redirect) {
        this.$router.push(this.$route.query.redirect)
      } else {
        this.$router.push({ name: 'Dashboard' })
      }
    }
  }
}
</script>
