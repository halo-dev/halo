<script lang="ts" setup>
import type { Editor } from "@/tiptap";
import { computed, type Component } from "vue";

const props = defineProps<{
  editor?: Editor | undefined;
  isActive?: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

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
