<script lang="ts" setup>
import { formatDatetime } from "@/utils/date";
import type { Attachment } from "@halo-dev/api-client";
import {
  VEntity,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import prettyBytes from "pretty-bytes";
import { toRefs } from "vue";

const props = withDefaults(
  defineProps<{
    attachment: Attachment;
    isSelected?: boolean;
  }>(),
  { isSelected: false }
);

const { attachment } = toRefs(props);

const emit = defineEmits<{
  (event: "select", attachment?: Attachment): void;
  (event: "open-detail", attachment: Attachment): void;
}>();
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template #checkbox>
      <slot name="checkbox" />
    </template>
    <template #start>
      <VEntityField>
        <template #description>
          <div class="h-10 w-10 rounded border bg-white p-1 hover:shadow-sm">
            <AttachmentFileTypeIcon
              :display-ext="false"
              :file-name="attachment.spec.displayName"
              :width="8"
              :height="8"
            />
          </div>
        </template>
      </VEntityField>
      <VEntityField
        :title="attachment.spec.displayName"
        @click="emit('open-detail', attachment)"
      >
        <template #description>
          <VSpace>
            <span class="text-xs text-gray-500">
              {{ attachment.spec.mediaType }}
            </span>
            <span class="text-xs text-gray-500">
              {{ prettyBytes(attachment.spec.size || 0) }}
            </span>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField :description="attachment.spec.ownerName" />
      <VEntityField v-if="attachment.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(attachment.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>
