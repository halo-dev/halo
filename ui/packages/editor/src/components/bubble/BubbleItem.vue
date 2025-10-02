<script lang="ts" setup>
import type { Editor } from "@/tiptap/vue-3";
import { Dropdown as VDropdown, vTooltip } from "floating-vue";
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
    componentRef.value = callback;
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
    @hide="componentRef = undefined"
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
      class="rounded-md p-2 text-lg text-gray-600 hover:bg-gray-100"
      @click="handleBubbleItemClick(editor)"
    >
      <component :is="icon" :style="iconStyle" class="h-5 w-5" />
    </button>
    <template #popper>
      <div
        class="relative max-h-72 w-96 overflow-hidden overflow-y-auto rounded-md bg-white p-1 shadow"
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
