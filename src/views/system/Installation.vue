<template>
  <div>
    <a-row>
      <a-col
        :xl="12"
        :offset="6"
      >
        <div class="card-container">
          <a-card :hoverable="true">
            <div class="logo">
              Halo<small style="font-size: 14px;">安装向导</small>
            </div>

            <a-form
              :form="form"
              :layout="horizontal"
            >
              <a-form-item label="博客名称">
                <a-input />
              </a-form-item>
              <a-form-item label="博客地址">
                <a-input />
              </a-form-item>
              <a-form-item label="用户邮箱">
                <a-input />
              </a-form-item>
              <a-form-item label="用户名">
                <a-input />
              </a-form-item>
              <a-form-item label="用户昵称">
                <a-input />
              </a-form-item>
              <a-form-item label="用户密码">
                <a-input />
              </a-form-item>
              <a-form-item label="确定密码">
                <a-input />
              </a-form-item>
              <a-form-item>
                <a-button type="primary">
                  安装
                </a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
export default {
  data() {
    return {
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      }
    }
  }
}
</script>

<style lang="less" scoped>
.card-container {
  padding-top: 50px;
}
.logo {
  font-size: 56px;
  text-align: center;
  margin-bottom: 25px;
  font-weight: 500;
  color: #444;
  text-shadow: #b2baba 0.1em 0.1em 0.2em;
}
</style>
