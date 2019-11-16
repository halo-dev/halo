<template>
  <div class="page-header-index-wide page-header-wrapper-grid-content-main">
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
                  @click="()=>this.attachmentDrawerVisible = true"
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
              <a-icon type="calendar" />{{ counts.establishDays || 0 }} 天
            </p>
          </div>
          <a-divider />
          <div class="general-profile">
            <a-list
              :loading="countsLoading"
              itemLayout="horizontal"
            >
              <a-list-item>累计发表了 {{ counts.postCount || 0 }} 篇文章。</a-list-item>
              <a-list-item>累计创建了 {{ counts.attachmentCount || 0 }} 个附件。</a-list-item>
              <a-list-item>累计获得了 {{ counts.commentCount || 0 }} 条评论。</a-list-item>
              <a-list-item>累计添加了 {{ counts.linkCount || 0 }} 个友链。</a-list-item>
              <a-list-item>文章总访问 {{ counts.visitCount || 0 }} 次。</a-list-item>
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
                      :autosize="{ minRows: 5 }"
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
                    <a-input-password v-model="passwordParam.oldPassword"/>
                  </a-form-item>
                  <a-form-item label="新密码：">
                    <a-input-password v-model="passwordParam.newPassword"/>
                  </a-form-item>
                  <a-form-item label="确认密码：">
                    <a-input-password v-model="passwordParam.confirmPassword"/>
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
  </div>
</template>

<script>
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import userApi from '@/api/user'
import adminApi from '@/api/admin'
import { mapMutations, mapGetters } from 'vuex'
import MD5 from 'md5.js'

export default {
  components: {
    AttachmentSelectDrawer
  },
  data() {
    return {
      countsLoading: true,
      attachmentDrawerVisible: false,
      user: {},
      counts: {},
      passwordParam: {
        oldPassword: null,
        newPassword: null,
        confirmPassword: null
      },
      attachment: {}
    }
  },
  computed: {
    passwordUpdateButtonDisabled() {
      return !(this.passwordParam.oldPassword && this.passwordParam.newPassword)
    },
    ...mapGetters(['options'])
  },
  created() {
    this.loadUser()
    this.getCounts()
  },
  methods: {
    ...mapMutations({ setUser: 'SET_USER' }),
    loadUser() {
      userApi.getProfile().then(response => {
        this.user = response.data.data
        this.profileLoading = false
      })
    },
    getCounts() {
      adminApi.counts().then(response => {
        this.counts = response.data.data
        this.countsLoading = false
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
    }
  }
}
</script>

<style lang="less" scoped>
.page-header-wrapper-grid-content-main {
  width: 100%;
  height: 100%;
  min-height: 100%;
  transition: 0.3s;

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
}
</style>
