<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import type { ListedAuthProvider } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VAvatar,
  VEntity,
  VEntityField,
  VSwitch,
} from "@halo-dev/components";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps<{
  authProvider: ListedAuthProvider;
}>();

const emit = defineEmits<{
  (event: "reload"): void;
}>();

const handleChangeStatus = async () => {
  Dialog.info({
    title: props.authProvider.enabled
      ? t("core.identity_authentication.operations.disable.title")
      : t("core.identity_authentication.operations.enable.title"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        if (props.authProvider.enabled) {
          await apiClient.authProvider.disableAuthProvider({
            name: props.authProvider.name,
          });

          Toast.success(t("core.common.toast.inactive_success"));
        } else {
          await apiClient.authProvider.enableAuthProvider({
            name: props.authProvider.name,
          });
          Toast.success(t("core.common.toast.active_success"));
        }

        emit("reload");
      } catch (e) {
        console.error(e);
      }
    },
  });
};
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField>
        <template #description>
          <VAvatar
            :alt="authProvider.displayName"
            :src="authProvider.logo"
            size="md"
          ></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        :title="authProvider.displayName"
        :description="authProvider.description"
        :route="{
          name: 'AuthProviderDetail',
          params: { name: authProvider.name },
        }"
        width="27rem"
      >
      </VEntityField>
    </template>
    <template #end>
      <VEntityField>
        <template #description>
          <div class="flex items-center">
            <VSwitch
              v-tooltip="{
                disabled: !authProvider.privileged,
                content: $t(
                  'core.identity_authentication.operations.disable_privileged.tooltip'
                ),
              }"
              :model-value="authProvider.enabled"
              :disabled="authProvider.privileged"
              @click="handleChangeStatus"
            />
          </div>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>
