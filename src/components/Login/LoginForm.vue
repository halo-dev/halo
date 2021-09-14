<template>
  <div>
    <a-form-model
      ref="loginForm"
      :model="form.model"
      :rules="form.rules"
      layout="vertical"
      @keyup.enter.native="form.needAuthCode ? handleLogin() : handleLoginClick()"
    >
      <a-form-model-item
        v-if="!form.needAuthCode"
        :style="{ 'animation-delay': '0.1s' }"
        class="animated fadeInUp"
        prop="username"
      >
        <a-input v-model="form.model.username" placeholder="用户名/邮箱">
          <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="user" />
        </a-input>
      </a-form-model-item>
      <a-form-model-item
        v-if="!form.needAuthCode"
        :style="{ 'animation-delay': '0.2s' }"
        class="animated fadeInUp"
        prop="password"
      >
        <a-input v-model="form.model.password" placeholder="密码" type="password">
          <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="lock" />
        </a-input>
      </a-form-model-item>
      <a-form-model-item
        v-if="form.needAuthCode"
        :style="{ 'animation-delay': '0.1s' }"
        class="animated fadeInUp"
        prop="authcode"
      >
        <a-input v-model="form.model.authcode" :maxLength="6" placeholder="两步验证码">
          <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="safety-certificate" />
        </a-input>
      </a-form-model-item>
      <a-form-model-item :style="{ 'animation-delay': '0.3s' }" class="animated fadeInUp">
        <a-button
          :block="true"
          :loading="form.logging"
          type="primary"
          @click="form.needAuthCode ? handleLogin() : handleLoginClick()"
        >
          {{ buttonName }}
        </a-button>
      </a-form-model-item>
    </a-form-model>
  </div>
</template>
<script>
import adminApi from '@/api/admin'
import { mapActions } from 'vuex'

export default {
  name: 'LoginForm',
  data() {
    const authcodeValidate = (rule, value, callback) => {
      if (!value && this.form.needAuthCode) {
        callback(new Error('* 请输入两步验证码'))
      } else {
        callback()
      }
    }
    return {
      form: {
        model: {
          authcode: null,
          password: null,
          username: null
        },
        rules: {
          username: [{ required: true, message: '* 用户名/邮箱不能为空', trigger: ['change'] }],
          password: [{ required: true, message: '* 密码不能为空', trigger: ['change'] }],
          authcode: [{ validator: authcodeValidate, trigger: ['change'] }]
        },
        needAuthCode: false,
        logging: false
      }
    }
  },
  computed: {
    buttonName() {
      return this.form.needAuthCode ? '验证' : '登录'
    }
  },
  methods: {
    ...mapActions(['login', 'refreshUserCache', 'refreshOptionsCache']),
    handleLoginClick() {
      const _this = this
      _this.$refs.loginForm.validate(valid => {
        if (valid) {
          _this.form.logging = true
          adminApi
            .loginPreCheck(_this.form.model.username, _this.form.model.password)
            .then(response => {
              const data = response.data.data
              if (data && data.needMFACode) {
                _this.form.needAuthCode = true
                _this.form.model.authcode = null
              } else {
                _this.handleLogin()
              }
            })
            .finally(() => {
              setTimeout(() => {
                _this.form.logging = false
              }, 300)
            })
        }
      })
    },
    handleLogin() {
      const _this = this
      _this.$refs.loginForm.validate(valid => {
        if (valid) {
          _this.form.logging = true
          _this
            .login(_this.form.model)
            .then(() => {
              _this.$emit('success')
            })
            .finally(() => {
              setTimeout(() => {
                _this.form.logging = false
              }, 300)
            })
        }
      })
    }
  }
}
</script>
