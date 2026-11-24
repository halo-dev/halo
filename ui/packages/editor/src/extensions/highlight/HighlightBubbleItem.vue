<script lang="ts" setup>
import MingcuteCloseLine from "~icons/mingcute/close-line";
import { BubbleButton } from "@/components";
import ColorPickerDropdown from "@/components/common/ColorPickerDropdown.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { ExtensionHighlight } from ".";

const props = defineProps<BubbleItemComponentProps>();

function handleSetColor(color?: string) {
  // Applying a highlight must not refocus the editor: the picker stays open
  // while the user drags, and stealing focus moves the caret and the floating
  // panels.
  if (props.editor.isActive(ExtensionHighlight.name)) {
    props.editor.chain().unsetHighlight().run();
  }

  props.editor
    ?.chain()
    .setHighlight(color ? { color } : undefined)
    .run();
}

function handleUnsetColor() {
  props.editor?.chain().focus().unsetHighlight().run();
}
</script>

<template>
  <ColorPickerDropdown @update:model-value="handleSetColor">
    <BubbleButton
      v-if="!visible || visible({ editor })"
      :title="i18n.global.t('editor.common.highlight')"
      :is-active="isActive?.({ editor })"
    >
      <template #icon>
        <component :is="icon" :style="iconStyle" />
      </template>
    </BubbleButton>
    <template #prefix>
      <div class="p-1">
        <div
          class="flex cursor-pointer items-center gap-2 rounded p-1 text-xs text-gray-600 hover:bg-gray-100 hover:text-gray-900"
          @click="handleUnsetColor"
        >
          <div class="inline-flex items-center gap-2">
            <MingcuteCloseLine />
            <span>
              {{ i18n.global.t("editor.extensions.highlight.unset") }}
            </span>
          </div>
        </div>
      </div>
      <div class="p-1">
        <div
          class="flex cursor-pointer items-center gap-2 rounded p-1 hover:bg-gray-100"
          @click="handleSetColor()"
        >
          <div
            class="size-5 cursor-pointer rounded-sm ring-gray-300 ring-offset-1 hover:ring-1"
            :style="{ 'background-color': '#fff8c5' }"
          ></div>
          <span class="text-xs text-gray-600">
            {{ i18n.global.t("editor.common.button.restore_default") }}
          </span>
        </div>
      </div>
    </template>
  </ColorPickerDropdown>
</template>
