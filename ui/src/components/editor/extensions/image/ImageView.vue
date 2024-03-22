<script lang="ts" setup>
import {
  Editor,
  type Node,
  type PMNode,
  type Decoration,
} from "@halo-dev/richtext-editor";
import { computed, onMounted, ref } from "vue";
import Image from "./index";
import { fileToBase64 } from "../../utils/upload";
import { VButton, IconImageAddLine } from "@halo-dev/components";
import { type AttachmentAttr } from "../../utils/attachment";
import { EditorLinkObtain } from "../../components";
import InlineBlockBox from "../../components/InlineBlockBox.vue";

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

const fileBase64 = ref<string>();
const uploadProgress = ref<number | undefined>(undefined);
const retryFlag = ref<boolean>(false);
const editorLinkObtain = ref();

const handleUploadAbort = () => {
  editorLinkObtain.value?.abort();
};

const initialization = computed(() => {
  return !src.value && !fileBase64.value;
});

const handleUploadReady = async (file: File) => {
  fileBase64.value = await fileToBase64(file);
  retryFlag.value = false;
};

const handleSetExternalLink = (attachment: AttachmentAttr) => {
  props.updateAttributes({
    src: attachment.url,
    alt: attachment.name,
  });
};

const handleUploadRetry = () => {
  editorLinkObtain.value?.retry();
};

const handleUploadProgress = (progress: number) => {
  uploadProgress.value = progress;
};

const handleUploadError = () => {
  retryFlag.value = true;
};

const resetUpload = () => {
  fileBase64.value = undefined;
  uploadProgress.value = undefined;
  if (props.getPos()) {
    props.updateAttributes({
      width: undefined,
      height: undefined,
      file: undefined,
    });
  }
};

const handleResetInit = () => {
  editorLinkObtain.value?.reset();
  props.updateAttributes({
    src: "",
    file: undefined,
  });
};

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
</script>

<template>
  <InlineBlockBox>
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
                "core.components.default_editor.extensions.upload.operations.replace.button"
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
                        "core.components.default_editor.extensions.upload.error"
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
                    "core.components.default_editor.extensions.upload.click_retry"
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
                            "core.components.default_editor.extensions.upload.loading"
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
      <div v-show="!src && !fileBase64">
        <EditorLinkObtain
          ref="editorLinkObtain"
          :accept="'image/*'"
          :editor="editor"
          :upload-to-attachment-file="extension.options.uploadImage"
          :uploaded-file="node?.attrs.file"
          @set-external-link="handleSetExternalLink"
          @on-upload-ready="handleUploadReady"
          @on-upload-progress="handleUploadProgress"
          @on-upload-finish="resetUpload"
          @on-upload-error="handleUploadError"
          @on-upload-abort="resetUpload"
        >
          <template #icon>
            <div
              class="flex h-14 w-14 items-center justify-center rounded-full bg-primary/20"
            >
              <IconImageAddLine class="text-xl text-primary" />
            </div>
          </template>
        </EditorLinkObtain>
      </div>
    </div>
  </InlineBlockBox>
</template>
