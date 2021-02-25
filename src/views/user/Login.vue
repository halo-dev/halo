<template>
  <div class="container-wrapper">
    <div class="halo-logo animated fadeInUp">
      <span>Halo
        <small v-if="apiForm.visible">API 设置</small>
      </span>
    </div>
    <div
      v-show="!apiForm.visible"
      class="login-form animated"
    >
      <LoginForm @success="onLoginSucceed" />
      <a-row>
        <a-col :span="24">
          <router-link :to="{ name:'ResetPassword' }">
            <a
              class="tip animated fadeInRight"
              v-if="resetPasswordButtonVisible"
              href="javascript:void(0);"
            >
              找回密码
            </a>
          </router-link>
          <a
            @click="handleToggleShowApiForm"
            class="tip animated fadeInUp"
            :style="{'animation-delay': '0.4s'}"
          >
            <a-icon type="setting" />
          </a>
        </a-col>
      </a-row>
    </div>
    <div
      v-show="apiForm.visible"
      class="api-form animated"
    >
      <a-form layout="vertical">
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.1s'}"
        >
          <a-tooltip
            placement="top"
            title="如果 Admin 不是独立部署，请不要更改此 API"
            trigger="click"
          >
            <a-input
              placeholder="API 地址"
              v-model="apiForm.apiUrl"
            >
              <a-icon
                slot="prefix"
                type="api"
                style="color: rgba(0,0,0,.25)"
              />
            </a-input>
          </a-tooltip>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.2s'}"
        >
          <a-button
            :block="true"
            @click="handleRestoreApiUrl"
          >恢复默认</a-button>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.3s'}"
        >
          <a-button
            type="primary"
            :block="true"
            @click="handleModifyApiUrl"
          >保存设置</a-button>
        </a-form-item>
        <a-row>
          <a
            @click="handleToggleShowApiForm"
            class="tip animated fadeInUp"
            :style="{'animation-delay': '0.4s'}"
          >
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
    LoginForm,
  },
  data() {
    return {
      resetPasswordButtonVisible: false,
      apiForm: {
        apiUrl: window.location.host,
        visible: false,
      },
    }
  },
  computed: {
    ...mapGetters({ defaultApiUrl: 'apiUrl' }),
  },
  beforeMount() {
    const _this = this
    _this.handleVerifyIsInstall()
    document.addEventListener('keydown', function(e) {
      if (e.keyCode === 72 && e.altKey && e.shiftKey) {
        _this.resetPasswordButtonVisible = !_this.resetPasswordButtonVisible
      }
    })
  },
  methods: {
    ...mapActions(['refreshUserCache', 'refreshOptionsCache']),
    ...mapMutations({
      setApiUrl: 'SET_API_URL',
      restoreApiUrl: 'RESTORE_API_URL',
    }),
    async handleVerifyIsInstall() {
      const response = await adminApi.isInstalled()
      if (!response.data.data) {
        this.$router.push({ name: 'Install' })
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
    },
  },
}
</script>
