<script lang="ts" setup>
import {
  DecorationSet,
  Editor,
  Extension,
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionCharacterCount,
  ExtensionClearFormat,
  ExtensionCode,
  ExtensionCodeBlock,
  ExtensionColor,
  ExtensionColumn,
  ExtensionColumns,
  ExtensionCommands,
  ExtensionDetails,
  ExtensionDocument,
  ExtensionDropcursor,
  ExtensionFontSize,
  ExtensionFormatBrush,
  ExtensionGapcursor,
  ExtensionHardBreak,
  ExtensionHeading,
  ExtensionHighlight,
  ExtensionHistory,
  ExtensionHorizontalRule,
  ExtensionIframe,
  ExtensionIndent,
  ExtensionItalic,
  ExtensionLink,
  ExtensionListKeymap,
  ExtensionNodeSelected,
  ExtensionOrderedList,
  ExtensionPlaceholder,
  ExtensionRangeSelection,
  ExtensionSearchAndReplace,
  ExtensionStrike,
  ExtensionSubscript,
  ExtensionSuperscript,
  ExtensionTable,
  ExtensionTaskList,
  ExtensionText,
  ExtensionTextAlign,
  ExtensionTrailingNode,
  ExtensionUnderline,
  Plugin,
  PluginKey,
  RichTextEditor,
  ToolbarItem,
  ToolboxItem,
  VueEditor,
  type Extensions,
} from "@halo-dev/richtext-editor";
// ui custom extension
import { i18n } from "@/locales";
import { usePluginModuleStore } from "@/stores/plugin";
import {
  GetThumbnailByUriSizeEnum,
  type Attachment,
} from "@halo-dev/api-client";
import {
  IconCalendar,
  IconCharacterRecognition,
  IconExchange,
  IconFolder,
  IconImageAddLine,
  IconLink,
  IconUserFollow,
  Toast,
  VButton,
  VDropdown,
  VDropdownItem,
  VLoading,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { useDebounceFn, useFileDialog, useLocalStorage } from "@vueuse/core";
import type { AxiosRequestConfig } from "axios";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import {
  defineAsyncComponent,
  inject,
  markRaw,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  shallowRef,
  watch,
  type ComputedRef,
} from "vue";
import { useI18n } from "vue-i18n";
import MdiFormatHeader1 from "~icons/mdi/format-header-1";
import MdiFormatHeader2 from "~icons/mdi/format-header-2";
import MdiFormatHeader3 from "~icons/mdi/format-header-3";
import MdiFormatHeader4 from "~icons/mdi/format-header-4";
import MdiFormatHeader5 from "~icons/mdi/format-header-5";
import MdiFormatHeader6 from "~icons/mdi/format-header-6";
import RiLayoutRightLine from "~icons/ri/layout-right-line";
import { useAttachmentSelect } from "./composables/use-attachment";
import { useExtension } from "./composables/use-extension";
import {
  UiExtensionAudio,
  UiExtensionImage,
  UiExtensionUpload,
  UiExtensionVideo,
} from "./extensions";
import { convertToMediaContents } from "./utils/attachment";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    title?: string;
    raw?: string;
    content: string;
    cover?: string;
    uploadImage?: (
      file: File,
      options?: AxiosRequestConfig
    ) => Promise<Attachment>;
  }>(),
  {
    title: "",
    raw: "",
    content: "",
    cover: undefined,
    uploadImage: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:title", value: string): void;
  (event: "update:raw", value: string): void;
  (event: "update:content", value: string): void;
  (event: "update", value: string): void;
  (event: "update:cover", value: string | undefined): void;
}>();

const owner = inject<ComputedRef<string | undefined>>("owner");
const publishTime = inject<ComputedRef<string | undefined>>("publishTime");
const permalink = inject<ComputedRef<string | undefined>>("permalink");

declare module "@halo-dev/richtext-editor" {
  interface Commands<ReturnType> {
    global: {
      openAttachmentSelector: (
        callback: (attachments: AttachmentLike[]) => void,
        options?: {
          accepts?: string[];
          min?: number;
          max?: number;
        }
      ) => ReturnType;
    };
  }
}

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

const editor = shallowRef<VueEditor>();
const editorTitleRef = ref();

const { pluginModules } = usePluginModuleStore();

const showSidebar = useLocalStorage("halo:editor:show-sidebar", true);

// Attachments
const AttachmentSelectorModal = defineAsyncComponent({
  loader: () => {
    if (utils.permission.has(["system:attachments:manage"])) {
      return import(
        "@console/modules/contents/attachments/components/AttachmentSelectorModal.vue"
      );
    }
    return import(
      "@uc/modules/contents/attachments/components/AttachmentSelectorModal.vue"
    );
  },
});

const attachmentSelectorModalVisible = ref(false);
const { onAttachmentSelect, attachmentResult } = useAttachmentSelect();

const initAttachmentOptions = {
  accepts: ["*/*"],
  min: undefined,
  max: undefined,
};

const attachmentOptions = ref<{
  accepts?: string[];
  min?: number;
  max?: number;
}>(initAttachmentOptions);

const onAttachmentSelectorModalClose = () => {
  attachmentOptions.value = initAttachmentOptions;
  attachmentSelectorModalVisible.value = false;
};

const { filterDuplicateExtensions } = useExtension();

const presetExtensions = [
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
  UiExtensionImage.configure({
    inline: true,
    allowBase64: false,
    HTMLAttributes: {
      loading: "lazy",
    },
    uploadImage: props.uploadImage,
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
    placeholder: t(
      "core.components.default_editor.extensions.placeholder.options.placeholder"
    ),
  }),
  ExtensionHighlight,
  ExtensionCommands,
  ExtensionCodeBlock,
  ExtensionIframe,
  UiExtensionVideo.configure({
    uploadVideo: props.uploadImage,
  }),
  UiExtensionAudio.configure({
    uploadAudio: props.uploadImage,
  }),
  ExtensionCharacterCount,
  ExtensionFontSize,
  ExtensionColor,
  ExtensionIndent,
  Extension.create({
    name: "custom-heading-extension",
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
  Extension.create({
    name: "custom-attachment-extension",
    addOptions() {
      // If user has no permission to view attachments, return
      if (
        !utils.permission.has([
          "system:attachments:manage",
          "uc:attachments:manage",
        ])
      ) {
        return this;
      }

      return {
        getToolboxItems({ editor }: { editor: Editor }) {
          return [
            {
              priority: 0,
              component: markRaw(ToolboxItem),
              props: {
                editor,
                icon: markRaw(IconFolder),
                title: i18n.global.t(
                  "core.components.default_editor.toolbox.attachment"
                ),
                action: () => {
                  editor.commands.openAttachmentSelector((attachment) => {
                    editor
                      .chain()
                      .focus()
                      .insertContent(convertToMediaContents(attachment))
                      .run();
                  });
                  return true;
                },
              },
            },
          ];
        },
      };
    },
    addCommands() {
      return {
        openAttachmentSelector: (callback, options) => () => {
          if (options) {
            attachmentOptions.value = options;
          }
          attachmentSelectorModalVisible.value = true;
          attachmentResult.updateAttachment = (
            attachments: AttachmentLike[]
          ) => {
            callback(attachments);
          };
          return true;
        },
      };
    },
  }),
  Extension.create({
    name: "custom-sidebar-toggle-extension",
    addOptions() {
      return {
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 1000,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: showSidebar.value,
              icon: markRaw(RiLayoutRightLine),
              title: i18n.global.t(
                "core.components.default_editor.toolbox.show_hide_sidebar"
              ),
              action: () => {
                showSidebar.value = !showSidebar.value;
              },
            },
          };
        },
      };
    },
  }),
  ExtensionColumns,
  ExtensionColumn,
  ExtensionNodeSelected,
  ExtensionTrailingNode,
  Extension.create({
    name: "get-heading-id-extension",
    addProseMirrorPlugins() {
      return [
        new Plugin({
          key: new PluginKey("get-heading-id"),
          props: {
            decorations: (state) => {
              const headings: HeadingNode[] = [];
              const { doc } = state;
              doc.descendants((node) => {
                if (node.type.name === ExtensionHeading.name) {
                  headings.push({
                    level: node.attrs.level,
                    text: node.textContent,
                    id: node.attrs.id,
                  });
                }
              });
              headingNodes.value = headings;
              if (!selectedHeadingNode.value) {
                selectedHeadingNode.value = headings[0];
              }
              return DecorationSet.empty;
            },
          },
        }),
      ];
    },
  }),
  ExtensionListKeymap,
  UiExtensionUpload,
  ExtensionSearchAndReplace,
  ExtensionClearFormat,
  ExtensionFormatBrush,
  ExtensionRangeSelection,
  ExtensionDetails.configure({
    persist: true,
  }),
];

const isInitialized = ref(false);

onMounted(async () => {
  const extensionsFromPlugins: Extensions = [];

  for (const pluginModule of pluginModules) {
    const callbackFunction =
      pluginModule?.extensionPoints?.["default:editor:extension:create"];

    if (typeof callbackFunction !== "function") {
      continue;
    }

    const extensions = await callbackFunction();

    extensionsFromPlugins.push(...extensions);
  }

  // debounce OnUpdate
  const debounceOnUpdate = useDebounceFn(() => {
    const html = editor.value?.getHTML() + "";
    emit("update:raw", html);
    emit("update:content", html);
    emit("update", html);
  }, 250);

  const extensions = filterDuplicateExtensions([
    ...presetExtensions,
    ...extensionsFromPlugins,
  ]);

  editor.value = new VueEditor({
    content: props.raw,
    extensions,
    parseOptions: {
      preserveWhitespace: true,
    },
    onUpdate: () => {
      debounceOnUpdate();
    },
    onCreate() {
      isInitialized.value = true;
      nextTick(() => {
        if (editor.value?.isEmpty && !props.title) {
          editorTitleRef.value.focus();
        } else {
          editor.value?.commands.focus();
        }
      });
    },
  });
});

onBeforeUnmount(() => {
  editor.value?.destroy();
});

const handleSelectHeadingNode = (node: HeadingNode) => {
  selectedHeadingNode.value = node;
  document.getElementById(node.id)?.scrollIntoView({ behavior: "smooth" });
};

watch(
  () => props.raw,
  () => {
    if (props.raw !== editor.value?.getHTML()) {
      editor.value?.commands.setContent(props.raw);
    }
  },
  {
    immediate: true,
  }
);

// fixme: temporary solution
const currentLocale = i18n.global.locale.value as
  | "zh-CN"
  | "en"
  | "zh"
  | "en-US";

function onTitleInput(event: Event) {
  emit("update:title", (event.target as HTMLInputElement).value);
}

function handleFocusEditor(event) {
  if (event.isComposing) {
    return;
  }
  editor.value?.commands.focus("start");
}

// Cover
const coverSelectorModalVisible = ref(false);

function onCoverSelect(attachments: AttachmentLike[]) {
  const attachment = attachments[0];
  if (!attachment) {
    return;
  }
  emit("update:cover", utils.attachment.getUrl(attachment));
}

const { onChange: onCoverInputChange, open: openCoverInputDialog } =
  useFileDialog({
    accept: "image/*", // Set to accept only image files
    multiple: false,
  });

const uploadProgress = ref(0);

onCoverInputChange((files) => {
  const file = files?.[0];
  if (!file) {
    return;
  }
  props
    .uploadImage?.(file, {
      onUploadProgress: (progress) => {
        uploadProgress.value = Math.round(
          (progress.loaded * 100) / (progress.total || 1)
        );
      },
    })
    .then((attachment) => {
      emit("update:cover", attachment.status?.permalink);
    })
    .catch((e: Error) => {
      Toast.error(
        t("core.components.default_editor.cover.toast.upload_failed", {
          message: e.message,
        })
      );
    })
    .finally(() => {
      uploadProgress.value = 0;
    });
});
</script>

<template>
  <VLoading v-if="!isInitialized" />
  <div v-else>
    <AttachmentSelectorModal
      v-if="attachmentSelectorModalVisible"
      v-bind="attachmentOptions"
      @select="onAttachmentSelect"
      @close="onAttachmentSelectorModalClose"
    />
    <!-- For cover image -->
    <AttachmentSelectorModal
      v-if="coverSelectorModalVisible"
      :min="1"
      :max="1"
      :accepts="['image/*']"
      @select="onCoverSelect"
      @close="coverSelectorModalVisible = false"
    />
    <RichTextEditor v-if="editor" :editor="editor" :locale="currentLocale">
      <template #content>
        <div class="group">
          <div
            v-if="cover || uploadProgress"
            class="group/cover aspect-h-7 aspect-w-16 overflow-hidden rounded-lg"
          >
            <img
              v-if="cover"
              :src="
                utils.attachment.getThumbnailUrl(
                  cover,
                  GetThumbnailByUriSizeEnum.Xl
                )
              "
              class="size-full object-cover"
            />
            <div
              v-if="uploadProgress"
              class="flex flex-col items-center justify-center bg-black/50 text-white"
            >
              <VLoading class="!py-3" />
              <span class="text-sm">{{ uploadProgress }}%</span>
            </div>
            <HasPermission
              :permissions="[
                'system:attachments:view',
                'uc:attachments:manage',
              ]"
            >
              <div
                class="!bottom-2 !left-auto !right-2 !top-auto !size-auto opacity-0 shadow-lg transition-opacity group-hover/cover:opacity-100"
              >
                <VDropdown>
                  <VButton type="secondary" size="sm">
                    <template #icon>
                      <IconExchange />
                    </template>
                    {{
                      $t("core.components.default_editor.cover.options.change")
                    }}
                  </VButton>
                  <template #popper>
                    <HasPermission :permissions="['uc:attachments:manage']">
                      <VDropdownItem @click="openCoverInputDialog()">
                        {{
                          $t(
                            "core.components.default_editor.cover.options.upload"
                          )
                        }}
                      </VDropdownItem>
                    </HasPermission>
                    <VDropdownItem @click="coverSelectorModalVisible = true">
                      {{
                        $t(
                          "core.components.default_editor.cover.options.attachment"
                        )
                      }}
                    </VDropdownItem>
                    <VDropdownItem @click="emit('update:cover', undefined)">
                      {{ $t("core.common.buttons.delete") }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
              </div>
            </HasPermission>
          </div>
          <HasPermission
            :permissions="['system:attachments:view', 'uc:attachments:manage']"
          >
            <div
              class="mt-2 opacity-0"
              :class="{
                'group-hover:opacity-100': !cover,
                'pointer-events-none': cover,
              }"
            >
              <VDropdown class="!inline-flex">
                <VButton size="xs">
                  <template #icon>
                    <IconImageAddLine />
                  </template>
                  {{ $t("core.components.default_editor.cover.options.add") }}
                </VButton>
                <template #popper>
                  <HasPermission :permissions="['uc:attachments:manage']">
                    <VDropdownItem @click="openCoverInputDialog()">
                      {{
                        $t(
                          "core.components.default_editor.cover.options.upload"
                        )
                      }}
                    </VDropdownItem>
                  </HasPermission>
                  <VDropdownItem @click="coverSelectorModalVisible = true">
                    {{
                      $t(
                        "core.components.default_editor.cover.options.attachment"
                      )
                    }}
                  </VDropdownItem>
                </template>
              </VDropdown>
            </div>
          </HasPermission>
          <input
            ref="editorTitleRef"
            :value="title"
            type="text"
            :placeholder="
              $t('core.components.default_editor.title_placeholder')
            "
            class="w-full border-x-0 !border-b border-t-0 !border-solid !border-gray-100 p-0 !py-2 text-4xl font-semibold leading-none placeholder:text-gray-300"
            @input="onTitleInput"
            @keydown.enter="handleFocusEditor"
          />
          <slot name="content" />
        </div>
      </template>
      <template v-if="showSidebar" #extra>
        <OverlayScrollbarsComponent
          element="div"
          :options="{ scrollbars: { autoHide: 'scroll' } }"
          class="h-full border-l bg-white"
          defer
        >
          <VTabs v-model:active-id="extraActiveId" type="outline">
            <VTabItem
              id="toc"
              :label="$t('core.components.default_editor.tabs.toc.title')"
            >
              <div class="p-1 pt-0">
                <ul v-if="headingNodes?.length" class="space-y-1">
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
                          {
                            '!bg-white': node.id === selectedHeadingNode?.id,
                          },
                        ]"
                      />
                      <span class="flex-1 truncate">{{ node.text }}</span>
                    </div>
                  </li>
                </ul>
                <div v-else class="flex flex-col items-center py-10">
                  <span class="text-sm text-gray-600">
                    {{ $t("core.components.default_editor.tabs.toc.empty") }}
                  </span>
                </div>
              </div>
            </VTabItem>
            <VTabItem
              id="information"
              :label="$t('core.components.default_editor.tabs.detail.title')"
            >
              <div class="flex flex-col gap-2 p-1 pt-0">
                <div class="grid grid-cols-2 gap-2">
                  <div
                    class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                  >
                    <div class="flex items-center justify-between">
                      <div
                        class="text-sm text-gray-500 group-hover:text-gray-900"
                      >
                        {{
                          $t(
                            "core.components.default_editor.tabs.detail.fields.character_count"
                          )
                        }}
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
                        {{
                          $t(
                            "core.components.default_editor.tabs.detail.fields.word_count"
                          )
                        }}
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
                        {{
                          $t(
                            "core.components.default_editor.tabs.detail.fields.publish_time"
                          )
                        }}
                      </div>
                      <div class="rounded bg-gray-200 p-0.5">
                        <IconCalendar
                          class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                        />
                      </div>
                    </div>
                    <div class="text-base font-medium text-gray-900">
                      {{
                        utils.date.format(publishTime) ||
                        $t(
                          "core.components.default_editor.tabs.detail.fields.draft"
                        )
                      }}
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
                        {{
                          $t(
                            "core.components.default_editor.tabs.detail.fields.owner"
                          )
                        }}
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
                        {{
                          $t(
                            "core.components.default_editor.tabs.detail.fields.permalink"
                          )
                        }}
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
        </OverlayScrollbarsComponent>
      </template>
    </RichTextEditor>
  </div>
</template>
