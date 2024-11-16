<script lang="ts" setup>
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import type { Attachment } from "@halo-dev/api-client";
import {
  VEntity,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import prettyBytes from "pretty-bytes";
import { computed, toRefs } from "vue";
import { useFetchAttachmentPolicy } from "../../../composables/use-attachment-policy";

const { currentUserHasPermission } = usePermission();

const props = withDefaults(
  defineProps<{
    attachment: Attachment;
    isSelected?: boolean;
  }>(),
  { isSelected: false }
);

const { attachment } = toRefs(props);

const { policies } = useFetchAttachmentPolicy();

const emit = defineEmits<{
  (event: "select", attachment?: Attachment): void;
  (event: "open-detail", attachment: Attachment): void;
}>();

const policyDisplayName = computed(() => {
  const policy = policies.value?.find(
    (p) => p.metadata.name === props.attachment.spec.policyName
  );
  return policy?.spec.displayName;
});
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template
      v-if="currentUserHasPermission(['system:attachments:manage'])"
      #checkbox
    >
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
      <VEntityField :description="policyDisplayName" />
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
