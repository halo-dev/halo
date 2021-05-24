<template>
  <div>
    <a-row :gutter="12">
      <a-col :lg="10" :md="24" class="pb-3">
        <a-card :bordered="false" :bodyStyle="{ padding: '16px' }">
          <div class="mb-6 text-center">
            <a-tooltip placement="right" :trigger="['hover']" title="点击可修改头像">
              <a-avatar
                :size="104"
                :src="userForm.model.avatar || '//cn.gravatar.com/avatar/?s=256&d=mm'"
                @click="attachmentDrawer.visible = true"
                class="cursor-pointer"
              />
            </a-tooltip>
            <div class="mt-4 mb-1 text-xl font-medium leading-5" style="color: rgba(0, 0, 0, 0.85);">
              {{ userForm.model.nickname }}
            </div>
            <div>{{ userForm.model.description }}</div>
          </div>
          <div>
            <p class="mb-3">
              <a-icon type="link" class="mr-3" /><a :href="options.blog_url" target="method">{{ options.blog_url }}</a>
            </p>
            <p class="mb-3"><a-icon type="mail" class="mr-3" />{{ userForm.model.email }}</p>
            <p class="mb-3"><a-icon type="calendar" class="mr-3" />{{ statistics.data.establishDays || 0 }} 天</p>
          </div>
          <a-divider />
          <div>
            <a-list :loading="statistics.loading" itemLayout="horizontal">
              <a-list-item>累计发表了 {{ statistics.data.postCount || 0 }} 篇文章。</a-list-item>
              <a-list-item>累计创建了 {{ statistics.data.categoryCount || 0 }} 个分类。</a-list-item>
              <a-list-item>累计创建了 {{ statistics.data.tagCount || 0 }} 个标签。</a-list-item>
              <a-list-item>累计获得了 {{ statistics.data.commentCount || 0 }} 条评论。</a-list-item>
              <a-list-item>累计添加了 {{ statistics.data.linkCount || 0 }} 个友链。</a-list-item>
              <a-list-item>文章总阅读 {{ statistics.data.visitCount || 0 }} 次。</a-list-item>
              <a-list-item></a-list-item>
            </a-list>
          </div>
        </a-card>
      </a-col>
      <a-col :lg="14" :md="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '0' }" :bordered="false" title="个人资料">
          <div class="card-container">
            <a-tabs type="card">
              <a-tab-pane key="1">
                <span slot="tab"> <a-icon type="idcard" />基本资料 </span>
                <a-form-model ref="userForm" :model="userForm.model" :rules="userForm.rules" layout="vertical">
                  <a-form-model-item label="用户名：" prop="username">
                    <a-input v-model="userForm.model.username" />
                  </a-form-model-item>
                  <a-form-model-item label="昵称：" prop="nickname">
                    <a-input v-model="userForm.model.nickname" />
                  </a-form-model-item>
                  <a-form-model-item label="电子邮箱：" prop="email">
                    <a-input v-model="userForm.model.email" />
                  </a-form-model-item>
                  <a-form-model-item label="个人说明：" prop="description">
                    <a-input :autoSize="{ minRows: 5 }" type="textarea" v-model="userForm.model.description" />
                  </a-form-model-item>
                  <a-form-model-item>
                    <ReactiveButton
                      type="primary"
                      @click="handleUpdateProfile"
                      @callback="handleUpdatedProfileCallback"
                      :loading="userForm.saving"
                      :errored="userForm.errored"
                      text="保存"
                      loadedText="保存成功"
                      erroredText="保存失败"
                    ></ReactiveButton>
                  </a-form-model-item>
                </a-form-model>
              </a-tab-pane>
              <a-tab-pane key="2">
                <span slot="tab"> <a-icon type="lock" />密码 </span>
                <a-form-model
                  ref="passwordForm"
                  :model="passwordForm.model"
                  :rules="passwordForm.rules"
                  layout="vertical"
                >
                  <a-form-model-item label="原密码：" prop="oldPassword">
                    <a-input-password v-model="passwordForm.model.oldPassword" autocomplete="new-password" />
                  </a-form-model-item>
                  <a-form-model-item label="新密码：" prop="newPassword">
                    <a-input-password v-model="passwordForm.model.newPassword" autocomplete="new-password" />
                  </a-form-model-item>
                  <a-form-model-item label="确认密码：" prop="confirmPassword">
                    <a-input-password v-model="passwordForm.model.confirmPassword" autocomplete="new-password" />
                  </a-form-model-item>
                  <a-form-model-item>
                    <ReactiveButton
                      type="primary"
                      @click="handleUpdatePassword"
                      @callback="handleUpdatedPasswordCallback"
                      :loading="passwordForm.saving"
                      :errored="passwordForm.errored"
                      text="确认更改"
                      loadedText="更改成功"
                      erroredText="更改失败"
                    ></ReactiveButton>
                  </a-form-model-item>
                </a-form-model>
              </a-tab-pane>
              <a-tab-pane key="3">
                <span slot="tab"> <a-icon type="safety-certificate" />两步验证 </span>
                <a-form-model layout="vertical">
                  <a-form-model-item label="两步验证：">
                    <a-switch
                      v-model="mfaParam.switch.checked"
                      :loading="mfaParam.switch.loading"
                      @change="handleMFASwitch"
                    />
                  </a-form-model-item>
                  <a-form-model-item label="两步验证应用：">
                    <a-list itemLayout="horizontal">
                      <a-list-item>
                        <b>Authy</b> 功能丰富 专为两步验证码
                        <a-divider type="vertical" />
                        <a target="_blank" href="https://authy.com/download/">
                          iOS/Android/Windows/Mac/Linux
                          <a-icon type="link" />
                        </a>
                        <a-divider type="vertical" />
                        <a
                          target="_blank"
                          href="https://chrome.google.com/webstore/detail/authy/gaedmjdfmmahhbjefcbgaolhhanlaolb?hl=cn"
                        >
                          Chrome 扩展
                          <a-icon type="link" />
                        </a>
                      </a-list-item>
                      <a-list-item>
                        <b>Google Authenticator</b> 简单易用，但不支持密钥导出备份
                        <a-divider type="vertical" />
                        <a target="_blank" href="https://apps.apple.com/us/app/google-authenticator/id388497605">
                          iOS
                          <a-icon type="link" />
                        </a>
                        <a-divider type="vertical" />
                        <a
                          target="_blank"
                          href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2&hl=cn"
                        >
                          Android
                          <a-icon type="link" />
                        </a>
                      </a-list-item>
                      <a-list-item>
                        <b>Microsoft Authenticator</b> 使用微软全家桶的推荐
                        <a-divider type="vertical" />
                        <a target="_blank" href="https://www.microsoft.com/zh-cn/account/authenticator">
                          iOS/Android
                          <a-icon type="link" />
                        </a>
                      </a-list-item>
                      <a-list-item>
                        <b>1Password</b> 强大安全的密码管理付费应用
                        <a-divider type="vertical" />
                        <a target="_blank" href="https://1password.com/zh-cn/downloads/">
                          iOS/Android/Windows/Mac/Linux/ChromeOS
                          <a-icon type="link" />
                        </a>
                      </a-list-item>
                    </a-list>
                  </a-form-model-item>
                </a-form-model>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="attachmentDrawer.visible"
      @listenToSelect="handleSelectAvatar"
      @listenToSelectGravatar="handleSelectGravatar"
      title="选择头像"
      isChooseAvatar
    />

    <a-modal
      :title="mfaParam.modal.title"
      :visible="mfaParam.modal.visible"
      :confirmLoading="false"
      :closable="false"
      icon="safety-certificate"
      :keyboard="false"
      :centered="true"
      :destroyOnClose="true"
      :width="400"
    >
      <template slot="footer">
        <a-button key="back" @click="handleCloseMFAuthModal">
          取消
        </a-button>
        <ReactiveButton
          key="submit"
          type="primary"
          @click="handleSetMFAuth"
          @callback="handleSetMFAuthCallback"
          :loading="mfaParam.saving"
          :errored="mfaParam.errored"
          text="确定"
          loadedText="设置成功"
          erroredText="设置失败"
        ></ReactiveButton>
      </template>
      <a-form-model ref="mfaForm" :model="mfaParam" :rules="mfaParam.rules" layout="vertical">
        <a-form-model-item v-if="mfaUsed" label="两步验证码" prop="authcode">
          <a-input v-model="mfaParam.authcode" :maxLength="6">
            <a-icon slot="prefix" type="safety-certificate" style="color: rgba(0,0,0,.25)" />
          </a-input>
        </a-form-model-item>
        <a-form-model-item v-if="!mfaUsed" label="1. 请扫描二维码或导入 key" :help="`MFAKey:${mfaParam.mfaKey}`">
          <template slot="extra">
            <span class="text-red-600"
              >* 建议保存此二维码或 MFAKey，验证设备丢失将无法找回，只能通过重置密码关闭二步验证。</span
            >
          </template>
          <img width="100%" :src="mfaParam.qrImage" />
        </a-form-model-item>
        <a-form-model-item v-if="!mfaUsed" label="2. 验证两步验证码" prop="authcode">
          <a-input v-model="mfaParam.authcode" :maxLength="6">
            <a-icon slot="prefix" type="safety-certificate" style="color: rgba(0,0,0,.25)" />
          </a-input>
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>

<script>
import userApi from '@/api/user'
import statisticsApi from '@/api/statistics'
import { mapMutations, mapGetters } from 'vuex'
import MD5 from 'md5.js'

export default {
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value && this.passwordForm.model.newPassword !== value) {
        callback(new Error('确认密码与新密码不一致'))
      } else {
        callback()
      }
    }
    return {
      attachmentDrawer: {
        visible: false
      },
      userForm: {
        model: {},
        saving: false,
        errored: false,
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
            { required: true, message: '* 电子邮箱地址不能为空', trigger: ['change'] },
            { type: 'email', message: '* 电子邮箱地址格式不正确', trigger: ['change'] },
            { max: 127, message: '* 电子邮箱的字符长度不能超过 255', trigger: ['change'] }
          ],
          description: [{ max: 1023, message: '* 个人说明的字符长度不能超过 1023', trigger: ['change'] }]
        }
      },
      statistics: {
        data: {},
        loading: false
      },
      passwordForm: {
        model: {
          oldPassword: null,
          newPassword: null,
          confirmPassword: null
        },
        saving: false,
        errored: false,
        rules: {
          oldPassword: [
            { required: true, message: '* 原密码不能为空', trigger: ['change'] },
            { max: 100, min: 8, message: '* 密码的字符长度必须在 8 - 100 之间', trigger: ['blur'] }
          ],
          newPassword: [
            { required: true, message: '* 新密码不能为空', trigger: ['change'] },
            { max: 100, min: 8, message: '* 密码的字符长度必须在 8 - 100 之间', trigger: ['change'] }
          ],
          confirmPassword: [
            { required: true, message: '* 确认密码不能为空', trigger: ['change'] },
            { validator: validateConfirmPassword, trigger: ['change'] }
          ]
        }
      },
      mfaParam: {
        mfaKey: null,
        mfaType: 'NONE',
        mfaUsed: false,
        authcode: null,
        qrImage: null,
        modal: {
          title: '确认开启两步验证？',
          visible: false
        },
        switch: {
          loading: false,
          checked: false
        },
        rules: {
          authcode: [{ required: true, message: '* 两步验证码不能为空', trigger: ['change'] }]
        },
        saving: false,
        errored: false
      }
    }
  },
  computed: {
    ...mapGetters(['options']),
    mfaType() {
      return this.mfaParam.mfaType
    },
    mfaUsed() {
      return this.mfaParam.mfaUsed
    }
  },
  created() {
    this.handleLoadStatistics()
  },
  watch: {
    mfaType(value) {
      if (value) {
        this.mfaParam.mfaUsed = value !== 'NONE'
      }
    },
    mfaUsed(value) {
      this.mfaParam.switch.checked = value
    }
  },
  methods: {
    ...mapMutations({ setUser: 'SET_USER' }),
    handleLoadStatistics() {
      this.statistics.loading = true
      statisticsApi
        .statisticsWithUser()
        .then(response => {
          this.userForm.model = response.data.data.user
          this.statistics.data = response.data.data
          this.mfaParam.mfaType = this.userForm.model.mfaType && this.userForm.model.mfaType
        })
        .finally(() => {
          setTimeout(() => {
            this.statistics.loading = false
          }, 200)
        })
    },
    handleUpdatePassword() {
      const _this = this
      _this.$refs.passwordForm.validate(valid => {
        if (valid) {
          this.passwordForm.saving = true
          userApi
            .updatePassword(this.passwordForm.model.oldPassword, this.passwordForm.model.newPassword)
            .catch(() => {
              this.passwordForm.errored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.passwordForm.saving = false
              }, 400)
            })
        }
      })
    },
    handleUpdatedPasswordCallback() {
      if (this.passwordForm.errored) {
        this.passwordForm.errored = false
      } else {
        this.passwordForm.model.oldPassword = null
        this.passwordForm.model.newPassword = null
        this.passwordForm.model.confirmPassword = null
      }
    },
    handleUpdateProfile() {
      const _this = this
      _this.$refs.userForm.validate(valid => {
        if (valid) {
          this.userForm.saving = true
          userApi
            .updateProfile(this.userForm.model)
            .then(response => {
              this.userForm.model = response.data.data
              this.setUser(Object.assign({}, this.userForm.model))
            })
            .catch(() => {
              this.userForm.errored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.userForm.saving = false
              }, 400)
            })
        }
      })
    },
    handleUpdatedProfileCallback() {
      if (this.userForm.errored) {
        this.userForm.errored = false
      }
    },
    handleSelectAvatar(data) {
      this.userForm.model.avatar = encodeURI(data.path)
      this.attachmentDrawer.visible = false
    },
    handleSelectGravatar() {
      this.userForm.model.avatar =
        '//cn.gravatar.com/avatar/' + new MD5().update(this.userForm.model.email).digest('hex') + '&d=mm'
      this.attachmentDrawer.visible = false
    },
    handleMFASwitch(useMFAuth) {
      // loding
      this.mfaParam.switch.loading = true
      if (!useMFAuth && this.mfaUsed) {
        // true -> false
        // show cloes MFA modal
        this.mfaParam.modal.title = '确认关闭两步验证？'
        this.mfaParam.modal.visible = true
      } else {
        // false -> true
        // show open MFA modal
        this.mfaParam.modal.title = '确认开启两步验证？'
        // generate MFAKey and Qr Image
        userApi.mfaGenerate('TFA_TOTP').then(response => {
          this.mfaParam.mfaKey = response.data.data.mfaKey
          this.mfaParam.qrImage = response.data.data.qrImage
          this.mfaParam.modal.visible = true
        })
      }
    },
    handleSetMFAuth() {
      const _this = this
      var mfaType = _this.mfaUsed ? 'NONE' : 'TFA_TOTP'
      _this.$refs.mfaForm.validate(valid => {
        if (valid) {
          _this.mfaParam.saving = true
          userApi
            .mfaUpdate(mfaType, _this.mfaParam.mfaKey, _this.mfaParam.authcode)
            .catch(() => {
              _this.mfaParam.errored = true
            })
            .finally(() => {
              setTimeout(() => {
                _this.mfaParam.saving = false
              }, 400)
            })
        }
      })
    },
    handleSetMFAuthCallback() {
      const _this = this
      if (_this.mfaParam.errored) {
        _this.mfaParam.errored = false
      } else {
        _this.handleCloseMFAuthModal()
        _this.handleLoadStatistics()
        _this.$message.success(_this.mfaUsed ? '两步验证已关闭！' : '两步验证已开启,下次登录生效！')
      }
    },
    handleCloseMFAuthModal() {
      this.mfaParam.modal.visible = false
      this.mfaParam.switch.loading = false
      this.mfaParam.switch.checked = this.mfaUsed
      // clean
      this.mfaParam.authcode = null
      this.mfaParam.qrImage = null
      this.mfaParam.mfaKey = null
    }
  }
}
</script>
