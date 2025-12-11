<script lang="ts" setup>
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";

const props = defineProps<BubbleItemComponentProps>();

const visible = computed(() => {
  if (!props.visible) {
    return true;
  }
  return props.editor && props.visible?.({ editor: props.editor });
});
</script>

<template>
  <div v-if="visible" class="editor-block__actions-separator"></div>
</template>

<style>
.editor-block__actions-separator {
  @apply mx-1.5 h-5 bg-slate-200;
  width: 1px;
}
</style>
