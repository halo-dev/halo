<template>
  <div class="container">
    <div class="loginLogo animated fadeInUp">
      <span>Halo</span>
    </div>
    <div class="loginBody animated">
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
        <a-row>
          <a-button
            type="primary"
            :block="true"
            @click="handleLogin"
            class="animated fadeInUp"
            :style="{'animation-delay': '0.3s'}"
          >登录</a-button>
        </a-row>
      </a-form>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  data() {
    return {
      username: null,
      password: null
    }
  },
  methods: {
    ...mapActions(['login']),
    handleLogin() {
      if (!this.username) {
        this.$message.warn('用户名不能为空')
        return
      }

      if (!this.password) {
        this.$message.warn('密码不能为空')
        return
      }

      this.login({ username: this.username, password: this.password }).then(response => {
        // Go to dashboard
        this.loginSuccess()
      })
    },
    loginSuccess() {
      if (this.$route.query.redirect) {
        this.$router.replace(this.$route.query.redirect)
      } else {
        this.$router.replace({ name: 'Dashboard' })
      }
      // 延迟 1 秒显示欢迎信息
      setTimeout(() => {
        this.$notification.success({
          message: '欢迎',
          description: `欢迎回来`
        })
      }, 1000)
    }
  }
}
</script>
<style>
body {
  height: 100%;
  background-color: #f8f8f8;
}
.container {
  position: absolute;
  top: 45%;
  left: 50%;
  margin: -160px 0 0 -160px;
  width: 320px;
  padding: 16px 32px 32px 32px;
  box-shadow: 0px 0px 20px 0px rgba(76, 50, 50, 0.08);
}
.loginLogo {
  margin-bottom: 20px;
  text-align: center;
}
.loginLogo span {
  vertical-align: text-bottom;
  font-size: 36px;
  display: inline-block;
  font-weight: 600;
  color: #1790fe;
  background-image: -webkit-gradient(
    linear,
    37.219838% 34.532506%,
    36.425669% 93.178216%,
    from(#36c8f5),
    to(#1790fe),
    color-stop(0.37, #1790fe)
  );
  -webkit-text-fill-color: transparent;
  -webkit-background-clip: text;
}
</style>
