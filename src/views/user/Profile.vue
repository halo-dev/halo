<template>
  <div>
    <a-row :gutter="12">
      <a-col
        :lg="10"
        :md="24"
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card
          :bordered="false"
          :bodyStyle="{ padding: '16px' }"
        >
          <div class="profile-center-avatarHolder">
            <a-tooltip
              placement="right"
              :trigger="['hover']"
              title="点击可修改头像"
            >
              <template slot="title">
                <span>prompt text</span>
              </template>
              <div class="avatar">
                <img
                  :src="user.avatar || '//cn.gravatar.com/avatar/?s=256&d=mm'"
                  @click="attachmentDrawerVisible = true"
                >
              </div>
            </a-tooltip>
            <div class="username">{{ user.nickname }}</div>
            <div class="bio">{{ user.description }}</div>
          </div>
          <div class="profile-center-detail">
            <p>
              <a-icon type="link" /><a
                :href="options.blog_url"
                target="method"
              >{{ options.blog_url }}</a>
            </p>
            <p>
              <a-icon type="mail" />{{ user.email }}
            </p>
            <p>
              <a-icon type="calendar" />{{ statistics.establishDays || 0 }} 天
            </p>
          </div>
          <a-divider />
          <div class="general-profile">
            <a-list
              :loading="statisticsLoading"
              itemLayout="horizontal"
            >
              <a-list-item>累计发表了 {{ statistics.postCount || 0 }} 篇文章。</a-list-item>
              <a-list-item>累计创建了 {{ statistics.categoryCount || 0 }} 个分类。</a-list-item>
              <a-list-item>累计创建了 {{ statistics.tagCount || 0 }} 个标签。</a-list-item>
              <a-list-item>累计获得了 {{ statistics.commentCount || 0 }} 条评论。</a-list-item>
              <a-list-item>累计添加了 {{ statistics.linkCount || 0 }} 个友链。</a-list-item>
              <a-list-item>文章总阅读 {{ statistics.visitCount || 0 }} 次。</a-list-item>
              <a-list-item></a-list-item>
            </a-list>
          </div>
        </a-card>
      </a-col>
      <a-col
        :lg="14"
        :md="24"
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card
          :bodyStyle="{ padding: '0' }"
          :bordered="false"
          title="个人资料"
        >
          <div class="card-container">
            <a-tabs type="card">
              <a-tab-pane key="1">
                <span slot="tab">
                  <a-icon type="idcard" />基本资料
                </span>
                <a-form layout="vertical">
                  <a-form-item label="用户名：">
                    <a-input v-model="user.username" />
                  </a-form-item>
                  <a-form-item label="昵称：">
                    <a-input v-model="user.nickname" />
                  </a-form-item>
                  <a-form-item label="邮箱：">
                    <a-input v-model="user.email" />
                  </a-form-item>
                  <a-form-item label="个人说明：">
                    <a-input
                      :autoSize="{ minRows: 5 }"
                      type="textarea"
                      v-model="user.description"
                    />
                  </a-form-item>
                  <a-form-item>
                    <a-button
                      @click="handleUpdateProfile"
                      type="primary"
                    >保存</a-button>
                  </a-form-item>
                </a-form>
              </a-tab-pane>
              <a-tab-pane key="2">
                <span slot="tab">
                  <a-icon type="lock" />密码
                </span>
                <a-form layout="vertical">
                  <a-form-item label="原密码：">
                    <a-input-password
                      v-model="passwordParam.oldPassword"
                      autocomplete="new-password"
                    />
                  </a-form-item>
                  <a-form-item label="新密码：">
                    <a-input-password
                      v-model="passwordParam.newPassword"
                      autocomplete="new-password"
                    />
                  </a-form-item>
                  <a-form-item label="确认密码：">
                    <a-input-password
                      v-model="passwordParam.confirmPassword"
                      autocomplete="new-password"
                    />
                  </a-form-item>
                  <a-form-item>
                    <a-button
                      :disabled="passwordUpdateButtonDisabled"
                      @click="handleUpdatePassword"
                      type="primary"
                    >确认更改</a-button>
                  </a-form-item>
                </a-form>
              </a-tab-pane>
              <a-tab-pane key="3">
                <span slot="tab">
                  <a-icon type="safety-certificate" />两步验证
                </span>
                <a-form-item label="两步验证：">
                  <a-switch
                    v-model="mfaParam.switch.checked"
                    :loading="mfaParam.switch.loading"
                    @change="handleMFASwitch"
                  />
                </a-form-item>
                <a-form-item label="两步验证应用：">
                  <a-list itemLayout="horizontal">
                    <a-list-item>
                      <b>Authy</b> 功能丰富 专为两步验证码
                      <a-divider type="vertical" />
                      <a
                        target="_blank"
                        href="https://authy.com/download/"
                      >
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
                      <a
                        target="_blank"
                        href="https://apps.apple.com/us/app/google-authenticator/id388497605"
                      >
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
                      <a
                        target="_blank"
                        href="https://www.microsoft.com/zh-cn/account/authenticator"
                      >
                        iOS/Android
                        <a-icon type="link" />
                      </a>
                    </a-list-item>
                    <a-list-item>
                      <b>1Password</b> 强大安全的密码管理付费应用
                      <a-divider type="vertical" />
                      <a
                        target="_blank"
                        href="https://1password.com/zh-cn/downloads/"
                      >
                        iOS/Android/Windows/Mac/Linux/ChromeOS
                        <a-icon type="link" />
                      </a>
                    </a-list-item>
                  </a-list>
                </a-form-item>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="attachmentDrawerVisible"
      @listenToSelect="handleSelectAvatar"
      @listenToSelectGravatar="handleSelectGravatar"
      title="选择头像"
      isChooseAvatar
    />

    <a-modal
      :title="mfaParam.modal.title"
      :visible="mfaParam.modal.visible"
      @ok="handleSetMFAuth"
      :confirmLoading="false"
      @cancel="handleCloseMFAuthModal"
      :closable="false"
      icon="safety-certificate"
      :keyboard="false"
      :centered="true"
      :destroyOnClose="true"
      :width="300"
    >
      <a-form v-if="mfaUsed">
        <a-form-item extra="* 需要验证两步验证码">
          <a-input
            placeholder="两步验证码"
            v-model="mfaParam.authcode"
            :maxLength="6"
          >
            <a-icon
              slot="prefix"
              type="safety-certificate"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>
      </a-form>

      <a-form v-else>
        <a-form-item
          label="1. 请扫描二维码或导入 key"
          :extra="`MFAKey:${mfaParam.mfaKey}`"
        >
          <img
            width="100%"
            :src="mfaParam.qrImage"
          />
        </a-form-item>
        <a-form-item label="2. 验证两步验证码">
          <a-input
            placeholder="两步验证码"
            v-model="mfaParam.authcode"
            :maxLength="6"
          >
            <a-icon
              slot="prefix"
              type="safety-certificate"
              style="color: rgba(0,0,0,.25)"
            />
          </a-input>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import userApi from '@/api/user'
import statisticsApi from '@/api/statistics'
import { mapMutations, mapGetters } from 'vuex'
import MD5 from 'md5.js'

export default {
  components: {
    AttachmentSelectDrawer
  },
  data() {
    return {
      statisticsLoading: true,
      attachmentDrawerVisible: false,
      user: {},
      statistics: {},
      passwordParam: {
        oldPassword: null,
        newPassword: null,
        confirmPassword: null
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
        }
      },
      attachment: {}
    }
  },
  computed: {
    passwordUpdateButtonDisabled() {
      return !(this.passwordParam.oldPassword && this.passwordParam.newPassword)
    },
    ...mapGetters(['options']),
    mfaType() {
      return this.mfaParam.mfaType
    },
    mfaUsed() {
      return this.mfaParam.mfaUsed
    }
  },
  created() {
    this.getStatistics()
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
    getStatistics() {
      statisticsApi.statisticsWithUser().then(response => {
        this.user = response.data.data.user
        this.statistics = response.data.data
        this.statisticsLoading = false
        this.mfaParam.mfaType = this.user.mfaType && this.user.mfaType
      })
    },
    handleUpdatePassword() {
      // Check confirm password
      if (this.passwordParam.newPassword !== this.passwordParam.confirmPassword) {
        this.$message.error('确认密码和新密码不匹配！')
        return
      }
      userApi.updatePassword(this.passwordParam.oldPassword, this.passwordParam.newPassword).then(response => {
        this.$message.success('密码修改成功！')
        this.passwordParam.oldPassword = null
        this.passwordParam.newPassword = null
        this.passwordParam.confirmPassword = null
      })
    },
    handleUpdateProfile() {
      if (!this.user.username) {
        this.$notification['error']({
          message: '提示',
          description: '用户名不能为空！'
        })
        return
      }
      if (!this.user.nickname) {
        this.$notification['error']({
          message: '提示',
          description: '用户昵称不能为空！'
        })
        return
      }
      if (!this.user.email) {
        this.$notification['error']({
          message: '提示',
          description: '邮箱不能为空！'
        })
        return
      }
      userApi.updateProfile(this.user).then(response => {
        this.user = response.data.data
        this.setUser(Object.assign({}, this.user))
        this.$message.success('资料更新成功！')
      })
    },
    handleSelectAvatar(data) {
      this.user.avatar = encodeURI(data.path)
      this.attachmentDrawerVisible = false
    },
    handleSelectGravatar() {
      this.user.avatar = '//cn.gravatar.com/avatar/' + new MD5().update(this.user.email).digest('hex') + '&d=mm'
      this.attachmentDrawerVisible = false
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
      var mfaType = this.mfaUsed ? 'NONE' : 'TFA_TOTP'
      if (mfaType === 'NONE') {
        if (!this.mfaParam.authcode) {
          this.$message.warn('两步验证码不能为空！')
          return
        }
      }
      userApi.mfaUpdate(mfaType, this.mfaParam.mfaKey, this.mfaParam.authcode).then(response => {
        this.handleCloseMFAuthModal()
        this.mfaParam.mfaType = response.data.data.mfaType
        this.$message.success(this.mfaUsed ? '两步验证已关闭！' : '两步验证已开启,下次登陆生效！')
      })
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

<style lang="less" scoped>
.profile-center-avatarHolder {
  text-align: center;
  margin-bottom: 24px;

  & > .avatar {
    margin: 0 auto;
    width: 104px;
    height: 104px;
    margin-bottom: 20px;
    border-radius: 50%;
    overflow: hidden;
    cursor: pointer;

    img {
      height: 100%;
      width: 100%;
    }
  }

  .username {
    color: rgba(0, 0, 0, 0.85);
    font-size: 20px;
    line-height: 28px;
    font-weight: 500;
    margin-bottom: 4px;
  }
}

.profile-center-detail {
  p {
    margin-bottom: 8px;
    padding-left: 26px;
    position: relative;
  }

  i {
    position: absolute;
    height: 14px;
    width: 14px;
    left: 0;
    top: 4px;
  }
}
</style>
