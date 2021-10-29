<template>
  <div class="container-wrapper animated fadeIn">
    <div class="halo-logo">
      <img src="/images/logo.svg" alt="Halo Logo" />
      <span v-if="apiForm.visible">API 设置</span>
    </div>
    <div v-show="!apiForm.visible" class="login-form">
      <LoginForm @success="onLoginSucceed" />
      <router-link v-if="resetPasswordButtonVisible" class="tip" :to="{ name: 'ResetPassword' }">
        找回密码
      </router-link>
      <a class="tip" @click="handleToggleShowApiForm">
        <a-icon type="setting" />
      </a>
    </div>
    <div v-show="apiForm.visible" class="api-form">
      <a-form layout="vertical">
        <a-form-item>
          <a-tooltip placement="top" title="如果 Admin 不是独立部署，请不要更改此 API" trigger="click">
            <a-input v-model="apiForm.apiUrl" placeholder="API 地址">
              <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="api" />
            </a-input>
          </a-tooltip>
        </a-form-item>
        <a-form-item>
          <a-button :block="true" @click="handleRestoreApiUrl">恢复默认</a-button>
        </a-form-item>
        <a-form-item>
          <a-button :block="true" type="primary" @click="handleModifyApiUrl">保存设置</a-button>
        </a-form-item>
        <a-row>
          <a class="tip" @click="handleToggleShowApiForm">
            <a-icon type="rollback" />
          </a>
        </a-row>
      </a-form>
    </div>
  </div>
</template>

<script>
import adminApi from '@/api/admin'
import { mapActions, mapGetters, mapMutations } from 'vuex'

import LoginForm from '@/components/Login/LoginForm'

export default {
  components: {
    LoginForm
  },
  data() {
    return {
      resetPasswordButtonVisible: false,
      apiForm: {
        apiUrl: window.location.host,
        visible: false
      }
    }
  },
  computed: {
    ...mapGetters({ defaultApiUrl: 'apiUrl' })
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
    ...mapMutations({
      setApiUrl: 'SET_API_URL',
      restoreApiUrl: 'RESTORE_API_URL'
    }),
    onRegisterResetPasswordKeydown(e) {
      // Windows / Linux: Shift + Alt + h
      // maxOS: Shift + Command + h
      if (e.keyCode === 72 && (e.altKey || e.metaKey) && e.shiftKey) {
        e.preventDefault()
        this.resetPasswordButtonVisible = !this.resetPasswordButtonVisible
      }
    },
    async handleVerifyIsInstall() {
      const response = await adminApi.isInstalled()
      if (!response.data.data) {
        await this.$router.push({ name: 'Install' })
      }
    },
    onLoginSucceed() {
      // Refresh the user info
      this.refreshUserCache()
      this.refreshOptionsCache()
      if (this.$route.query.redirect) {
        this.$router.replace(this.$route.query.redirect)
      } else {
        this.$router.replace({ name: 'Dashboard' })
      }
    },
    handleModifyApiUrl() {
      this.setApiUrl(this.apiForm.apiUrl)
      this.apiForm.visible = false
    },
    handleRestoreApiUrl() {
      this.restoreApiUrl()
      this.apiForm.apiUrl = this.defaultApiUrl
    },
    handleToggleShowApiForm() {
      this.apiForm.visible = !this.apiForm.visible
      if (this.apiForm.visible) {
        this.apiForm.apiUrl = this.defaultApiUrl
      }
    }
  }
}
</script>
