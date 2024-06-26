<script lang="ts" setup>
import { formatDatetime } from "@/utils/date";
import type { UserDevice } from "@halo-dev/api-client";
import {
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { ref } from "vue";
import { useUserAgent } from "../composables/use-user-agent";
import { useUserDevice } from "../composables/use-user-device";

const props = withDefaults(
  defineProps<{
    device: UserDevice;
  }>(),
  {}
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { os, browser } = useUserAgent(props.device.device.spec.userAgent);

const { handleRevoke } = useUserDevice(props.device);
</script>

<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :width="650"
    :title="$t('core.uc_profile.device.detail_modal.title')"
    @close="emit('close')"
  >
    <VDescription>
      <VDescriptionItem
        class="!px-4"
        :label="$t('core.uc_profile.device.detail_modal.fields.os')"
        :content="os"
      />
      <VDescriptionItem
        class="!px-4"
        :label="$t('core.uc_profile.device.detail_modal.fields.browser')"
        :content="browser"
      />
      <VDescriptionItem
        class="!px-4"
        label="IP"
        :content="device.device.spec.ipAddress"
      />
      <VDescriptionItem
        class="!px-4"
        :label="
          $t('core.uc_profile.device.detail_modal.fields.creation_timestamp')
        "
        :content="formatDatetime(device.device.metadata.creationTimestamp)"
      />
      <VDescriptionItem
        class="!px-4"
        :label="
          $t('core.uc_profile.device.detail_modal.fields.last_accessed_times')
        "
        :content="formatDatetime(device.device.spec.lastAccessedTime)"
      />
      <VDescriptionItem
        class="!px-4"
        :label="
          $t(
            'core.uc_profile.device.detail_modal.fields.last_authenticated_time'
          )
        "
        :content="formatDatetime(device.device.spec.lastAuthenticatedTime)"
      />
    </VDescription>
    <template #footer>
      <VSpace>
        <VButton type="danger" @click="handleRevoke">
          {{ $t("core.common.buttons.revoke") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
