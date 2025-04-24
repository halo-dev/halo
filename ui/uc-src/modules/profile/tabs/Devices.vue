<script lang="ts" setup>
import { ucApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VButton,
  VEntityContainer,
  VLoading,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import DeviceListItem from "./components/DeviceListItem.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

const { data, isLoading } = useQuery({
  queryKey: ["uc:devices"],
  queryFn: async () => {
    const { data } = await ucApiClient.security.device.listDevices();
    return data;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.some(
      (device) => !!device.device.metadata.deletionTimestamp
    );

    return hasDeletingData ? 1000 : false;
  },
});

const otherDevices = computed(() => {
  if (!data.value) {
    return [];
  }

  return data.value.filter((device) => !device.currentDevice);
});

function handleRevokeOtherDevices() {
  Dialog.warning({
    title: t("core.uc_profile.device.operations.revoke_others.title"),
    description: t(
      "core.uc_profile.device.operations.revoke_others.description"
    ),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      for (const device of otherDevices.value) {
        await ucApiClient.security.device.revokeDevice({
          deviceId: device.device.metadata.name,
        });
      }

      queryClient.invalidateQueries({ queryKey: ["uc:devices"] });

      Toast.success(
        t("core.uc_profile.device.operations.revoke_others.toast_success")
      );
    },
  });
}
</script>

<template>
  <VLoading v-if="isLoading" />

  <TransitionGroup v-else appear name="fade">
    <div class="overflow-hidden rounded-base border">
      <VEntityContainer>
        <DeviceListItem
          v-for="device in data"
          :key="device?.device.spec.sessionId"
          :device="device"
        />
      </VEntityContainer>
    </div>
    <div v-if="otherDevices.length" class="mt-5">
      <VButton @click="handleRevokeOtherDevices">
        {{ $t("core.uc_profile.device.operations.revoke_others.title") }}
      </VButton>
    </div>
  </TransitionGroup>
</template>
