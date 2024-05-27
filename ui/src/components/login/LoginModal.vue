<script lang="ts" setup>
import { Toast, VModal } from "@halo-dev/components";
import LoginForm from "@/components/login/LoginForm.vue";
import { useUserStore } from "@/stores/user";
import { useI18n } from "vue-i18n";
import SocialAuthProviders from "./SocialAuthProviders.vue";
import { ref } from "vue";

const userStore = useUserStore();
const { t } = useI18n();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const onLoginSucceed = () => {
  modal.value?.close();
  Toast.success(t("core.login.operations.submit.toast_success"));
};

function onClose() {
  userStore.loginModalVisible = false;
}
</script>

<template>
  <VModal
    v-if="userStore.loginModalVisible"
    ref="modal"
    :mount-to-body="true"
    :width="400"
    :centered="true"
    :title="$t('core.login.modal.title')"
    @close="onClose"
  >
    <LoginForm v-if="userStore.loginModalVisible" @succeed="onLoginSucceed" />
    <SocialAuthProviders />
  </VModal>
</template>
