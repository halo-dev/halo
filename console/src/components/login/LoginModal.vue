<script lang="ts" setup>
import { Toast, VModal } from "@halo-dev/components";
import LoginForm from "@/components/login/LoginForm.vue";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

const onVisibleChange = (visible: boolean) => {
  userStore.loginModalVisible = visible;
};

const onLoginSucceed = () => {
  onVisibleChange(false);
  Toast.success("登录成功");
};
</script>

<template>
  <VModal
    :visible="userStore.loginModalVisible"
    :mount-to-body="true"
    :width="400"
    :centered="true"
    title="重新登录"
    @update:visible="onVisibleChange"
  >
    <LoginForm v-if="userStore.loginModalVisible" @succeed="onLoginSucceed" />
  </VModal>
</template>
