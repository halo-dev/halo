<script lang="ts" setup>
import { VDropdownItem } from "@halo-dev/components";
import type { AttachmentLike } from "@halo-dev/ui-shared";
import { ref } from "vue";

defineProps<{
  multiple: boolean;
  accepts: string[];
}>();

const emit = defineEmits<{
  (event: "selected", attachments: AttachmentLike[]): void;
}>();

const selectorModalVisible = ref(false);

function onAttachmentsSelect(attachments: AttachmentLike[]) {
  if (!attachments.length) {
    return;
  }
  emit("selected", attachments);
}
</script>
<template>
  <VDropdownItem @click="selectorModalVisible = true">
    {{ $t("core.formkit.attachment.operations.select") }}
  </VDropdownItem>

  <AttachmentSelectorModal
    v-if="selectorModalVisible"
    :max="multiple ? undefined : 1"
    :accepts="accepts"
    @select="onAttachmentsSelect"
    @close="selectorModalVisible = false"
  />
</template>
