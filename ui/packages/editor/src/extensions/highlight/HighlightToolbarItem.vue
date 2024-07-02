<script lang="ts" setup>
import { ToolbarItem } from "@/components";
import ColorPickerDropdown from "@/components/common/ColorPickerDropdown.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import type { Component } from "vue";
import MdiFormatColorMarkerCancel from "~icons/mdi/format-color-marker-cancel";

const props = withDefaults(
  defineProps<{
    editor?: Editor;
    isActive?: boolean;
    disabled?: boolean;
    title?: string;
    action?: () => void;
    icon?: Component;
  }>(),
  {
    editor: undefined,
    isActive: false,
    disabled: false,
    title: undefined,
    action: undefined,
    icon: undefined,
  }
);

function handleSetColor(color?: string) {
  props.editor
    ?.chain()
    .focus()
    .setHighlight(color ? { color } : undefined)
    .run();
}

function handleUnsetColor() {
  props.editor?.chain().focus().unsetHighlight().run();
}
</script>

<template>
  <ColorPickerDropdown @update:model-value="handleSetColor">
    <ToolbarItem v-bind="props" @click="handleSetColor()" />
    <template #prefix>
      <div class="p-1">
        <div
          class="flex items-center gap-2 rounded cursor-pointer hover:bg-gray-100 p-1"
          @click="handleUnsetColor"
        >
          <div class="inline-flex items-center gap-2">
            <MdiFormatColorMarkerCancel />
            <span class="text-xs text-gray-600">
              {{ i18n.global.t("editor.extensions.highlight.unset") }}
            </span>
          </div>
        </div>
      </div>
      <div class="p-1">
        <div
          class="flex items-center gap-2 rounded cursor-pointer hover:bg-gray-100 p-1"
          @click="handleSetColor()"
        >
          <div
            class="h-5 w-5 rounded-sm cursor-pointer hover:ring-1 ring-offset-1 ring-gray-300"
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
