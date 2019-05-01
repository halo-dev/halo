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
            :bordered="false"
            title="Halo 安装向导"
            class="install-card"
          >

            <a-steps :current="stepCurrent">
              <a-step title="博主信息">
              </a-step>
              <a-step title="博客信息">
              </a-step>
              <a-step title="数据迁移">
              </a-step>
            </a-steps>
            <a-divider dashed />
            <!-- Blogger info -->
            <a-form
              layout="horizontal"
              v-show="stepCurrent == 0"
            >
              <a-form-item class="animated fadeInUp">
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
              </a-form-item>
              <a-form-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.1s'}"
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
              </a-form-item>
              <a-form-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.2s'}"
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
              </a-form-item>
              <a-form-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.3s'}"
              >
                <a-input
                  v-model="installation.password"
                  type="password"
                  placeholder="用户密码"
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
                :style="{'animation-delay': '0.4s'}"
              >
                <a-input
                  v-model="installation.confirmPassword"
                  type="password"
                  placeholder="确定密码"
                >
                  <a-icon
                    slot="prefix"
                    type="lock"
                    style="color: rgba(0,0,0,.25)"
                  />
                </a-input>
              </a-form-item>
            </a-form>

            <!-- Blog info -->

            <a-form
              layout="horizontal"
              v-show="stepCurrent == 1"
            >
              <a-form-item class="animated fadeInUp">
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
              </a-form-item>
              <a-form-item
                class="animated fadeInUp"
                :style="{'animation-delay': '0.2s'}"
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
              </a-form-item>
            </a-form>

            <!-- Data migration -->
            <div v-show="stepCurrent == 2">
              <a-alert
                style="margin-bottom: 1rem"
                message="如果有迁移需求，请点击并选择'迁移文件'"
                type="info"
                class="animated fadeInUp"
              />
              <Upload
                :name="migrationUploadName"
                accept="application/json"
                :uploadHandler="handleMigrationUpload"
                @remove="handleMigrationFileRemove"
                class="animated fadeIn"
                :style="{'animation-delay': '0.2s'}"
              >
                <p class="ant-upload-drag-icon">
                  <a-icon type="inbox" />
                </p>
                <p class="ant-upload-text">点击选择文件或将文件拖拽到此处</p>
                <p class="ant-upload-hint">仅支持单个文件上传</p>
              </Upload>
            </div>

            <a-row
              class="install-action"
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
                @click="handleInstall"
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
import recoveryApi from '@/api/recovery'

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
      migrationUploadName: 'file',
      migrationData: null,
      stepCurrent: 0
    }
  },
  created() {
    this.installation.url = window.location.protocol + '//' + window.location.host
  },
  methods: {
    handleMigrationUpload(data) {
      this.$log.debug('Selected data', data)
      this.migrationData = data
      return new Promise((resolve, reject) => {
        this.$log.debug('Handle uploading')
        resolve()
      })
    },
    handleMigrationFileRemove(file) {
      this.$log.debug('Removed file', file)
      this.$log.debug('Migration file from data', this.migrationData.get(this.migrationUploadName))
      if (this.migrationData.get(this.migrationUploadName).uid === file.uid) {
        this.migrationData = null
        this.migrationFile = null
      }
    },
    install() {
      adminApi.install(this.installation).then(response => {
        this.$log.debug('Installation response', response)
        this.$message.success('安装成功')
        setTimeout(() => {
          this.$router.push({ name: 'Dashboard' })
        }, 300)
      })
    },
    handleInstall() {
      const password = this.installation.password
      const confirmPassword = this.installation.confirmPassword

      this.$log.debug('Password', password)
      this.$log.debug('Confirm password', confirmPassword)

      if (password !== confirmPassword) {
        this.$message.error('确认密码和密码不匹配')
        return
      }

      // Handle migration
      if (this.migrationData) {
        recoveryApi.migrate(this.migrationData).then(response => {
          this.$log.debug('Migrated successfullly')
          this.$message.success('数据迁移成功')
          this.install()
        })
      } else {
        this.install()
      }
    }
  }
}
</script>

<style lang="less" scoped>
.height-100 {
  height: 100vh;
}

.install-action {
  margin-top: 1rem;
}
.previus-button {
  margin-right: 1rem;
}

.install-card {
  box-shadow: 0px 10px 20px 0px rgba(236, 236, 236, 0.86);
}
</style>
