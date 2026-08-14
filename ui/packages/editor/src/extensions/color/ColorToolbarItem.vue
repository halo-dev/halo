<script lang="ts" setup>
import { onMounted, onUnmounted, shallowRef } from "vue";
import { ToolbarItem } from "@/components";
import ColorPickerDropdown from "@/components/common/ColorPickerDropdown.vue";
import { i18n } from "@/locales";
import type { ToolbarItemComponentProps } from "@/types";

const props = defineProps<ToolbarItemComponentProps>();
const dropdownShown = shallowRef(false);

const storage = props.editor.storage.color;

function openColorPicker() {
  dropdownShown.value = true;
}

onMounted(() => {
  storage.openToolbarColorPicker = openColorPicker;
});

onUnmounted(() => {
  if (storage.openToolbarColorPicker === openColorPicker) {
    storage.openToolbarColorPicker = undefined;
  }
});

function handleSetColor(color?: string) {
  if (!color) {
    return;
  }
  props.editor?.chain().focus().setColor(color).run();
}

function handleUnsetColor() {
  props.editor?.chain().focus().unsetColor().run();
}
</script>

<template>
  <ColorPickerDropdown
    v-model:shown="dropdownShown"
    @update:model-value="handleSetColor"
  >
    <ToolbarItem v-bind="props" />
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
