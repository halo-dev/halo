<template>
  <div class="user-wrapper">
    <a href="/" target="_blank">
      <span class="action">
        <a-icon type="link"></a-icon>
      </span>
    </a>
    <a href="javascript:void(0)" @click="showOptionModal">
      <span class="action">
        <a-icon type="setting"></a-icon>
      </span>
    </a>
    <header-comment class="action"/>
    <a-dropdown>
      <span class="action ant-dropdown-link user-dropdown-menu">
        <a-avatar class="avatar" size="small" :src="avatar"/>
      </span>
      <a-menu slot="overlay" class="user-dropdown-menu-wrapper">
        <a-menu-item key="0">
          <router-link :to="{ name: 'Profile' }">
            <a-icon type="user"/>
            <span>个人资料</span>
          </router-link>
        </a-menu-item>
        <a-menu-divider/>
        <a-menu-item key="3">
          <a href="javascript:;" @click="handleLogout">
            <a-icon type="logout"/>
            <span>退出登录</span>
          </a>
        </a-menu-item>
      </a-menu>
    </a-dropdown>
    <a-modal title="样式设置" v-model="optionVisible"></a-modal>
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
  data() {
    return {
      optionVisible: false,
      avatar: 'https://gravatar.loli.net/avatar/?s=256&d=mm'
    }
  },
  methods: {
    ...mapActions(['Logout']),
    ...mapGetters(['nickname']),
    handleLogout() {
      const that = this

      this.$confirm({
        title: '提示',
        content: '真的要注销登录吗 ?',
        onOk() {
          return that
            .Logout({})
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
    showOptionModal() {
      this.optionVisible = true
    }
  }
}
</script>
