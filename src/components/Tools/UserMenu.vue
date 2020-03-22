<template>
  <div class="user-wrapper">
    <a
      :href="options.blog_url"
      target="_blank"
    >
      <a-tooltip
        placement="bottom"
        title="点击跳转到首页"
      >
        <span class="action">
          <a-icon type="link" />
        </span>
      </a-tooltip>
    </a>
    <a
      href="javascript:void(0)"
      @click="handleShowLayoutSetting"
    >
      <a-tooltip
        placement="bottom"
        title="后台布局设置"
      >
        <span class="action">
          <a-icon type="setting" />
        </span>
      </a-tooltip>
    </a>
    <header-comment class="action" />
    <a-dropdown>
      <span
        class="action ant-dropdown-link user-dropdown-menu"
        v-if="user"
      >
        <a-avatar
          class="avatar"
          size="small"
          :src="user.avatar || '//cn.gravatar.com/avatar/?s=256&d=mm'"
        />
      </span>
      <a-menu
        slot="overlay"
        class="user-dropdown-menu-wrapper"
      >
        <a-menu-item key="0">
          <router-link :to="{ name: 'Profile' }">
            <a-icon type="user" />
            <span>个人资料</span>
          </router-link>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="1">
          <a
            href="javascript:;"
            @click="handleLogout"
          >
            <a-icon type="logout" />
            <span>退出登录</span>
          </a>
        </a-menu-item>
      </a-menu>
    </a-dropdown>
  </div>
</template>

<script>
import HeaderComment from './HeaderComment'
import { mapActions, mapGetters } from 'vuex'

export default {
  name: 'UserMenu',
  components: {
    HeaderComment
  },
  computed: {
    ...mapGetters(['user', 'options'])
  },
  methods: {
    ...mapActions(['logout', 'ToggleLayoutSetting']),
    handleLogout() {
      const that = this

      this.$confirm({
        title: '提示',
        content: '确定要注销登录吗 ?',
        onOk() {
          return that
            .logout({})
            .then(() => {
              window.location.reload()
            })
            .catch(err => {
              that.$message.error({
                title: '错误',
                description: err.message
              })
            })
        },
        onCancel() {}
      })
    },
    handleShowLayoutSetting() {
      this.ToggleLayoutSetting(true)
    }
  }
}
</script>
