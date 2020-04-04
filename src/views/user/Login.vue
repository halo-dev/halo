<template>
  <div class="container-wrapper">
    <div class="halo-logo animated fadeInUp">
      <span>Halo
        <small v-if="apiModifyVisible">API 设置</small>
        <small v-if="authcodeVisible">两步验证</small>
      </span>
    </div>
    <div
      v-show="formVisible == 'login-form'"
      class="login-form animated"
    >
      <a-form
        layout="vertical"
        @keyup.enter.native="handleLoginPreCheck"
      >
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.1s'}"
        >
          <a-input
            placeholder="用户名/邮箱"
            v-model="username"
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
            v-model="password"
            type="password"
            placeholder="密码"
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
          :style="{'animation-delay': '0.3s'}"
        >
          <a-button
            :loading="landing"
            type="primary"
            :block="true"
            @click="handleLoginPreCheck"
          >登录</a-button>
        </a-form-item>

        <a-row>
          <router-link :to="{ name:'ResetPassword' }">
            <a
              class="tip animated fadeInRight"
              v-if="resetPasswordButton"
              href="javascript:void(0);"
            >
              找回密码
            </a>
          </router-link>
          <a
            @click="toggleShowApiForm"
            class="tip animated fadeInUp"
            :style="{'animation-delay': '0.4s'}"
          >
            <a-icon type="setting" />
          </a>
        </a-row>
      </a-form>
    </div>
    <div
      v-show="apiModifyVisible"
      class="api-form animated"
    >
      <a-form layout="vertical">
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.1s'}"
          extra="* 如果 Admin 不是独立部署，请不要更改此 API"
        >
          <a-input
            placeholder="API 地址"
            v-model="apiUrl"
          >
            <a-icon
              slot="prefix"
              type="api"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.2s'}"
        >
          <a-button
            :block="true"
            @click="handleApiUrlRestore"
          >恢复默认</a-button>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.3s'}"
        >
          <a-button
            type="primary"
            :block="true"
            @click="handleApiModifyOk"
          >保存设置</a-button>
        </a-form-item>

        <a-row>
          <a
            @click="toggleShowApiForm"
            class="tip animated fadeInUp"
            :style="{'animation-delay': '0.4s'}"
          >
            <a-icon type="rollback" />
          </a>
        </a-row>
      </a-form>
    </div>

    <div
      v-show="authcodeVisible"
      class="authcode-form animated"
    >
      <a-form layout="vertical" @keyup.enter.native="handleLogin">
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.1s'}"
        >
          <a-input
            placeholder="两步验证码"
            v-model="authcode"
            :maxLength="6"
          >
            <a-icon
              slot="prefix"
              type="safety-certificate"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.3s'}"
        >
          <a-button
            :loading="landing"
            type="primary"
            :block="true"
            @click="handleLogin"
          >验证</a-button>
        </a-form-item>

        <a-row>
          <a
            @click="toggleShowLoginForm"
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

export default {
  data() {
    return {
      username: null,
      password: null,
      authcode: null,
      needAuthCode: false,
      formVisible: 'login-form', // login-form api-form authcode-form
      loginVisible: true,
      apiModifyVisible: false,
      authcodeVisible: false,
      defaultApiBefore: window.location.protocol + '//',
      apiUrl: window.location.host,
      resetPasswordButton: false,
      landing: false
    }
  },
  computed: {
    ...mapGetters({ defaultApiUrl: 'apiUrl' })
  },
  created() {
    this.verifyIsInstall()
    const _this = this
    document.addEventListener('keydown', function(e) {
      if (e.keyCode === 72 && e.altKey && e.shiftKey) {
        _this.toggleHidden()
      }
    })
  },
  watch: {
    formVisible(value) {
      this.loginVisible = (value === 'authcode-form')
      this.apiModifyVisible = (value === 'api-form')
      this.authcodeVisible = (value === 'authcode-form')
    }
  },
  methods: {
    ...mapActions(['login', 'loadUser', 'loadOptions']),
    ...mapMutations({
      setApiUrl: 'SET_API_URL',
      restoreApiUrl: 'RESTORE_API_URL'
    }),
    verifyIsInstall() {
      adminApi.isInstalled().then(response => {
        if (!response.data.data) {
          this.$router.push({ name: 'Install' })
        }
      })
    },
    handleLoginPreCheck() {
      if (!this.username) {
        this.$message.warn('用户名不能为空！')
        return
      }

      if (!this.password) {
        this.$message.warn('密码不能为空！')
        return
      }

      adminApi.loginPreCheck(this.username, this.password).then(response => {
        if (response.data.data && response.data.data.needMFACode) {
          this.formVisible = 'authcode-form'
          this.authcode = null
          this.needAuthCode = true
        } else {
          this.needAuthCode = false
          this.handleLogin()
        }
      })
    },
    handleLogin() {
      if (!this.username) {
        this.$message.warn('用户名不能为空！')
        return
      }

      if (!this.password) {
        this.$message.warn('密码不能为空！')
        return
      }

      if (this.needAuthCode && !this.authcode) {
        this.$message.warn('两步验证码不能为空！')
        return
      }

      this.landing = true
      this.login({ username: this.username, password: this.password, authcode: this.authcode })
        .then(response => {
          // Go to dashboard
          this.loginSuccess()
        })
        .finally(() => {
          setTimeout(() => {
            this.landing = false
          }, 500)
        })
    },
    loginSuccess() {
      // Cache the user info
      this.loadUser()
      this.loadOptions()
      if (this.$route.query.redirect) {
        this.$router.replace(this.$route.query.redirect)
      } else {
        this.$router.replace({ name: 'Dashboard' })
      }
    },
    handleApiModifyOk() {
      this.setApiUrl(this.apiUrl)
      this.formVisible = 'login-form'
    },
    handleApiUrlRestore() {
      this.restoreApiUrl()
      this.apiUrl = this.defaultApiUrl
    },
    toggleShowApiForm() {
      this.formVisible = this.apiModifyVisible ? 'login-form' : 'api-form'
      this.apiModifyVisible = !this.apiModifyVisible
      if (this.apiModifyVisible) {
        this.apiUrl = this.defaultApiUrl
      }
    },
    toggleShowLoginForm() {
      this.formVisible = 'login-form'
      this.password = null
    },
    toggleHidden() {
      this.resetPasswordButton = !this.resetPasswordButton
    }
  }
}
</script>
