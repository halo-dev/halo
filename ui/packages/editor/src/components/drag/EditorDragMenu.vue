<script lang="ts" setup>
import { type PropType } from "vue";
import type { PMNode, VueEditor } from "@/tiptap";
import type { DragButtonType } from "@/types";
import EditorDragButtonItem from "./EditorDragButtonItem.vue";

defineProps({
  editor: {
    type: Object as PropType<VueEditor>,
    required: true,
  },
  node: {
    type: Object as PropType<PMNode | null>,
    required: true,
  },
  pos: {
    type: Number,
    required: true,
  },
  items: {
    type: Array as PropType<DragButtonType[]>,
    required: true,
  },
});

const emit = defineEmits<{
  (e: "close"): void;
}>();
</script>

<template>
  <div class="flex min-w-60 flex-col gap-0.5 bg-white">
    <EditorDragButtonItem
      v-for="(item, index) in items"
      :key="item.key || String(index)"
      :editor="editor"
      :node="node"
      :pos="pos"
      v-bind="item"
      @close="emit('close')"
    />
  </div>
</template>
