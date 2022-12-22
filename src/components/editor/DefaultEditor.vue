<script lang="ts" setup>
import {
  Extension,
  RichTextEditor,
  useEditor,
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
  ExtensionListItem,
  ExtensionOrderedList,
  ExtensionParagraph,
  ExtensionStrike,
  ExtensionText,
  ExtensionImage,
  ExtensionTaskList,
  ExtensionTaskItem,
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
  CommandsSuggestion,
  CommentParagraph,
  CommandHeader1,
  CommandHeader2,
  CommandHeader3,
  CommandHeader4,
  CommandHeader5,
  CommandHeader6,
  CommandCodeBlock,
  CommandIframe,
  CommandTable,
  CommandBulletList,
  CommandOrderedList,
  CommandTaskList,
  ExtensionCodeBlock,
  lowlight,
  UndoMenuItem,
  RedoMenuItem,
  BoldMenuItem,
  ItalicMenuItem,
  UnderlineMenuItem,
  StrikeMenuItem,
  QuoteMenuItem,
  CodeMenuItem,
  SuperScriptMenuItem,
  SubScriptMenuItem,
  CodeBlockMenuItem,
  HeadingMenuItem,
  AlignLeftMenuItem,
  AlignCenterMenuItem,
  AlignRightMenuItem,
  AlignJustifyMenuItem,
  TableMenuItem,
  BulletListMenuItem,
  OrderedListMenuItem,
  TaskListMenuItem,
  HighlightMenuItem,
  Separator,
} from "@halo-dev/richtext-editor";
import {
  IconCalendar,
  IconCharacterRecognition,
  IconLink,
  IconUserFollow,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import AttachmentSelectorModal from "@/modules/contents/attachments/components/AttachmentSelectorModal.vue";
import ExtensionCharacterCount from "@tiptap/extension-character-count";
import MdiFileImageBox from "~icons/mdi/file-image-box";
import MdiFormatHeader1 from "~icons/mdi/format-header-1";
import MdiFormatHeader2 from "~icons/mdi/format-header-2";
import MdiFormatHeader3 from "~icons/mdi/format-header-3";
import MdiFormatHeader4 from "~icons/mdi/format-header-4";
import MdiFormatHeader5 from "~icons/mdi/format-header-5";
import MdiFormatHeader6 from "~icons/mdi/format-header-6";
import {
  computed,
  inject,
  markRaw,
  nextTick,
  ref,
  watch,
  type ComputedRef,
} from "vue";
import { formatDatetime } from "@/utils/date";
import { useAttachmentSelect } from "@/modules/contents/attachments/composables/use-attachment";

const props = withDefaults(
  defineProps<{
    raw?: string;
    content: string;
  }>(),
  {
    raw: "",
    content: "",
  }
);

const emit = defineEmits<{
  (event: "update:raw", value: string): void;
  (event: "update:content", value: string): void;
  (event: "update", value: string): void;
}>();

const owner = inject<ComputedRef<string | undefined>>("owner");
const publishTime = inject<ComputedRef<string | undefined>>("publishTime");
const permalink = inject<ComputedRef<string | undefined>>("permalink");

interface HeadingNode {
  id: string;
  level: number;
  text: string;
}

const headingIcons = {
  1: markRaw(MdiFormatHeader1),
  2: markRaw(MdiFormatHeader2),
  3: markRaw(MdiFormatHeader3),
  4: markRaw(MdiFormatHeader4),
  5: markRaw(MdiFormatHeader5),
  6: markRaw(MdiFormatHeader6),
};

const headingNodes = ref<HeadingNode[]>();
const selectedHeadingNode = ref<HeadingNode>();
const extraActiveId = ref("toc");
const attachmentSelectorModal = ref(false);

const editor = useEditor({
  content: props.raw,
  extensions: [
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
    ExtensionListItem,
    ExtensionOrderedList,
    ExtensionParagraph,
    ExtensionStrike,
    ExtensionText,
    ExtensionImage.configure({
      inline: true,
      HTMLAttributes: {
        loading: "lazy",
      },
    }),
    ExtensionTaskList,
    ExtensionTaskItem,
    ExtensionLink.configure({
      autolink: true,
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
    ExtensionCommands.configure({
      suggestion: {
        ...CommandsSuggestion,
        items: ({ query }: { query: string }) => {
          return [
            CommentParagraph,
            CommandHeader1,
            CommandHeader2,
            CommandHeader3,
            CommandHeader4,
            CommandHeader5,
            CommandHeader6,
            CommandCodeBlock,
            CommandTable,
            CommandBulletList,
            CommandOrderedList,
            CommandTaskList,
            CommandIframe,
          ].filter((item) =>
            [...item.keywords, item.title].some((keyword) =>
              keyword.includes(query)
            )
          );
        },
      },
    }),
    ExtensionCodeBlock.configure({
      lowlight,
    }),
    ExtensionIframe,
    ExtensionCharacterCount,
    Extension.create({
      addGlobalAttributes() {
        return [
          {
            types: ["heading"],
            attributes: {
              id: {
                default: null,
              },
            },
          },
        ];
      },
    }),
  ],
  autofocus: "start",
  onUpdate: () => {
    emit("update:raw", editor.value?.getHTML() + "");
    emit("update:content", editor.value?.getHTML() + "");
    emit("update", editor.value?.getHTML() + "");
    nextTick(() => {
      handleGenerateTableOfContent();
    });
  },
});

const toolbarMenuItems = computed(() => {
  if (!editor.value) return [];
  return [
    UndoMenuItem(editor.value),
    RedoMenuItem(editor.value),
    Separator(),
    HeadingMenuItem(editor.value),
    BoldMenuItem(editor.value),
    ItalicMenuItem(editor.value),
    UnderlineMenuItem(editor.value),
    StrikeMenuItem(editor.value),
    HighlightMenuItem(editor.value),
    Separator(),
    QuoteMenuItem(editor.value),
    CodeMenuItem(editor.value),
    SuperScriptMenuItem(editor.value),
    SubScriptMenuItem(editor.value),
    Separator(),
    BulletListMenuItem(editor.value),
    OrderedListMenuItem(editor.value),
    TaskListMenuItem(editor.value),
    Separator(),
    CodeBlockMenuItem(editor.value),
    TableMenuItem(editor.value),
    Separator(),
    AlignLeftMenuItem(editor.value),
    AlignCenterMenuItem(editor.value),
    AlignRightMenuItem(editor.value),
    AlignJustifyMenuItem(editor.value),
    {
      type: "button",
      icon: markRaw(MdiFileImageBox),
      title: "插入附件",
      action: () => (attachmentSelectorModal.value = true),
      isActive: () => false,
    },
  ];
});

const bubbleMenuItems = computed(() => {
  if (!editor.value) return [];
  return [
    BoldMenuItem(editor.value),
    ItalicMenuItem(editor.value),
    UnderlineMenuItem(editor.value),
    StrikeMenuItem(editor.value),
    HighlightMenuItem(editor.value),
    QuoteMenuItem(editor.value),
    CodeMenuItem(editor.value),
    CodeBlockMenuItem(editor.value),
    SuperScriptMenuItem(editor.value),
    SubScriptMenuItem(editor.value),
    AlignLeftMenuItem(editor.value),
    AlignCenterMenuItem(editor.value),
    AlignRightMenuItem(editor.value),
    AlignJustifyMenuItem(editor.value),
  ];
});

const handleGenerateTableOfContent = () => {
  if (!editor.value) {
    return;
  }

  const headings: HeadingNode[] = [];
  const transaction = editor.value.state.tr;

  editor.value.state.doc.descendants((node, pos) => {
    if (node.type.name === "heading") {
      const id = `heading-${headings.length + 1}`;

      if (node.attrs.id !== id) {
        transaction?.setNodeMarkup(pos, undefined, {
          ...node.attrs,
          id,
        });
      }

      headings.push({
        level: node.attrs.level,
        text: node.textContent,
        id,
      });
    }
  });

  transaction.setMeta("addToHistory", false);
  transaction.setMeta("preventUpdate", true);

  editor.value.view.dispatch(transaction);

  headingNodes.value = headings;

  if (!selectedHeadingNode.value) {
    selectedHeadingNode.value = headings[0];
  }
};

const handleSelectHeadingNode = (node: HeadingNode) => {
  selectedHeadingNode.value = node;
  document.getElementById(node.id)?.scrollIntoView({ behavior: "smooth" });
};

const { onAttachmentSelect } = useAttachmentSelect(editor);

watch(
  () => props.raw,
  () => {
    if (props.raw !== editor.value?.getHTML()) {
      editor.value?.commands.setContent(props.raw);
      nextTick(() => {
        handleGenerateTableOfContent();
      });
    }
  }
);
</script>

<template>
  <AttachmentSelectorModal
    v-model:visible="attachmentSelectorModal"
    @select="onAttachmentSelect"
  />
  <RichTextEditor
    v-if="editor"
    :editor="editor"
    :toolbar-menu-items="toolbarMenuItems"
    :bubble-menu-items="bubbleMenuItems"
    :content-styles="{
      width: 'calc(100% - 18rem)',
    }"
  >
    <template #extra>
      <div class="h-full w-72 overflow-y-auto border-l bg-white">
        <VTabs v-model:active-id="extraActiveId" type="outline">
          <VTabItem id="toc" label="大纲">
            <div class="p-1 pt-0">
              <ul class="space-y-1">
                <li
                  v-for="(node, index) in headingNodes"
                  :key="index"
                  :class="[
                    { 'bg-gray-100': node.id === selectedHeadingNode?.id },
                  ]"
                  class="group cursor-pointer truncate rounded-base px-1.5 py-1 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                  @click="handleSelectHeadingNode(node)"
                >
                  <div
                    :style="{
                      paddingLeft: `${(node.level - 1) * 0.8}rem`,
                    }"
                    class="flex items-center gap-2"
                  >
                    <component
                      :is="headingIcons[node.level]"
                      class="h-4 w-4 rounded-sm bg-gray-100 p-0.5 group-hover:bg-white"
                      :class="[
                        { '!bg-white': node.id === selectedHeadingNode?.id },
                      ]"
                    />
                    <span class="flex-1 truncate">{{ node.text }}</span>
                  </div>
                </li>
              </ul>
            </div>
          </VTabItem>
          <VTabItem id="information" label="详情">
            <div class="flex flex-col gap-2 p-1 pt-0">
              <div class="grid grid-cols-2 gap-2">
                <div
                  class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                >
                  <div class="flex items-center justify-between">
                    <div
                      class="text-sm text-gray-500 group-hover:text-gray-900"
                    >
                      字符数
                    </div>
                    <div class="rounded bg-gray-200 p-0.5">
                      <IconCharacterRecognition
                        class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                      />
                    </div>
                  </div>
                  <div class="text-base font-medium text-gray-900">
                    {{ editor.storage.characterCount.characters() }}
                  </div>
                </div>
                <div
                  class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                >
                  <div class="flex items-center justify-between">
                    <div
                      class="text-sm text-gray-500 group-hover:text-gray-900"
                    >
                      词数
                    </div>
                    <div class="rounded bg-gray-200 p-0.5">
                      <IconCharacterRecognition
                        class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                      />
                    </div>
                  </div>
                  <div class="text-base font-medium text-gray-900">
                    {{ editor.storage.characterCount.words() }}
                  </div>
                </div>
              </div>

              <div v-if="publishTime" class="grid grid-cols-1 gap-2">
                <div
                  class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                >
                  <div class="flex items-center justify-between">
                    <div
                      class="text-sm text-gray-500 group-hover:text-gray-900"
                    >
                      发布时间
                    </div>
                    <div class="rounded bg-gray-200 p-0.5">
                      <IconCalendar
                        class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                      />
                    </div>
                  </div>
                  <div class="text-base font-medium text-gray-900">
                    {{ formatDatetime(publishTime) || "未发布" }}
                  </div>
                </div>
              </div>
              <div v-if="owner" class="grid grid-cols-1 gap-2">
                <div
                  class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                >
                  <div class="flex items-center justify-between">
                    <div
                      class="text-sm text-gray-500 group-hover:text-gray-900"
                    >
                      创建者
                    </div>
                    <div class="rounded bg-gray-200 p-0.5">
                      <IconUserFollow
                        class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                      />
                    </div>
                  </div>
                  <div class="text-base font-medium text-gray-900">
                    {{ owner }}
                  </div>
                </div>
              </div>
              <div v-if="permalink" class="grid grid-cols-1 gap-2">
                <div
                  class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                >
                  <div class="flex items-center justify-between">
                    <div
                      class="text-sm text-gray-500 group-hover:text-gray-900"
                    >
                      访问链接
                    </div>
                    <div class="rounded bg-gray-200 p-0.5">
                      <IconLink
                        class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                      />
                    </div>
                  </div>
                  <div>
                    <a
                      :href="permalink"
                      :title="permalink"
                      target="_blank"
                      class="text-sm text-gray-900 hover:text-blue-600"
                    >
                      {{ permalink }}
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </VTabItem>
        </VTabs>
      </div>
    </template>
  </RichTextEditor>
</template>
