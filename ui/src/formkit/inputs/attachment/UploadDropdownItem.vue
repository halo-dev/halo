<script lang="ts" setup>
import { consoleApiClient, type Attachment } from "@halo-dev/api-client";
import { VDropdownItem } from "@halo-dev/components";
import { useFileDialog } from "@vueuse/core";

const props = defineProps<{
  multiple: boolean;
  accepts: string[];
}>();

const emit = defineEmits<{
  (event: "selected", attachments: Attachment[]): void;
}>();

const { onChange: onFileInputChange, open: openFileInputDialog } =
  useFileDialog({
    accept: props.accepts.join(", "),
    multiple: props.multiple,
  });

onFileInputChange(async (files) => {
  if (!files?.length) {
    return;
  }

  const attachments: Attachment[] = [];
  for (const file of files) {
    const { data } = await consoleApiClient.storage.attachment.uploadAttachment(
      {
        // TODO: use policy name from context
        policyName: "default-policy",
        file: file,
      }
    );
    attachments.push(data);
  }

  emit("selected", attachments);
});
</script>
<template>
  <VDropdownItem @click="openFileInputDialog()">
    {{ $t("core.common.buttons.upload") }}
  </VDropdownItem>
</template>
