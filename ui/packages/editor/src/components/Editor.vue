<script lang="ts" setup>
import { i18n } from "@/locales";
import { EditorContent, VueEditor } from "@/tiptap";
import { watch, type CSSProperties, type PropType } from "vue";
import EditorBubbleMenu from "./EditorBubbleMenu.vue";
import EditorHeader from "./EditorHeader.vue";
import EditorDragHandle from "./drag/EditorDragHandle.vue";

const props = defineProps({
  editor: {
    type: Object as PropType<VueEditor>,
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
    <editor-drag-handle :editor="editor" />
    <editor-header :editor="editor" />
    <div class="editor-entry">
      <div class="editor-main">
        <div v-if="$slots.content" class="editor-main-extra">
          <slot name="content" />
        </div>

        <editor-content
          :editor="editor"
          :style="contentStyles"
          class="editor-main-content markdown-body"
        />
      </div>
      <div v-if="$slots.extra" class="editor-entry-extra">
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>
