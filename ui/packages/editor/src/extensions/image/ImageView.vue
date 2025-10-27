<script lang="ts" setup>
import { i18n } from "@/locales";
import type { NodeViewProps } from "@/tiptap/vue-3";
import { NodeViewWrapper } from "@/tiptap/vue-3";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
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
  props.editor.commands.setNodeSelection(props.getPos() || 0);
}

const aspectRatio = ref<number>(0);
const inputRef = ref<HTMLInputElement>();
const resizeRef = ref<HTMLDivElement>();
const imageLoadError = ref<boolean>(false);

function onImageLoaded() {
  if (!resizeRef.value) return;

  aspectRatio.value =
    resizeRef.value.clientWidth / resizeRef.value.clientHeight;
  imageLoadError.value = false;
}

function onImageError() {
  imageLoadError.value = true;
}

let cleanupResize: (() => void) | null = null;
const resizeHandleRef = ref<HTMLDivElement>();

function setupResizeListener() {
  if (!resizeHandleRef.value) {
    return;
  }

  if (cleanupResize) {
    cleanupResize();
  }

  const handleElement = resizeHandleRef.value;
  let startX: number, startWidth: number;
  let rafId: number | null = null;

  function handleMouseDown(e: MouseEvent) {
    if (e.button !== 0) {
      return;
    }

    e.preventDefault();
    e.stopPropagation();
    startX = e.clientX;
    startWidth = resizeRef.value?.clientWidth || 1;
    document.documentElement.addEventListener("mousemove", doDrag, false);
    document.documentElement.addEventListener("mouseup", stopDrag, false);
    document.documentElement.addEventListener(
      "contextmenu",
      handleContextMenu,
      false
    );
    document.documentElement.addEventListener("mouseleave", stopDrag, false);
    window.addEventListener("blur", stopDrag, false);
  }

  function doDrag(e: MouseEvent) {
    if (!resizeRef.value) {
      return;
    }

    if (rafId !== null) {
      cancelAnimationFrame(rafId);
    }

    rafId = requestAnimationFrame(() => {
      if (!resizeRef.value) {
        return;
      }

      const newWidth = Math.max(
        100,
        Math.min(
          startWidth + e.clientX - startX,
          props.editor.view.dom?.clientWidth || 0
        )
      );

      const width = newWidth.toFixed(0) + "px";
      const height =
        aspectRatio.value > 0
          ? (newWidth / aspectRatio.value).toFixed(0) + "px"
          : "auto";

      props.editor
        .chain()
        .updateAttributes(Image.name, { width, height })
        .setNodeSelection(props.getPos() || 0)
        .focus()
        .run();
    });
  }

  function handleContextMenu(e: MouseEvent) {
    e.preventDefault();
    stopDrag();
  }

  function stopDrag() {
    if (rafId !== null) {
      cancelAnimationFrame(rafId);
      rafId = null;
    }
    document.documentElement.removeEventListener("mousemove", doDrag, false);
    document.documentElement.removeEventListener("mouseup", stopDrag, false);
    document.documentElement.removeEventListener(
      "contextmenu",
      handleContextMenu,
      false
    );
    document.documentElement.removeEventListener("mouseleave", stopDrag, false);
    window.removeEventListener("blur", stopDrag, false);
  }

  handleElement.addEventListener("mousedown", handleMouseDown);

  cleanupResize = () => {
    handleElement.removeEventListener("mousedown", handleMouseDown);
    document.documentElement.removeEventListener("mousemove", doDrag, false);
    document.documentElement.removeEventListener("mouseup", stopDrag, false);
    document.documentElement.removeEventListener(
      "contextmenu",
      handleContextMenu,
      false
    );
    document.documentElement.removeEventListener("mouseleave", stopDrag, false);
    window.removeEventListener("blur", stopDrag, false);
    if (rafId !== null) {
      cancelAnimationFrame(rafId);
    }
    cleanupResize = null;
  };
}

onMounted(() => {
  if (!src.value) {
    inputRef.value?.focus();
    return;
  }
  setupResizeListener();
});

onUnmounted(() => {
  if (cleanupResize) {
    cleanupResize();
  }
});

watch([src, resizeHandleRef], () => {
  if (src.value && resizeHandleRef.value) {
    setupResizeListener();
  }
});
</script>

<template>
  <node-view-wrapper as="div">
    <div v-if="!src" class="w-full p-1.5">
      <input
        ref="inputRef"
        v-model.lazy="src"
        class="block w-full rounded-md border !border-solid border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500"
        :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
        tabindex="-1"
        @focus="handleSetFocus"
      />
    </div>
    <div
      v-else
      ref="resizeRef"
      class="resize-container relative inline-block max-w-full overflow-hidden rounded-md text-center"
      :class="{
        'rounded ring-2': selected,
        'ring-red-500': imageLoadError,
      }"
    >
      <img
        v-if="!imageLoadError"
        :src="src"
        :title="node.attrs.title"
        :alt="alt"
        :href="href"
        :width="node.attrs.width"
        class="max-w-full rounded-md"
        @load="onImageLoaded"
        @error="onImageError"
      />
      <div
        v-else
        class="flex h-full w-full items-center justify-center bg-gray-100 text-gray-400"
      >
        <span>{{
          i18n.global.t("editor.extensions.image.image_load_error")
        }}</span>
      </div>
      <div
        v-if="selected && !imageLoadError"
        ref="resizeHandleRef"
        class="resizer-handler resizer-br"
        title="拖拽调整大小"
      ></div>
    </div>
  </node-view-wrapper>
</template>
