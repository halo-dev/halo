<script lang="ts" setup>
import { vTooltip, Dropdown as VDropdown } from "floating-vue";
import type { Editor } from "@/tiptap/vue-3";
import { ref, type Component } from "vue";

const props = withDefaults(
  defineProps<{
    editor: Editor;
    isActive?: ({ editor }: { editor: Editor }) => boolean;
    visible?: ({ editor }: { editor: Editor }) => boolean;
    icon?: Component;
    iconStyle?: string;
    title?: string;
    action?: ({ editor }: { editor: Editor }) => Component | void;
  }>(),
  {
    isActive: () => false,
    visible: () => true,
    title: undefined,
    action: undefined,
    icon: undefined,
    iconStyle: undefined,
  }
);

const componentRef = ref<Component | void>();
const handleBubbleItemClick = (editor: Editor) => {
  if (!props.action) {
    return;
  }
  const callback = props.action?.({ editor });
  if (typeof callback === "object") {
    if (componentRef.value) {
      componentRef.value = undefined;
    } else {
      componentRef.value = callback;
    }
  }
};
</script>

<template>
  <VDropdown
    class="inline-flex"
    :triggers="[]"
    :auto-hide="true"
    :shown="!!componentRef"
    :distance="10"
  >
    <button
      v-if="visible({ editor })"
      v-tooltip="{
        content: title,
        distance: 8,
        delay: {
          show: 0,
        },
      }"
      :class="{ 'bg-gray-200 !text-black': isActive({ editor }) }"
      :title="title"
      class="text-gray-600 text-lg hover:bg-gray-100 p-2 rounded-md"
      @click="handleBubbleItemClick(editor)"
    >
      <component :is="icon" :style="iconStyle" class="w-5 h-5" />
    </button>
    <template #popper>
      <div
        class="relative rounded-md bg-white overflow-hidden drop-shadow w-96 p-1 max-h-72 overflow-y-auto"
      >
        <KeepAlive>
          <component :is="componentRef" v-bind="props"></component>
        </KeepAlive>
      </div>
    </template>
  </VDropdown>
</template>
<style>
.v-popper__popper.v-popper__popper--show-from .v-popper__wrapper {
  transform: scale(0.9);
}

.v-popper__popper.v-popper__popper--show-to .v-popper__wrapper {
  transform: none;
  transition: transform 0.1s;
}
</style>
