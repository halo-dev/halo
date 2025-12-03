<script lang="ts" setup>
import type { Editor } from "@/tiptap";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { ref, type Component } from "vue";
import BubbleButton from "./BubbleButton.vue";

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
    <BubbleButton
      :title="title"
      :is-active="isActive({ editor })"
      @click="handleBubbleItemClick(editor)"
    >
      <template #icon>
        <component :is="icon" :style="iconStyle" class="size-5" />
      </template>
    </BubbleButton>
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
