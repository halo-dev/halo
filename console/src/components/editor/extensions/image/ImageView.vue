<script lang="ts" setup>
import {
  Editor,
  type Node,
  type PMNode,
  type Decoration,
} from "@halo-dev/richtext-editor";
import { NodeViewWrapper } from "@halo-dev/richtext-editor";
import { computed, onMounted, ref } from "vue";
import Image from "./index";
import { watch } from "vue";
import { fileToBase64, uploadFile } from "../../utils/upload";
import type { Attachment } from "@halo-dev/api-client";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { useFileDialog } from "@vueuse/core";
import { onUnmounted } from "vue";
import {
  VButton,
  VSpace,
  IconImageAddLine,
  VDropdown,
} from "@halo-dev/components";
import { getAttachmentUrl } from "../../utils/attachment";
import { i18n } from "@/locales";

const props = defineProps<{
  editor: Editor;
  node: PMNode;
  decorations: Decoration[];
  selected: boolean;
  extension: Node;
  getPos: () => number;
  updateAttributes: (attributes: Record<string, unknown>) => void;
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

const originalFile = ref<File>();
const fileBase64 = ref<string>();
const uploadProgress = ref<number | undefined>(undefined);
const retryFlag = ref<boolean>(false);
const controller = ref<AbortController>();
const initSrc = ref<string>();

const initialization = computed(() => {
  return !src.value && !fileBase64.value;
});

const handleEnterSetSrc = () => {
  if (!initSrc.value) {
    return;
  }
  props.updateAttributes({ src: initSrc.value });
};

const openAttachmentSelector = () => {
  props.editor.commands.openAttachmentSelector(
    (attachments: AttachmentLike[]) => {
      if (attachments.length > 0) {
        const attachment = attachments[0];
        const attachmentAttr = getAttachmentUrl(attachment);
        props.updateAttributes({
          src: attachmentAttr.url,
          alt: attachmentAttr.name,
        });
      }
    },
    {
      accepts: ["image/*"],
      min: 1,
      max: 1,
    }
  );
};

const { open, reset, onChange } = useFileDialog({
  accept: "image/*",
  multiple: false,
});

const handleUploadAbort = () => {
  if (!controller.value) {
    return;
  }
  controller.value.abort();
  resetUpload();
};

const handleUploadRetry = () => {
  if (!originalFile.value) {
    return;
  }
  handleUploadImage(originalFile.value);
};

const handleUploadImage = async (file: File) => {
  originalFile.value = file;
  fileBase64.value = await fileToBase64(file);
  retryFlag.value = false;

  const result = await uploadFile(file, props.extension.options.uploadImage);
  controller.value = result.controller;
  result.onUploadProgress = (progress) => {
    uploadProgress.value = progress;
  };

  result.onFinish = (attachment?: Attachment) => {
    if (attachment) {
      props.updateAttributes({
        src: attachment.status?.permalink,
      });
    }

    resetUpload();
  };

  result.onError = () => {
    retryFlag.value = true;
  };
};

const resetUpload = () => {
  reset();
  originalFile.value = undefined;
  fileBase64.value = undefined;
  uploadProgress.value = undefined;
  controller.value?.abort();
  controller.value = undefined;
  if (props.getPos()) {
    props.updateAttributes({
      width: undefined,
      height: undefined,
    });
  }
};

const handleResetInit = () => {
  resetUpload();
  props.updateAttributes({
    src: "",
    file: undefined,
  });
};

onChange((files) => {
  if (!files) {
    return;
  }
  if (files.length > 0) {
    handleUploadImage(files[0]);
  }
});

watch(
  () => props.node?.attrs.file,
  async (file) => {
    if (!file) {
      return;
    }
    handleUploadImage(file);
  },
  {
    immediate: true,
  }
);

const aspectRatio = ref<number>(0);
const resizeRef = ref<HTMLDivElement>();

function onImageLoaded() {
  if (!resizeRef.value) return;

  aspectRatio.value =
    resizeRef.value.clientWidth / resizeRef.value.clientHeight;
}

onMounted(() => {
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

onUnmounted(() => {
  handleUploadAbort();
});
</script>

<template>
  <node-view-wrapper as="div" class="inline-block w-full">
    <div
      ref="resizeRef"
      class="group relative inline-block max-w-full overflow-hidden rounded-md text-center"
      :class="{
        'rounded ring-2': selected,
        'resize-x': !initialization,
      }"
      :style="{
        width: initialization ? '100%' : node.attrs.width,
        height: initialization ? '100%' : node.attrs.height,
      }"
    >
      <div v-if="src || fileBase64" class="relative">
        <img
          :src="src || fileBase64"
          :title="node.attrs.title"
          :alt="alt"
          :href="href"
          class="h-full w-full"
          @load="onImageLoaded"
        />

        <div
          v-if="src"
          class="absolute left-0 top-0 hidden h-1/4 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent p-2 ease-in-out group-hover:flex"
        >
          <VButton size="sm" type="secondary" @click="handleResetInit">
            {{
              $t(
                "core.components.default_editor.extensions.image.operations.replace.button"
              )
            }}
          </VButton>
        </div>

        <div
          v-if="fileBase64"
          class="absolute top-0 h-full w-full bg-black bg-opacity-20"
        >
          <div class="absolute top-[50%] w-full space-y-2 text-white">
            <template v-if="retryFlag">
              <div class="px-10">
                <div
                  class="relative h-4 w-full overflow-hidden rounded-full bg-gray-200"
                >
                  <div class="h-full w-full bg-red-600"></div>
                  <div
                    class="absolute left-[50%] top-0 -translate-x-[50%] text-xs leading-4 text-white"
                  >
                    {{
                      $t(
                        "core.components.default_editor.extensions.image.upload.error"
                      )
                    }}
                  </div>
                </div>
              </div>
              <div
                class="inline-block cursor-pointer text-sm hover:opacity-70"
                @click="handleUploadRetry"
              >
                {{
                  $t(
                    "core.components.default_editor.extensions.image.upload.click_retry"
                  )
                }}
              </div>
            </template>
            <template v-else>
              <div class="px-10">
                <div
                  class="relative h-4 w-full overflow-hidden rounded-full bg-gray-200"
                >
                  <div
                    class="h-full bg-primary"
                    :style="{
                      width: `${uploadProgress || 0}%`,
                    }"
                  ></div>
                  <div
                    class="absolute left-[50%] top-0 -translate-x-[50%] text-xs leading-4 text-white"
                  >
                    {{
                      uploadProgress
                        ? `${uploadProgress}%`
                        : `${$t(
                            "core.components.default_editor.extensions.image.upload.loading"
                          )}...`
                    }}
                  </div>
                </div>
              </div>

              <div
                class="inline-block cursor-pointer text-sm hover:opacity-70"
                @click="handleUploadAbort"
              >
                {{ $t("core.common.buttons.cancel") }}
              </div>
            </template>
          </div>
        </div>
      </div>
      <div v-else>
        <div class="flex w-full items-center justify-center">
          <div
            class="flex h-64 w-full cursor-pointer flex-col items-center justify-center border-2 border-dashed border-gray-300 bg-gray-50"
          >
            <div
              class="flex flex-col items-center justify-center space-y-7 pb-6 pt-5"
            >
              <div
                class="flex h-14 w-14 items-center justify-center rounded-full bg-primary/20"
              >
                <IconImageAddLine class="text-xl text-primary" />
              </div>
              <VSpace>
                <VButton @click="open()">
                  {{ $t("core.common.buttons.upload") }}
                </VButton>
                <VButton @click="openAttachmentSelector">
                  {{
                    $t(
                      "core.components.default_editor.extensions.image.attachment.title"
                    )
                  }}</VButton
                >
                <VDropdown>
                  <VButton>{{
                    $t(
                      "core.components.default_editor.extensions.image.permalink.title"
                    )
                  }}</VButton>
                  <template #popper>
                    <input
                      v-model="initSrc"
                      class="block w-full rounded-md border border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 hover:bg-gray-100"
                      :placeholder="
                        i18n.global.t(
                          'core.components.default_editor.extensions.image.permalink.placeholder'
                        )
                      "
                      @keydown.enter="handleEnterSetSrc"
                    />
                  </template>
                </VDropdown>
              </VSpace>
            </div>
          </div>
        </div>
      </div>
    </div>
  </node-view-wrapper>
</template>
