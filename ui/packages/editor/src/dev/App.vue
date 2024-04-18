<script lang="ts" setup>
import { watchEffect } from "vue";
import { useLocalStorage } from "@vueuse/core";
import {
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionCode,
  ExtensionDocument,
  ExtensionDropcursor,
  ExtensionGapcursor,
  ExtensionHardBreak,
  ExtensionHeading,
  ExtensionHistory,
  ExtensionHorizontalRule,
  ExtensionItalic,
  ExtensionOrderedList,
  ExtensionStrike,
  ExtensionText,
  ExtensionImage,
  ExtensionTaskList,
  ExtensionLink,
  ExtensionTextAlign,
  ExtensionUnderline,
  ExtensionTable,
  ExtensionSubscript,
  ExtensionSuperscript,
  ExtensionPlaceholder,
  ExtensionHighlight,
  ExtensionCommands,
  ExtensionIframe,
  ExtensionVideo,
  ExtensionAudio,
  ExtensionCodeBlock,
  ExtensionColor,
  ExtensionFontSize,
  lowlight,
  RichTextEditor,
  useEditor,
  ExtensionIndent,
  ExtensionDraggable,
  ExtensionColumns,
  ExtensionColumn,
  ExtensionNodeSelected,
  ExtensionTrailingNode,
  ExtensionListKeymap,
  ExtensionSearchAndReplace,
  ExtensionClearFormat,
  ExtensionFormatBrush,
} from "../index";

const content = useLocalStorage("content", "");

const editor = useEditor({
  content: content.value,
  extensions: [
    ExtensionBlockquote,
    ExtensionBold,
    ExtensionBulletList,
    ExtensionCode,
    ExtensionDocument,
    ExtensionDropcursor.configure({
      width: 2,
      class: "dropcursor",
      color: "skyblue",
    }),
    ExtensionGapcursor,
    ExtensionHardBreak,
    ExtensionHeading,
    ExtensionHistory,
    ExtensionHorizontalRule,
    ExtensionItalic,
    ExtensionOrderedList,
    ExtensionStrike,
    ExtensionText,
    ExtensionImage.configure({
      HTMLAttributes: {
        loading: "lazy",
      },
    }),
    ExtensionTaskList,
    ExtensionLink.configure({
      autolink: false,
      openOnClick: false,
    }),
    ExtensionTextAlign.configure({
      types: ["heading", "paragraph"],
    }),
    ExtensionUnderline,
    ExtensionTable.configure({
      resizable: true,
    }),
    ExtensionSubscript,
    ExtensionSuperscript,
    ExtensionPlaceholder.configure({
      placeholder: "输入 / 以选择输入类型",
    }),
    ExtensionHighlight,
    ExtensionVideo,
    ExtensionAudio,
    ExtensionCommands,
    ExtensionCodeBlock.configure({
      lowlight,
    }),
    ExtensionIframe,
    ExtensionColor,
    ExtensionFontSize,
    ExtensionIndent,
    ExtensionDraggable,
    ExtensionColumns,
    ExtensionColumn,
    ExtensionNodeSelected,
    ExtensionTrailingNode,
    ExtensionListKeymap,
    ExtensionSearchAndReplace,
    ExtensionClearFormat,
    ExtensionFormatBrush,
  ],
  parseOptions: {
    preserveWhitespace: true,
  },
  onUpdate: () => {
    content.value = editor.value?.getHTML() + "";
  },
});

watchEffect(() => {
  // console.log(editor.value?.getHTML());
});
</script>

<template>
  <div style="height: 100vh" class="flex">
    <RichTextEditor v-if="editor" :editor="editor" />
  </div>
</template>
