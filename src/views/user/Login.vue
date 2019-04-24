<template>
  <div class="container loginForm">
    <div class="loginLogo animated fadeInUp">
      Halo
    </div>
    <div class="loginBody animated">
      <a-form layout="vertical">
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.1s'}"
        >
          <a-input placeholder="用户名/邮箱"></a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.2s'}"
        >
          <a-input
            type="password"
            placeholder="密码"
          ></a-input>
        </a-form-item>
        <a-form-item
          class="animated fadeInUp"
          :style="{'animation-delay': '0.3s'}"
        >
          <a-button
            type="primary"
            block="true"
          >登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script>
export default {
  beforeCreate() {
    this.form = this.$form.createForm(this)
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          console.log('Received values of form: ', values)
        }
      })
    }
  }
}
</script>
<style>
body {
  background-color: #f5f5f5;
}
* {
  outline: 0;
}
.container {
  padding-right: 15px;
  padding-left: 15px;
  margin-right: auto;
  margin-left: auto;
}
.loginForm {
  max-width: 380px;
  margin-top: 10%;
}
.loginLogo {
  font-size: 56px;
  text-align: center;
  margin-bottom: 25px;
  font-weight: 500;
  color: #444;
  text-shadow: #b2baba 0.1em 0.1em 0.2em;
}
.loginBody {
  padding: 20px;
  background-color: #fff;
  -o-box-shadow: -4px 7px 46px 2px rgba(0, 0, 0, 0.1);
  box-shadow: -4px 7px 46px 2px rgba(0, 0, 0, 0.1);
}
</style>
