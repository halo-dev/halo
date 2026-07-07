<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import { computed, watch, type CSSProperties, type PropType } from "vue";
import type { ExtensionUploadStorage } from "@/extensions/upload";
import { i18n } from "@/locales";
import { EditorContent, VueEditor } from "@/tiptap";
import EditorBubbleMenu from "./bubble/EditorBubbleMenu.vue";
import EditorDragHandle from "./drag/EditorDragHandle.vue";
import EditorHeader from "./EditorHeader.vue";

const props = defineProps({
  editor: {
    type: Object as PropType<VueEditor>,
    required: true,
  },
  contentStyles: {
    type: Object as PropType<CSSProperties>,
    required: false,
    default: () => ({}),
  },
  locale: {
    type: String as PropType<"zh-CN" | "en" | "zh" | "en-US">,
    required: false,
    default: "zh-CN",
  },
});

watch(
  () => props.locale,
  () => {
    i18n.global.locale.value = props.locale;
  },
  {
    immediate: true,
  }
);

const uploadStorage = computed(() => {
  return (props.editor.storage as { upload?: ExtensionUploadStorage }).upload;
});
const externalAssetPromptVisible = computed(() => {
  return uploadStorage.value?.externalAssetPrompt.visible.value || false;
});
const externalAssetPromptCount = computed(() => {
  return uploadStorage.value?.externalAssetPrompt.count.value || 0;
});
const transferringExternalAssets = computed(() => {
  return uploadStorage.value?.externalAssetPrompt.transferring.value || false;
});

function handleTransferExternalAssets() {
  void uploadStorage.value?.transferExternalAssets();
}

function handleDismissExternalAssetsPrompt() {
  uploadStorage.value?.dismissExternalAssetsPrompt();
}
</script>
<template>
  <div v-if="editor" class="halo-rich-text-editor">
    <editor-bubble-menu :editor="editor" />
    <editor-drag-handle :editor="editor" />
    <editor-header :editor="editor" />
    <div
      v-if="externalAssetPromptVisible"
      class="mx-4 mb-2 flex items-center justify-between gap-3 rounded-base border border-primary/20 bg-primary/5 px-3 py-2 text-sm text-gray-700"
    >
      <span>
        {{
          i18n.global.t(
            "editor.extensions.upload.operations.transfer_in_batch.description",
            { count: externalAssetPromptCount }
          )
        }}
      </span>
      <div class="flex shrink-0 items-center gap-2">
        <VButton
          size="xs"
          type="secondary"
          :loading="transferringExternalAssets"
          @click="handleTransferExternalAssets"
        >
          {{
            i18n.global.t("editor.extensions.upload.operations.transfer.button")
          }}
        </VButton>
        <VButton size="xs" ghost @click="handleDismissExternalAssetsPrompt">
          {{ i18n.global.t("editor.common.button.cancel") }}
        </VButton>
      </div>
    </div>
    <div class="editor-entry">
      <div class="editor-main">
        <div v-if="$slots.content" class="editor-main-extra">
          <slot name="content" />
        </div>

        <editor-content
          :editor="editor"
          :style="contentStyles"
          class="editor-main-content markdown-body"
        />
      </div>
      <div v-if="$slots.extra" class="editor-entry-extra">
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>
