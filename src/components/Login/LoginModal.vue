<template>
  <div>
    <a-modal
      v-model="loginModal"
      title="重新登录"
      :footer="null"
      :width="320"
      :maskClosable="false"
      @cancel="handleCancelLogin"
    >
      <LoginForm @success="onLoginSucceed" />
    </a-modal>
  </div>
</template>
<script>
import LoginForm from './LoginForm'
import { mapGetters, mapActions } from 'vuex'
export default {
  name: 'LoginModal',
  components: {
    LoginForm
  },
  computed: {
    ...mapGetters(['loginModal'])
  },
  methods: {
    ...mapActions(['ToggleLoginModal']),
    onLoginSucceed() {
      this.$emit('success')
    },
    handleCancelLogin() {
      this.ToggleLoginModal(false)
    }
  }
}
</script>
<style scoped></style>
