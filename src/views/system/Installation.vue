<template>
  <div>
    <a-row align="middle" class="h-screen" justify="center" type="flex">
      <a-col :lg="16" :md="20" :sm="20" :xl="12" :xs="23" :xxl="8">
        <div class="card-container animated fadeIn">
          <a-card :bordered="false" style="box-shadow: rgba(99, 99, 99, 0.2) 0 2px 8px 0;">
            <div class="halo-logo">
              <span
                >Halo
                <small>安装向导</small>
              </span>
            </div>
            <a-alert :message="`欢迎使用 Halo，您正在安装的是 Halo ${VERSION}。`" show-icon type="success" />
            <!-- Blogger info -->
            <div class="mt-5 mb-5">
              <a-radio-group v-model="installationMode">
                <a-radio-button value="new">
                  全新安装
                </a-radio-button>
                <a-radio-button value="import">
                  数据导入
                </a-radio-button>
              </a-radio-group>
            </div>
            <a-form-model
              v-show="isInstallMode"
              ref="installationForm"
              :model="form.model"
              :rules="form.rules"
              class="installationForm animated fadeIn"
              layout="horizontal"
            >
              <a-divider dashed orientation="left">
                管理员信息
              </a-divider>
              <a-form-model-item prop="username">
                <a-input v-model="form.model.username" placeholder="用户名">
                  <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="user" />
                </a-input>
              </a-form-model-item>
              <a-form-model-item prop="username">
                <a-input v-model="form.model.nickname" placeholder="用户昵称">
                  <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="user" />
                </a-input>
              </a-form-model-item>
              <a-form-model-item prop="email">
                <a-input v-model="form.model.email" placeholder="用户邮箱">
                  <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="mail" />
                </a-input>
              </a-form-model-item>
              <a-form-model-item prop="password">
                <a-input v-model="form.model.password" placeholder="登录密码（8-100位）" type="password">
                  <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="lock" />
                </a-input>
              </a-form-model-item>
              <a-form-model-item prop="confirmPassword">
                <a-input v-model="form.model.confirmPassword" placeholder="确认登录密码" type="password">
                  <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="lock" />
                </a-input>
              </a-form-model-item>
              <a-divider dashed orientation="left">
                站点信息
              </a-divider>
              <a-form-model-item prop="url">
                <a-input v-model="form.model.url" placeholder="博客地址">
                  <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="link" />
                </a-input>
              </a-form-model-item>
              <a-form-model-item prop="title">
                <a-input v-model="form.model.title" placeholder="博客标题">
                  <a-icon slot="prefix" style="color: rgba(0,0,0,.25)" type="book" />
                </a-input>
              </a-form-model-item>
            </a-form-model>

            <!-- Data migration -->
            <div v-show="isImportMode" class="animated fadeIn">
              <FilePondUpload
                ref="upload"
                :accepts="['application/json']"
                :multiple="false"
                :uploadHandler="onImportUpload"
                label="拖拽或点击选择数据文件<br>请确认是否为 Halo 后台导出的文件。"
                name="file"
              ></FilePondUpload>
            </div>

            <div class="mt-8">
              <ReactiveButton
                v-if="isInstallMode"
                :errored="form.installErrored"
                :loading="form.installing"
                block
                erroredText="安装失败"
                icon="check"
                loadedText="安装成功"
                size="large"
                text="安装"
                type="primary"
                @callback="handleInstallCallback"
                @click="handleInstall"
              ></ReactiveButton>
              <ReactiveButton
                v-if="isImportMode"
                :errored="form.importErrored"
                :loading="form.importing"
                block
                erroredText="导入失败"
                icon="import"
                loadedText="导入成功"
                size="large"
                text="导入"
                type="primary"
                @callback="handleImportCallback"
                @click="handleImport"
              ></ReactiveButton>
            </div>
          </a-card>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import apiClient from '@/utils/api-client'
import { mapActions } from 'vuex'

export default {
  data() {
    const confirmPasswordValidate = (rule, value, callback) => {
      if (value !== this.form.model.password) {
        callback(new Error('确认密码与所输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      installationMode: 'new', // new or import
      form: {
        model: {},
        rules: {
          username: [
            { required: true, message: '* 用户名不能为空', trigger: ['change'] },
            { max: 50, message: '* 用户名的字符长度不能超过 50', trigger: ['change'] }
          ],
          nickname: [
            { required: true, message: '* 用户昵称不能为空', trigger: ['change'] },
            { max: 255, message: '* 用户昵称的字符长度不能超过 255', trigger: ['change'] }
          ],
          email: [
            { required: true, message: '* 电子邮件地址不能为空', trigger: ['change'] },
            { max: 127, message: '* 电子邮件地址的字符长度不能超过 127', trigger: ['change'] },
            {
              pattern: /\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/g,
              message: '* 电子邮件地址的格式不正确',
              trigger: ['change']
            }
          ],
          password: [
            { required: true, message: '* 密码不能为空', trigger: ['change'] },
            { min: 8, max: 100, message: '* 密码的字符长度必须在 8 - 100 之间', trigger: ['change'] }
          ],
          confirmPassword: [
            { required: true, message: '* 确认密码不能为空', trigger: ['change'] },
            { validator: confirmPasswordValidate, trigger: ['change'] }
          ],
          url: [{ required: true, message: '* 博客地址不能为空', trigger: ['change'] }],
          title: [{ required: true, message: '* 博客标题不能为空', trigger: ['change'] }]
        },
        installing: false,
        installErrored: false,

        importing: false,
        importErrored: false,
        importData: null
      }
    }
  },
  beforeMount() {
    this.handleVerifyIsInstall()
    this.$set(this.form.model, 'url', window.location.protocol + '//' + window.location.host)
  },
  computed: {
    isInstallMode() {
      return this.installationMode === 'new'
    },
    isImportMode() {
      return this.installationMode === 'import'
    }
  },
  methods: {
    ...mapActions(['installCleanToken']),
    async handleVerifyIsInstall() {
      const response = await apiClient.isInstalled()
      if (response.data) {
        await this.$router.push({ name: 'Login' })
      }
    },
    handleInstall() {
      this.$refs.installationForm.validate(valid => {
        if (valid) {
          this.form.installing = true
          this.installCleanToken(this.form.model)
            .then(response => {
              this.$log.debug('Installation response', response)
            })
            .catch(() => {
              this.form.installErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.form.installing = false
              }, 400)
            })
        }
      })
    },
    handleInstallCallback() {
      if (this.form.installErrored) {
        this.form.installErrored = false
      } else {
        this.$message.success('安装成功！')
        this.$router.push({ name: 'Login' })
      }
    },
    onImportUpload(data) {
      this.$log.debug('Selected data', data)
      this.form.importData = data
      return new Promise(resolve => {
        this.$log.debug('Handle uploading')
        resolve()
      })
    },
    handleImport() {
      if (!this.form.importData) {
        this.$message.warning('请先上传数据文件！')
        return
      }
      this.form.importing = true
      apiClient.migration
        .migrate(this.form.importData)
        .then(() => {
          this.$log.debug('Migrated successfully')
        })
        .catch(() => {
          this.form.importErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.form.importing = false
          }, 400)
        })
    },
    handleImportCallback() {
      if (this.form.importErrored) {
        this.form.importErrored = false
      } else {
        this.$message.success('导入成功！')
        this.$router.replace({ name: 'Login' })
      }
    }
  }
}
</script>
<style lang="less" scoped>
::v-deep .installationForm {
  .ant-divider {
    .ant-divider-inner-text {
      padding-left: 0;
    }

    &::before {
      width: 0;
    }
  }
}
</style>
