<script lang="ts" setup>
import { i18n } from "@/locales";
import type { Attachment } from "@halo-dev/api-client";
import { Toast, VButton, VDropdown, VDropdownItem } from "@halo-dev/components";
import {
  utils,
  type AttachmentLike,
  type AttachmentSimple,
} from "@halo-dev/ui-shared";
import { useFileDialog } from "@vueuse/core";
import type { AxiosRequestConfig } from "axios";
import { ref } from "vue";
import Input from "../base/Input.vue";

const props = defineProps<{
  originalLink?: string;
  accept?: string;
  upload: (file: File, options?: AxiosRequestConfig) => Promise<Attachment>;
}>();

const emit = defineEmits<{
  (event: "change", attachment?: AttachmentSimple): void;
}>();

// Attachment Selector Modal
const attachmentSelectorModalVisible = ref(false);

function onAttachmentSelect(attachments: AttachmentLike[]) {
  if (!attachments.length) {
    return;
  }
  const attachment = attachments[0];
  const attachmentSimple = utils.attachment.convertToSimple(attachment);
  emit("change", attachmentSimple);
  attachmentSelectorModalVisible.value = false;
}

// Upload
const { onChange: onInputChange, open } = useFileDialog({
  accept: props.accept,
  multiple: false,
});

onInputChange((files) => {
  const file = files?.[0];
  if (!file) {
    return;
  }
  props
    .upload?.(file)
    .then((attachment) => {
      emit("change", utils.attachment.convertToSimple(attachment));
    })
    .catch((e: Error) => {
      Toast.error(
        `${i18n.global.t("editor.extensions.upload.error")} - ${e.message}`
      );
    });
});

function onExternalLinkChange(value?: string | number) {
  if (value && typeof value === "string") {
    emit("change", utils.attachment.convertToSimple(value));
  }
}
</script>
<template>
  <VDropdown class="inline-flex">
    <VButton size="sm" type="secondary">
      {{ i18n.global.t("editor.extensions.upload.operations.replace.button") }}
    </VButton>
    <template #popper>
      <VDropdownItem
        v-if="utils.permission.has(['uc:attachments:manage'])"
        @click="open()"
      >
        {{ i18n.global.t("editor.common.button.upload") }}
      </VDropdownItem>
      <VDropdownItem
        v-if="
          utils.permission.has([
            'system:attachments:view',
            'uc:attachments:manage',
          ])
        "
        @click="attachmentSelectorModalVisible = true"
      >
        {{ i18n.global.t("editor.extensions.upload.attachment.title") }}
      </VDropdownItem>
      <VDropdown>
        <VDropdownItem>
          {{ i18n.global.t("editor.extensions.upload.permalink.title") }}
        </VDropdownItem>
        <template #popper>
          <div class="w-80">
            <Input
              :model-value="originalLink"
              :placeholder="
                i18n.global.t('editor.extensions.upload.permalink.placeholder')
              "
              @update:model-value="onExternalLinkChange"
            />
          </div>
        </template>
      </VDropdown>
    </template>
  </VDropdown>

  <AttachmentSelectorModal
    v-if="attachmentSelectorModalVisible"
    :accepts="[accept].filter(Boolean)"
    :min="1"
    :max="1"
    @select="onAttachmentSelect"
    @close="attachmentSelectorModalVisible = false"
  />
</template>
