<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import type { ListedAuthProvider } from "@halo-dev/api-client";
import {
  Dialog,
  IconList,
  IconSettings,
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
    <template #prepend>
      <div
        class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
      >
        <IconList class="h-3.5 w-3.5" />
      </div>
    </template>
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
      <VEntityField>
        <template #description>
          <RouterLink
            class="cursor-pointer rounded p-1 text-gray-600 transition-all hover:text-blue-600 group-hover:bg-gray-200/60"
            :to="{
              name: 'AuthProviderDetail',
              params: { name: authProvider.name },
            }"
          >
            <IconSettings />
          </RouterLink>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>
