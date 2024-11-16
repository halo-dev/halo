<script lang="ts" setup>
import { i18n } from "@/locales";
import type { NodeViewProps } from "@/tiptap/vue-3";
import { NodeViewWrapper } from "@/tiptap/vue-3";
import { isAllowedUri } from "@/utils/is-allowed-uri";
import { computed, onMounted, ref } from "vue";

const props = defineProps<NodeViewProps>();

const src = computed({
  get: () => {
    return props.node?.attrs.src;
  },
  set: (src: string) => {
    if (!src || !isAllowedUri(src)) {
      return;
    }
    props.updateAttributes({ src: src });
  },
});

const frameborder = computed(() => {
  return props.node.attrs.frameborder;
});

function handleSetFocus() {
  props.editor.commands.setNodeSelection(props.getPos());
}
const inputRef = ref();

onMounted(() => {
  if (!src.value) {
    inputRef.value.focus();
  }
});
</script>

<template>
  <node-view-wrapper as="div" class="inline-block w-full">
    <div
      class="inline-block overflow-hidden transition-all text-center relative h-full max-w-full"
      :style="{
        width: node.attrs.width,
      }"
    >
      <div v-if="!src" class="p-1.5">
        <input
          ref="inputRef"
          v-model.lazy="src"
          class="block px-2 w-full py-1.5 text-sm text-gray-900 border border-gray-300 rounded-md bg-gray-50 focus:ring-blue-500 focus:border-blue-500"
          :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
          tabindex="-1"
          @focus="handleSetFocus"
        />
      </div>
      <iframe
        v-else
        class="rounded-md"
        :src="node!.attrs.src"
        :width="node.attrs.width"
        :height="node.attrs.height"
        scrolling="yes"
        :frameborder="frameborder"
        framespacing="0"
        allowfullscreen="true"
        :class="{
          'border-2': frameborder === '1',
        }"
        @mouseenter="handleSetFocus"
      ></iframe>
    </div>
  </node-view-wrapper>
</template>
