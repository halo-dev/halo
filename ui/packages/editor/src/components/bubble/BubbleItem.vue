<script lang="ts" setup>
import { VDropdown } from "@halo-dev/components";
import { shallowRef, type Component } from "vue";
import { useHaloKeyboardShortcut } from "@/composables/use-halo-keyboard-shortcut";
import type { Editor } from "@/tiptap";
import type { BubbleItemComponentProps } from "@/types";
import KeyboardShortcutTooltip from "../keyboard-shortcuts/KeyboardShortcutTooltip.vue";
import BubbleButton from "./BubbleButton.vue";

const props = withDefaults(defineProps<BubbleItemComponentProps>(), {
  isActive: () => false,
  visible: () => true,
});

const componentRef = shallowRef<Component | void>();
const shortcut = useHaloKeyboardShortcut(props.editor, () => props.shortcutId);
const handleBubbleItemClick = (editor: Editor) => {
  if (!props.action) {
    return;
  }
  const callback = props.action?.({ editor });
  if (typeof callback === "object") {
    componentRef.value = callback;
  }
};
</script>

<template>
  <VDropdown
    v-if="visible({ editor })"
    class="inline-flex"
    :triggers="[]"
    :auto-hide="true"
    :shown="!!componentRef"
    :distance="10"
    @hide="componentRef = undefined"
  >
    <KeyboardShortcutTooltip
      v-slot="tooltipProps"
      :title="title"
      :shortcut="shortcut?.keys[0]"
    >
      <BubbleButton
        :title="tooltipProps.ariaLabel"
        :is-active="isActive({ editor })"
        custom-tooltip
        @click="handleBubbleItemClick(editor)"
      >
        <template #icon>
          <component :is="icon" :style="iconStyle" class="size-5" />
        </template>
      </BubbleButton>
    </KeyboardShortcutTooltip>
    <template #popper>
      <KeepAlive>
        <component :is="componentRef" v-bind="props"></component>
      </KeepAlive>
    </template>
  </VDropdown>
</template>
