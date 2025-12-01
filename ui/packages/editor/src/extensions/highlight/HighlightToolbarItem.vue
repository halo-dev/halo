<script lang="ts" setup>
import { ToolbarItem } from "@/components";
import ColorPickerDropdown from "@/components/common/ColorPickerDropdown.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { Component } from "vue";
import MingcuteCloseLine from "~icons/mingcute/close-line";
import { ExtensionHighlight } from ".";

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
  if (props.editor?.isActive(ExtensionHighlight.name)) {
    props.editor?.chain().focus().unsetHighlight().run();
  }

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
          class="flex cursor-pointer items-center gap-2 rounded p-1 hover:bg-gray-100"
          @click="handleUnsetColor"
        >
          <div class="inline-flex items-center gap-2">
            <MingcuteCloseLine />
            <span class="text-xs text-gray-600">
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
