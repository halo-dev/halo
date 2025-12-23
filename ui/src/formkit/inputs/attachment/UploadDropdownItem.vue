<script lang="ts" setup>
import { ucApiClient, type Attachment } from "@halo-dev/api-client";
import { VDropdownItem } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
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
    if (utils.permission.has(["system:attachments:manage"])) {
      // fixme: we need unified upload attachment endpoint,
      // See https://github.com/halo-dev/halo/issues/7973
      const { data } =
        await ucApiClient.storage.attachment.createAttachmentForPost({
          file: file,
          waitForPermalink: true,
        });
      attachments.push(data);
    } else if (utils.permission.has(["uc:attachments:manage"])) {
      const { data } = await ucApiClient.storage.attachment.uploadUcAttachment({
        file: file,
      });
      attachments.push(data);
    } else {
      throw new Error("No permission to upload attachment");
    }
  }

  emit("selected", attachments);
});
</script>
<template>
  <VDropdownItem @click="openFileInputDialog()">
    {{ $t("core.common.buttons.upload") }}
  </VDropdownItem>
</template>
