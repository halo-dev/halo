<script lang="ts" setup>
import { Editor, EditorContent } from "@/tiptap/vue-3";
import EditorHeader from "./EditorHeader.vue";
import EditorBubbleMenu from "./EditorBubbleMenu.vue";
import { watch, type CSSProperties, type PropType } from "vue";
import { i18n } from "@/locales";

const props = defineProps({
  editor: {
    type: Object as PropType<Editor>,
    required: true,
  },
  contentStyles: {
    type: Object as PropType<CSSProperties>,
    required: false,
    default: () => ({}),
  },
  locale: {
    type: String as PropType<"zh-CN" | "en" | "zh" | "en-US">,
    required: false,
    default: "zh-CN",
  },
});

watch(
  () => props.locale,
  () => {
    i18n.global.locale.value = props.locale;
  },
  {
    immediate: true,
  }
);
</script>
<template>
  <div v-if="editor" class="halo-rich-text-editor">
    <editor-bubble-menu :editor="editor" />
    <editor-header :editor="editor" />
    <div class="h-full flex flex-row w-full">
      <div class="overflow-y-auto overflow-x-hidden flex-1 relative bg-white">
        <editor-content
          :editor="editor"
          :style="contentStyles"
          class="editor-content markdown-body"
        />
      </div>
      <div
        v-if="$slots.extra"
        class="h-full hidden sm:!block w-72 flex-shrink-0"
      >
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>
