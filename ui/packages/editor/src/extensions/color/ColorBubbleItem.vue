<script lang="ts" setup>
import { BubbleButton } from "@/components";
import ColorPickerDropdown from "@/components/common/ColorPickerDropdown.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";

const props = defineProps<BubbleItemComponentProps>();

function handleSetColor(color?: string) {
  if (!color) {
    return;
  }
  // Applying a color must not refocus the editor: the picker stays open while
  // the user drags, and stealing focus moves the caret and the floating panels.
  props.editor?.chain().setColor(color).run();
}

function handleUnsetColor() {
  props.editor?.chain().focus().unsetColor().run();
}
</script>

<template>
  <ColorPickerDropdown @update:model-value="handleSetColor">
    <BubbleButton
      v-if="!visible || visible({ editor })"
      :title="i18n.global.t('editor.common.color')"
      :is-active="isActive?.({ editor })"
    >
      <template #icon>
        <component :is="icon" :style="iconStyle" />
      </template>
    </BubbleButton>
    <template #prefix>
      <div class="p-1">
        <div
          class="flex cursor-pointer items-center gap-2 rounded p-1 hover:bg-gray-100"
          @click="handleUnsetColor"
        >
          <div
            class="size-5 cursor-pointer rounded-sm bg-black ring-gray-300 ring-offset-1 hover:ring-1"
          ></div>
          <span class="text-xs text-gray-600">
            {{ i18n.global.t("editor.common.button.restore_default") }}
          </span>
        </div>
      </div>
    </template>
  </ColorPickerDropdown>
</template>
