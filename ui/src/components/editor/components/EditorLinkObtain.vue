<script setup lang="ts">
import { VButton, VSpace, VDropdown } from "@halo-dev/components";
import type { CoreEditor } from "@halo-dev/richtext-editor";
import { useFileDialog } from "@vueuse/core";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { getAttachmentUrl, type AttachmentAttr } from "../utils/attachment";
import { i18n } from "@/locales";
import { onUnmounted, ref } from "vue";
import { watch } from "vue";
import { uploadFile } from "../utils/upload";
import type { Attachment } from "@halo-dev/api-client";
import type { AxiosRequestConfig } from "axios";
import HasPermission from "@/components/permission/HasPermission.vue";

const props = withDefaults(
  defineProps<{
    editor: CoreEditor;
    accept: string;
    uploadedFile: File;
    uploadToAttachmentFile: (
      file: File,
      options?: AxiosRequestConfig
    ) => Promise<Attachment>;
  }>(),
  {
    accept: "*",
    uploadedFile: undefined,
  }
);

const emit = defineEmits<{
  (event: "setExternalLink", attachment: AttachmentAttr): void;
  (event: "onUploadReady", file: File): void;
  (event: "onUploadProgress", progress: number): void;
  (event: "onUploadFinish"): void;
  (event: "onUploadError", error: Error): void;
  (event: "onUploadAbort"): void;
}>();

const externalLink = ref("");

const handleEnterSetExternalLink = () => {
  if (!externalLink.value) {
    return;
  }
  emit("setExternalLink", {
    url: externalLink.value,
  });
};

const { open, reset, onChange } = useFileDialog({
  accept: props.accept,
  multiple: false,
});

const openAttachmentSelector = () => {
  props.editor.commands.openAttachmentSelector(
    (attachments: AttachmentLike[]) => {
      if (attachments.length > 0) {
        const attachment = attachments[0];
        const attachmentAttr = getAttachmentUrl(attachment);
        emit("setExternalLink", attachmentAttr);
      }
    },
    {
      accepts: [props.accept],
      min: 1,
      max: 1,
    }
  );
};
const controller = ref<AbortController>();
const originalFile = ref<File>();
const uploadState = ref<"init" | "uploading" | "error">("init");
const uploadProgress = ref<number | undefined>(undefined);

/**
 *
 * Upload files to the attachment library.
 *
 * @param file attachments that need to be uploaded to the attachment library
 */
const handleUploadFile = (file: File) => {
  controller.value = new AbortController();
  originalFile.value = file;
  uploadState.value = "uploading";
  uploadProgress.value = undefined;
  emit("onUploadReady", file);
  uploadFile(file, props.uploadToAttachmentFile, {
    controller: controller.value,
    onUploadProgress: (progress) => {
      uploadProgress.value = progress;
      emit("onUploadProgress", progress);
    },

    onFinish: (attachment?: Attachment) => {
      if (attachment) {
        emit("setExternalLink", {
          url: attachment.status?.permalink,
        });
      }
      handleResetUpload();
      emit("onUploadFinish");
    },

    onError: (error: Error) => {
      if (error.name !== "CanceledError") {
        uploadState.value = "error";
      }
      emit("onUploadError", error);
    },
  });
};

const handleUploadAbort = () => {
  emit("onUploadAbort");
  handleResetUpload();
};

const handleUploadRetry = () => {
  if (!controller.value) {
    return;
  }
  controller.value.abort();
  if (!originalFile.value) {
    return;
  }
  handleUploadFile(originalFile.value);
};

const handleResetUpload = () => {
  uploadState.value = "init";
  controller.value?.abort();
  controller.value = undefined;
  originalFile.value = undefined;
  uploadProgress.value = undefined;
  reset();
};

onChange((files) => {
  if (!files) {
    return;
  }
  if (files.length > 0) {
    handleUploadFile(files[0]);
  }
});

watch(
  () => props.uploadedFile,
  async (file) => {
    if (!file) {
      return;
    }
    handleUploadFile(file);
  },
  {
    immediate: true,
  }
);

onUnmounted(() => {
  handleUploadAbort();
});

defineExpose({
  abort: handleUploadAbort,
  retry: handleUploadRetry,
  reset: handleResetUpload,
});
</script>
<template>
  <div class="flex h-64 w-full items-center justify-center">
    <slot
      v-if="$slots.uploading && uploadState === 'uploading'"
      name="uploading"
      :progress="uploadProgress"
    ></slot>
    <slot
      v-else-if="$slots.error && uploadState === 'error'"
      name="error"
    ></slot>
    <div
      v-else
      class="flex h-full w-full cursor-pointer flex-col items-center justify-center border-2 border-dashed border-gray-300 bg-gray-50"
    >
      <div
        class="flex flex-col items-center justify-center space-y-7 pb-6 pt-5"
      >
        <slot v-if="$slots.icon" name="icon"></slot>
        <VSpace>
          <HasPermission :permissions="['uc:attachments:manage']">
            <VButton @click="open()">
              {{ $t("core.common.buttons.upload") }}
            </VButton>
          </HasPermission>

          <HasPermission :permissions="['system:attachments:manage']">
            <VButton @click="openAttachmentSelector">
              {{
                $t(
                  "core.components.default_editor.extensions.upload.attachment.title"
                )
              }}
            </VButton>
          </HasPermission>

          <VDropdown>
            <VButton>
              {{
                $t(
                  "core.components.default_editor.extensions.upload.permalink.title"
                )
              }}
            </VButton>
            <template #popper>
              <input
                v-model="externalLink"
                class="block w-full rounded-md border border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 hover:bg-gray-100"
                :placeholder="
                  i18n.global.t(
                    'core.components.default_editor.extensions.upload.permalink.placeholder'
                  )
                "
                @keydown.enter="handleEnterSetExternalLink"
              />
            </template>
          </VDropdown>
        </VSpace>
      </div>
    </div>
  </div>
</template>
