<template>
  <div>
    <a-row
      type="flex"
      justify="center"
      align="middle"
      style="height: 100vh;"
    >
      <a-col
        :xl="8"
        :md="12"
        :sm="20"
        :xs="24"
      >
        <div class="card-container">
          <a-card
            :bordered="false"
            title="Halo 安装向导"
            style="box-shadow: 0px 10px 20px 0px rgba(236, 236, 236, 0.86);"
          >

            <a-steps :current="stepCurrent">
              <a-step title="博主信息">
              </a-step>
              <a-step title="博客信息">
              </a-step>
              <a-step title="数据导入">
              </a-step>
            </a-steps>
            <a-divider dashed />
            <!-- Blogger info -->
            <a-form-model
              ref="generalForm"
              :model="installation"
              :rules="generalRules"
              layout="horizontal"
              v-show="stepCurrent == 0"
            >
              <a-form-model-item
                class="animated fadeInUp"
                prop="username"
              >
                <a-input
                  v-model="installation.username"
                  placeholder="用户名"
                >
                  <a-icon
                    slot="prefix"
                    type="user"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-model-item>
              <a-form-model-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.1s'}"
                prop="nickname"
              >
                <a-input
                  v-model="installation.nickname"
                  placeholder="用户昵称"
                >
                  <a-icon
                    slot="prefix"
                    type="smile"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-model-item>
              <a-form-model-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.2s'}"
                prop="email"
              >
                <a-input
                  v-model="installation.email"
                  placeholder="用户邮箱"
                >
                  <a-icon
                    slot="prefix"
                    type="mail"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-model-item>
              <a-form-model-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.3s'}"
                prop="password"
              >
                <a-input
                  v-model="installation.password"
                  type="password"
                  placeholder="用户密码（8-100位）"
                >
                  <a-icon
                    slot="prefix"
                    type="lock"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-model-item>
              <a-form-model-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.4s'}"
                prop="confirmPassword"
              >
                <a-input
                  v-model="installation.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                >
                  <a-icon
                    slot="prefix"
                    type="lock"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-model-item>
            </a-form-model>

            <!-- Blog info -->

            <a-form-model
              layout="horizontal"
              v-show="stepCurrent == 1"
              ref="blogForm"
              :model="installation"
              :rules="blogRules"
            >
              <a-form-model-item
                class="animated fadeInUp"
                prop="url"
              >
                <a-input
                  v-model="installation.url"
                  placeholder="博客地址"
                >
                  <a-icon
                    slot="prefix"
                    type="link"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-model-item>
              <a-form-model-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.2s'}"
                prop="title"
              >
                <a-input
                  v-model="installation.title"
                  placeholder="博客标题"
                >
                  <a-icon
                    slot="prefix"
                    type="book"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-model-item>
            </a-form-model>

            <!-- Data migration -->
            <div v-show="stepCurrent == 2">
              <a-alert
                style="margin-bottom: 1rem"
                message="如果有数据导入需求，请点击并选择之前导出的文件。需要注意的是，并不是所有数据都会导入，该初始化表单的数据会覆盖你导入的数据。"
                type="info"
              />
              <FilePondUpload
                ref="upload"
                name="file"
                accept="application/json"
                label="拖拽或点击选择数据文件，请确认是否为 Halo 后台导出的文件。"
                :multiple="false"
                :uploadHandler="handleMigrationUpload"
                :loadOptions="false"
              ></FilePondUpload>
            </div>

            <a-row
              class="install-action"
              type="flex"
              justify="space-between"
              style="margin-top: 1rem;"
            >
              <div>
                <a-button
                  class="previus-button"
                  v-if="stepCurrent != 0"
                  @click="stepCurrent--"
                  style="margin-right: 1rem;"
                >上一步</a-button>
                <a-button
                  type="primary"
                  v-if="stepCurrent != 2"
                  @click="handleNextStep"
                >下一步</a-button>
              </div>
              <a-button
                v-if="stepCurrent == 2"
                type="primary"
                icon="upload"
                @click="handleInstall"
                :loading="installing"
              >安装</a-button>
            </a-row>
          </a-card>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import adminApi from '@/api/admin'
import migrateApi from '@/api/migrate'

export default {
  data() {
    const confirmPasswordValidate = (rule, value, callback) => {
      if (value !== this.installation.password) {
        callback(new Error('确认密码与所输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      installation: {},
      stepCurrent: 0,
      migrationData: null,
      installing: false,

      generalRules: {
        username: [
          { required: true, message: '* 用户名不能为空', trigger: ['change', 'blur'] },
          { max: 50, message: '* 用户名的字符长度不能超过 50', trigger: ['change', 'blur'] }
        ],
        nickname: [
          { required: true, message: '* 用户昵称不能为空', trigger: ['change', 'blur'] },
          { max: 255, message: '* 用户昵称的字符长度不能超过 255', trigger: ['change', 'blur'] }
        ],
        email: [
          { required: true, message: '* 电子邮件地址不能为空', trigger: ['change', 'blur'] },
          { max: 127, message: '* 电子邮件地址的字符长度不能超过 127', trigger: ['change', 'blur'] },
          {
            pattern: /\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/g,
            message: '* 电子邮件地址的格式不正确',
            trigger: ['change', 'blur']
          }
        ],
        password: [
          { required: true, message: '* 密码不能为空', trigger: ['change', 'blur'] },
          { min: 8, max: 100, message: '* 密码的字符长度必须在 8 - 100 之间', trigger: ['change', 'blur'] }
        ],
        confirmPassword: [
          { required: true, message: '* 确认密码不能为空', trigger: ['change', 'blur'] },
          { validator: confirmPasswordValidate, trigger: ['change', 'blur'] }
        ]
      },
      blogRules: {
        url: [{ required: true, message: '* 博客地址不能为空', trigger: ['change', 'blur'] }],
        title: [{ required: true, message: '* 博客标题不能为空', trigger: ['change', 'blur'] }]
      }
    }
  },
  created() {
    this.verifyIsInstall()
    this.$set(this.installation, 'url', window.location.protocol + '//' + window.location.host)
  },
  methods: {
    async verifyIsInstall() {
      await adminApi.isInstalled().then(response => {
        if (response.data.data) {
          this.$router.push({ name: 'Login' })
        }
      })
    },
    handleNextStep() {
      if (this.stepCurrent === 0) {
        this.$refs.generalForm.validate(valid => {
          if (valid) {
            this.stepCurrent++
          } else {
            return false
          }
        })
      } else if (this.stepCurrent === 1) {
        this.$refs.blogForm.validate(valid => {
          if (valid) {
            this.stepCurrent++
          } else {
            return false
          }
        })
      }
    },
    handleMigrationUpload(data) {
      this.$log.debug('Selected data', data)
      this.migrationData = data
      return new Promise((resolve, reject) => {
        this.$log.debug('Handle uploading')
        resolve()
      })
    },
    install() {
      adminApi
        .install(this.installation)
        .then(response => {
          this.$log.debug('Installation response', response)
          this.$message.success('安装成功！')
          setTimeout(() => {
            this.$router.push({ name: 'Login' })
          }, 300)
        })
        .finally(() => {
          this.installing = false
        })
    },
    handleInstall() {
      this.installing = true
      if (this.migrationData) {
        const hide = this.$message.loading('数据导入中...', 0)
        migrateApi
          .migrate(this.migrationData)
          .then(response => {
            this.$log.debug('Migrated successfullly')
            this.$message.success('数据导入成功！')
            this.install()
          })
          .finally(() => {
            hide()
          })
      } else {
        this.install()
      }
    }
  }
}
</script>
