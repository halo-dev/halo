<script lang="ts" setup>
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import type { UserDevice } from "@halo-dev/api-client";
import {
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { computed, markRaw, ref, type Component, type Raw } from "vue";
import RiComputerLine from "~icons/ri/computer-line";
import RiSmartphoneLine from "~icons/ri/smartphone-line";
import RiTabletLine from "~icons/ri/tablet-line";
import { useUserAgent } from "../composables/use-user-agent";
import { useUserDevice } from "../composables/use-user-device";
import DeviceDetailModal from "./DeviceDetailModal.vue";

const props = withDefaults(
  defineProps<{
    device: UserDevice;
  }>(),
  {}
);

const { ua, os, browser } = useUserAgent(props.device.device.spec.userAgent);

const deviceIcons: Record<string, Raw<Component>> = {
  console: markRaw(RiComputerLine),
  mobile: markRaw(RiSmartphoneLine),
  tablet: markRaw(RiTabletLine),
};

const getDeviceIcon = computed(() => {
  const deviceType = ua.value.getDevice().type;
  const defaultIcon = deviceIcons["console"];

  if (!deviceType) {
    return defaultIcon;
  }

  return deviceIcons[deviceType] || defaultIcon;
});

const { handleRevoke } = useUserDevice(props.device);

const detailModal = ref(false);
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField>
        <template #description>
          <VStatusDot :state="device.active ? 'success' : 'default'" />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <component :is="getDeviceIcon" class="text-lg" />
        </template>
      </VEntityField>
      <VEntityField
        :title="os"
        :description="browser"
        class="cursor-pointer"
        @click="detailModal = true"
      >
        <template v-if="device.currentDevice" #extra>
          <VTag>
            {{ $t("core.uc_profile.device.list.fields.current") }}
          </VTag>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="device.device.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField :description="device.device.spec.ipAddress"></VEntityField>
      <VEntityField v-if="device.device.spec.lastAccessedTime">
        <template #description>
          <span
            v-tooltip="formatDatetime(device.device.spec.lastAccessedTime)"
            class="truncate text-xs tabular-nums text-gray-500"
          >
            {{
              $t("core.uc_profile.device.list.fields.last_accessed_time", {
                time: relativeTimeTo(device.device.spec.lastAccessedTime),
              })
            }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem @click="detailModal = true">
        {{ $t("core.common.buttons.detail") }}
      </VDropdownItem>
      <VDropdownDivider />
      <VDropdownItem type="danger" @click="handleRevoke">
        {{ $t("core.common.buttons.revoke") }}
      </VDropdownItem>
    </template>
  </VEntity>

  <DeviceDetailModal
    v-if="detailModal"
    :device="device"
    @close="detailModal = false"
  />
</template>
