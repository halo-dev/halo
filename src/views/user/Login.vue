<template>
  <div class="container-wrapper">
    <div class="halo-logo animated fadeInUp">
      <span>Halo</span>
    </div>
    <div class="animated">
      <a-form
        layout="vertical"
        @keyup.enter.native="handleLogin"
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
            type="primary"
            :block="true"
            @click="handleLogin"
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
            @click="handleApiModifyModalOpen"
            class="tip animated fadeInUp"
            :style="{'animation-delay': '0.4s'}"
          >
            API 设置
          </a>
        </a-row>

        <a-modal
          title="API 设置"
          :visible="apiModifyVisible"
          @ok="handleApiModifyOk"
          @cancel="handleApiModifyCancel"
        >
          <a-form>
            <a-form-item extra="如果 halo admin 不是独立部署，请不要更改此 API">
              <a-input v-model="apiUrl"></a-input>
            </a-form-item>

            <a-form-item>
              <a-button @click="handleApiUrlRestore">
                恢复默认
              </a-button>
            </a-form-item>
          </a-form>
        </a-modal>
      </a-form>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters, mapMutations } from 'vuex'

export default {
  data() {
    return {
      username: null,
      password: null,
      apiModifyVisible: false,
      defaultApiBefore: window.location.protocol + '//',
      apiUrl: window.location.host,
      resetPasswordButton: false
    }
  },
  computed: {
    ...mapGetters({ defaultApiUrl: 'apiUrl' })
  },
  created() {
    const _this = this
    document.addEventListener('keydown', function(e) {
      if (e.keyCode === 72 && e.altKey && e.shiftKey) {
        _this.toggleHidden()
      }
    })
  },
  methods: {
    ...mapActions(['login', 'loadUser', 'loadOptions']),
    ...mapMutations({
      setApiUrl: 'SET_API_URL',
      restoreApiUrl: 'RESTORE_API_URL'
    }),
    handleLogin() {
      if (!this.username) {
        this.$message.warn('用户名不能为空！')
        return
      }

      if (!this.password) {
        this.$message.warn('密码不能为空！')
        return
      }

      this.login({ username: this.username, password: this.password }).then(response => {
        // Go to dashboard
        this.loginSuccess()
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
    handleApiModifyModalOpen() {
      this.apiUrl = this.defaultApiUrl
      this.apiModifyVisible = true
    },
    handleApiModifyOk() {
      this.setApiUrl(this.apiUrl)
      this.apiModifyVisible = false
    },
    handleApiModifyCancel() {
      this.apiModifyVisible = false
    },
    handleApiUrlRestore() {
      this.restoreApiUrl()
      this.apiUrl = this.defaultApiUrl
    },
    toggleHidden() {
      this.resetPasswordButton = !this.resetPasswordButton
    }
  }
}
</script>
<style lang="less">
body {
  height: 100%;
  background-color: #f5f5f5;
}

.container-wrapper {
  background: #ffffff;
  position: absolute;
  border-radius: 5px;
  top: 45%;
  left: 50%;
  margin: -160px 0 0 -160px;
  width: 320px;
  padding: 18px 28px 28px 28px;
  box-shadow: -4px 7px 46px 2px rgba(0, 0, 0, 0.1);

  .halo-logo {
    margin-bottom: 20px;
    text-align: center;
    span {
      vertical-align: text-bottom;
      font-size: 38px;
      display: inline-block;
      font-weight: 600;
      color: #1790fe;
      background-image: linear-gradient(-20deg, #6e45e2 0%, #88d3ce 100%);
      -webkit-text-fill-color: transparent;
      -webkit-background-clip: text;
      background-clip: text;
      small {
        margin-left: 5px;
        font-size: 35%;
      }
    }
  }
  .tip {
    cursor: pointer;
    margin-left: 0.5rem;
    float: right;
  }
}
</style>
