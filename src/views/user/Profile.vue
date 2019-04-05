<template>
  <div class="page-header-index-wide page-header-wrapper-grid-content-main">
    <a-row :gutter="24">
      <a-col :lg="10" :md="24">
        <a-card :bodyStyle="{ padding: '16' }" :bordered="false">
          <div class="profile-center-avatarHolder">
            <div class="avatar">
              <img :src="user.avatar || 'https://gravatar.loli.net/avatar/?s=256&d=mm'" />
            </div>
            <div class="username">{{ user.nickname }}</div>
            <div class="bio">{{ user.description }}</div>
          </div>
          <div class="profile-center-detail">
            <p>
              <a-icon type="mail" />
              {{ user.email }}
            </p>
            <p>
              <a-icon type="calendar" />
              {{ counts.establishDays || 0 }} 天
            </p>
          </div>
          <a-divider />
          <div class="general-profile">
            <a-list :loading="countsLoading" itemLayout="horizontal">
              <a-list-item>累计发表了 {{ counts.postCount || 0 }} 篇文章。</a-list-item>
              <a-list-item>累计创建了 {{ counts.linkCount || 0 }} 个标签。</a-list-item>
              <a-list-item>累计获得了 {{ counts.commentCount || 0 }} 条评论。</a-list-item>
              <a-list-item>累计添加了 {{ counts.linkCount || 0 }} 个友链。</a-list-item>
              <a-list-item>文章总访问 {{ counts.visitCount || 0 }} 次。</a-list-item>
              <a-list-item></a-list-item>
            </a-list>
          </div>
        </a-card>
      </a-col>
      <a-col :lg="14" :md="24">
        <a-card :bodyStyle="{ padding: '0' }" :bordered="false" title="个人资料">
          <div class="card-container">
            <a-tabs type="card">
              <a-tab-pane key="1" tab="基本资料">
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
                    <a-input :autosize="{ minRows: 5 }" type="textarea" v-model="user.description" />
                  </a-form-item>
                  <a-form-item>
                    <a-button @click="updateProfile" type="primary">保存</a-button>
                  </a-form-item>
                </a-form>
              </a-tab-pane>
              <a-tab-pane key="2" tab="密码">
                <a-form layout="vertical">
                  <a-form-item label="原密码：">
                    <a-input type="password" v-model="passwordParam.oldPassword" />
                  </a-form-item>
                  <a-form-item label="新密码：">
                    <a-input type="password" v-model="passwordParam.newPassword" />
                  </a-form-item>
                  <a-form-item label="确认密码：">
                    <a-input type="password" v-model="passwordParam.confirmPassword" />
                  </a-form-item>
                  <a-form-item>
                    <a-button
                      :disabled="passwordUpdateButtonDisabled"
                      @click="updatePassword"
                      type="primary"
                    >确认更改
                    </a-button>
                  </a-form-item>
                </a-form>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import userApi from '@/api/user'
import adminApi from '@/api/admin'

export default {
  components: {},
  data() {
    return {
      countsLoading: true,
      user: {},
      counts: {},
      passwordParam: {
        oldPassword: null,
        newPassword: null,
        confirmPassword: null
      }
    }
  },
  computed: {
    passwordUpdateButtonDisabled() {
      return !(this.passwordParam.oldPassword && this.passwordParam.newPassword)
    }
  },
  created() {
    this.loadUser()
    this.getCounts()
  },
  methods: {
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
    updatePassword() {
      // Check confirm password
      if (this.passwordParam.newPassword !== this.passwordParam.confirmPassword) {
        this.$message.error('确认密码和新密码不匹配！')
        return
      }

      userApi.updatePassword(this.passwordParam.oldPassword, this.passwordParam.newPassword).then(response => {})
    },
    updateProfile() {
      userApi.updateProfile(this.user).then(response => {
        this.user = response.data.data
        this.$message.success('资料更新成功！')
      })
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

<style>
.card-container {
  background: #f5f5f5;
}

.card-container > .ant-tabs-card > .ant-tabs-content {
  margin-top: -16px;
}

.card-container > .ant-tabs-card > .ant-tabs-content > .ant-tabs-tabpane {
  background: #fff;
  padding: 16px;
}

.card-container > .ant-tabs-card > .ant-tabs-bar {
  border-color: #fff;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab {
  border-color: transparent;
  background: transparent;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab-active {
  border-color: #fff;
  background: #fff;
}

.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}
</style>
