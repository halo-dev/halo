<script lang="ts" setup>
import StatusDotField from "@/components/entity-fields/StatusDotField.vue";
import { ucApiClient } from "@halo-dev/api-client";
import {
  VButton,
  VEntity,
  VEntityField,
  VLoading,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import RiShieldKeyholeLine from "~icons/ri/shield-keyhole-line";
import TotpConfigureModal from "./components/TotpConfigureModal.vue";
import TotpDeletionModal from "./components/TotpDeletionModal.vue";
import TwoFactorDisableModal from "./components/TwoFactorDisableModal.vue";
import TwoFactorEnableModal from "./components/TwoFactorEnableModal.vue";

const { data: settings, isLoading } = useQuery({
  queryKey: ["two-factor-settings"],
  queryFn: async () => {
    const { data } =
      await ucApiClient.security.twoFactor.getTwoFactorAuthenticationSettings();
    return data;
  },
});

const twoFactorEnableModalVisible = ref(false);
const twoFactorDisableModalVisible = ref(false);

function onEnabledChange(payload: Event) {
  const target = payload.target as HTMLInputElement;
  // Do not change the checked state of the checkbox
  target.checked = !target.checked;
  if (settings.value?.enabled) {
    twoFactorDisableModalVisible.value = true;
  } else {
    twoFactorEnableModalVisible.value = true;
  }
}

const totpConfigureModalVisible = ref(false);
const totpDeletionModalVisible = ref(false);
</script>

<template>
  <div class="my-5">
    <label class="flex cursor-pointer items-center space-x-2">
      <input
        type="checkbox"
        :checked="settings?.enabled"
        @change="onEnabledChange"
      />
      <span class="text-sm font-medium text-gray-700">启用两步验证</span>
    </label>
  </div>

  <VLoading v-if="isLoading" />

  <Transition v-else appear name="fade">
    <ul
      class="box-border h-full w-full divide-y divide-gray-100 overflow-hidden rounded-base border"
      role="list"
    >
      <li class="bg-gray-50 px-4 py-3">
        <span class="text-sm font-semibold text-gray-900">验证方式</span>
      </li>
      <li>
        <VEntity>
          <template #start>
            <VEntityField>
              <template #description>
                <RiShieldKeyholeLine />
              </template>
            </VEntityField>
            <VEntityField
              title="TOTP"
              description="使用 TOTP 应用程序配置两步验证"
            />
          </template>
          <template #end>
            <StatusDotField
              :state="settings?.totpConfigured ? 'success' : 'default'"
              :text="settings?.totpConfigured ? '已配置' : '未配置'"
            ></StatusDotField>
            <VEntityField>
              <template #description>
                <VSpace>
                  <VButton size="sm" @click="totpConfigureModalVisible = true">
                    {{ settings?.totpConfigured ? "重新配置" : "配置" }}
                  </VButton>
                  <VButton
                    v-if="settings?.totpConfigured"
                    size="sm"
                    type="danger"
                    @click="totpDeletionModalVisible = true"
                  >
                    停用
                  </VButton>
                </VSpace>
              </template>
            </VEntityField>
          </template>
        </VEntity>
      </li>
    </ul>
  </Transition>

  <TotpConfigureModal
    v-if="totpConfigureModalVisible"
    @close="totpConfigureModalVisible = false"
  />

  <TotpDeletionModal
    v-if="totpDeletionModalVisible"
    @close="totpDeletionModalVisible = false"
  />

  <TwoFactorEnableModal
    v-if="twoFactorEnableModalVisible"
    @close="twoFactorEnableModalVisible = false"
  />

  <TwoFactorDisableModal
    v-if="twoFactorDisableModalVisible"
    @close="twoFactorDisableModalVisible = false"
  />
</template>
