<script lang="ts" setup>
import {
  Toast,
  VButton,
  VDropdown,
  VDropdownItem,
  VPopover,
} from "@halo-dev/components";
import {
  utils,
  type AttachmentLike,
  type AttachmentSimple,
} from "@halo-dev/ui-shared";
import { useFileDialog } from "@vueuse/core";
import { ref } from "vue";
import { i18n } from "@/locales";
import type { UploadFile } from "@/utils/upload";
import Input from "../base/Input.vue";

const props = defineProps<{
  originalLink?: string;
  accept?: string;
  upload: UploadFile;
}>();

const emit = defineEmits<{
  (event: "change", attachment?: AttachmentSimple): void;
}>();

// Attachment Selector Modal
const attachmentSelectorModalVisible = ref(false);

// The permalink panel is portaled outside the menu, so keep the menu open while
// the user interacts with it.
const permalinkOpen = ref(false);

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
      if (!attachment) {
        return;
      }
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
  <VDropdown class="inline-flex" :auto-hide="!permalinkOpen">
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
      <VPopover v-model:open="permalinkOpen">
        <!--
          A `VDropdownItem` always closes the parent dropdown when clicked. Use a
          plain menu item so the permalink panel can open without closing the menu.
        -->
        <div
          class="dropdown-item-wrapper dropdown-item-wrapper--default"
          role="menuitem"
          tabindex="-1"
        >
          <div class="flex items-center gap-3">
            {{ i18n.global.t("editor.extensions.upload.permalink.title") }}
          </div>
        </div>
        <template #content>
          <div class="w-80">
            <Input
              auto-focus
              :model-value="originalLink"
              :placeholder="
                i18n.global.t('editor.extensions.upload.permalink.placeholder')
              "
              @update:model-value="onExternalLinkChange"
            />
          </div>
        </template>
      </VPopover>
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
