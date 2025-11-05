<script lang="ts" setup>
import HasPermission from "@/components/permission/HasPermission.vue";
import { IconImageAddLine, VButton } from "@halo-dev/components";
import type { AttachmentSimple } from "@halo-dev/console-shared";
import { NodeViewWrapper, type NodeViewProps } from "@halo-dev/richtext-editor";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { EditorLinkObtain } from "../../components";
import { useExternalAssetsTransfer } from "../../composables/use-attachment";
import { fileToBase64 } from "../../utils/upload";
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

const handleSetExternalLink = (attachment?: AttachmentSimple) => {
  if (!attachment) return;
  props.updateAttributes({
    src: attachment.url,
    alt: attachment.alt,
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

  const { file } = props.node.attrs;
  if (file) {
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
const inputRef = ref<HTMLInputElement>();

function onImageLoaded() {
  if (!resizeRef.value) return;

  aspectRatio.value =
    resizeRef.value.clientWidth / resizeRef.value.clientHeight;
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
        1,
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

const { isExternalAsset, transferring, handleTransfer } =
  useExternalAssetsTransfer(src, handleSetExternalLink);

const isPercentageWidth = computed(() => {
  return props.node?.attrs.width?.includes("%");
});
</script>

<template>
  <node-view-wrapper
    as="div"
    class="w-full"
    :class="{
      'w-fit': !isPercentageWidth && src,
      flex: isPercentageWidth,
    }"
  >
    <div
      ref="resizeRef"
      class="resize-container group relative inline-block max-w-full overflow-hidden rounded-md text-center"
      :class="{
        'rounded ring-2': selected,
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
          :width="isPercentageWidth ? '100%' : node.attrs.width"
          :height="isPercentageWidth ? '100%' : node.attrs.height"
          class="max-w-full rounded-md"
          @load="onImageLoaded"
        />

        <div
          v-if="selected"
          ref="resizeHandleRef"
          class="resizer-handler resizer-br"
          title="拖拽调整大小"
        ></div>

        <div
          v-if="src"
          class="absolute left-0 top-0 hidden h-1/4 w-full cursor-pointer justify-end gap-2 rounded-md bg-gradient-to-b from-gray-300 to-transparent p-2 ease-in-out group-hover:flex"
        >
          <HasPermission :permissions="['uc:attachments:manage']">
            <VButton
              v-if="isExternalAsset"
              v-tooltip="
                $t(
                  'core.components.default_editor.extensions.upload.operations.transfer.tooltip'
                )
              "
              :loading="transferring"
              size="sm"
              ghost
              @click="handleTransfer"
            >
              {{
                $t(
                  "core.components.default_editor.extensions.upload.operations.transfer.button"
                )
              }}
            </VButton>
          </HasPermission>

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
  </node-view-wrapper>
</template>
