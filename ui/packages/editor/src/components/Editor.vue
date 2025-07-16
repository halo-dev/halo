<script lang="ts" setup>
import { i18n } from "@/locales";
import { Editor, EditorContent } from "@/tiptap/vue-3";
import { watch, type CSSProperties, type PropType } from "vue";
import EditorBubbleMenu from "./EditorBubbleMenu.vue";
import EditorHeader from "./EditorHeader.vue";

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
    <div class="flex h-full w-full flex-row overflow-hidden">
      <div class="relative flex-1 overflow-y-auto bg-white">
        <div v-if="$slots.content" class="editor-header-extra">
          <slot name="content" />
        </div>

        <editor-content
          :editor="editor"
          :style="contentStyles"
          class="editor-content markdown-body relative"
        />
      </div>
      <div
        v-if="$slots.extra"
        class="hidden h-full w-72 flex-none flex-shrink-0 sm:!block"
      >
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>
