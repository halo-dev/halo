<script lang="ts" setup>
import type { PMNode, VueEditor } from "@/tiptap";
import type { DragButtonType } from "@/types";
import { matchShortcut } from "@/utils/keyboard";
import { onMounted, onUnmounted, type PropType, ref } from "vue";
import EditorDragButtonItem from "./EditorDragButtonItem.vue";

const props = defineProps({
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
  class: {
    type: String,
    required: false,
  },
});

const emit = defineEmits<{
  (e: "close"): void;
}>();

const itemRefs = ref<Map<string, InstanceType<typeof EditorDragButtonItem>>>(
  new Map()
);

const handleKeyDown = (event: KeyboardEvent) => {
  for (const item of props.items) {
    if (item.keyboard && matchShortcut(event, item.keyboard)) {
      event.preventDefault();
      event.stopPropagation();

      const key = item.key || "";
      const itemRef = itemRefs.value.get(key);
      if (itemRef) {
        itemRef.triggerClick();
      }
      return;
    }
  }
};

onMounted(() => {
  window.addEventListener("keydown", handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener("keydown", handleKeyDown);
});

const setItemRef = (key: string, ref: any) => {
  if (ref) {
    itemRefs.value.set(key, ref);
  } else {
    itemRefs.value.delete(key);
  }
};
</script>

<template>
  <div
    :class="[
      'min-w-60 rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5',
      props.class,
    ]"
  >
    <div class="flex flex-col gap-0.5 px-2 py-1">
      <EditorDragButtonItem
        v-for="(item, index) in items"
        :ref="(el) => setItemRef(item.key || String(index), el)"
        :editor="editor"
        :node="node"
        :pos="pos"
        :key="item.key || String(index)"
        v-bind="item"
        @close="emit('close')"
      />
    </div>
  </div>
</template>
