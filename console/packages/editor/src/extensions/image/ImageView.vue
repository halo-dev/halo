<script lang="ts" setup>
import { i18n } from "@/locales";
import type { Editor, Node } from "@/tiptap/vue-3";
import { NodeViewWrapper } from "@/tiptap/vue-3";
import type { Node as ProseMirrorNode, Decoration } from "@/tiptap/pm";
import { computed, onMounted, ref } from "vue";
import { useResizeObserver } from "@vueuse/core";

const props = defineProps<{
  editor: Editor;
  node: ProseMirrorNode;
  decorations: Decoration[];
  selected: boolean;
  extension: Node<any, any>;
  getPos: () => number;
  updateAttributes: (attributes: Record<string, any>) => void;
  deleteNode: () => void;
}>();

const src = computed({
  get: () => {
    return props.node?.attrs.src;
  },
  set: (src: string) => {
    props.updateAttributes({
      src: src,
    });
  },
});

const alt = computed({
  get: () => {
    return props.node?.attrs.alt;
  },
  set: (alt: string) => {
    props.updateAttributes({ alt: alt });
  },
});

const href = computed({
  get: () => {
    return props.node?.attrs.href;
  },
  set: (href: string) => {
    props.updateAttributes({ href: href });
  },
});
function handleSetFocus() {
  props.editor.commands.setNodeSelection(props.getPos());
}

const inputRef = ref();
const resizeRef = ref();
const init = ref(true);

onMounted(() => {
  if (!src.value) {
    inputRef.value.focus();
  } else {
    useResizeObserver(resizeRef.value, (entries) => {
      const entry = entries[0];
      const { height } = entry.contentRect;
      if (height == 0) {
        return;
      }
      if (!props.selected && !init.value) {
        handleSetFocus();
      }
      init.value = false;
    });
  }
});
</script>

<template>
  <node-view-wrapper as="div" class="inline-block w-full">
    <div v-if="!src" class="p-1.5 w-full">
      <input
        ref="inputRef"
        v-model.lazy="src"
        class="block px-2 w-full py-1.5 text-sm text-gray-900 border border-gray-300 rounded-md bg-gray-50 focus:ring-blue-500 focus:border-blue-500"
        :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
        tabindex="-1"
        @focus="handleSetFocus"
      />
    </div>
    <div
      v-else
      ref="resizeRef"
      class="resize-x inline-block overflow-hidden text-center relative rounded-md"
      :class="{
        'ring-2 rounded': selected,
      }"
      :style="{
        width: node.attrs.width,
        height: node.attrs.height,
      }"
    >
      <img
        :src="src"
        :title="node.attrs.title"
        :alt="alt"
        :href="href"
        class="w-full h-full"
      />
    </div>
  </node-view-wrapper>
</template>
