<template>
  <div>
    <a-row
      class="height-100"
      type="flex"
      justify="center"
      align="middle"
    >
      <a-col
        :xl="8"
        :md="12"
        :sm="20"
        :xs="24"
      >
        <div class="card-container">
          <a-card
            :hoverable="true"
            title="Halo 安装向导"
          >
            <!-- <div class="logo">
              Halo<small style="font-size: 14px;">安装向导</small>
            </div> -->

            <a-steps :current="stepCurrent">
              <a-step title="博主信息">
                <a-icon
                  type="user"
                  slot="icon"
                />
              </a-step>
              <a-step title="博客信息">
                <a-icon
                  type="rocket"
                  slot="icon"
                />
              </a-step>
              <a-step title="数据迁移">
                <a-icon
                  type="fork"
                  slot="icon"
                />
              </a-step>
            </a-steps>
            <a-divider dashed />
            <!-- Blogger info -->
            <a-form
              layout="horizontal"
              v-show="stepCurrent == 0"
            >
              <a-form-item
                label="用户名"
                v-bind="formItemLayout"
              >
                <a-input v-model="installation.username" />
              </a-form-item>
              <a-form-item
                label="用户昵称"
                v-bind="formItemLayout"
              >
                <a-input v-model="installation.nickname" />
              </a-form-item>
              <a-form-item
                label="用户邮箱"
                v-bind="formItemLayout"
              >
                <a-input v-model="installation.email" />
              </a-form-item>
              <a-form-item
                label="用户密码"
                v-bind="formItemLayout"
              >
                <a-input v-model="installation.password" />
              </a-form-item>
              <a-form-item
                label="确定密码"
                v-bind="formItemLayout"
              >
                <a-input v-model="installation.confirmPassword" />
              </a-form-item>
            </a-form>

            <!-- Blog info -->

            <a-form
              layout="horizontal"
              v-show="stepCurrent == 1"
            >
              <a-form-item
                label="博客标题"
                v-bind="formItemLayout"
              >
                <a-input v-model="installation.title" />
              </a-form-item>
              <a-form-item
                label="博客地址"
                v-bind="formItemLayout"
              >
                <a-input v-model="installation.url" />
              </a-form-item>
            </a-form>

            <!-- Data migration -->
            <a-row
              type="flex"
              justify="space-between"
            >
              <div>
                <a-button
                  class="previus-button"
                  v-if="stepCurrent != 0"
                  @click="stepCurrent--"
                >上一步</a-button>
                <a-button
                  type="primary"
                  v-if="stepCurrent != 2"
                  @click="stepCurrent++"
                >下一步</a-button>
              </div>
              <a-button
                v-if="stepCurrent == 2"
                type="danger"
                icon="upload"
              >安装</a-button>
            </a-row>
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
      formItemLayout: {
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
          lg: { span: 4 },
          xl: { span: 4 },
          xxl: { span: 3 }
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 19 },
          lg: { span: 20 },
          xl: { span: 20 },
          xxl: { span: 21 }
        }
      },
      installation: {},
      stepCurrent: 0
    }
  }
}
</script>

<style lang="less" scoped>
// .card-container {
//   padding-top: 50px;
// }
.logo {
  font-size: 56px;
  text-align: center;
  margin-bottom: 25px;
  font-weight: 500;
  color: #444;
  text-shadow: #b2baba 0.1em 0.1em 0.2em;
}

.height-100 {
  height: 100vh;
}

.previus-button {
  margin-right: 1rem;
}
</style>
