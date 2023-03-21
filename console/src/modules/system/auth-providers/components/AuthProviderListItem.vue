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

const props = defineProps<{
  authProvider: ListedAuthProvider;
}>();

const emit = defineEmits<{
  (event: "reload"): void;
}>();

const handleChangeStatus = async () => {
  Dialog.info({
    title: `确定要${
      props.authProvider.enabled ? "停用" : "启用"
    }该身份认证方式吗？`,
    onConfirm: async () => {
      try {
        if (props.authProvider.enabled) {
          await apiClient.authProvider.disableAuthProvider({
            name: props.authProvider.name,
          });

          Toast.success("停用成功");
        } else {
          await apiClient.authProvider.enableAuthProvider({
            name: props.authProvider.name,
          });
          Toast.success("启用成功");
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
      <VEntityField v-permission="['system:plugins:manage']">
        <template #description>
          <div class="flex items-center">
            <VSwitch
              :model-value="authProvider.enabled"
              @click="handleChangeStatus"
            />
          </div>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>
