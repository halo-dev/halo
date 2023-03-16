<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import type { AuthProvider } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VAvatar,
  VEntity,
  VEntityField,
  VStatusDot,
  VSwitch,
} from "@halo-dev/components";
import cloneDeep from "lodash.clonedeep";

const props = defineProps<{
  authProvider: AuthProvider;
}>();

const emit = defineEmits<{
  (event: "reload"): void;
}>();

const handleChangeStatus = async () => {
  const authProviderToUpdate = cloneDeep(props.authProvider);

  Dialog.info({
    title: `确定要${
      authProviderToUpdate.spec.enabled ? "停用" : "启用"
    }该身份认证方式吗？`,
    onConfirm: async () => {
      try {
        authProviderToUpdate.spec.enabled = !authProviderToUpdate.spec.enabled;
        await apiClient.extension.authProvider.updateauthHaloRunV1alpha1AuthProvider(
          {
            name: authProviderToUpdate.metadata.name,
            authProvider: authProviderToUpdate,
          }
        );

        Toast.success(
          `${authProviderToUpdate.spec.enabled ? "启用" : "停用"}成功`
        );

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
            :alt="authProvider.spec.displayName"
            :src="authProvider.spec.logo"
            size="md"
          ></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        :title="authProvider.spec.displayName"
        :description="authProvider.spec.description"
        :route="{
          name: 'AuthProviderDetail',
          params: { name: authProvider.metadata.name },
        }"
        width="27rem"
      >
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="authProvider.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot v-tooltip="`删除中`" state="warning" animate />
        </template>
      </VEntityField>
      <VEntityField v-permission="['system:plugins:manage']">
        <template #description>
          <div class="flex items-center">
            <VSwitch
              :model-value="authProvider.spec.enabled"
              @click="handleChangeStatus"
            />
          </div>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>
