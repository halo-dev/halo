<script lang="ts" setup>
import type { Editor } from "@/tiptap";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown, vTooltip } from "@halo-dev/components";
import { ref, type Component } from "vue";

const props = withDefaults(defineProps<BubbleItemComponentProps>(), {
  isActive: () => false,
  visible: () => true,
});

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
    v-if="visible({ editor })"
    class="inline-flex"
    :triggers="[]"
    :auto-hide="true"
    :shown="!!componentRef"
    :distance="10"
    @hide="componentRef = undefined"
  >
    <button
      v-tooltip="{
        content: title,
        distance: 8,
        delay: {
          show: 0,
        },
      }"
      :class="{ 'bg-gray-200 !text-black': isActive({ editor }) }"
      :title="title"
      class="inline-flex size-8 items-center justify-center rounded-md text-lg text-gray-600 hover:bg-gray-100 active:!bg-gray-200"
      @click="handleBubbleItemClick(editor)"
    >
      <component :is="icon" :style="iconStyle" class="size-5" />
    </button>
    <template #popper>
      <div
        class="relative max-h-72 w-96 overflow-hidden overflow-y-auto bg-white"
      >
        <KeepAlive>
          <component :is="componentRef" v-bind="props"></component>
        </KeepAlive>
      </div>
    </template>
  </VDropdown>
</template>
