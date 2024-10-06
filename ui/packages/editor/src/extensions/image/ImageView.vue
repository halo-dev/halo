<script lang="ts" setup>
import { i18n } from "@/locales";
import type { NodeViewProps } from "@/tiptap/vue-3";
import { NodeViewWrapper } from "@/tiptap/vue-3";
import { computed, onMounted, ref } from "vue";
import Image from "./index";

const props = defineProps<NodeViewProps>();

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

const aspectRatio = ref<number>(0);
const inputRef = ref();
const resizeRef = ref<HTMLDivElement>();

function onImageLoaded() {
  if (!resizeRef.value) return;

  aspectRatio.value =
    resizeRef.value.clientWidth / resizeRef.value.clientHeight;
}

onMounted(() => {
  if (!src.value) {
    inputRef.value.focus();
    return;
  }

  if (!resizeRef.value) return;

  let startX: number, startWidth: number;

  resizeRef.value.addEventListener("mousedown", function (e) {
    startX = e.clientX;
    startWidth = resizeRef.value?.clientWidth || 1;
    document.documentElement.addEventListener("mousemove", doDrag, false);
    document.documentElement.addEventListener("mouseup", stopDrag, false);
  });

  function doDrag(e: MouseEvent) {
    if (!resizeRef.value) return;

    const newWidth = Math.min(
      startWidth + e.clientX - startX,
      resizeRef.value.parentElement?.clientWidth || 0
    );

    const width = newWidth.toFixed(0) + "px";
    const height = (newWidth / aspectRatio.value).toFixed(0) + "px";
    props.editor
      .chain()
      .updateAttributes(Image.name, { width, height })
      .setNodeSelection(props.getPos())
      .focus()
      .run();
  }

  function stopDrag() {
    document.documentElement.removeEventListener("mousemove", doDrag, false);
    document.documentElement.removeEventListener("mouseup", stopDrag, false);
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
      class="resize-x inline-block overflow-hidden text-center relative rounded-md max-w-full"
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
        @load="onImageLoaded"
      />
    </div>
  </node-view-wrapper>
</template>
