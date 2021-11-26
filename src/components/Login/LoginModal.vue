<template>
  <div>
    <a-modal
      v-model="loginModal"
      :footer="null"
      :maskClosable="false"
      :width="320"
      title="重新登录"
      @cancel="handleCancelLogin"
    >
      <LoginForm @success="onLoginSucceed" />
    </a-modal>
  </div>
</template>
<script>
import LoginForm from './LoginForm'
import { mapActions, mapGetters } from 'vuex'

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
