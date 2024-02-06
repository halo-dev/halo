<script lang="ts" setup>
import { Toast, VModal } from "@halo-dev/components";
import LoginForm from "@/components/login/LoginForm.vue";
import { useUserStore } from "@/stores/user";
import { useI18n } from "vue-i18n";
import SocialAuthProviders from "./SocialAuthProviders.vue";

const userStore = useUserStore();
const { t } = useI18n();

const onVisibleChange = (visible: boolean) => {
  userStore.loginModalVisible = visible;
};

const onLoginSucceed = () => {
  onVisibleChange(false);
  Toast.success(t("core.login.operations.submit.toast_success"));
};
</script>

<template>
  <VModal
    :visible="userStore.loginModalVisible"
    :mount-to-body="true"
    :width="400"
    :centered="true"
    :title="$t('core.login.modal.title')"
    @update:visible="onVisibleChange"
  >
    <LoginForm v-if="userStore.loginModalVisible" @succeed="onLoginSucceed" />
    <SocialAuthProviders />
  </VModal>
</template>
